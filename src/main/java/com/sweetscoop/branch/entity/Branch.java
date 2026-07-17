package com.sweetscoop.branch.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sweetscoop.kiosk.entity.Kiosk;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "BRANCH")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({
    "hibernateLazyInitializer",
    "handler"
})
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
        name = "branch_name",
        length = 50,
        nullable = false
    )
    private String branchName;

    @Column(
        name = "location",
        length = 100
    )
    private String location;

    @Builder.Default
    @OneToMany(
        mappedBy = "branch",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    @JsonIgnoreProperties("branch")
    private List<Kiosk> kiosks = new ArrayList<>();
}