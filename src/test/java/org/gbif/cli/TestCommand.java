package org.gbif.cli;

import com.google.common.base.Optional;

public class TestCommand extends Command {

  public TestCommand(String name) {
    super(name);
  }

  @Override
  public Optional<String> getUsage() {
    return Optional.absent();
  }

  @Override
  public void run(String... arguments) {
  }

}
