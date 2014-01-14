package org.gbif.cli;

public class CommandException extends RuntimeException {

  private static final long serialVersionUID = -2445063163456910880L;

  public CommandException() {
  }

  public CommandException(String s) {
    super(s);
  }

  public CommandException(Throwable throwable) {
    super(throwable);
  }

  public CommandException(String s, Throwable throwable) {
    super(s, throwable);
  }

}
