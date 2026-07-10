package com.sweetscoop.payment.repository;

import com.sweetscoop.payment.dto.OrderCreateRequestDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {
    int insertOrder(OrderCreateRequestDTO orderDto);
}