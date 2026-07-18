package com.sweetscoop.item.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sweetscoop.item.entity.Item;
import com.sweetscoop.item.service.ItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public List<Item> list() {
        return itemService.findAll();
    }

    @GetMapping("/{id}")
    public Item detail(@PathVariable Integer id) {
        return itemService.findById(id);
    }

    @PostMapping
    public Item create(@RequestBody Item item) {
        return itemService.save(item);
    }

    @PutMapping("/{id}")
    public Item update(@PathVariable Integer id,
                       @RequestBody Item item) {
        return itemService.update(id, item);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        itemService.delete(id);
    }
}