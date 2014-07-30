package org.gbif.cli.converter;

import java.util.UUID;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

/**
 * Converts parameters into UUIDs.
 */
public class UuidConverter implements IStringConverter<UUID> {
  @Override
  public UUID convert(String value) {
    try {
      return UUID.fromString(value);
    } catch(IllegalArgumentException ex) {
      throw new ParameterException(value + " is not a valid uuid");
    }
  }
}
