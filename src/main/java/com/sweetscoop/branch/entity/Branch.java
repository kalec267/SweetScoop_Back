package com.sweetscoop.branch.entity;

import java.util.ArrayList;
import java.util.List;

import com.sweetscoop.kiosk.entity.Kiosk;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "BRANCH")
@Getter
@Setter
@NoArgsConstructor
public class Branch {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "branch_name", length = 50, nullable = false)
    private String branchName;

    @Column(name = "location", length = 100)
    private String location;

    // 1:N 연관관계 매핑 (지저분한 getStatus() 로직은 서비스로 가고 데이터 구조만 남음)
    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Kiosk> kiosks = new ArrayList<>();
}
