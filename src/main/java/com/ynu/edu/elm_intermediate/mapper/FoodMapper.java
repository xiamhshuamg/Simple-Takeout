package com.ynu.edu.elm_intermediate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ynu.edu.elm_intermediate.entity.Food;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FoodMapper extends BaseMapper<Food> {
}
