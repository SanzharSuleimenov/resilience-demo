package com.parqour.resiliencedemo.config;

import com.parqour.resiliencedemo.application.port.out.GetParkingRoutePort;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BulkheadCustomConfig implements CommandLineRunner {

  private final GetParkingRoutePort getParkingRoutePort;

  @Override
  public void run(String... args) throws Exception {
    BulkheadRegistry registry = bulkheadRegistry();
    getParkingRoutePort.findAll()
        .forEach(parkingRoute -> registry.bulkhead(parkingRoute.getName()));
  }

  @Bean
  public BulkheadRegistry bulkheadRegistry() {
    BulkheadConfig config = BulkheadConfig.custom()
        .maxConcurrentCalls(30)
        .maxWaitDuration(Duration.ofMillis(700))
        .build();

    return BulkheadRegistry.of(config);
  }
}
