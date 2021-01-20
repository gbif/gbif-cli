package org.gbif.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class ApplicationTest {

  @Test
  public void testAddedCommand() {
    Application app = new Application();
    Command command = new TestCommand("test");
    app.addCommand(command);

    int returnCode = app.run();
    assertTrue(returnCode > 0);

    returnCode = app.run("foo");
    assertTrue(returnCode > 0);
  }

  @Test
  public void testNoExplicitCommands() {
    Application app = new Application();

    int returnCode = app.run();
    assertTrue(returnCode > 0);

    returnCode = app.run("foo");
    assertTrue(returnCode > 0);
  }

  @Test
  public void testCommandExecution() {
    Application app = new Application();

    Command command = spy(new TestCommand("mock"));
    app.addCommand(command);
    int returnCode = app.run("mock", "foo", "bar");
    verify(command).run("foo", "bar");
    assertEquals(0, returnCode);
  }

  @Test
  public void testCommandException() {
    Application app = new Application();

    Command command = spy(new TestCommand("mock"));
    app.addCommand(command);
    doThrow(new RuntimeException("foo")).when(command).run("foo", "bar");
    int returnCode = app.run("mock", "foo", "bar");
    verify(command).run("foo", "bar");
    assertTrue(returnCode > 0);
  }
}
