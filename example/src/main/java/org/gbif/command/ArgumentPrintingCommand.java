package org.gbif.command;

import org.gbif.cli.Command;

import com.google.common.base.Optional;
import org.kohsuke.MetaInfServices;

@MetaInfServices(Command.class)
public class ArgumentPrintingCommand extends Command {

  public ArgumentPrintingCommand() {
    super("argument-printer");
  }

  @Override
  public Optional<String> getUsage() {
    return Optional.of("This command simply prints all the arguments you provide, one per line");
  }

  @Override
  public void run(String... arguments) {
    for (String argument : arguments) {
      System.out.println(argument);
    }
  }

}
