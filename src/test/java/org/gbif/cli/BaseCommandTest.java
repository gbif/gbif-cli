package org.gbif.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BaseCommandTest {

  @Test
  public void testHelp() {
    Command command = new TestBaseCommand();
    command.run("-h");
  }

  @Test
  public void testFailure() {
    Command command = new TestBaseCommand();
    assertThrows(CommandException.class, () -> command.run("foobar"));
  }

  @Test
  public void testYamlConfigFile() {
    TestBaseCommand command = new TestBaseCommand();
    command.run("--foo", "CLI", "--conf", "target/test-classes/configtest.yaml");
    assertEquals("YAML", command.getConfigurationObject().foo);
    assertEquals("CLI", command.getParameterObject().foo);
  }

  @Test
  public void testJsonConfigFile() {
    TestBaseCommand command = new TestBaseCommand();
    command.run("--foo", "CLI", "--conf", "target/test-classes/configtest.json");
    assertEquals("JSON", command.getConfigurationObject().foo);
    assertEquals("CLI", command.getParameterObject().foo);
  }

  @Test
  public void testTwoConfigFiles() {
    TestBaseCommand command = new TestBaseCommand();
    command.run("--foo", "CLI", "--conf", "target/test-classes/configtest.json", "target/test-classes/configtest.yaml");
    assertEquals("YAML", command.getConfigurationObject().foo);
    assertEquals("CLI", command.getParameterObject().foo);

    command.run("--foo", "CLI", "--conf", "target/test-classes/configtest.yaml", "target/test-classes/configtest.json");
    assertEquals("JSON", command.getConfigurationObject().foo);
    assertEquals("CLI", command.getParameterObject().foo);
  }

  @Test
  public void testIllegalFileName() {
    TestBaseCommand command = new TestBaseCommand();
    Throwable e = assertThrows(
        CommandException.class,
        () -> command.run("--foo", "CLI", "--conf", "doesnotexist"));
    assertTrue(e.getMessage().contains("exist"));
  }

  @Test
  public void testIllegalFile() {
    TestBaseCommand command = new TestBaseCommand();
    Throwable e = assertThrows(
        CommandException.class,
        () -> command.run("--foo", "CLI", "--conf", "target/test-classes/invalid.json"));
    assertTrue(e.getMessage().contains("reading"));
  }

  @Test
  public void testFailedValidation() {
    TestBaseCommand command = new TestBaseCommand();
    Throwable e = assertThrows(CommandException.class, command::run);
    assertTrue(e.getMessage().contains("validation"));
  }
}
