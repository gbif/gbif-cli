package org.gbif.cli.converter;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConverterFactoryTest {

  @Test
  public void testUUID() {
    ConvertTestCommand command = new ConvertTestCommand();
    command.run("--conf", "target/test-classes/convertertest.yaml");

    assertEquals("Bingo", command.getConfigurationObject().foo);
    assertEquals(
        UUID.fromString("8ea44a78-c6af-11e2-9b88-00145eb45e9a"),
        command.getConfigurationObject().uuid);
    assertNotNull(command.getConfigurationObject().uri);
  }
}
