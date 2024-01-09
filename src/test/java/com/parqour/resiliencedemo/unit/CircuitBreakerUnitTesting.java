package com.parqour.resiliencedemo.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.parqour.resiliencedemo.ResilienceDemoApplication;
import com.parqour.resiliencedemo.adapter.out.persistence.ParkingRoute;
import com.parqour.resiliencedemo.application.port.out.GetParkingRoutePort;
import com.parqour.resiliencedemo.application.port.out.TopUpBalancePort;
import com.parqour.resiliencedemo.application.port.service.UpstreamService;
import com.parqour.resiliencedemo.service.PaymentService;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.Optional;
import java.util.stream.IntStream;
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

@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class})
@SpringBootTest(classes = ResilienceDemoApplication.class)
@Execution(ExecutionMode.SAME_THREAD)
public class CircuitBreakerUnitTesting {

  @MockBean
  private GetParkingRoutePort getParkingRoutePort;
  @SpyBean
  private TopUpBalancePort topUpBalancePort;

  @Autowired
  private CircuitBreakerRegistry circuitBreakerRegistry;
  @Autowired
  private PaymentService paymentService;

  @Test
  void requestPayWhenCircuitBreakerIsClosedAndUpstreamServicesIsOk() {
    circuitBreakerRegistry.circuitBreaker(UpstreamService.CB_EXTERNAL_SERVICE)
        .transitionToClosedState();

    ParkingRoute tempParkingRoute = new ParkingRoute(1L, "localhost", 8080, "my local laptop", "1");

    when(getParkingRoutePort.findByUid(eq("1")))
        .thenReturn(Optional.of(tempParkingRoute));
    doReturn("topped up").when(topUpBalancePort).topUpBalance(tempParkingRoute);

    String actualResponse = paymentService.pay("123TEST01", "1");

    verify(getParkingRoutePort).findByUid(eq("1"));
    verify(topUpBalancePort).topUpBalance(tempParkingRoute);

    assertEquals("123TEST01 result: topped up", actualResponse);
  }

  @Test
  void requestPayWhenCircuitBreakerIsClosedAndUpstreamServiceTimesOut() {
    circuitBreakerRegistry.circuitBreaker(UpstreamService.CB_EXTERNAL_SERVICE)
        .transitionToClosedState();

    ParkingRoute tempParkingRoute = new ParkingRoute(1L, "localhost", 8080, "my local laptop", "1");

    when(getParkingRoutePort.findByUid(eq("1")))
        .thenReturn(Optional.of(tempParkingRoute));
    doThrow(new RuntimeException()).when(topUpBalancePort).topUpBalance(tempParkingRoute);

    try {
      paymentService.pay("123TEST01", "1");

      fail("Test scenario expects Runtime Exception at this point");
    } catch (RuntimeException e) {
      assertEquals(RuntimeException.class, e.getClass());
      verify(getParkingRoutePort).findByUid(eq("1"));
    }
  }

  @Test
  void requestPayWhenCircuitBreakerIsOpen() {
    circuitBreakerRegistry.circuitBreaker(UpstreamService.CB_EXTERNAL_SERVICE)
        .transitionToOpenState();

    ParkingRoute tempParkingRoute = new ParkingRoute(1L, "localhost", 8080, "my local laptop", "1");

    when(getParkingRoutePort.findByUid(eq("1")))
        .thenReturn(Optional.of(tempParkingRoute));

    try {
      paymentService.pay("123TEST01", "1");

      fail("Test scenario expects failure at this point");
    } catch (RuntimeException e) {
      assertEquals(CallNotPermittedException.class, e.getClass());
      verifyNoInteractions(topUpBalancePort);
    }
  }

  @Test
  void requestMinNumberOfFailedRequestAndExpectCircuitBreakerTransitionToOpenState() {
    circuitBreakerRegistry.circuitBreaker(UpstreamService.CB_EXTERNAL_SERVICE)
        .transitionToClosedState();

    ParkingRoute tempParkingRoute = new ParkingRoute(1L, "localhost", 8080, "my local laptop", "1");
    ParkingRoute differentRoute = new ParkingRoute(2L, "localhost", 8080, "my local laptop", "2");
    ParkingRoute thirdRoute = new ParkingRoute(3L, "localhost", 8080, "my local laptop", "3");

    when(getParkingRoutePort.findByUid(eq("1")))
        .thenReturn(Optional.of(tempParkingRoute));
    when(getParkingRoutePort.findByUid(eq("2")))
        .thenReturn(Optional.of(differentRoute));

    // somehow Circuit Breaker tracks these two invocations and applies them to buffered calls count
    doThrow(new RuntimeException()).when(topUpBalancePort).topUpBalance(tempParkingRoute);
    doReturn("topped up").when(topUpBalancePort).topUpBalance(differentRoute);
    doThrow(new RuntimeException("Bad request")).when(topUpBalancePort)
        .topUpBalance(thirdRoute);

    try {
      int minNumberOfCalls = circuitBreakerRegistry
          .circuitBreaker(UpstreamService.CB_EXTERNAL_SERVICE)
          .getCircuitBreakerConfig()
          .getMinimumNumberOfCalls() - 3;

      IntStream.rangeClosed(1, minNumberOfCalls)
          .forEach((i) -> {
            try {
              paymentService.pay("123TEST02", "1");
            } catch (RuntimeException e) {
              assertEquals(RuntimeException.class, e.getClass());
            }
          });

      paymentService.pay("123TEST03", "2");

      fail("Test scenario expects failure at this point");
    } catch (RuntimeException e) {
      assertEquals(CallNotPermittedException.class, e.getClass());
    }
  }
}
