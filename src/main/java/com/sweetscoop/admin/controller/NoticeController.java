package com.sweetscoop.admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sweetscoop.admin.dto.request.NoticeRequest;
import com.sweetscoop.admin.dto.response.NoticeResponse;
import com.sweetscoop.admin.entity.Notice;
import com.sweetscoop.admin.service.NoticeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
@CrossOrigin
public class NoticeController {


    private final NoticeService noticeService;



    @PostMapping
    public Notice save(@RequestBody NoticeRequest request){

        return noticeService.save(request);

    }




    @GetMapping
    public List<NoticeResponse> findAll(){

        return noticeService.findAll();

    }





    @GetMapping("/{id}")
    public NoticeResponse findById(
            @PathVariable Integer id
    ){

        return noticeService.findById(id);

    }





    @PutMapping("/{id}")
    public NoticeResponse update(
            @PathVariable Integer id,
            @RequestBody NoticeRequest dto
    ){

        return noticeService.update(id,dto);

    }





    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Integer id
    ){

        noticeService.delete(id);

    }

}