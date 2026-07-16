package com.sweetscoop.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardController {

    @RequestMapping({
        "/",
        "/size",
        "/cup",
        "/menu",
        "/payment",
        "/payment/success",
        "/complete",
        "/admin/branches"
    })
    public String forwardVueRoutes() {
        return "forward:/index.html";
    }
}