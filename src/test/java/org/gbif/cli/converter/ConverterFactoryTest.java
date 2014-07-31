package org.gbif.cli.converter;

import java.util.UUID;

import junit.framework.TestCase;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ConverterFactoryTest {

  @Test
  public void testUUID() {
    ConvertTestCommand command = new ConvertTestCommand();
    command.run("--conf", "target/test-classes/convertertest.yaml");

    assertThat(command.getConfigurationObject().foo).isEqualTo("Bingo");
    assertThat(command.getConfigurationObject().uuid).isEqualTo(UUID.fromString("8ea44a78-c6af-11e2-9b88-00145eb45e9a"));
    TestCase.assertNotNull(command.getConfigurationObject().uri);
  }
}