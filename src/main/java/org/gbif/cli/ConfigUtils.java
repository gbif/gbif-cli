package org.gbif.cli;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Properties;

import com.google.common.base.Strings;

/**
 * Utils class to extract properties from a configuration bean instance.
 * By default this class adds all public field names to the generated properties recursively following nesting beans.
 * Two field annotations are supported to alter this behavior:
 * {@link IgnoreProperty} tells this util to ignore the field from the resulting properties and
 * {@link PropertyName} can be used to assign a different local name than the default field name.
 */
public class ConfigUtils {

  /**
   * Builds properties from a configuration bean using no namespace prefix.
   *
   * @param config the configuration object to inspect
   *
   * @return properties instance derived
   */
  public static Properties toProperties(Object config) {
    return toProperties("", config);
  }

  /**
   * Builds properties from a configuration bean using a given namespace prefix which is added to all properties.
   *
   * @param config the configuration object to inspect
   *
   * @return properties instance derived
   */
  public static Properties toProperties(String prefix, Object config) {
    Properties props = new Properties();

    Field[] fields = config.getClass().getDeclaredFields();
    for (Field f : fields) {
      // ignore private fields
      if (Modifier.isPublic(f.getModifiers()) && f.getAnnotation(IgnoreProperty.class) == null) {
        try {
          final String localName =
            f.getAnnotation(PropertyName.class) == null ? f.getName() : f.getAnnotation(PropertyName.class).value();
          final String qualName = Strings.isNullOrEmpty(prefix) ? localName : prefix + "." + localName;
          Object val = f.get(config);
          if (val == null) {
            val = "";
          }
          // a gbif class? then we need to recursively inspect deeper
          if (f.getType().getCanonicalName().startsWith("org")) {
            Properties subProps = toProperties(qualName, val);
            props.putAll(subProps);
          } else if (!Collection.class.isAssignableFrom(f.getType())) {
            props.setProperty(qualName, val.toString());
          }
        } catch (IllegalAccessException ignored) {
          // should never get here cause its accessible
        }
      }
    }

    return props;
  }

  private ConfigUtils() {
    throw new UnsupportedOperationException("Can't initialize class");
  }

}
