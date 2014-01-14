package org.gbif.basecommand;

import org.gbif.cli.BaseCommand;
import org.gbif.cli.Command;

import org.kohsuke.MetaInfServices;

/**
 * This command prints a configurable "Hello world" statement by taking an optional greeting from the command line.
 * <p/>
 * <pre><code>
 *   java -jar example.jar hello-world-2 --greeting Hi
 *   java -jar example.jar hello-world-2 --g Hi
 *   java -jar example.jar hello-world-2 --conf complex.yaml
 * </code></pre>
 * <p/>
 * This command is using the {@link MetaInfServices} annotation with {@link Command} as an argument. By using this
 * annotation this command will be found automatically by the application and no further work has to be done to
 * register this command. Just executing the main method in the {@link org.gbif.cli.Application} class is now enough to
 * be able to execute this command.
 */
@MetaInfServices(Command.class)
public class ComplexHelloWorldCommand extends BaseCommand {

  private final ComplexHelloWorldConfiguration configuration = new ComplexHelloWorldConfiguration();

  public ComplexHelloWorldCommand() {
    super("hello-world-2");
  }

  @Override
  protected void doRun() {
    System.out.println(configuration.greeting + " world!");
  }

  @Override
  protected Object getConfigurationObject() {
    return configuration;
  }

}
