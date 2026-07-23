package com.sweetscoop.payment.repository;

import com.sweetscoop.admin.entity.Promotion;
import com.sweetscoop.payment.dto.PaymentRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PaymentMapper {
    Map<String, Object> selectOrderForUpdate(@Param("orderId") String string);
    int insertPayment(@Param("payment") PaymentRequestDTO payment, @Param("pgId") String pgId);
    int updateOrderStatus(@Param("orderId") String string, @Param("status") String status, @Param("waitingNo") int waitingNo);
    Map<String, Object> selectReceiptDetails(@Param("orderId") String string);
    List<Map<String, Object>> selectOrderItems(@Param("orderId") String orderId);
    Integer selectMaxWaitingNo();
	List<Promotion> findAll();
}