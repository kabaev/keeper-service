package com.kabaev.shop.service.keeper.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/keeper")
public class TestController {

    /**
     * Return greeting from the keeper-service
     *
     * @param name the one to greet
     * @return greeting
     */
    @GetMapping("/{name}")
    public String getGreeting(@PathVariable("name") String name) {
        return String.format("Greet, %s, from the keeper-service!", name);
    }

}
