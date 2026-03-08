package com.ynu.edu.elm_intermediate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ynu.edu.elm_intermediate.entity.DeliveryAddress;
import com.ynu.edu.elm_intermediate.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AddressMapper extends BaseMapper<DeliveryAddress> {
}
