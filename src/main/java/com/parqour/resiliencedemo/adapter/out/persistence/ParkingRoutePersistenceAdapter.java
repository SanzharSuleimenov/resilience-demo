package com.parqour.resiliencedemo.adapter.out.persistence;

import com.parqour.resiliencedemo.application.port.out.GetParkingRoutePort;
import com.parqour.resiliencedemo.application.port.out.SaveParkingRoutePort;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParkingRoutePersistenceAdapter implements GetParkingRoutePort, SaveParkingRoutePort {

  private final ParkingRouteRepository parkingRouteRepository;

  @Override
  public Optional<ParkingRoute> findByUid(String uid) {
    return parkingRouteRepository.findByUid(uid);
  }

  @Override
  public void saveParkingRoutes(List<ParkingRoute> parkingRouteList) {
    parkingRouteRepository.saveAllAndFlush(parkingRouteList);
  }

  @Override
  public long count() {
    return parkingRouteRepository.count();
  }

  @PostConstruct
  public void init() {
    long count = count();
    if (count == 0) {
      log.info("Parking Routes missing. Starting data insertion...");
      List<ParkingRoute> parkingRouteList = List.of(
          ParkingRoute.builder().host("localhost").port(8080).uid("1").name("localhost--1").build(),
          ParkingRoute.builder().host("localhost").port(8080).uid("2").name("localhost--2").build(),
          ParkingRoute.builder().host("localhost").port(8080).uid("3").name("localhost--3").build(),
          ParkingRoute.builder().host("localhost").port(8080).uid("4").name("localhost--4").build(),
          ParkingRoute.builder().host("localhost").port(8080).uid("5").name("localhost--5").build());
      saveParkingRoutes(parkingRouteList);
      log.info("Parking Routes inserted.");
    } else {
      log.info("Parking Routes are already present.");
    }
  }
}
