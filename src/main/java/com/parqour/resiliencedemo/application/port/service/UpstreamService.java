package com.parqour.resiliencedemo.application.port.service;

import com.parqour.resiliencedemo.adapter.out.persistence.ParkingRoute;
import com.parqour.resiliencedemo.application.port.out.TopUpBalancePort;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpstreamService implements TopUpBalancePort {

  private final RestTemplate restTemplate;
  private final CircuitBreakerRegistry circuitBreakerRegistry;

  public static final String CB_EXTERNAL_SERVICE = "externalService";

  @Override
  public String topUpBalance(ParkingRoute parkingRoute) throws Exception {
    CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(parkingRoute.getName());
    var result = CircuitBreaker.decorateCallable(circuitBreaker, () -> topUpRequest(parkingRoute));
    var response = result.call();
    return response.getBody();
  }

  public ResponseEntity<String> topUpRequest(ParkingRoute parkingRoute) {
    return restTemplate.postForEntity(
        "http://%s:%d/top-up".formatted(parkingRoute.getHost(), parkingRoute.getPort()),
        null,
        String.class);
  }

  public String fallback(String plateNumber, String parkingUID, Exception e) {
    log.error("Fallback method. Params: {};{}. Reason: {}", plateNumber, parkingUID,
        e.getMessage());
    return "Caught exception: %s".formatted(e.getMessage());
  }
}
