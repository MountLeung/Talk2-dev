package org.itxuexi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("f")
public class HiController {
    @GetMapping("hi")
    public Object hi(){
        return "Hi!";
    }
}
