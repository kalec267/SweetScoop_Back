package com.sweetscoop.coupon.service;
import com.sweetscoop.coupon.dto.CouponDto;
import com.sweetscoop.coupon.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CouponService {
	
    @Autowired 
    private CouponRepository couponRepository;
    
    public List<CouponDto> getCouponsByMemberId(Integer memberId) {
        return couponRepository.findByMemberIdOrderByIdDesc(memberId).stream().map(CouponDto::new).toList();
    }
}