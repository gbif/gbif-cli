package org.gbif.servicecommand;

import org.gbif.cli.Command;
import org.gbif.cli.service.ServiceCommand;

import com.google.common.util.concurrent.Service;
import org.kohsuke.MetaInfServices;

@MetaInfServices(Command.class)
public class HelloWorldServiceCommand extends ServiceCommand {

  private final HelloWorldServiceConfiguration configuration = new HelloWorldServiceConfiguration();

  public HelloWorldServiceCommand() {
    super("hello-world-service");
  }

  @Override
  protected Object getConfigurationObject() {
    return configuration;
  }

  @Override
  protected Service getService() {
    return new HelloWorldService(configuration);
  }

}
