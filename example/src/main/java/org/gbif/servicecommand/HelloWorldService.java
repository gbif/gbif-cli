package org.gbif.servicecommand;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

public class HelloWorldService extends AbstractExecutionThreadService {

  private final HelloWorldServiceConfiguration configuration;

  public HelloWorldService(HelloWorldServiceConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void run() throws Exception {
    while (isRunning()) {
      System.out.println(configuration.helloworld.greeting + " world!");
      Thread.sleep(1000 * configuration.interval);
    }
  }

}
