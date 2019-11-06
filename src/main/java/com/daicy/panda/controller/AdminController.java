package com.daicy.panda.controller;

import com.daicy.panda.annotation.Controller;
import com.daicy.panda.annotation.RequestMapping;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.controller
 * @date:19-11-6
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @RequestMapping("/")
    public String index() {
        return "Welcome to panda!";
    }

    @RequestMapping("/go")
    public String go() {
        return "go go!";
    }
}
