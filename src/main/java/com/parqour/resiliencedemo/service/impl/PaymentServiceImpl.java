package com.parqour.resiliencedemo.service.impl;

import com.parqour.resiliencedemo.domain.ParkingRoute;
import com.parqour.resiliencedemo.repository.ParkingRouteRepository;
import com.parqour.resiliencedemo.service.PaymentService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final RestClient restClient = RestClient.create();
  private final ParkingRouteRepository parkingRouteRepository;

  @Override
  @CircuitBreaker(name = "default", fallbackMethod = "fallback")
  public String pay(String plateNumber, String parkingUID) {
    Optional<ParkingRoute> parkingRouteOptional = parkingRouteRepository.findByUid(parkingUID);
    if (parkingRouteOptional.isEmpty()) throw new EntityNotFoundException("Parking not found");
    ParkingRoute parkingRoute = parkingRouteOptional.get();
    String result = restClient.post()
        .uri(uriBuilder -> uriBuilder
            .scheme("http")
            .host(parkingRoute.getHost())
            .port(parkingRoute.getPort())
            .path("/top-up")
            .build())
        .retrieve()
        .toEntity(String.class)
        .getBody();

    return "%s result: %s".formatted(plateNumber, result);
  }

  private String fallback(String plateNumber, String parkingUID, Exception e) {
    log.error("Fallback method. Params: {};{}. Reason: {}", plateNumber, parkingUID, e.getMessage());
    return "Caught exception: %s".formatted(e.getMessage());
  }
}
