package org.gbif.servicecommand;

import org.gbif.basecommand.ComplexHelloWorldConfiguration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

public class HelloWorldServiceConfiguration {

  /* Using this annotation makes the ComplexHelloWorldConfiguration part of this configuration for the purpose of
     parsing the command line arguments. It is treated as if all fields annotated with @Parameter were included directly
     in this class. */
  @ParametersDelegate
  @Valid
  @NotNull
  public ComplexHelloWorldConfiguration helloworld = new ComplexHelloWorldConfiguration();

  @Parameter(
    names = {"-i", "--interval"},
    description = "In which interval should we print the message, in seconds")
  @Min(1)
  public int interval = 1;

}
