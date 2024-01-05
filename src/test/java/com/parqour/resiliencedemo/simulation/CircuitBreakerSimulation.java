package com.parqour.resiliencedemo.simulation;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.listFeeder;
import static io.gatling.javaapi.core.CoreDsl.nothingFor;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

import com.github.javafaker.Faker;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CircuitBreakerSimulation extends Simulation {

  {
    setUp(paymentScenario()
        .injectOpen(
            nothingFor(Duration.ofSeconds(5)),
            rampUsers(100).during(Duration.ofSeconds(30)),
            constantUsersPerSec(8).during(Duration.ofSeconds(60)).randomized(),
            rampUsersPerSec(9).to(4).during(Duration.ofSeconds(45)).randomized())
        .protocols(httpProtocolBuilder()));
  }

  private static HttpProtocolBuilder httpProtocolBuilder() {
    return http.baseUrl("http://localhost:8080")
        .maxConnectionsPerHost(10)
        .userAgentHeader("Gatling circuit breaker / local");
  }

  private static HttpRequestActionBuilder payRequest() {
    return http("Pay Request")
        .post("/api/v1/pay")
        .queryParam("parking_uid", "#{parking_uid}")
        .queryParam("plate_number", "#{plate_number}");
  }

  private static ScenarioBuilder paymentScenario() {
    return scenario("Payment simulation with circuit breaker on post method")
        .feed(plateNumberFeeder().circular())
        .feed(parkingUIDFeeder().circular())
        .repeat(100).on(exec(payRequest()));
  }

  private static FeederBuilder<Object> plateNumberFeeder() {
    Faker faker = new Faker();
    List<Map<String, Object>> feeder = new LinkedList<>();
    for (int i = 0; i < 10000; i++) {
      if (i < 1000) {
        feeder.add(Collections.singletonMap("plate_number", "lpn_" + faker.number()));
      } else if (i < 2000) {
        feeder.add(Collections.singletonMap("plate_number", "lpn_" + faker.animal()));
      } else if (i < 3000) {
        feeder.add(Collections.singletonMap("plate_number", "lpn_" + faker.ancient()));
      } else if (i < 4000) {
        feeder.add(Collections.singletonMap("plate_number", "lpn_" + faker.yoda()));
      } else if (i < 5000) {
        feeder.add(Collections.singletonMap("plate_number", "lpn_" + faker.zelda()));
      } else if (i < 6000) {
        feeder.add(Collections.singletonMap("plate_number", "lpn_" + faker.witcher()));
      } else if (i < 7000) {
        feeder.add(Collections.singletonMap("plate_number", "lpn_" + faker.weather()));
      } else if (i < 8000) {
        feeder.add(Collections.singletonMap("plate_number", "lpn_" + faker.university()));
      } else if (i < 9000) {
        feeder.add(Collections.singletonMap("plate_number", "lpn_" + faker.twinPeaks()));
      } else {
        feeder.add(Collections.singletonMap("plate_number", "lpn_" + faker.team()));
      }
    }

    return listFeeder(feeder);
  }

  private static FeederBuilder<Object> parkingUIDFeeder() {
    Iterator<Map<String, Object>> iterator;
    List<Map<String, Object>> feeder = new LinkedList<>();
    for (int i = 0; i < 5; i++) {
      feeder.add(Collections.singletonMap("parking_uid", i));
    }
    return listFeeder(feeder);
  }
}
