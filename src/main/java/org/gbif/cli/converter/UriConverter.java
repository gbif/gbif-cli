package org.gbif.cli.converter;

import java.net.URI;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

/**
 * Converts parameters into URIs.
 */
public class UriConverter implements IStringConverter<URI> {
  @Override
  public URI convert(String value) {
    try {
      return URI.create(value);
    } catch(IllegalArgumentException ex) {
      throw new ParameterException(value + " is not a valid URI");
    }
  }
}
