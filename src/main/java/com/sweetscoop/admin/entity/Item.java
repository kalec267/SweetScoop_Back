package com.sweetscoop.admin.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ITEM")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(nullable = false)
    private Integer unit;

    @Column(name = "item_name", length = 50, nullable = false)
    private String itemName;
}