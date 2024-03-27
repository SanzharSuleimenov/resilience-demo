package com.parqour.resiliencedemo.payment_gateway;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;
import org.apache.commons.lang3.RandomStringUtils;

public class AuthThenCheckAndPaySimulation extends Simulation {

  {
    setUp(
        authAndCheckAndPayScenario("1")
            .injectOpen(
                rampUsersPerSec(1).to(10).during(30)
            )
            .protocols(httpProtocolBuilder1()),
        authAndCheckAndPayScenario("2")
            .injectOpen(
                constantUsersPerSec(2).during(30)
            )
            .protocols(httpProtocolBuilder2()),
        authAndCheckAndPayScenario("3")
            .injectOpen(
                constantUsersPerSec(2).during(30)
            )
            .protocols(httpProtocolBuilder3())
    );
  }

  private static HttpProtocolBuilder httpProtocolBuilder1() {
    return http.baseUrl("http://localhost:8080")
        .maxConnectionsPerHost(40)
        .userAgentHeader("Gatling local");
  }

  private static HttpProtocolBuilder httpProtocolBuilder2() {
    return http.baseUrl("http://localhost:8080")
        .maxConnectionsPerHost(20)
        .userAgentHeader("Gatling local");
  }

  private static HttpProtocolBuilder httpProtocolBuilder3() {
    return http.baseUrl("http://localhost:8080")
        .maxConnectionsPerHost(20)
        .userAgentHeader("Gatling local");
  }

  private static HttpRequestActionBuilder authRequest(String uid) {
    return http("Auth request to " + uid)
        .post("/user/ext_login")
        .header("Authorization", "Basic dGVzdDp0ZXN0")
        .header("Content-Type", "application/x-www-form-urlencoded")
        .formParam("grant_type", "client_credentials");
  }

  private static HttpRequestActionBuilder checkRequest(String uid) {
    return http("Check request to " + uid)
        .post("/payment/check")
        .header("Authorization", "Bearer #{access_token}")
        .asJson()
        .body(StringBody("""
            {
                "command": "check",
                "account": "90VK080",
                "parking_uid": "%s",
                "service_id": 1
            }
            """.formatted(uid)));
  }

  private static HttpRequestActionBuilder payRequest(String uid) {
    return http("Pay request to " + uid)
        .post("/payment/pay")
        .header("Authorization", "Bearer #{access_token}")
        .asJson()
        .body(StringBody(session -> """
            {
                "command": "pay",
                "account": "99OH072",
                "txn_id": "%s",
                "sum": "3",
                "parking_uid": "%s",
                "service_id": "1"
            }
            """.formatted(RandomStringUtils.randomAlphabetic(10), uid)));
  }

  private static ScenarioBuilder authAndCheckAndPayScenario(String uid) {
    return scenario("Auth and Check and then Pay to " + uid)
        .exec(authRequest(uid)
            .check(status().is(200))
            .check(jsonPath("$.access_token").ofString().exists().saveAs("access_token"))
        )
        .exec(checkRequest(uid)
            .check(status().is(200))
        )
        .exec(payRequest(uid)
            .check(status().is(200)));
  }

}
