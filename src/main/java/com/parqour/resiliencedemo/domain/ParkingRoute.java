package com.parqour.resiliencedemo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "parking_route")
public class ParkingRoute {

  @Id
  @GeneratedValue(generator = "parking_route_sequence_generator")
  @GenericGenerator(name = "parking_route_sequence_generator",
      parameters = {
          @Parameter(name = "sequence_name", value = "parking_route_sequence"),
          @Parameter(name = "initial_value", value = "1"),
          @Parameter(name = "increment_size", value = "1")
      })
  private long id;

  private String host;

  private int port;

  private String name;

  private String uid;
}
