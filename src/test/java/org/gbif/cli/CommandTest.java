package org.gbif.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommandTest {

  @Test
  public void testCommand() {
    Command command = new TestCommand("test");
    assertEquals("test", command.getName());
    assertFalse(command.getUsage().isPresent());
  }

  @Test
  public void testNullConstructor() {
    assertThrows(NullPointerException.class, () -> new TestCommand(null));
  }

  @Test
  public void testEmptyConstructor() {
    assertThrows(IllegalArgumentException.class, () -> new TestCommand(""));
  }
}
