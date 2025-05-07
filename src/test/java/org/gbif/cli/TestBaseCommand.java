package org.gbif.cli;

import jakarta.validation.constraints.NotNull;

import com.beust.jcommander.Parameter;

public class TestBaseCommand extends BaseCommand {

  public static class ParameterObject {

    @Parameter(names = "--foo")
    @NotNull
    public String foo;

  }

  public ParameterObject parameter = new ParameterObject();

  public ParameterObject config = new ParameterObject();

  public TestBaseCommand() {
    super("basecommand");
  }

  @Override
  protected ParameterObject getConfigurationObject() {
    return config;
  }

  @Override
  protected ParameterObject getParameterObject() {
    return parameter;
  }

  @Override
  protected void doRun() {

  }
}
