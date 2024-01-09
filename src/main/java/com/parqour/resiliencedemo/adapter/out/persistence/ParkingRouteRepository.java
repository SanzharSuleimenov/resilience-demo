package com.parqour.resiliencedemo.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingRouteRepository extends JpaRepository<ParkingRoute, Long> {

  Optional<ParkingRoute> findByUid(String uid);
}
