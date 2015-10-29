package org.gbif.cli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * To be used in JCommander configurations to indicates a property name for configuration parameter.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyName {

  /**
   * @return the name of the property to be used instead of the field name
   */
  String value ();
}
