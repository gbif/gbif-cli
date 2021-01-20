package org.gbif.cli;

import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ConfigUtilsTest {

  @SuppressWarnings({"unused", "FieldMayBeFinal"})
  public static class TestConfig {
    public GenericParameters generic = new GenericParameters();
    @IgnoreProperty
    public GenericParameters gene = new GenericParameters();
    public int count = 100;
    public final String name = "Tim";
    public static Date now = new Date();
    private long secret = 9L;
    @PropertyName("nine")
    public long eight = 9L;
    public UUID uuid = UUID.randomUUID();
  }

  @Test
  public void testToProperties() {
    Properties props = ConfigUtils.toProperties(new GenericParameters());
    assertEquals("false", props.getProperty("verbose"));
    assertNull(props.getProperty("configurationFiles"));
  }

  @Test
  public void testToPropertiesPrefixed() {
    Properties props = ConfigUtils.toProperties("org.gbif",new GenericParameters());
    assertEquals("false", props.getProperty("org.gbif.verbose"));
  }

  @Test
  public void testToNestedProperties() {
    Properties props = ConfigUtils.toProperties("clb", new TestConfig());
    assertEquals("false", props.getProperty("clb.generic.verbose"));
    assertEquals("100", props.getProperty("clb.count"));
    assertEquals("Tim", props.getProperty("clb.name"));
    assertNotNull(props.getProperty("clb.now"));
    assertNull(props.getProperty("clb.secret"));
    assertNull(props.getProperty("clb.gene"));
    assertNull(props.getProperty("clb.gene.verbose"));
    assertEquals("9", props.getProperty("clb.nine"));
  }
}
