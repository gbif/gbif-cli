package org.gbif.cli;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class BaseCommandTest {

  @Test
  public void testHelp() {
    Command command = new TestBaseCommand();
    command.run("-h");
  }

  @Test(expected = CommandException.class)
  public void testFailure() {
    Command command = new TestBaseCommand();
    command.run("foobar");
  }

  @Test
  public void testYamlConfigFile() {
    TestBaseCommand command = new TestBaseCommand();
    command.run("--foo", "CLI", "--conf", "target/test-classes/configtest.yaml");
    assertThat(command.getConfigurationObject().foo).isEqualTo("YAML");
    assertThat(command.getParameterObject().foo).isEqualTo("CLI");
  }

  @Test
  public void testJsonConfigFile() {
    TestBaseCommand command = new TestBaseCommand();
    command.run("--foo", "CLI", "--conf", "target/test-classes/configtest.json");
    assertThat(command.getConfigurationObject().foo).isEqualTo("JSON");
    assertThat(command.getParameterObject().foo).isEqualTo("CLI");
  }

  @Test
  public void testTwoConfigFiles() {
    TestBaseCommand command = new TestBaseCommand();
    command.run("--foo", "CLI", "--conf", "target/test-classes/configtest.json", "target/test-classes/configtest.yaml");
    assertThat(command.getConfigurationObject().foo).isEqualTo("YAML");
    assertThat(command.getParameterObject().foo).isEqualTo("CLI");

    command.run("--foo", "CLI", "--conf", "target/test-classes/configtest.yaml", "target/test-classes/configtest.json");
    assertThat(command.getConfigurationObject().foo).isEqualTo("JSON");
    assertThat(command.getParameterObject().foo).isEqualTo("CLI");
  }

  @Test(expected = CommandException.class)
  public void testIllegalFileName() {
    TestBaseCommand command = new TestBaseCommand();
    try {
      command.run("--foo", "CLI", "--conf", "doesnotexist");
    } catch (CommandException e) {
      assertThat(e).hasMessageContaining("exist");
      throw e;
    }
  }

  @Test(expected = CommandException.class)
  public void testIllegalFile() {
    TestBaseCommand command = new TestBaseCommand();
    try {
      command.run("--foo", "CLI", "--conf", "target/test-classes/invalid.json");
    } catch (CommandException e) {
      assertThat(e).hasMessageContaining("reading");
      throw e;
    }
  }

  @Test(expected = CommandException.class)
  public void testFailedValidation() {
    TestBaseCommand command = new TestBaseCommand();
    try {
      command.run();
    } catch (CommandException e) {
      assertThat(e).hasMessageContaining("validation");
      throw e;
    }
  }


}
