package com.parqour.resiliencedemo.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.parqour.resiliencedemo.ResilienceDemoApplication;
import com.parqour.resiliencedemo.adapter.out.persistence.ParkingRoute;
import com.parqour.resiliencedemo.application.port.out.GetParkingRoutePort;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;
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
public class BulkheadTest {

  @SpyBean
  private BulkheadRegistry bulkheadRegistry;

  @MockBean
  private GetParkingRoutePort getParkingRoutePort;

  @Test
  void testBulkheadInstancesCreated() {
    when(getParkingRoutePort.findAll()).thenReturn(List.of(
        ParkingRoute.builder().host("localhost").port(8080).uid("1").name("localhost--1").build(),
        ParkingRoute.builder().host("localhost").port(8080).uid("2").name("localhost--2").build(),
        ParkingRoute.builder().host("localhost").port(8080).uid("3").name("localhost--3").build(),
        ParkingRoute.builder().host("localhost").port(8080).uid("4").name("localhost--4").build(),
        ParkingRoute.builder().host("localhost").port(8080).uid("5").name("localhost--5").build(),
        ParkingRoute.builder().host("localhost").port(8080).uid("5").name("localhost--6").build(),
        ParkingRoute.builder().host("localhost").port(8080).uid("5").name("localhost--7").build(),
        ParkingRoute.builder().host("localhost").port(8080).uid("5").name("localhost--8").build(),
        ParkingRoute.builder().host("localhost").port(8080).uid("5").name("localhost--9").build(),
        ParkingRoute.builder().host("localhost").port(8080).uid("5").name("localhost--10").build(),
        ParkingRoute.builder().host("localhost").port(8080).uid("5").name("localhost--11").build()
    ));

    getParkingRoutePort.findAll()
        .forEach(parkingRoute -> bulkheadRegistry.bulkhead(parkingRoute.getName()));

    assertEquals(11, bulkheadRegistry.getAllBulkheads().size());
  }
}
