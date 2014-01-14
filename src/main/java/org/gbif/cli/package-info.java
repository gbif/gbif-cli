/**
 * Provides an easy way to create CLI (command line interface) applications.
 * <p/>
 * This project has two main abstractions: an application and a command.
 * <p/>
 * An <em>application</em> is a <em>collection</em> of <em>commands</em>.
 * <p/>
 * <p/>
 * <h2>Applications</h2>
 * <p/>
 * An application is the top-level entry point into our CLI. It consists of multiple commands. Each command has a name
 * and may optionally take different parameters. The command name should be the first argument to the program.
 * <pre><code>
 *   java -jar cli.jar commandName [...]
 * </code></pre>
 * <p/>
 * <p/>
 * {@link Application} is a concrete class so it can be used immediately and it looks for {@link Command}s
 * automatically using the {@link java.util.ServiceLoader} mechanism. If that can't be used the best way is to subclass
 * {@code Application} and override the {@link Application#initialize()} method to register {@code Commands} using
 * {@link Application#addCommand(Command)}.
 * <p/>
 * <pre><code>
 * public class TestApplication extends Application {
 *
 *   {@literal @Override}
 *   public void initialize() {
 *     addCommand(new TestCommand());
 *   }
 *
 *   public static void main(String[] args) {
 *     System.exit(new TestApplication().run(args));
 *   }
 *
 * }
 * </code></pre>
 * <p/>
 * The application can now be called and it will provide usage instructions for all registered commands.
 * <p/>
 * <h2>Commands</h2>
 * <p/>
 * A Command encapsulates a single task or tool being run as part of the CLI. This can be a quick one-off task as well
 * as long running services. The main class is {@link Command} which can be extended. It provides a name and if picked
 * by the user the application will pass all arguments on that are meant for the command. The application may parse
 * some arguments itself, those and the command name will be stripped from the arguments the command receives.
 * <p/>
 * This is a simple command that doesn't process any additional parameters and thus has no usage. The application will
 * print a message explaining this.
 * <pre><code>
 * public class TestCommand extends Command {
 *
 *   public TestCommand() {
 *     super("test");
 *   }
 *
 *   {@literal @Override}
 *   public Optional<String> getUsage() {
 *     return Optional.absent();
 *   }
 *
 *   {@literal @Override}
 *   public void run(String... arguments) {
 *     System.out.println("Hello world!");
 *   }
 *
 * }
 * </code></pre>
 *
 * <h3>Base commands</h3>
 *
 * We also have a class called {@link BaseCommand} which provides extra functionality on top of regular Commands.
 *
 * It allows for the following things:
 * <ul>
 *   <li>Print the help for the command</li>
 *   <li>Allow specifying a configuration object which will be populated using JCommander from the command line
 *   parameters</li>
 *   <li>Allow specifying a configuration object which will be populated from a specified file in JSON or YAML</li>
 *   <li>Both configuration objects will be verified using annotations and they can be the same object</li>
 *   <li>Allow some configuration of logging</li>
 * </ul>
 *
 * <h3>Service commands</h3>
 *
 * To be able to write long running services which run until explicitly terminated (e.g. servers) this library provides
 * a {@link ServiceCommand} which can be extended to manage the life cycle of a Service.
 *
 * <h2>Command discovery</h2>
 *
 * As the Application is using the ServiceLoader mechanism to detect commands you have to write the appropriate
 * META-INF/services/org.gbif.cli.Command file. To make this easier you can use a library called metainf-services by
 * adding a dependency like this:
 * <pre><code>
 *   <dependency>
 *     <groupId>org.kohsuke.metainf-services</groupId>
 *     <artifactId>metainf-services</artifactId>
 *     <version>...</version>
 *     <optional>true</optional>
 *   </dependency>
 * </code></pre>
 *
 * Then annotate your commands with either {@code {@literal @MetaInfServices}}
 * or {@code {@literal @MetaInfServices(Command.class}}. Please read up on the
 * <a href="http://metainf-services.kohsuke.org/">homepage</a> of the library for more details.
 *
 * <h2>Jackson usage</h2>
 *
 * This project uses Jackson 2 because only that supports YAML. That means you have to be very careful in projects
 * using this to use the proper annotations. If you need to annotate any Configuration classes for example you need
 * to use the annotations from Jackson 2 ({@code com.fasterxml.jackson.annotation} package).
 */

package org.gbif.cli;
