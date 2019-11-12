package com.daicy.panda.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.controller
 * @date:19-11-6
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @RequestMapping("/")
    public String index() {
        return "Welcome to panda!";
    }

    @RequestMapping("/go")
    public String go(String aa,Integer bb) {
        return aa + "go go!" + bb;
    }
}
