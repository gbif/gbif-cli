package org.gbif.cli.service;

import org.gbif.cli.BaseCommand;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.UncheckedExecutionException;
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

  private final Thread mainThread = Thread.currentThread();

  private volatile boolean running = true;

  protected ServiceCommand(String name) {
    super(name);
  }

  /**
   * This method will be called after the command line arguments and the configuration file have been processed.
   * <p/>
   * The service will be started and on shutdown of the JVM it will also try to execute an orderly shutdown of the
   * service (using {@link Service#start()} and {@link Service#stop()} respectively).
   */
  protected abstract Service getService();

  @Override
  protected final void doRun() {
    final ForwardingJoinableService service = new ForwardingJoinableService(getService());

    Runtime.getRuntime().addShutdownHook(new ShutdownThread(service));

    LOG.info("Starting service...");
    try {
      service.startAndWait();
      LOG.info("Service started, now in state [{}]", service.state());
    } catch (UncheckedExecutionException e) {
      LOG.warn("Service failed to start", e);
    }

    try {
      while (service.state() == Service.State.RUNNING) {
        service.join();
        LOG.debug("Got notified by service, service terminated");
      }
    } catch (InterruptedException e) {
      LOG.debug("We were interrupted waiting for the service to stop, will rejoin", e);
    }

    synchronized (mainThread) {
      running = false;
      mainThread.notifyAll();
    }

    LOG.debug("Application shutting down");
  }

  /**
   * This thread is intended to be passed to {@link Runtime#addShutdownHook(Thread)} and it tries to stop the service
   * that it's being passed.
   */
  @SuppressWarnings("ClassExplicitlyExtendsThread")
  private class ShutdownThread extends Thread {

    private final ForwardingJoinableService service;

    private ShutdownThread(ForwardingJoinableService service) {
      this.service = service;
    }

    @Override
    public void run() {
      LOG.debug("Shutdown Hook called");
      try {
        if (service.isRunning()) {
          LOG.info("Service stopping...");
          ListenableFuture<Service.State> stopFuture = service.stop();
          // TODO: Maybe wait with a timeout here after which we force close?
          stopFuture.get();
          LOG.info("Service stopped");
        }
        // TODO: Maybe wait with a timeout here after which we force close?
        LOG.debug("Waiting for main Thread to exit");
        synchronized (mainThread) {
          while (running) {
            mainThread.wait();
          }
        }
        LOG.debug("Main thread exited");
      } catch (Exception e) {
        LOG.debug("Caught exception in shutdown hook", e);
      }
    }
  }
}
