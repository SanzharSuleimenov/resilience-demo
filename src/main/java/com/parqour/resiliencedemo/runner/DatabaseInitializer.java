package com.parqour.resiliencedemo.runner;

import com.parqour.resiliencedemo.domain.ParkingRoute;
import com.parqour.resiliencedemo.repository.ParkingRouteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

  private final ParkingRouteRepository parkingRouteRepository;

  @Override
  public void run(String... args) throws Exception {
    long count = parkingRouteRepository.count();
    if (count == 0) {
      log.info("Parking Routes missing. Starting data insertion...");
      List<ParkingRoute> parkingRouteList = List.of(
          ParkingRoute.builder().host("localhost").port(8080).uid("1").name("localhost--1").build(),
          ParkingRoute.builder().host("localhost").port(8080).uid("2").name("localhost--2").build(),
          ParkingRoute.builder().host("localhost").port(8080).uid("3").name("localhost--3").build(),
          ParkingRoute.builder().host("localhost").port(8080).uid("4").name("localhost--4").build(),
          ParkingRoute.builder().host("localhost").port(8080).uid("5").name("localhost--5").build());
      parkingRouteRepository.saveAllAndFlush(parkingRouteList);
      log.info("Parking Routes inserted.");
    } else {
      log.info("Parking Routes are already present.");
    }
  }
}
