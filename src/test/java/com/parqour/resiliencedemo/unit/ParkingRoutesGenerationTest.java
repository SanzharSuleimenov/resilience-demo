package com.parqour.resiliencedemo.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.parqour.resiliencedemo.ResilienceDemoApplication;
import com.parqour.resiliencedemo.adapter.out.persistence.ParkingRouteRepository;
import java.util.LinkedList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ResilienceDemoApplication.class)
public class ParkingRoutesGenerationTest {

  @Autowired
  ParkingRouteRepository parkingRouteRepository;

  @Test
  void checkDependencyInjection() {
    assertNotNull(parkingRouteRepository);
  }

  @Test
  void testStringBuilderGeneration() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("localhost--");
    stringBuilder.append(1);
    assertThat(stringBuilder.toString()).isEqualTo("localhost--1");
    assertEquals(12, stringBuilder.length());
    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
    assertEquals(11, stringBuilder.length());
    assertThat(stringBuilder.toString()).isEqualTo("localhost--");
    stringBuilder.append(2);
    assertEquals(12, stringBuilder.length());
    assertThat(stringBuilder.toString()).isEqualTo("localhost--2");
  }

  @Test
  void testHostNamesGeneration() {
    LinkedList<String> linkedList = new LinkedList<>();
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("Instance--");
    for (int i = 1; i < 201; i++) {
      stringBuilder.append(i);
      linkedList.add(stringBuilder.toString());
      if (i < 10) stringBuilder.deleteCharAt(stringBuilder.length() - 1);
      else if (i < 100) stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
      else stringBuilder.delete(stringBuilder.length() - 3, stringBuilder.length());
    }

    assertEquals(200, linkedList.size());
    assertThat(linkedList.getFirst()).isEqualTo("Instance--1");
    assertThat(linkedList.getLast()).isEqualTo("Instance--200");
  }
}
