package org.gbif.cli;

import com.google.common.base.Optional;

public class MetaInfCommand extends Command {

  public MetaInfCommand() {
    super("metainfcommand");
  }

  @Override
  public Optional<String> getUsage() {
    return Optional.absent();
  }

  @Override
  public void run(String... arguments) {
  }

}
