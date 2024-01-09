package com.parqour.resiliencedemo.service.impl;

import com.parqour.resiliencedemo.adapter.out.persistence.ParkingRoute;
import com.parqour.resiliencedemo.application.port.out.GetParkingRoutePort;
import com.parqour.resiliencedemo.application.port.out.TopUpBalancePort;
import com.parqour.resiliencedemo.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final GetParkingRoutePort getParkingRoutePort;
  private final TopUpBalancePort topUpBalancePort;

  @Override
  public String pay(String plateNumber, String parkingUID) {
    Optional<ParkingRoute> parkingRouteOptional = getParkingRoutePort.findByUid(parkingUID);
    if (parkingRouteOptional.isEmpty()) {
      throw new EntityNotFoundException("Parking not found");
    }
    ParkingRoute parkingRoute = parkingRouteOptional.get();
    String result = topUpBalancePort.topUpBalance(parkingRoute);

    return "%s result: %s".formatted(plateNumber, result);
  }
}
