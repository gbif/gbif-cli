package org.gbif.cli;

import com.beust.jcommander.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class GenericParameters {

  @Parameter(
    names = {"-h", "--help"},
    description = "Prints help about this command",
    help = true)
  public boolean help;

  @Parameter(
    names = {"-c", "--conf"},
    description = "Configuration files to use. Later ones override earlier ones",
    variableArity = true)
  public List<String> configurationFiles = new ArrayList<String>();

  @Parameter(
    names = "--log-config",
    description = "Name of a logback XML configuration file to use")
  public String logbackConfig;

  @Parameter(
    names = "--log-level",
    description = "Name of the log level to use for the root logger, if an invalid name is given DEBUG will be used")
  public String logLevel;

  @Parameter(
    names = {"-v", "--verbose"},
    description = "Enables DEBUG logging. This overrides any --log-level setting")
  public boolean verbose;

  @Override
  public String toString() {
    return new StringJoiner(", ", GenericParameters.class.getSimpleName() + "[", "]")
        .add("help=" + help)
        .add("configurationFiles=" + configurationFiles)
        .add("logbackConfig='" + logbackConfig + "'")
        .add("logLevel='" + logLevel + "'")
        .add("verbose=" + verbose)
        .toString();
  }
}
