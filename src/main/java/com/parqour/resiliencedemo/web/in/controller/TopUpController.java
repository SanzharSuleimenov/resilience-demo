package com.parqour.resiliencedemo.web.in.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TopUpController {

  @PostMapping("/top-up")
  public String topUp(@RequestParam int val) throws InterruptedException {
    if (val > 300 && val < 1400) {
      Thread.sleep(15_000L);
    } else if (val > 3000 && val < 4500) {
      Thread.sleep(20_000L);
    }
    return "topped up";
  }
}
