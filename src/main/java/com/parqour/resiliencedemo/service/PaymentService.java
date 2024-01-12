package com.parqour.resiliencedemo.service;

public interface PaymentService {

  String pay(String plateNumber, String parkingUID) throws Exception;
}
