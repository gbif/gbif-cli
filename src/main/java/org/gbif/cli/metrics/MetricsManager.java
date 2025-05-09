package org.gbif.cli.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;

/**
 * Manages metrics collection and exposure for Prometheus.
 */
public class MetricsManager {
  private static final Logger LOG = LoggerFactory.getLogger(MetricsManager.class);
  
  public static final PrometheusMeterRegistry REGISTRY = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
  
  private static HttpServer server;
  private static int port = 9090; // Default port
  
  static {
    // JVM metrics
    new ClassLoaderMetrics().bindTo(REGISTRY);
    new JvmMemoryMetrics().bindTo(REGISTRY);
    new JvmGcMetrics().bindTo(REGISTRY);
    new JvmThreadMetrics().bindTo(REGISTRY);
    new ProcessorMetrics().bindTo(REGISTRY);
    new UptimeMetrics().bindTo(REGISTRY);
    
    // File descriptor metrics (Linux/Unix only)
    try {
      new FileDescriptorMetrics().bindTo(REGISTRY);
    } catch (Exception e) {
      LOG.warn("Unable to bind FileDescriptorMetrics", e);
    }
    
    // Register to global registry
    Metrics.addRegistry(REGISTRY);
  }
  
  /**
   * Start the metrics HTTP server to expose Prometheus metrics.
   * @param serverPort The port on which the HTTP server should listen
   */
  public static void startMetricsServer(int serverPort) {
    try {
      port = serverPort;
      server = HttpServer.create(new InetSocketAddress(port), 0);
      server.createContext("/metrics", httpExchange -> {
        String response = REGISTRY.scrape();
        httpExchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = httpExchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      });
      
      server.setExecutor(Executors.newFixedThreadPool(2));
      server.start();
      
    try {
      InetAddress addr = InetAddress.getLocalHost();
      String hostName = addr.getHostName();
      String hostAddress = addr.getHostAddress();
      LOG.info("Prometheus metrics exposed at:");
      LOG.info(" - Hostname: http://{}:{}/metrics", hostName, port);
      LOG.info(" - IP      : http://{}:{}/metrics", hostAddress, port);
      } catch (UnknownHostException e) {
        LOG.warn("Unable to resolve hostname. Defaulting to localhost");
        LOG.info("Prometheus metrics exposed at http://localhost:{}/metrics", port);
      }
    } catch (IOException e) {
      LOG.error("Failed to start metrics server", e);
    }
  }
  
  /**
   * Stop the metrics server.
   */
  public static void stopMetricsServer() {
    if (server != null) {
      server.stop(0);
      LOG.info("Metrics server stopped");
    }
  }
  
  /**
   * Create and register a counter.
   * @param name Name of the counter
   * @param description Description of what the counter measures
   * @return The created counter
   */
  public static Counter createCounter(String name, String description) {
    return Counter.builder(name)
        .description(description)
        .register(REGISTRY);
  }
} 