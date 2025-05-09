package org.gbif.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
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

  @Test
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  public void testMetricsEnabled() throws InterruptedException {
    Application app = new Application();
    Command command = new TestCommand("test");
    app.addCommand(command);

    // Run with metrics enabled
    int returnCode = app.run("test", "--metrics-enabled");
    
    // Expect success code (0)
    assertEquals(0, returnCode, "Command should execute successfully");
    
    // Give the server time to start
    Thread.sleep(1000);
    
    // Verify that metrics endpoint is accessible
    try {
      URL url = new URL("http://localhost:9090/metrics");
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      int responseCode = connection.getResponseCode();
      
      assertEquals(200, responseCode, "Metrics endpoint should return HTTP 200");
      
      // Verify content
      assertTrue(connection.getContentType().contains("text/plain"), 
          "Content type should be text/plain");
      
    } catch (IOException e) {
      fail("Failed to connect to metrics endpoint: " + e.getMessage());
    }
    
    // Test with non-existent command should fail
    returnCode = app.run("foo", "--metrics-enabled");
    assertTrue(returnCode > 0, "Non-existent command should fail");
  }
}
