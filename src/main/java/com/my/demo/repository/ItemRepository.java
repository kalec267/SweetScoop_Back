package com.my.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.my.demo.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Integer> {

}