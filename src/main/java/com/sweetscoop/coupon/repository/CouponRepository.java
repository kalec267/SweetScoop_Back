package com.sweetscoop.coupon.repository;
import com.sweetscoop.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CouponRepository extends JpaRepository<Coupon, Integer> {
    List<Coupon> findByMemberIdOrderByIdDesc(Integer memberId);
    boolean existsByMemberIdAndNameAndIsUsedFalse(Integer memberId, String name);
}