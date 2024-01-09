package com.parqour.resiliencedemo.application.port.out;

import com.parqour.resiliencedemo.adapter.out.persistence.ParkingRoute;

public interface TopUpBalancePort {

  String topUpBalance(ParkingRoute parkingRoute);
}
