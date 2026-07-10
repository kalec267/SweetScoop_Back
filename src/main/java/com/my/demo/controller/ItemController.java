package com.my.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.my.demo.entity.Item;
import com.my.demo.service.ItemService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;


    // 목록 화면
    @GetMapping
    public String list(Model model) {

        model.addAttribute("items", itemService.findAll());

        return "item/list";
    }


    // 등록 화면
    @GetMapping("/new")
    public String createForm(Model model) {

        model.addAttribute("item", new Item());

        return "item/create";
    }


    // 등록 처리
    @PostMapping
    public String create(@ModelAttribute Item item) {

        itemService.save(item);
        System.out.println("등록완료");
        return "redirect:/items";
    }
    
    // 수정 화면
    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Integer id,
            Model model) {

        Item item = itemService.findById(id);

        model.addAttribute("item", item);

        return "item/edit";
    }


    // 수정 처리
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Integer id,
            @ModelAttribute Item item) {

        itemService.update(id, item);

        return "redirect:/items";
    }


    // 삭제
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Integer id) {

        itemService.delete(id);

        return "redirect:/items";
    }
    
}