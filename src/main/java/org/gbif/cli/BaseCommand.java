package org.gbif.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.StatusPrinter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Optional;
import org.gbif.cli.converter.ConverterFactory;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.yaml.snakeyaml.parser.ParserException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * This base command can be used for all Commands that want to have their parameters validated and who want to allow
 * specifying configuration data in a JSON or YAML file.
 * <p/>
 * JSR 303 annotations can be used for validation as well as the built-in methods from JCommander.
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public abstract class BaseCommand extends Command {

  private static final Logger LOG = LoggerFactory.getLogger(BaseCommand.class);

  private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

  private static final Validator VALIDATOR =
      Validation.byDefaultProvider()
          .configure()
          .messageInterpolator(new ParameterMessageInterpolator())
          .buildValidatorFactory()
          .getValidator();

  static {
    MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  private final String name;

  protected BaseCommand(String name) {
    super(name);
    this.name = name;
  }

  /**
   * The object returned by this method will be populated from the JSON or YAML files specified using the {@code --conf}
   * command line parameter. The files given in this way will be applied in the order they appeared on the command line
   * so later files might overwrite earlier ones.
   * <p/>
   * This can be the same object as the one returned from {@link #getParameterObject()} in which case command line
   * parameters override options from the files.
   */
  protected abstract Object getConfigurationObject();

  /**
   * This is the object which has JCommander annotations and will be populated from the command line arguments.
   * <p/>
   * This can be the same object as the one returned from {@link #getConfigurationObject()}} in which case command line
   * parameters override options from the files. The default implementation defers to {@link #getConfigurationObject()}.
   */
  protected Object getParameterObject() {
    return getConfigurationObject();
  }

  /**
   * This method will be called when the generic parameters have been handled and configuration and parameter objects
   * have been populated.
   */
  protected abstract void doRun();

  @Override
  public Optional<String> getUsage() {
    JCommander jCommander = new JCommander();
    jCommander.addConverterFactory(new ConverterFactory());
    jCommander.setProgramName(name);
    jCommander.addObject(new GenericParameters());
    jCommander.addObject(getParameterObject());

    StringBuilder sb = new StringBuilder();
    jCommander.usage(sb);

    String sep = System.getProperty("line.separator");
    try {
      JsonSchema jsonSchema = MAPPER.generateJsonSchema(getConfigurationObject().getClass());
      sb.append(sep);
      sb.append("The configuration file has the following JSON schema but accepts either YAML or JSON:");
      sb.append(sep);
      sb.append(jsonSchema);
    } catch (Exception e) {
      LOG.debug("Error generating JSON schema, this is most likely a programming error", e);
    }

    return Optional.of(sb.toString());
  }

  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  @Override
  public final void run(String... arguments) {
    // First we process the generic parameters which might cause us to bail out early if there is an error or if the
    // user requested us to print usage instructions
    GenericParameters genericParameters = new GenericParameters();
    JCommander jCommander = new JCommander(genericParameters);
    jCommander.addConverterFactory(new ConverterFactory());
    jCommander.setAcceptUnknownOptions(true);
    processCommandLineParameters(jCommander, arguments);
    if (genericParameters.help) {
      System.out.println(getUsage().get());
      return;
    }

    processConfigurationFiles(genericParameters.configurationFiles);

    List<String> remainingArgs = jCommander.getUnknownOptions();
    jCommander = new JCommander(getParameterObject());
    jCommander.addConverterFactory(new ConverterFactory());
    processCommandLineParameters(jCommander, remainingArgs.toArray(new String[remainingArgs.size()]));

    configureLogging(genericParameters);
    validateObjects();

    doRun();
  }

  private void processCommandLineParameters(JCommander jCommander, String... arguments) {
    try {
      jCommander.parse(arguments);
    } catch (ParameterException e) {
      System.err.println("Error parsing command line options:");
      System.err.println(e.getMessage());
      throw new CommandException("Error during command line parsing", e);
    }
  }

  /**
   * We take every passed in configuration file, test if it exists and try to write it to the Object provided by {@link
   * #getConfigurationObject()}. This method prints errors it encounters.
   *
   * @param configurationFiles to process
   */
  private void processConfigurationFiles(Iterable<String> configurationFiles) {
    for (String fileName : configurationFiles) {
      File file = new File(fileName);
      if (!file.exists()) {
        System.err.println("Error reading configuration file [" + fileName + "] because it does not exist");
        throw new CommandException("Error reading configuration file [" + fileName + "] because it does not exist");
      }
      ObjectReader reader = MAPPER.readerForUpdating(getConfigurationObject());
      try {
        reader.readValue(file);
      } catch (IOException e) {
        System.err.println("Error reading configuration file [" + fileName + "]");
        throw new CommandException("Error reading configuration file [" + fileName + "]", e);
      } catch (ParserException e) {
        System.err.println("Error reading configuration file [" + fileName + "]");
        throw new CommandException("Error reading configuration file [" + fileName + "]", e);
      }
    }
  }

  /**
   * Reconfigures Logback.
   * <p/>
   * Usually logback loads a <em>logback.xml</em> file but we change that. We allow a command line parameter to
   * override the config file to use. If that isn't specified or doesn't exist we try to load a file called
   * <em>logback-$commandName.xml</em> and if that too doesn't exist we are using the default.
   * <p/>
   * Additionally we set the ROOT log level to the one that was provided, if any, or to DEBUG if requested otherwise we
   * leave it at the default.
   */
  private void configureLogging(GenericParameters parameters) {
    // Set up JUL logging to go through SLF4J
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();

    URL loggingConfigUrl = null;

    // Try to load a logback configuration file from the file system if given on the command line
    if (parameters.logbackConfig != null && !parameters.logbackConfig.isEmpty()) {
      File configFile = new File(parameters.logbackConfig);
      if (!configFile.exists() || !configFile.isFile()) {
        throw new CommandException("Logback configuration file does not exist");
      }
      try {
        loggingConfigUrl = configFile.toURI().toURL();
      } catch (MalformedURLException e) {
        // This should not happen because we already checked for existence
        LOG.debug("Failed to create URL for logback configuration file");
        throw new CommandException(e);
      }
    }

    // Try to automatically load a logback configuration file named after the command
    if (loggingConfigUrl == null) {
      loggingConfigUrl = Loader.getResource("logback-" + getName() + ".xml", Loader.getClassLoaderOfObject(this));
    }

    // If we got a configuration file reconfigure logback now
    if (loggingConfigUrl != null) {
      LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
      context.reset();
      JoranConfigurator joran = new JoranConfigurator();
      joran.setContext(context);
      try {
        joran.doConfigure(loggingConfigUrl);
      } catch (JoranException e) {
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
      }
    }

    // Change log levels if needed
    if (parameters.verbose || (parameters.logLevel != null && !parameters.logLevel.isEmpty())) {
      ch.qos.logback.classic.Logger root =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
      root.setLevel(Level.toLevel(parameters.logLevel));

      if (parameters.verbose) {
        root.setLevel(Level.DEBUG);
      }
    }
  }

  /**
   * This validates the parameter and configuration object and prints violations if there are some. If the objects are
   * exactly the same it will only be validated once.
   */
  @SuppressWarnings("ObjectEquality")
  private void validateObjects() {
    validateObject(VALIDATOR, getConfigurationObject());
    if (getParameterObject() != getConfigurationObject()) {
      validateObject(VALIDATOR, getParameterObject());
    }
  }

  /**
   * Validates a single object, printing any violations it encounters.
   *
   * @param validator to use
   * @param object    to validate
   */
  private void validateObject(Validator validator, Object object) {
    Set<ConstraintViolation<Object>> violations = validator.validate(object);
    if (!violations.isEmpty()) {
      System.err.println("Failed validation of command line parameters:");
      for (ConstraintViolation<Object> violation : violations) {
        System.err.println("  " + violation.getPropertyPath() + ": " + violation.getMessage());
      }
      throw new CommandException("Failed validation");
    }
  }

}
