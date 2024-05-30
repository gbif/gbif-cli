package org.gbif.cli.converter;

import org.gbif.cli.BaseCommand;

import java.net.URI;
import java.util.UUID;
import javax.validation.constraints.NotNull;

import com.beust.jcommander.Parameter;

public class ConvertTestCommand extends BaseCommand {

  public enum Sex {MALE, FEMALE};

  public static class TestConfig {

    @Parameter(names = "--foo")
    @NotNull
    public String foo;

    @Parameter(names = "--uuid")
    @NotNull
    public UUID uuid;

    @Parameter(names = "--sex")
    @NotNull
    public Sex sex;

    @Parameter(names = "--uri")
    @NotNull
    public URI uri;
  }

  public TestConfig config = new TestConfig();

  public ConvertTestCommand() {
    super("convertertest");
  }

  @Override
  protected TestConfig getConfigurationObject() {
    return config;
  }

  @Override
  protected void doRun() {

  }
}
