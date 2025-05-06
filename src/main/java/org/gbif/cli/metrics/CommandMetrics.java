package org.gbif.cli.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Provides utility methods for collecting metrics related to CLI commands.
 */
public class CommandMetrics {
  private static final Logger LOG = LoggerFactory.getLogger(CommandMetrics.class);
  
  private static final ConcurrentHashMap<String, Counter> COMMAND_COUNTERS = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Counter> COMMAND_ERROR_COUNTERS = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Timer> COMMAND_TIMERS = new ConcurrentHashMap<>();
  
  // Prevent instantiation
  private CommandMetrics() {}
  
  /**
   * Start the metrics server.
   * 
   * @param metricsPort Port to expose metrics on
   */
  public static void initializeMetrics(int metricsPort) {
    MetricsManager.startMetricsServer(metricsPort);
    LOG.info("Metrics server started on port {}", metricsPort);
  }
  
  /**
   * Registers a command execution and increments its counter.
   * 
   * @param commandName Name of the command
   * @return The counter for the command
   */
  public static Counter registerCommandExecution(String commandName) {
    String metricName = sanitizeMetricName(commandName) + "_executions_total";
    Counter counter = COMMAND_COUNTERS.computeIfAbsent(metricName, 
        k -> MetricsManager.createCounter(k, "Total number of executions for command " + commandName));
    counter.increment();
    return counter;
  }
  
  /**
   * Registers a command error and increments its counter.
   * 
   * @param commandName Name of the command
   * @return The error counter for the command
   */
  public static Counter registerCommandError(String commandName) {
    String metricName = sanitizeMetricName(commandName) + "_errors_total";
    Counter counter = COMMAND_ERROR_COUNTERS.computeIfAbsent(metricName,
        k -> MetricsManager.createCounter(k, "Total number of errors for command " + commandName));
    counter.increment();
    return counter;
  }
  
  /**
   * Records the execution time of a command.
   * 
   * @param commandName Name of the command
   * @param code Code to execute and time
   * @param <T> Return type of the code block
   * @return The result of the code block
   */
  public static <T> T timeCommand(String commandName, Supplier<T> code) {
    String metricName = sanitizeMetricName(commandName) + "_duration_seconds";
    Timer timer = COMMAND_TIMERS.computeIfAbsent(metricName,
        k -> Timer.builder(k)
            .description("Execution time for command " + commandName)
            .register(MetricsManager.REGISTRY));
    
    return timer.record(code);
  }
  
  /**
   * Records the execution time of a command with no return value.
   * 
   * @param commandName Name of the command
   * @param runnable Code to execute and time
   */
  public static void timeCommand(String commandName, Runnable runnable) {
    String metricName = sanitizeMetricName(commandName) + "_duration_seconds";
    Timer timer = COMMAND_TIMERS.computeIfAbsent(metricName,
        k -> Timer.builder(k)
            .description("Execution time for command " + commandName)
            .register(MetricsManager.REGISTRY));
    
    timer.record(runnable);
  }
  
  /**
   * Sanitizes a command name to be used as a metric name.
   * Prometheus metric names must match the regex [a-zA-Z_:][a-zA-Z0-9_:]*
   * 
   * @param commandName Command name to sanitize
   * @return Sanitized metric name
   */
  private static String sanitizeMetricName(String commandName) {
    return commandName.replaceAll("[^a-zA-Z0-9_:]", "_").toLowerCase();
  }
} 