package com.ynu.edu.elm_intermediate.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ynu.edu.elm_intermediate.common.AppException;
import com.ynu.edu.elm_intermediate.entity.Business;
import com.ynu.edu.elm_intermediate.entity.Customer;
import com.ynu.edu.elm_intermediate.entity.Orders;
import com.ynu.edu.elm_intermediate.entity.VisitLog;
import com.ynu.edu.elm_intermediate.mapper.BusinessMapper;
import com.ynu.edu.elm_intermediate.mapper.CustomerMapper;
import com.ynu.edu.elm_intermediate.mapper.OrderMapper;
import com.ynu.edu.elm_intermediate.mapper.VisitLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员只有两个功能：
 * 1) 删除商家
 * 2) 删除用户
 * 另外提供列表给前端展示
 */
@Service
public class AdminService {

    private final CustomerMapper customerMapper;
    private final BusinessMapper businessMapper;
    private final OrderMapper orderMapper;
    private final VisitLogMapper visitLogMapper;

    public AdminService(CustomerMapper customerMapper,
                        BusinessMapper businessMapper,
                        OrderMapper orderMapper,
                        VisitLogMapper visitLogMapper) {
        this.customerMapper = customerMapper;
        this.businessMapper = businessMapper;
        this.orderMapper = orderMapper;
        this.visitLogMapper = visitLogMapper;
    }

    public List<Customer> listCustomers() {
        // 前端不需要看密码，安全起见清空
        List<Customer> list = customerMapper.selectList(null);
        for (Customer c : list) { c.setPassword(null); }
        return list;
    }

    public List<Business> listBusinesses() {
        List<Business> list = businessMapper.selectList(null);
        for (Business b : list) { b.setPassword(null); }
        return list;
    }

    @Transactional
    public void deleteCustomer(int customerId) {
        Customer c = customerMapper.selectById(customerId);
        if (c == null) throw new AppException(404, "用户不存在", 404);
        // 手动删除该用户的所有订单（无外键约束）
        orderMapper.delete(new LambdaQueryWrapper<Orders>().eq(Orders::getCustomerId, customerId));
        // 删除用户（级联删除）
        customerMapper.deleteById(customerId);
    }

    @Transactional
    public void deleteBusiness(int businessId) {
        Business b = businessMapper.selectById(businessId);
        if (b == null) throw new AppException(404, "商家不存在", 404);
        //手动删订单
        orderMapper.delete(new LambdaQueryWrapper<Orders>().eq(Orders::getBusinessId, businessId));
        //访问日志
        visitLogMapper.delete(new LambdaQueryWrapper<VisitLog>().eq(VisitLog::getBusinessId, businessId));
        // 删除商家
        businessMapper.deleteById(businessId);
    }
}
