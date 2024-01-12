package com.parqour.resiliencedemo.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.parqour.resiliencedemo.ResilienceDemoApplication;
import com.parqour.resiliencedemo.adapter.out.persistence.ParkingRoute;
import com.parqour.resiliencedemo.application.port.out.GetParkingRoutePort;
import com.parqour.resiliencedemo.service.PaymentService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.Optional;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class})
@SpringBootTest(classes = ResilienceDemoApplication.class)
@Execution(ExecutionMode.SAME_THREAD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MultipleCircuitBreakersUnitTest {

  @MockBean
  private GetParkingRoutePort getParkingRoutePort;
  @MockBean
  private RestTemplate restTemplate;

  @Autowired
  private CircuitBreakerRegistry circuitBreakerRegistry;
  @Autowired
  private PaymentService paymentService;

  @Test
  @Order(1)
  void requestRoutesOneCircuitBreaker() throws Exception {

    final String nameDefault = "externalService";

    circuitBreakerRegistry.circuitBreaker(nameDefault).reset();
    circuitBreakerRegistry.circuitBreaker(nameDefault).transitionToClosedState();

    ParkingRoute baseRoute = new ParkingRoute(1L, "localhost", 8080, nameDefault, "10");
    ParkingRoute firstRoute = new ParkingRoute(2L, "localhost", 8080, nameDefault, "1");
    ParkingRoute secondRoute = new ParkingRoute(3L, "localhost", 8080, nameDefault, "2");

    when(getParkingRoutePort.findByUid(eq("10"))).thenReturn(Optional.of(baseRoute));
    when(getParkingRoutePort.findByUid(eq("1"))).thenReturn(Optional.of(firstRoute));
    when(getParkingRoutePort.findByUid(eq("2"))).thenReturn(Optional.of(secondRoute));

    when(restTemplate.postForEntity("http://localhost:8080/top-up", null, String.class))
        .thenThrow(new RuntimeException("Base route exception"));

    try {
      paymentService.pay("123TEST01", "10");
      fail("Test shouldn't reach this point A");
    } catch (RuntimeException e) {
      assertEquals("Base route exception", e.getMessage());
      assertEquals(1,
          circuitBreakerRegistry.circuitBreaker(nameDefault).getMetrics().getNumberOfFailedCalls());
    }

    try {
      paymentService.pay("123TEST01", "1");
      fail("Test shouldn't reach this point B");
    } catch (RuntimeException e) {
      assertEquals("Base route exception", e.getMessage());
      assertEquals(2,
          circuitBreakerRegistry.circuitBreaker(nameDefault).getMetrics().getNumberOfFailedCalls());
    }

    try {
      paymentService.pay("123TEST01", "2");
      fail("Test shouldn't reach this point C");
    } catch (RuntimeException e) {
      assertEquals("Base route exception", e.getMessage());
      assertEquals(3,
          circuitBreakerRegistry.circuitBreaker(nameDefault).getMetrics().getNumberOfFailedCalls());
    }
  }

  @Test
  @Order(2)
  void requestRoutesWithDifferentCircuitBreakers() throws Exception {
    final String nameDefault = "externalService";
    final String name1 = "test-localhost-1";
    final String name2 = "test-localhost-2";

    circuitBreakerRegistry.circuitBreaker(nameDefault).reset();
    circuitBreakerRegistry.circuitBreaker(nameDefault).transitionToClosedState();
    circuitBreakerRegistry.circuitBreaker(name1, "default")
        .transitionToClosedState();
    circuitBreakerRegistry.circuitBreaker(name2, "default")
        .transitionToClosedState();

    ParkingRoute baseRoute = new ParkingRoute(1L, "localhost", 8080, nameDefault, "10");
    ParkingRoute firstRoute = new ParkingRoute(2L, "localhost", 8080, name1, "1");
    ParkingRoute secondRoute = new ParkingRoute(3L, "localhost", 8080, name2, "2");

    assertEquals(3, circuitBreakerRegistry.getAllCircuitBreakers().size());

    when(getParkingRoutePort.findByUid(eq("10"))).thenReturn(Optional.of(baseRoute));
    when(getParkingRoutePort.findByUid(eq("1"))).thenReturn(Optional.of(firstRoute));
    when(getParkingRoutePort.findByUid(eq("2"))).thenReturn(Optional.of(secondRoute));

    when(restTemplate.postForEntity("http://localhost:8080/top-up", null, String.class))
        .thenThrow(new RuntimeException("Base route exception"));

    try {
      paymentService.pay("123TEST01", "10");
      fail("Test shouldn't reach this point A");
    } catch (RuntimeException e) {
      assertEquals("Base route exception", e.getMessage());
      assertEquals(1,
          circuitBreakerRegistry.circuitBreaker(nameDefault).getMetrics().getNumberOfFailedCalls());
    }

    try {
      paymentService.pay("123TEST01", "1");
      fail("Test shouldn't reach this point B");
    } catch (RuntimeException e) {
      assertEquals("Base route exception", e.getMessage());
      assertEquals(1,
          circuitBreakerRegistry.circuitBreaker(name1).getMetrics().getNumberOfFailedCalls());
    }

    try {
      paymentService.pay("123TEST01", "2");
      fail("Test shouldn't reach this point C");
    } catch (RuntimeException e) {
      assertEquals("Base route exception", e.getMessage());
      assertEquals(1,
          circuitBreakerRegistry.circuitBreaker(name2).getMetrics().getNumberOfFailedCalls());
    }
  }
}
