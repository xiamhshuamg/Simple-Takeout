package com.ynu.edu.elm_intermediate.modules.business.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ynu.edu.elm_intermediate.common.AppException;
import com.ynu.edu.elm_intermediate.entity.Business;
import com.ynu.edu.elm_intermediate.entity.Food;
import com.ynu.edu.elm_intermediate.entity.Orders;
import com.ynu.edu.elm_intermediate.entity.VisitLog;
import com.ynu.edu.elm_intermediate.mapper.BusinessMapper;
import com.ynu.edu.elm_intermediate.mapper.FoodMapper;
import com.ynu.edu.elm_intermediate.mapper.OrderMapper;
import com.ynu.edu.elm_intermediate.mapper.VisitLogMapper;
import com.ynu.edu.elm_intermediate.modules.business.vo.BusinessVO;
import com.ynu.edu.elm_intermediate.modules.business.vo.FoodVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BusinessService {

    private final BusinessMapper businessMapper;
    private final FoodMapper foodMapper;
    private final OrderMapper orderMapper;
    private final VisitLogMapper visitLogMapper;

    public BusinessService(BusinessMapper businessMapper, FoodMapper foodMapper, OrderMapper orderMapper, VisitLogMapper visitLogMapper) {
        this.businessMapper = businessMapper;
        this.foodMapper = foodMapper;
        this.orderMapper = orderMapper;
        this.visitLogMapper = visitLogMapper;
    }

    //商家列表
    public List<BusinessVO> list(String keyword, Double userLat, Double userLng) {
        //创建查询条件构建器
        LambdaQueryWrapper<Business> wrapper = new LambdaQueryWrapper<>();
        //只查询营业中的商家
        wrapper.eq(Business::getIsOpen, 1);
        //添加搜索条件
        if (keyword != null && !keyword.isBlank()) {
            //商家名称包含关键词或者描述包含关键词
            wrapper.and(w -> w.like(Business::getBusinessName, keyword)
                    .or().like(Business::getBusinessExplain, keyword));
        }
        //按商家ID降序排序
        wrapper.orderByDesc(Business::getBusinessId);
        //获取结果列表
        List<Business> list = businessMapper.selectList(wrapper);
                //map将一种类型转换为另一种类型
        return list.stream()
                .map(b -> {
                    BusinessVO vo = toVO(b);

                    // 如果用户和商家都有位置信息，计算距离
                    if (userLat != null && userLng != null && b.getLat() != null && b.getLng() != null) {
                        double distKm = getDistance(userLat, userLng, b.getLat().doubleValue(), b.getLng().doubleValue());
                        vo.setDistance(String.format("%.1fkm", distKm));

                        // 根据距离动态计算 ETA (配送时间)
                        // 算法示例：基础 20 分钟 + 每公里 8 分钟
                        int estimatedMinutes = 20 + (int)(distKm * 8);
                        vo.setEta(estimatedMinutes + "分钟");
                    } else {
                        // 如果没有定位，默认 30 分钟
                        vo.setEta("30分钟");
                    }
                    return vo;
                })
                // 比较两个商家VO：v1 和 v2
                .sorted((v1, v2) -> {
                    if (userLat == null || userLng == null) return 0;
                    // 2. 解析距离字符串为数字
                    double d1 = parseDistance(v1.getDistance());
                    double d2 = parseDistance(v2.getDistance());
                    // 3. 比较距离
                    return Double.compare(d1, d2);
                })
                .collect(Collectors.toList());
    }

    //获取菜单
    public List<FoodVO> foods(int bid) {
        return foodMapper.selectList(new LambdaQueryWrapper<Food>()
                        .eq(Food::getBusinessId, bid)
                        .eq(Food::getStatus, 1)
                        .orderByAsc(Food::getFoodId))
                .stream().map(this::toFoodVO).toList();
    }

    // 获取商家信息
    public BusinessVO getBusinessInfo(int bid) {
        Business b = businessMapper.selectById(bid);
        if (b == null) throw new AppException(404, "商家不存在", 404);

        return toVO(b);
    }
    //访客记录
    public void addVisitLog(int bid) {
        try {
            VisitLog log = new VisitLog();
            log.setBusinessId(bid);
            log.setVisitTime(LocalDateTime.now());
            visitLogMapper.insert(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateInfo(int bid, BusinessVO req) {
        Business b = new Business();
        b.setBusinessId(bid);
        if (req.getName() != null && !req.getName().isEmpty()) b.setBusinessName(req.getName());
        if (req.getDesc() != null) b.setBusinessExplain(req.getDesc());
        if (req.getMinOrder() != null) b.setStarPrice(req.getMinOrder());
        if (req.getDeliveryFee() != null) b.setDeliveryPrice(req.getDeliveryFee());
        if (req.getImg() != null && !req.getImg().isEmpty()) b.setBusinessImg(req.getImg());
        if (req.getLat() != null) b.setLat(req.getLat());
        if (req.getLng() != null) b.setLng(req.getLng());
        businessMapper.updateById(b);
    }

    public void saveFood(int bid, FoodVO req) {
        Food f = new Food();
        f.setBusinessId(bid);
        f.setFoodName(req.getName());
        f.setFoodExplain(req.getDesc());
        f.setFoodPrice(req.getPrice());
        if (req.getId() != 0) {
            f.setFoodId(req.getId());
            foodMapper.updateById(f);
        } else {
            f.setFoodImg(req.getImg() == null ? "/img/img/sp01.png" : req.getImg());
            foodMapper.insert(f);
        }
    }

    public void removeFood(int bid, int fid) {
        foodMapper.delete(new LambdaQueryWrapper<Food>()
                .eq(Food::getFoodId, fid)
                .eq(Food::getBusinessId, bid));
    }

    // 统计今日数据 + 访问总量
    public Map<String, Object> getStats(int bid) {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        // 今日订单数 + 今日收入
        QueryWrapper<Orders> orderWrapper = new QueryWrapper<>();
        orderWrapper.select("count(*) as c", "IFNULL(sum(totalPrice), 0) as s")
                .eq("businessId", bid)
                .eq("status", "paid")
                .between("createdAt", startOfDay, endOfDay);
        List<Map<String, Object>> result = orderMapper.selectMaps(orderWrapper);
        Map<String, Object> map = (result != null && !result.isEmpty()) ? result.get(0) : Map.of("c", 0, "s", 0);
        Long todayVisitors;
        Long totalVisitors;
        try {
            todayVisitors = visitLogMapper.selectCount(new LambdaQueryWrapper<VisitLog>()
                    .eq(VisitLog::getBusinessId, bid)
                    .between(VisitLog::getVisitTime, startOfDay, endOfDay));
            totalVisitors = visitLogMapper.selectCount(new LambdaQueryWrapper<VisitLog>()
                    .eq(VisitLog::getBusinessId, bid));
        } catch (Exception e) {
            e.printStackTrace();
            todayVisitors = 0L;
            totalVisitors = 0L;
        }
        return Map.of(
                "todayOrders", map.get("c"),
                "todayIncome", map.get("s"),
                "todayVisitors", todayVisitors,
                "totalVisitors", totalVisitors
        );
    }

    private BusinessVO toVO(Business b) {
        BusinessVO vo = new BusinessVO();
        vo.setId(b.getBusinessId());
        vo.setName(b.getBusinessName());
        vo.setDesc(b.getBusinessExplain());
        vo.setMinOrder(b.getStarPrice());
        vo.setDeliveryFee(b.getDeliveryPrice());
        vo.setImg(b.getBusinessImg());
        vo.setRating(b.getRating());
        vo.setMonthlySales(b.getMonthlySales());
        vo.setDistance(b.getDistance());
        vo.setEta(b.getEta());
        vo.setIsOpen(b.getIsOpen());
        vo.setLat(b.getLat());
        vo.setLng(b.getLng());
        return vo;
    }

    public void updateBusinessStatus(int bid, int isOpen) {
        Business b = new Business();
        b.setBusinessId(bid);
        b.setIsOpen(isOpen);
        businessMapper.updateById(b);
    }

    public void updateFoodStatus(int bid, int fid, int status) {
        Food f = new Food();
        f.setFoodId(fid);
        f.setStatus(status);
        foodMapper.update(f, new LambdaQueryWrapper<Food>()
                .eq(Food::getFoodId, fid)
                .eq(Food::getBusinessId, bid));
    }

    private FoodVO toFoodVO(Food f) {
        FoodVO vo = new FoodVO();
        vo.setId(f.getFoodId());
        vo.setName(f.getFoodName());
        vo.setDesc(f.getFoodExplain());
        vo.setPrice(f.getFoodPrice());
        vo.setImg(f.getFoodImg());
        return vo;
    }

    private double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private double parseDistance(String distStr) {
        if (distStr == null || !distStr.endsWith("km")) return 999999.0;
        try {
            return Double.parseDouble(distStr.replace("km", ""));
        } catch (Exception e) {
            return 999999.0;
        }
    }

    // 商家管理后台：获取全部菜单
    public List<FoodVO> foodsAll(int bid) {
        List<Food> list = foodMapper.selectList(new LambdaQueryWrapper<Food>()
                .eq(Food::getBusinessId, bid)
                .orderByAsc(Food::getFoodId));
        return list.stream().map(f -> {
            FoodVO vo = toFoodVO(f);
            vo.setStatus(f.getStatus());
            return vo;
        }).toList();
    }
}
