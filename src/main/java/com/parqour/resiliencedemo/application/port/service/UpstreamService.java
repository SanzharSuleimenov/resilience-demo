package com.parqour.resiliencedemo.application.port.service;

import com.parqour.resiliencedemo.adapter.out.persistence.ParkingRoute;
import com.parqour.resiliencedemo.application.port.out.TopUpBalancePort;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
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
  private final BulkheadRegistry bulkheadRegistry;

  private static final AtomicInteger count = new AtomicInteger(0);

  public static final String CB_EXTERNAL_SERVICE = "externalService";

  @Override
  public String topUpBalance(ParkingRoute parkingRoute) {
    CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(parkingRoute.getName());
    Bulkhead bulkhead = bulkheadRegistry.bulkhead(parkingRoute.getName());

    int val = count.incrementAndGet();
    Supplier<ResponseEntity<String>> supplier = () -> topUpRequest(parkingRoute, val);
    Supplier<ResponseEntity<String>> decorateSupplier = Decorators.ofSupplier(supplier)
        .withCircuitBreaker(circuitBreaker)
        .withBulkhead(bulkhead)
        .decorate();

    var response = decorateSupplier.get();
    return response.getBody();
  }

  public ResponseEntity<String> topUpRequest(ParkingRoute parkingRoute, int val) {
    return restTemplate.postForEntity(
        "http://%s:%d/top-up?val=%d".formatted(parkingRoute.getHost(), parkingRoute.getPort(), val),
        null,
        String.class);
  }

  public String fallback(String plateNumber, String parkingUID, Exception e) {
    log.error("Fallback method. Params: {};{}. Reason: {}", plateNumber, parkingUID,
        e.getMessage());
    return "Caught exception: %s".formatted(e.getMessage());
  }
}
