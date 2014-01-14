package org.gbif.basecommand;

import javax.validation.constraints.NotNull;

import com.beust.jcommander.Parameter;

public class ComplexHelloWorldConfiguration {

  @Parameter(
    names = {"-g", "--greeting"},
    description = "The greeting to use for the world")
  @NotNull
  public String greeting = "Hello";

}
