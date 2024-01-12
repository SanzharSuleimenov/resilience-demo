package com.parqour.resiliencedemo.config;

import com.parqour.resiliencedemo.application.port.out.GetParkingRoutePort;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CircuitBreakerConfig implements CommandLineRunner {

  private final GetParkingRoutePort getParkingRoutePort;
  private final CircuitBreakerRegistry circuitBreakerRegistry;

  public CircuitBreakerConfig(GetParkingRoutePort getParkingRoutePort,
      CircuitBreakerRegistry circuitBreakerRegistry) {
    this.getParkingRoutePort = getParkingRoutePort;
    this.circuitBreakerRegistry = circuitBreakerRegistry;
  }

  @Override
  public void run(String... args) throws Exception {
    log.info("Configuring Circuit Breakers started...");
    getParkingRoutePort.findAll()
        .forEach(parkingRoute -> {
          circuitBreakerRegistry.circuitBreaker(parkingRoute.getName(), "default");
        });
  }
}
