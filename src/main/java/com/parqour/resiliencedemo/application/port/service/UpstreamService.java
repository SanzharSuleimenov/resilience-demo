package com.parqour.resiliencedemo.application.port.service;

import com.parqour.resiliencedemo.application.port.out.TopUpBalancePort;
import com.parqour.resiliencedemo.adapter.out.persistence.ParkingRoute;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class UpstreamService implements TopUpBalancePort {

  private final RestClient restClient = RestClient.create();

  public static final String CB_EXTERNAL_SERVICE = "externalService";

  @Override
  @CircuitBreaker(name = CB_EXTERNAL_SERVICE)
  public String topUpBalance(ParkingRoute parkingRoute) {

    return restClient.post()
        .uri(uriBuilder -> uriBuilder
            .scheme("http")
            .host(parkingRoute.getHost())
            .port(parkingRoute.getPort())
            .path("/top-up")
            .build())
        .retrieve()
        .toEntity(String.class)
        .getBody();
  }

  public String fallback(String plateNumber, String parkingUID, Exception e) {
    log.error("Fallback method. Params: {};{}. Reason: {}", plateNumber, parkingUID, e.getMessage());
    return "Caught exception: %s".formatted(e.getMessage());
  }
}
