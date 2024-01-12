package com.parqour.resiliencedemo.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.parqour.resiliencedemo.ResilienceDemoApplication;
import com.parqour.resiliencedemo.adapter.out.persistence.ParkingRoute;
import com.parqour.resiliencedemo.application.port.out.GetParkingRoutePort;
import com.parqour.resiliencedemo.application.port.service.UpstreamService;
import com.parqour.resiliencedemo.service.PaymentService;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class})
@SpringBootTest(classes = ResilienceDemoApplication.class)
@Execution(ExecutionMode.SAME_THREAD)
public class CircuitBreakerUnitTesting {

  @MockBean
  private GetParkingRoutePort getParkingRoutePort;
  @MockBean
  private RestTemplate restTemplate;


  @Autowired
  private CircuitBreakerRegistry circuitBreakerRegistry;
  @Autowired
  private PaymentService paymentService;

  @Test
  void requestPayWhenCircuitBreakerIsClosedAndUpstreamServicesIsOk() throws Exception {
    circuitBreakerRegistry.circuitBreaker(UpstreamService.CB_EXTERNAL_SERVICE)
        .reset();
    circuitBreakerRegistry.circuitBreaker(UpstreamService.CB_EXTERNAL_SERVICE)
        .transitionToClosedState();

    ParkingRoute tempParkingRoute = new ParkingRoute(1L, "localhost", 8080,
        UpstreamService.CB_EXTERNAL_SERVICE, "1");

    when(getParkingRoutePort.findByUid(eq("1")))
        .thenReturn(Optional.of(tempParkingRoute));
    when(restTemplate.postForEntity("http://localhost:8080/top-up", null, String.class))
        .thenReturn(ResponseEntity.ok("topped up"));

    String actualResponse = paymentService.pay("123TEST01", "1");

    verify(getParkingRoutePort).findByUid(eq("1"));
    verify(restTemplate).postForEntity("http://localhost:8080/top-up", null, String.class);

    assertEquals("123TEST01 result: topped up", actualResponse);
  }

  @Test
  void requestPayWhenCircuitBreakerIsClosedAndUpstreamServiceTimesOut() throws Exception {
    circuitBreakerRegistry.circuitBreaker(UpstreamService.CB_EXTERNAL_SERVICE)
        .reset();
    circuitBreakerRegistry.circuitBreaker(UpstreamService.CB_EXTERNAL_SERVICE)
        .transitionToClosedState();

    ParkingRoute tempParkingRoute = new ParkingRoute(1L, "localhost", 8080,
        UpstreamService.CB_EXTERNAL_SERVICE, "1");

    when(getParkingRoutePort.findByUid(eq("1")))
        .thenReturn(Optional.of(tempParkingRoute));
    when(restTemplate.postForEntity("http://localhost:8080/top-up", null, String.class))
        .thenThrow(new RuntimeException());

    try {
      paymentService.pay("123TEST01", "1");

      fail("Test scenario expects Runtime Exception at this point");
    } catch (RuntimeException e) {
      assertEquals(RuntimeException.class, e.getClass());
      assertEquals(1,
          circuitBreakerRegistry.circuitBreaker(UpstreamService.CB_EXTERNAL_SERVICE).getMetrics()
              .getNumberOfFailedCalls());
    }
  }

  @Test
  void requestPayWhenCircuitBreakerIsOpen() {
    circuitBreakerRegistry.circuitBreaker(UpstreamService.CB_EXTERNAL_SERVICE)
        .reset();
    circuitBreakerRegistry.circuitBreaker(UpstreamService.CB_EXTERNAL_SERVICE)
        .transitionToOpenState();

    ParkingRoute tempParkingRoute = new ParkingRoute(1L, "localhost", 8080,
        UpstreamService.CB_EXTERNAL_SERVICE, "1");

    when(getParkingRoutePort.findByUid(eq("1")))
        .thenReturn(Optional.of(tempParkingRoute));

    try {
      paymentService.pay("123TEST01", "1");

      fail("Test scenario expects failure at this point");
    } catch (Exception e) {
      assertEquals(CallNotPermittedException.class, e.getClass());
      verifyNoInteractions(restTemplate);
    }
  }
}
