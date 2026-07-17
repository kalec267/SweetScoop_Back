package com.sweetscoop.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "BRANCH")
@Getter @Setter
public class ScmBranch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "branch_name", nullable = false, length = 50)
    private String branchName;

    @Column(name = "location", length = 100)
    private String location;
}