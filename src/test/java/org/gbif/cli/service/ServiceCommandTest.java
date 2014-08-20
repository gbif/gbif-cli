package org.gbif.cli.service;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.Service;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ServiceCommandTest {

  public static class TestService extends ServiceCommand {
    private static final Logger LOG = LoggerFactory.getLogger(TestService.class);
    private boolean running;

    private final Service service;
    protected TestService() {
      super("test");
      service = new AbstractIdleService() {
        @Override
        protected void startUp() throws Exception {
          LOG.debug("Start test service ...");
          Thread.sleep(1000);
          running = true;
          LOG.debug("test service started");
        }

        @Override
        protected void shutDown() throws Exception {
          LOG.debug("shutdown test service ...");
          Thread.sleep(1000);
          running = false;
          LOG.debug("test service stopped");
        }
      };
    }

    @Override
    protected Service getService() {
      return service;
    }

    public void throwError() {
      throw new IllegalStateException("I was told to throw");
    }

    @Override
    protected Object getConfigurationObject() {
      return new Object();
    }

    public boolean isRunning() {
      return running;
    }
  }

  @Test
  public void testRun() throws Exception {
    TestService command = new TestService();
    assertFalse(command.isRunning());

    command.doRun();
    assertTrue(command.isRunning());

    Thread.sleep(1000);
    assertTrue(command.isRunning());

    Thread.sleep(1000);
    assertTrue(command.isRunning());
  }

  @Test
  public void testRunException() throws Exception {
    TestService command = new TestService();
    assertFalse(command.isRunning());

    command.doRun();
    assertTrue(command.isRunning());

    try {
      command.throwError();
    } catch (IllegalStateException e) {
    }
    Thread.sleep(1000);
    // service still running
    assertTrue(command.isRunning());
  }

}