package com.ynu.edu.elm_intermediate.modules.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ynu.edu.elm_intermediate.common.AppException;
import com.ynu.edu.elm_intermediate.entity.*;
import com.ynu.edu.elm_intermediate.mapper.*;
import com.ynu.edu.elm_intermediate.modules.order.dto.OrderCreateRequest;
import com.ynu.edu.elm_intermediate.modules.order.dto.OrderPayRequest;
import com.ynu.edu.elm_intermediate.modules.order.vo.OrderItemVO;
import com.ynu.edu.elm_intermediate.modules.order.vo.OrderVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AddressMapper addressMapper;
    private final FoodMapper foodMapper;
    private final BusinessMapper businessMapper;

    public OrderService(OrderMapper orderMapper, OrderItemMapper orderItemMapper, AddressMapper addressMapper, FoodMapper foodMapper, BusinessMapper businessMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.addressMapper = addressMapper;
        this.foodMapper = foodMapper;
        this.businessMapper = businessMapper;
    }

    @Transactional//开启事务，如果下单过程中任何一处报错，则全部失败
    public Map<String, Object> create(int uid, OrderCreateRequest req) {
        // 1. 校验地址
        DeliveryAddress addr = addressMapper.selectById(req.getAddressId());
        if (addr == null || !addr.getCustomerId().equals(uid)) {
            throw new AppException(403, "地址无效");
        }
        // 2. 查菜品获取所有菜品id
        List<Integer> foodIds = req.getItems().stream().map(OrderCreateRequest.Item::getFoodId).toList();
        //查出这些菜品
        List<Food> foods = foodMapper.selectBatchIds(foodIds);
        //转为map
        Map<Integer, Food> foodMap = foods.stream().collect(Collectors.toMap(Food::getFoodId, f -> f));
        // 3. 计算金额 & 准备明细
        BigDecimal foodTotal = BigDecimal.ZERO;//0
        List<OrderItem> itemsToSave = new ArrayList<>();
        for (var itemReq : req.getItems()) {
            Food f = foodMap.get(itemReq.getFoodId());//通过之前的id去查询
            if (f == null) throw new AppException(400, "菜品不存在ID:" + itemReq.getFoodId());
            
            BigDecimal lineTotal = f.getFoodPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            foodTotal = foodTotal.add(lineTotal);
            //明细
            OrderItem oi = new OrderItem();
            oi.setFoodId(f.getFoodId());
            oi.setFoodName(f.getFoodName());
            oi.setFoodPrice(f.getFoodPrice());
            oi.setQuantity(itemReq.getQuantity());
            itemsToSave.add(oi);
        }
        // 4. 创建订单
        Orders order = new Orders();
        order.setCustomerId(uid);
        order.setBusinessId(req.getBusinessId());
        order.setDaId(addr.getDaId());
        order.setReceiverName(addr.getContactName());
        order.setReceiverPhone(addr.getContactTel());
        order.setReceiverAddress(addr.getAddress());
        order.setDeliveryFee(req.getDeliveryFee());
        order.setTotalPrice(foodTotal.add(req.getDeliveryFee()));
        order.setStatus("unpaid");
        order.setCreatedAt(LocalDateTime.now());
        
        orderMapper.insert(order); // MP 回填 ID
        
        // 5. 保存明细
        for (OrderItem oi : itemsToSave) {
            oi.setOrderId(order.getOrderId());
            orderItemMapper.insert(oi);
        }

        return Map.of("orderId", order.getOrderId(), "total", order.getTotalPrice());
    }

    public Map<String, Object> pay(int uid, OrderPayRequest req) {
        Orders order = orderMapper.selectById(req.getOrderId());
        if (order == null) throw new AppException(404, "订单不存在");
        
        order.setStatus("paid");
        order.setPayMethod(req.getPayMethod());
        order.setPaidAt(LocalDateTime.now());
        orderMapper.updateById(order);
        
        return Map.of("status", "paid");
    }

    public List<OrderVO> list(int uid) {
        // 1. 查订单主表
        List<Orders> orders = orderMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getCustomerId, uid)
                .orderByDesc(Orders::getOrderId));
        
        if (orders.isEmpty()) return List.of();

        // 2. 补全商家名称
        List<Integer> bizIds = orders.stream().map(Orders::getBusinessId).distinct().toList();
        Map<Integer, String> bizNames = businessMapper.selectBatchIds(bizIds).stream()
                .collect(Collectors.toMap(Business::getBusinessId, Business::getBusinessName));

        // 3. 查所有相关明细
        List<Integer> orderIds = orders.stream().map(Orders::getOrderId).toList();
        List<OrderItem> allItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .in(OrderItem::getOrderId, orderIds));
        
        // 分组
        Map<Integer, List<OrderItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        // 4. 组装 VO
        return orders.stream().map(o -> {
            OrderVO vo = new OrderVO();
            vo.setId(String.valueOf(o.getOrderId()));
            vo.setBusinessName(bizNames.getOrDefault(o.getBusinessId(), "商家已下线"));
            vo.setDeliveryFee(o.getDeliveryFee());
            vo.setTotal(o.getTotalPrice());
            vo.setStatus(o.getStatus());
            vo.setCreatedAt(o.getCreatedAt().toString());
            vo.setPayMethod(o.getPayMethod());
            
            List<OrderItemVO> itemVOs = itemMap.getOrDefault(o.getOrderId(), List.of())
                    .stream()
                    .map(it -> new OrderItemVO(it.getFoodName(), it.getFoodPrice(), it.getQuantity()))
                    .toList();
            vo.setItems(itemVOs);
            return vo;
        }).toList();
    }

    public OrderVO getOrderDetail(int orderId, int uid, String role) {
        Orders order = orderMapper.selectById(orderId);
        if (order == null) throw new AppException(404, "订单不存在");

        // 权限检查：只能看自己的订单
        if ("customer".equals(role) && !order.getCustomerId().equals(uid)) {
            throw new AppException(403, "无权查看他人订单");
        }

        // 组装 VO (复用 list 的逻辑，或者简单封装)
        OrderVO vo = new OrderVO();
        vo.setId(String.valueOf(order.getOrderId()));
        vo.setTotal(order.getTotalPrice());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt().toString()); // ISO 格式时间

        return vo;
    }

    public List<OrderVO> listByBusiness(int businessId) {
        // 1. 根据商家ID查订单
        List<Orders> orders = orderMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getBusinessId, businessId)
                .orderByDesc(Orders::getOrderId));

        if (orders.isEmpty()) return List.of();

        // 2. 查订单明细
        List<Integer> orderIds = orders.stream().map(Orders::getOrderId).toList();
        List<OrderItem> allItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .in(OrderItem::getOrderId, orderIds));

        // 3. 分组
        Map<Integer, List<OrderItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        // 4. 组装 VO
        return orders.stream().map(o -> {
            OrderVO vo = new OrderVO();
            vo.setId(String.valueOf(o.getOrderId()));
            // 商家端看 businessName 没意义，不如显示客户名，或者前端自己处理
            vo.setBusinessName(o.getBusinessName());

            // 填入收货信息
            vo.setReceiverName(o.getReceiverName());
            vo.setReceiverPhone(o.getReceiverPhone());
            vo.setReceiverAddress(o.getReceiverAddress());

            vo.setDeliveryFee(o.getDeliveryFee());
            vo.setTotal(o.getTotalPrice());
            vo.setStatus(o.getStatus());
            vo.setCreatedAt(o.getCreatedAt().toString());
            vo.setPayMethod(o.getPayMethod());

            List<OrderItemVO> itemVOs = itemMap.getOrDefault(o.getOrderId(), List.of())
                    .stream()
                    .map(it -> new OrderItemVO(it.getFoodName(), it.getFoodPrice(), it.getQuantity()))
                    .toList();
            vo.setItems(itemVOs);
            return vo;
        }).toList();
    }
}