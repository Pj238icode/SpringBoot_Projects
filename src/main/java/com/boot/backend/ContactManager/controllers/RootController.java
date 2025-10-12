package com.boot.backend.ContactManager.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/root")
public class RootController {

    @GetMapping("/hello")
    public String sayHello(){
        return "Hello World";
    }


}
