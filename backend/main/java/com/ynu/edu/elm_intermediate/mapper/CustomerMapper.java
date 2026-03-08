package com.ynu.edu.elm_intermediate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ynu.edu.elm_intermediate.entity.Customer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
