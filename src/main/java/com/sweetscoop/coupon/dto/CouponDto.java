package com.sweetscoop.coupon.dto;
import com.sweetscoop.coupon.entity.Coupon;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CouponDto {
    private Integer id;
    private Integer memberId;
    private String name;
    private LocalDateTime issueDate;
    private LocalDateTime expiryDate;
    private Double discountValue;
    private Boolean isUsed;
    private LocalDateTime usedAt;

    public CouponDto(Coupon coupon) {
        this.id = coupon.getId();
        this.memberId = coupon.getMemberId();
        this.name = coupon.getName();
        this.issueDate = coupon.getIssueDate();
        this.expiryDate = coupon.getExpiryDate();
        this.discountValue = coupon.getDiscountValue();
        this.isUsed = coupon.getIsUsed();
        this.usedAt = coupon.getUsedAt();
    }
}