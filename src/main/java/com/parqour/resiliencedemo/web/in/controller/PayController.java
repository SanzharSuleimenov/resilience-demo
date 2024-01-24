package com.parqour.resiliencedemo.web.in.controller;

import com.parqour.resiliencedemo.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PayController {

  private final PaymentService paymentService;

  @PostMapping("/v1/pay")
  public ResponseEntity<String> pay(@RequestParam(name = "parking_uid") String parkingUID,
      @RequestParam(name = "plate_number") String plateNumber) throws Exception {

    return ResponseEntity.ok(paymentService.pay(plateNumber, parkingUID));
  }
}
