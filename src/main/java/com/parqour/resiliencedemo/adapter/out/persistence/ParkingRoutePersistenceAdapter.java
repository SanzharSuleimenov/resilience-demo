package com.parqour.resiliencedemo.adapter.out.persistence;

import com.parqour.resiliencedemo.application.port.out.GetParkingRoutePort;
import com.parqour.resiliencedemo.application.port.out.SaveParkingRoutePort;
import jakarta.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParkingRoutePersistenceAdapter implements GetParkingRoutePort, SaveParkingRoutePort {

  private final ParkingRouteRepository parkingRouteRepository;

  @Override
  @Cacheable("parking_routes")
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

  @Override
  public List<ParkingRoute> findAll() {
    return parkingRouteRepository.findAll();
  }

  @PostConstruct
  public void init() {
    long count = count();
    if (count == 0) {
      log.info("Parking Routes missing. Starting data insertion...");
      List<ParkingRoute> parkingRouteList = new LinkedList<>();
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("localhost--");
      for (int i = 1; i < 201; ++i) {
        stringBuilder.append(i);
        parkingRouteList.add(ParkingRoute.builder()
            .host("localhost")
            .port(8080)
            .uid(String.valueOf(i))
            .name(stringBuilder.toString())
            .build());
        if (i < 10) {
          stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        } else if (i < 100) {
          stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
        } else {
          stringBuilder.delete(stringBuilder.length() - 3, stringBuilder.length());
        }
      }
      saveParkingRoutes(parkingRouteList);
      log.info("Parking Routes inserted.");
    } else {
      log.info("Parking Routes are already present.");
    }
  }
}
