package com.ng.dtogen.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/warmup")
public class WarmupController {

    @GetMapping
    public String warmup() {
        System.out.println("Warmup endpoint hit - backend waking up");
        return "Backend is warming up";
    }
}
