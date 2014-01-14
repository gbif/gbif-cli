package org.gbif.cli.service;

import com.google.common.util.concurrent.ForwardingService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps another service (using Guava's {@link ForwardingService} to provide a way to {@link #join()} a
 * service and wait for its termination.
 */
class ForwardingJoinableService extends ForwardingService {

  private static final Logger LOG = LoggerFactory.getLogger(ForwardingJoinableService.class);

  private final Object joinLock = new Object();

  private final Service delegate;

  protected ForwardingJoinableService(Service delegate) {
    this.delegate = delegate;

    addListener(new Listener() {
      @Override
      public void starting() {
        LOG.debug("New state: [STARTING]");
      }

      @Override
      public void running() {
        LOG.debug("New state: [RUNNING]");
      }

      @Override
      public void stopping(State from) {
        LOG.debug("New state: [STOPPING] from [{}]", from);
      }

      @Override
      public void terminated(State from) {
        LOG.debug("New state: [TERMINATED] from [{}]", from);

        synchronized (joinLock) {
          joinLock.notifyAll();
        }
      }

      @Override
      public void failed(State from, Throwable failure) {
        LOG.debug("New state: [FAILED] from [{}]", from, failure);

        synchronized (joinLock) {
          joinLock.notifyAll();
        }
      }
    }, MoreExecutors.sameThreadExecutor());

  }

  @Override
  protected Service delegate() {
    return delegate;
  }

  /**
   * Waits for this service to exit RUNNING state.
   */
  public void join() throws InterruptedException {
    LOG.debug("Thread joined service");
    synchronized (joinLock) {
      while (isRunning()) {
        LOG.debug("Calling wait");
        joinLock.wait();
      }
    }
    LOG.debug("Thread returning from join");
  }

}
