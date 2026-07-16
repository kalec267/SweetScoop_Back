package com.sweetscoop.kiosk.entity;

import com.sweetscoop.branch.entity.Branch;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "KIOSK")
@Getter
@Setter
@NoArgsConstructor
public class Kiosk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "status", length = 20, nullable = false)
    private String status; // "정상", "꺼짐", "장애"

    // 💡 중요: 어떤 분점에 속한 키오스크인지 연결 (외래키)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;
 
}
