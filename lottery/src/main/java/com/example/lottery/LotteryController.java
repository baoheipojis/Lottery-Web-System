package com.example.lottery;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lottery")
public class LotteryController {

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to the Lottery System!";
    }
}
