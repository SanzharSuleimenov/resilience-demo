package com.parqour.resiliencedemo.simulation;


import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BulkheadLoadSimulation extends Simulation {

  {
    setUp(paymentScenario()
        .injectOpen(
            nothingFor(Duration.ofSeconds(5)),
            atOnceUsers(10),
            rampUsers(30).during(Duration.ofSeconds(5)),
            rampUsersPerSec(20).to(40).during(Duration.ofSeconds(30))
        )
        .protocols(httpProtocolBuilder()));
  }

  private static HttpProtocolBuilder httpProtocolBuilder() {
    return http.baseUrl("http://localhost:8080")
        .maxConnectionsPerHost(40)
        .userAgentHeader("Gatling bulkhead / local");
  }

  private static ScenarioBuilder paymentScenario() {
    return scenario("Payment simulation with semaphore bulkhead on post method")
        .feed(plateNumberFeeder().random())
        .feed(parkingUIDFeeder().random())
        .exec(payRequest());
  }

  private static HttpRequestActionBuilder payRequest() {
    return http("Pay Request")
        .post("/api/v1/pay")
        .queryParam("parking_uid", "#{parking_uid}")
        .queryParam("plate_number", "#{plate_number}");
  }

  private static FeederBuilder<Object> plateNumberFeeder() {
    Faker faker = new Faker();
    List<Map<String, Object>> feeder = new LinkedList<>();
    for (int i = 0; i < 200; i++) {
      feeder.add(Collections.singletonMap("plate_number", "lpn_" + faker.animal()));
    }

    return listFeeder(feeder);
  }

  private static FeederBuilder<Object> parkingUIDFeeder() {
    List<Map<String, Object>> feeder = new LinkedList<>();
    for (int i = 1; i < 6; i++) {
      feeder.add(Collections.singletonMap("parking_uid", i));
    }

    return listFeeder(feeder);
  }
}
