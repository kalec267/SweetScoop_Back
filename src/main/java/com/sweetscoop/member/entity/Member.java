package com.sweetscoop.member.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "MEMBER")
@Getter
@Setter
@NoArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "order_count", columnDefinition = "INT DEFAULT 0")
    private Integer orderCount = 0;

    @Column(name = "point", columnDefinition = "INT DEFAULT 0")
    private Integer point = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}