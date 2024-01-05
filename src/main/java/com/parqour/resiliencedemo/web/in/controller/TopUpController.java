package com.parqour.resiliencedemo.web.in.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TopUpController {

  @PostMapping("/top-up")
  public String topUp() {
    return "topped up";
  }
}
