package com.parqour.resiliencedemo.application.port.out;

import com.parqour.resiliencedemo.adapter.out.persistence.ParkingRoute;
import java.util.List;
import java.util.Optional;

public interface GetParkingRoutePort {

  Optional<ParkingRoute> findByUid(String uid);

  List<ParkingRoute> findAll();

  long count();
}
