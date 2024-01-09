package com.parqour.resiliencedemo.application.port.out;

import com.parqour.resiliencedemo.adapter.out.persistence.ParkingRoute;
import java.util.List;

public interface SaveParkingRoutePort {

  void saveParkingRoutes(List<ParkingRoute> parkingRouteList);
}
