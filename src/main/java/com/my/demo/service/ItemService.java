package com.my.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.my.demo.entity.Item;
import com.my.demo.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;


    // 등록
    public Item save(Item item) {
        return itemRepository.save(item);
    }


    // 전체 조회
    public List<Item> findAll() {
        return itemRepository.findAll();
    }


    // 선택 조회
    public Item findById(Integer id) {
        return itemRepository.findById(id)
                .orElseThrow();
    }


    // 수정
    public Item update(Integer id, Item item) {

        Item entity = itemRepository.findById(id)
                .orElseThrow();

        entity.setCategoryId(item.getCategoryId());
        entity.setUnit(item.getUnit());
        entity.setItemName(item.getItemName());

        return itemRepository.save(entity);
    }


    // 삭제
    public void delete(Integer id) {
        itemRepository.deleteById(id);
    }
}