package org.gbif.cli.converter;

import java.net.URI;
import java.util.UUID;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IStringConverterFactory;

/**
 * Converter factory for all custom converters in this package.
 */
public class ConverterFactory implements IStringConverterFactory {

  public Class<? extends IStringConverter<?>> getConverter(Class forType) {
    if (forType.equals(UUID.class)) {
      return UuidConverter.class;

    } else if (forType.equals(URI.class)) {
      return UriConverter.class;
    }

    return null;
  }
}
