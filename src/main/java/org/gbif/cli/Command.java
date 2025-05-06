package org.gbif.cli;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;

/**
 * Base class for commands which needs to be subclassed.
 */
public abstract class Command {

  private final String name;

  protected Command(String name) {
    this.name = checkNotNull(name, "name can't be null");
    checkArgument(!name.isEmpty(), "name can't be empty");
  }

  public final String getName() {
    return name;
  }

  /**
   * Optionally returns a string describing the usage of this command.
   *
   * @return {@link Optional#absent()} ()} if this command doesn't take any further parameters otherwise a string
   *         describing
   *         the parameters
   */
  public abstract Optional<String> getUsage();

  /**
   * This is the main entry point into this command.
   *
   * @param arguments all arguments that are relevant for this command. This does not include the command name or any
   *                  other arguments that the Application itself might have parsed
   */
  public abstract void run(String... arguments);

}
