package com.sweetscoop.admin.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private BranchManager manager;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hqManager_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private HqManager hqManager;
    
    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column(name="answered_at")
    private LocalDateTime answeredAt;
    
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
    
    public void answer(String answer, HqManager hqManager){

        this.answer = answer;
        this.hqManager = hqManager;
        this.answeredAt = LocalDateTime.now();

    }
    
}