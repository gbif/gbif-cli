package org.gbif.cli.service;

import org.gbif.cli.BaseCommand;

import com.google.common.util.concurrent.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This provides a convenient way to manage long-running services from a CLI. It is based on the {@link BaseCommand}
 * and thus inherits all its possibilities to configure a command.
 * <p/>
 * You mostly need to implement the {@link #getService()} method to return a Guava {@link Service} class. This will
 * then be started and stopped on JVM shutdown.
 */
public abstract class ServiceCommand extends BaseCommand {

  private static final Logger LOG = LoggerFactory.getLogger(ServiceCommand.class);

  protected ServiceCommand(String name) {
    super(name);
  }

  /**
   * This method will be called after the command line arguments and the configuration file have been processed.
   * <p/>
   * The service will be started and on shutdown of the JVM it will also try to execute an orderly shutdown of the
   * service (using {@link Service#startAndWait()} and {@link Service#stopAndWait()} respectively).
   */
  protected abstract Service getService();

  @Override
  protected final void doRun() {
    final Service service = getService();

    Runtime.getRuntime().addShutdownHook(new ShutdownThread(service));

    LOG.info("Service starting ...");
    try {
      service.startAndWait();
      // guava 15+ only
//      service.startAsync();
//      service.awaitRunning();
      LOG.info("Service started");
    } catch (IllegalStateException e) {
      LOG.warn("Service failed to start", e);
    } catch (RuntimeException e) {
      LOG.warn("Service failed to start", e);
    }

    // keep it running
    synchronized(this) {
      while (true) {
        LOG.debug("Main command waiting for shutdown ...");
        try {
          this.wait();
        } catch (InterruptedException e) {
          LOG.debug("We were interrupted waiting for the service to stop, wait again", e);
        }
      }
    }
  }

  /**
   * This thread is intended to be passed to {@link Runtime#addShutdownHook(Thread)} and it tries to stop the service
   * that it's being passed.
   */
  @SuppressWarnings("ClassExplicitlyExtendsThread")
  private class ShutdownThread extends Thread {

    private final Service service;

    private ShutdownThread(Service service) {
      this.service = service;
    }

    @Override
    public void run() {
      LOG.debug("Shutdown Hook called");
      try {
        if (service.isRunning()) {
          LOG.info("Service stopping ...");
          service.stopAndWait();
          // guava 15+ only
//          service.stopAsync();
//          // TODO: Maybe wait with a timeout here after which we force close?
//          service.awaitTerminated();
          LOG.info("Service stopped");
        }
      } catch (Exception e) {
        LOG.debug("Caught exception in shutdown hook", e);
      }
    }
  }
}
