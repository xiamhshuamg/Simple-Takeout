package com.ynu.edu.elm_intermediate.modules.address.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ynu.edu.elm_intermediate.common.AppException;
import com.ynu.edu.elm_intermediate.entity.DeliveryAddress;
import com.ynu.edu.elm_intermediate.mapper.AddressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    private final AddressMapper addressMapper;

    public AddressService(AddressMapper addressMapper) {

        this.addressMapper = addressMapper;
    }

    public List<DeliveryAddress> listByCustomer(int uid) {
        return addressMapper.selectList(new LambdaQueryWrapper<DeliveryAddress>()
                .eq(DeliveryAddress::getCustomerId, uid)
                .orderByDesc(DeliveryAddress::getIsDefault) // 默认地址排前面
                .orderByDesc(DeliveryAddress::getDaId));
    }

    public void add(int uid, String name, int sex, String tel, String addr) {
        // 1. 检查是否是第一条地址，如果是，默认设为默认地址(1)，否则为0
        Long count = addressMapper.selectCount(new LambdaQueryWrapper<DeliveryAddress>().eq(DeliveryAddress::getCustomerId, uid));
        int isDefault = (count == null || count == 0) ? 1 : 0;

        DeliveryAddress da = new DeliveryAddress();
        da.setCustomerId(uid);
        da.setContactName(name);
        da.setContactSex(sex);
        da.setContactTel(tel);
        da.setAddress(addr);
        da.setIsDefault(isDefault);
        
        addressMapper.insert(da);
    }

    @Transactional
    public void delete(int daId, int uid) {
        DeliveryAddress da = addressMapper.selectOne(new LambdaQueryWrapper<DeliveryAddress>()
                .eq(DeliveryAddress::getDaId, daId)
                .eq(DeliveryAddress::getCustomerId, uid)); // 确保删的是自己的
        
        if (da == null) return;

        // 删除
        addressMapper.deleteById(daId);

        // 如果删的是默认地址，需要把最新的一条设为默认
        if (da.getIsDefault() == 1) {
            DeliveryAddress next = addressMapper.selectOne(new LambdaQueryWrapper<DeliveryAddress>()
                    .eq(DeliveryAddress::getCustomerId, uid)
                    .orderByDesc(DeliveryAddress::getDaId)
                    .last("LIMIT 1"));
            if (next != null) {
                next.setIsDefault(1);
                addressMapper.updateById(next);
            }
        }
    }

    @Transactional
    public void setDefault(int daId, int uid) {
        // 1. 把该用户所有地址设为非默认
        addressMapper.update(null, new LambdaUpdateWrapper<DeliveryAddress>()
                .eq(DeliveryAddress::getCustomerId, uid)
                .set(DeliveryAddress::getIsDefault, 0));

        // 2. 把目标地址设为默认
        addressMapper.update(null, new LambdaUpdateWrapper<DeliveryAddress>()
                .eq(DeliveryAddress::getDaId, daId)
                .eq(DeliveryAddress::getCustomerId, uid)
                .set(DeliveryAddress::getIsDefault, 1));
    }
}