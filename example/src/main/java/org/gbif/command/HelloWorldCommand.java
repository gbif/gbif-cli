package org.gbif.command;

import org.gbif.cli.Command;

import com.google.common.base.Optional;
import org.kohsuke.MetaInfServices;

@MetaInfServices(Command.class)
public class HelloWorldCommand extends Command {

  public HelloWorldCommand() {
    super("hello-world-1");
  }

  @Override
  public Optional<String> getUsage() {
    return Optional.absent();
  }

  @Override
  public void run(String... arguments) {
    System.out.println("Hello World!");
  }

}
