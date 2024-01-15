package com.parqour.resiliencedemo.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.parqour.resiliencedemo.ResilienceDemoApplication;
import com.parqour.resiliencedemo.adapter.out.persistence.ParkingRoute;
import com.parqour.resiliencedemo.application.port.out.GetParkingRoutePort;
import com.parqour.resiliencedemo.application.port.service.UpstreamService;
import com.parqour.resiliencedemo.service.PaymentService;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import java.time.Duration;
import java.util.Optional;
import java.util.Timer;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.web.client.RestTemplate;

@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class})
@SpringBootTest(classes = ResilienceDemoApplication.class)
public class BulkheadUnitTest {

  @MockBean
  private GetParkingRoutePort getParkingRoutePort;

  @Autowired
  private BulkheadRegistry bulkheadRegistry;
  @Autowired
  private PaymentService paymentService;

  @Test
  void verifyCorrectBulkhead() {
    assertEquals(130, bulkheadRegistry.bulkhead(UpstreamService.CB_EXTERNAL_SERVICE).getBulkheadConfig()
        .getMaxConcurrentCalls());
    assertEquals(Duration.ofMillis(700),
        bulkheadRegistry.bulkhead(UpstreamService.CB_EXTERNAL_SERVICE).getBulkheadConfig().getMaxWaitDuration());
  }

  @Test
  void concurrentCalls() throws Exception {
    ParkingRoute parkingRoute = new ParkingRoute(1L, "localhost", 8080,
        UpstreamService.CB_EXTERNAL_SERVICE, "1");
    StopWatch stopWatch = StopWatch.create();
    stopWatch.start();
    when(getParkingRoutePort.findByUid(eq("1"))).thenReturn(Optional.of(parkingRoute));
    String result = paymentService.pay("1234TEST", "1");
    stopWatch.stop();
    assertTrue((stopWatch.getStopTime() - stopWatch.getStartTime()) > 5000);
    assertEquals("1234TEST result: topped up", result);
  }
}
