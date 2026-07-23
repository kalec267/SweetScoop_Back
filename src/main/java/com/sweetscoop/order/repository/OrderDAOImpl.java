package com.sweetscoop.order.repository;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sweetscoop.order.model.OrderItemMenuVO;
import com.sweetscoop.order.model.OrderItemOptionVO;
import com.sweetscoop.order.model.OrderItemVO;
import com.sweetscoop.order.model.OrderVO;
import com.sweetscoop.payment.model.PaymentVO;
@Repository
public class OrderDAOImpl implements OrderDAO {

    private static final String NAMESPACE = "com.sweetscoop.order.repository.OrderDAO.";

    @Autowired
    private SqlSession sqlSession;

    @Override
    public int insertOrder(OrderVO order) {
        return sqlSession.insert(NAMESPACE + "insertOrder", order);
    }

    @Override
    public int insertOrderItem(OrderItemVO orderItem) {
        return sqlSession.insert(NAMESPACE + "insertOrderItem", orderItem);
    }

    @Override
    public int insertOrderItemMenu(OrderItemMenuVO orderItemMenu) {
        return sqlSession.insert(NAMESPACE + "insertOrderItemMenu", orderItemMenu);
    }

    @Override
    public int insertOrderItemOption(OrderItemOptionVO orderItemOption) {
        return sqlSession.insert(NAMESPACE + "insertOrderItemOption", orderItemOption);
    }

    @Override
    public int insertPayment(PaymentVO payment) {
        return sqlSession.insert(NAMESPACE + "insertPayment", payment);
    }

    @Override
    public OrderVO selectOrder(int id) {
        return sqlSession.selectOne(NAMESPACE + "selectOrder", id);
    }

    @Override
    public List<OrderVO> selectOrderList() {
        return sqlSession.selectList(NAMESPACE + "selectOrderList");
    }

    @Override
    public int updateOrderStatus(OrderVO order) {
        return sqlSession.update(NAMESPACE + "updateOrderStatus", order);
    }

    @Override
    public int deleteOrder(int id) {
        return sqlSession.delete(NAMESPACE + "deleteOrder", id);
    }
    
    @Override
    public void deletePaymentByOrderId(int id) {
        sqlSession.delete(NAMESPACE + "deletePaymentByOrderId", id);
    }

    @Override
    public void deleteOrderItemOptionByOrderId(int id) {
        sqlSession.delete(NAMESPACE + "deleteOrderItemOptionByOrderId", id);
    }

    @Override
    public void deleteOrderItemMenuByOrderId(int id) {
        sqlSession.delete(NAMESPACE + "deleteOrderItemMenuByOrderId", id);
    }

    @Override
    public void deleteOrderItemByOrderId(int id) {
        sqlSession.delete(NAMESPACE + "deleteOrderItemByOrderId", id);
    }
}
