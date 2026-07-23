package com.sweetscoop.item.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ITEM")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({
    "hibernateLazyInitializer",
    "handler"
})
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(nullable = false)
    private Integer unit;

    @Column(name = "item_name", nullable = false, length = 50)
    private String itemName;
    
    @Column(name = "cost_price")
    private Integer costPrice;
}