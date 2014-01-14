package org.gbif.cli;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class CommandTest {

  @Test
  public void testCommand() {
    Command command = new TestCommand("test");
    assertThat(command.getName()).isEqualTo("test");
    assertThat(command.getUsage().isPresent()).isFalse();
  }

  @Test(expected = NullPointerException.class)
  public void testNullConstructor() {
    new TestCommand(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyConstructor() {
    new TestCommand("");
  }
}
