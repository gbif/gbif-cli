package org.gbif.cli.service;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.AbstractIdleService;

/**
 * Service that can be scheduled to start at a certain time and then run on a fixed schedule.
 * See {@link ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long, TimeUnit)}
 */
public abstract class ScheduledService extends AbstractIdleService {

  private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  /**
   * Method called from @{link #getTimeStart}
   */
  protected abstract void scheduledRun();

  /**
   * At what time the scheduling should start.
   * Calling this method multiple time may result in different values if the timeStart is LocalTime.now().
   *
   * @return timeStart at the moment of the call
   */
  protected abstract LocalTime getTimeStart();

  /**
   * Interval in minutes this service should run after {@link #getTimeStart()}
   * @return
   */
  protected abstract int getIntervalInMinutes();

  @Override
  final protected void startUp() throws Exception {
    LocalTime timeStart =  getTimeStart();
    long initialDelay = timeStart == null ? 0 : LocalTime.now().until(timeStart, ChronoUnit.MINUTES);

    //if the delay is passed
    if (initialDelay < 0) {
      initialDelay = initialDelay + ChronoUnit.DAYS.getDuration().toMinutes();
    }
    scheduler.scheduleAtFixedRate(this::scheduledRun, initialDelay, getIntervalInMinutes(), TimeUnit.MINUTES);
  }

  @Override
  final protected void shutDown() throws Exception {
    scheduler.shutdown();
  }
}
