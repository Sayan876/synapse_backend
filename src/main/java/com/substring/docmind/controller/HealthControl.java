
package com.substring.docmind.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthControl {

    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}
