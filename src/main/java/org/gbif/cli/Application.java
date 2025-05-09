package org.gbif.cli;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import com.google.common.base.Optional;
import java.util.ServiceLoader;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import com.google.common.collect.Maps;
import org.gbif.cli.metrics.CommandMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is the main entry point into any CLI application.
 */
public class Application {

  private static final Logger LOG = LoggerFactory.getLogger(Application.class);

  // The supported commands in the order they were added
  private final Map<String, Command> commands = Maps.newTreeMap();

  protected Application() {
  }

  /**
   * Adds a command to this Application. If another command with the same name was already registered it will be
   * replaced by this new one.
   *
   * @param command to add or replace
   */
  protected final void addCommand(Command command) {
    checkNotNull(command);
    commands.put(command.getName(), command);
    LOG.debug("Added Command [{}]", command.getName());
  }

  /**
   * Can be overridden by subclasses to initialize themselves for example by adding commands.
   */
  public void initialize() {}

  /**
   * Main entry point. This method searches for other commands on the classpath using the {@link ServiceLoader} and
   * parses the command line to see which command to run.
   *
   * @param arguments command line arguments
   *
   * @return exit code
   */
  public final int run(String... arguments) {
    configureLogback();

    // Try to load additional commands
    ServiceLoader<Command> loader = ServiceLoader.load(Command.class);
    for (Command command : loader) {
      addCommand(command);
    }

    initialize();

    if (arguments.length == 0) {
      printUsage();
      return 1;
    }

    // Command has to be the very first argument
    String commandName = arguments[0];
    if (!commands.containsKey(commandName)) {
      LOG.error("The command name you supplied is not a valid command.");
      LOG.error("You supplied: [{}]", commandName);
      if (commands.isEmpty()) {
        LOG.error("There are no valid commands. This is most likely a programming error.");
        LOG.error("Hint: Maybe you forgot to annotate your commands with '@MetaInfServices(Command.class)'?");
      } else {
        LOG.error("The valid commands are:");
        for (String availableCommand : commands.keySet()) {
          LOG.error(" - {}", availableCommand);
        }
      }
      return 1;
    }

    // Copy the remaining arguments (minus command name)
    String[] commandArguments = Arrays.copyOfRange(arguments, 1, arguments.length);

    // Parse the generic parameters to check for metrics configuration
    GenericParameters genericParameters = new GenericParameters();
    try {
      new com.beust.jcommander.JCommander(genericParameters).parse(commandArguments);
      
      // Initialize metrics if enabled
      if (genericParameters.metricsEnabled) {
        LOG.info("Initializing metrics on port {}", genericParameters.metricsPort);
        CommandMetrics.initializeMetrics(genericParameters.metricsPort);
      }
    } catch (Exception e) {
      // Ignore parsing errors, they will be handled properly later
      LOG.debug("Error parsing metrics parameters", e);
    }

    Command command = commands.get(commandName);
    try {
      if (genericParameters.metricsEnabled) {
        // Record command execution metrics
        CommandMetrics.registerCommandExecution(commandName);
        CommandMetrics.timeCommand(commandName, () -> command.run(commandArguments));
      } else {
        command.run(commandArguments);
      }
    } catch (Exception t) {
      if (genericParameters.metricsEnabled) {
        CommandMetrics.registerCommandError(commandName);
      }
      LOG.error("Command threw exception", t);
      return 1;
    }
    return 0;
  }

  /**
   * This checks if the user has provided a logback configuration file using the usual logback loading mechanism. If he
   * does not logback will already have run {@link ch.qos.logback.classic.BasicConfigurator#configureDefaultContext();}
   * which means we're logging to the console at DEBUG level. To prevent that we try the same mechanism that logback
   * uses to find a configuration file. If it can't find this file then it resets logback so that it doesn't print
   * anything.
   * <p/>
   * The drawback here is that it doesn't take into account the configurability of logback files using the BaseCommand.
   */
  private void configureLogback() {
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    URL loggingConfigUrl = ConfigurationWatchListUtil.getMainWatchURL(context);
    if (loggingConfigUrl == null) {
      context.reset();
    }
  }

  private void printUsage() {
    LOG.info("Usage");
    for (Map.Entry<String, Command> commandEntry : commands.entrySet()) {
      LOG.info("");
      LOG.info("Command: {}", commandEntry.getKey());
      LOG.info("---------------------------------------");

      Optional<String> usage = commandEntry.getValue().getUsage();
      LOG.info(usage.or("This command does not have any options"));
    }
  }

  public static void main(String[] args) {
    System.exit(new Application().run(args));
  }
}
