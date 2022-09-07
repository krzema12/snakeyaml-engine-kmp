/*
 * Copyright (c) 2018, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snakeyaml.engine.v2.emitter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.DumpSettingsBuilder;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.StreamDataWriter;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.events.DocumentStartEvent;
import org.snakeyaml.engine.v2.events.ImplicitTuple;
import org.snakeyaml.engine.v2.events.ScalarEvent;
import org.snakeyaml.engine.v2.events.StreamStartEvent;
import org.snakeyaml.engine.v2.exceptions.ComposerException;

@Tag("fast")
public class EmitterTest {

  private String dump(DumpSettings settings, Object map) {
    Dump yaml = new Dump(settings);
    return yaml.dumpToString(map);
  }

  @Test
  public void testWriteFolded() {
    DumpSettings settings =
        DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.FOLDED).build();
    String folded = "0123456789 0123456789\n0123456789 0123456789";
    Map<String, String> map = new LinkedHashMap<String, String>();
    map.put("aaa", folded);
    map.put("bbb", "\nbla-bla\n");
    String output = dump(settings, map);
    String expected =
        "\"aaa\": >-\n  0123456789 0123456789\n\n  0123456789 0123456789\n\"bbb\": >2\n\n  bla-bla\n";
    assertEquals(expected, output);
  }

  @Test
  public void testWriteLiteral() {
    DumpSettings settings =
        DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.LITERAL).build();
    String folded = "0123456789 0123456789 0123456789 0123456789";
    Map<String, String> map = new LinkedHashMap<String, String>();
    map.put("aaa", folded);
    map.put("bbb", "\nbla-bla\n");
    String output = dump(settings, map);
    String expected =
        "\"aaa\": |-\n  0123456789 0123456789 0123456789 0123456789\n\"bbb\": |2\n\n  bla-bla\n";
    assertEquals(expected, output);
  }

  @Test
  public void testWritePlain() {
    DumpSettings settings = DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.PLAIN).build();
    String folded = "0123456789 0123456789\n0123456789 0123456789";
    Map<String, String> map = new LinkedHashMap<String, String>();
    map.put("aaa", folded);
    map.put("bbb", "\nbla-bla");
    String output = dump(settings, map);
    String expected =
        "aaa: |-\n  0123456789 0123456789\n  0123456789 0123456789\nbbb: |2-\n\n  bla-bla\n";
    assertEquals(expected, output);
  }

  @Test
  public void testWritePlainPretty() {
    DumpSettings settings = DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.PLAIN)
        .setMultiLineFlow(true).build();
    String folded = "0123456789 0123456789\n0123456789 0123456789";
    Map<String, String> map = new LinkedHashMap<String, String>();
    map.put("aaa", folded);
    map.put("bbb", "\nbla-bla");
    String output = dump(settings, map);
    String expected =
        "aaa: |-\n  0123456789 0123456789\n  0123456789 0123456789\nbbb: |2-\n\n  bla-bla\n";
    assertEquals(expected, output);
  }

  @Test
  public void testWriteSingleQuoted() {
    DumpSettings settings =
        DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.SINGLE_QUOTED).build();
    String folded = "0123456789 0123456789\n0123456789 0123456789";
    Map<String, String> map = new LinkedHashMap<String, String>();
    map.put("aaa", folded);
    map.put("bbb", "\nbla-bla");
    String output = dump(settings, map);
    String expected =
        "'aaa': '0123456789 0123456789\n\n  0123456789 0123456789'\n'bbb': '\n\n  bla-bla'\n";
    assertEquals(expected, output);
  }

  @Test
  public void testWriteDoubleQuoted() {
    DumpSettings settings =
        DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED).build();
    String folded = "0123456789 0123456789\n0123456789 0123456789";
    Map<String, String> map = new LinkedHashMap<String, String>();
    map.put("aaa", folded);
    map.put("bbb", "\nbla-bla");
    String output = dump(settings, map);
    String expected =
        "\"aaa\": \"0123456789 0123456789\\n0123456789 0123456789\"\n\"bbb\": \"\\nbla-bla\"\n";
    assertEquals(expected, output);
  }

  // Issue #158
  @Test
  public void testWriteSupplementaryUnicode() throws IOException {
    DumpSettings settings = DumpSettings.builder().setUseUnicodeEncoding(false).build();
    String burger = new String(Character.toChars(0x1f354));
    String halfBurger = "\uD83C";
    StreamDataWriter output = new MyDumperWriter();
    Emitter emitter = new Emitter(settings, output);

    emitter.emit(new StreamStartEvent(Optional.empty(), Optional.empty()));
    emitter.emit(new DocumentStartEvent(false, Optional.empty(), new HashMap<>(), Optional.empty(),
        Optional.empty()));
    emitter.emit(new ScalarEvent(Optional.empty(), Optional.empty(), new ImplicitTuple(true, false),
        burger + halfBurger, ScalarStyle.DOUBLE_QUOTED, Optional.empty(), Optional.empty()));
    String expected = "! \"\\U0001f354\\ud83c\"";
    assertEquals(expected, output.toString());
  }

  @Test
  public void testSplitLineExpectFirstFlowSequenceItem() {
    DumpSettingsBuilder builder =
        DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
            .setDefaultFlowStyle(FlowStyle.FLOW).setWidth(8);
    Map<String, Object> map = new TreeMap<String, Object>();
    map.put("12345", Collections.singletonList("1111111111"));

    // Split lines enabled (default)
    String output = dump(builder.build(), map);
    assertEquals("{\"12345\": [\n    \"1111111111\"]}\n", output);

    // Split lines disabled
    output = dump(builder.setSplitLines(false).build(), map);
    assertEquals("{\"12345\": [\"1111111111\"]}\n", output);
  }

  @Test
  public void testWriteIndicatorIndent() {
    DumpSettings settings = DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK).setIndent(5)
        .setIndicatorIndent(2).build();
    List<?> topLevel =
        Arrays.asList(Collections.singletonMap("k1", "v1"), Collections.singletonMap("k2", "v2"));
    Map<String, ?> map = Collections.singletonMap("aaa", topLevel);
    String output = dump(settings, map);
    String expected = "aaa:\n  -  k1: v1\n  -  k2: v2\n";
    assertEquals(expected, output);
  }

  @Test
  public void testSplitLineExpectFlowSequenceItem() {
    DumpSettingsBuilder builder =
        DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
            .setDefaultFlowStyle(FlowStyle.FLOW).setWidth(8);
    // Split lines enabled (default)
    Dump yaml1 = new Dump(builder.build());
    String output = yaml1.dumpToString(Arrays.asList("1111111111", "2222222222"));
    assertEquals("[\"1111111111\",\n  \"2222222222\"]\n", output);
    output = yaml1.dumpToString(Arrays.asList("1", "2"));
    assertEquals("[\"1\", \"2\"]\n", output);

    // Split lines disabled
    Dump yaml2 = new Dump(builder.setSplitLines(false).build());
    output = yaml2.dumpToString(Arrays.asList("1111111111", "2222222222"));
    assertEquals("[\"1111111111\", \"2222222222\"]\n", output);
    output = yaml2.dumpToString(Arrays.asList("1", "2"));
    assertEquals("[\"1\", \"2\"]\n", output);
  }

  @Test
  public void testSplitLineExpectFirstFlowMappingKey() {
    DumpSettingsBuilder builder =
        DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
            .setDefaultFlowStyle(FlowStyle.FLOW).setWidth(16);
    Map<String, String> nonSplitMap = new TreeMap<String, String>();
    nonSplitMap.put("3", "4");
    Map<String, Map<String, String>> nonSplitContainerMap =
        new TreeMap<String, Map<String, String>>();
    nonSplitContainerMap.put("1 2", nonSplitMap);
    Map<String, String> splitMap = new TreeMap<String, String>();
    splitMap.put("3333333333", "4444444444");
    Map<String, Map<String, String>> splitContainerMap = new TreeMap<String, Map<String, String>>();
    splitContainerMap.put("1111111111 2222222222", splitMap);

    // Split lines enabled (default)
    String output = dump(builder.build(), splitContainerMap);
    assertEquals("{\"1111111111 2222222222\": {\n    \"3333333333\": \"4444444444\"}}\n", output);
    output = dump(builder.build(), nonSplitContainerMap);
    assertEquals("{\"1 2\": {\"3\": \"4\"}}\n", output);

    // Split lines disabled
    DumpSettings noSplit = builder.setSplitLines(false).build();
    output = dump(noSplit, splitContainerMap);
    assertEquals("{\"1111111111 2222222222\": {\"3333333333\": \"4444444444\"}}\n", output);
    output = dump(noSplit, nonSplitContainerMap);
    assertEquals("{\"1 2\": {\"3\": \"4\"}}\n", output);
  }

  @Test
  public void testSplitLineExpectFlowMappingKey() {
    DumpSettingsBuilder builder =
        DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
            .setDefaultFlowStyle(FlowStyle.FLOW).setWidth(16);
    Map<String, String> nonSplitMap = new TreeMap<String, String>();
    nonSplitMap.put("1", "2");
    nonSplitMap.put("3", "4");
    Map<String, String> splitMap = new TreeMap<String, String>();
    splitMap.put("1111111111", "2222222222");
    splitMap.put("3333333333", "4444444444");

    // Split lines enabled (default)
    String output = dump(builder.build(), splitMap);
    assertEquals("{\"1111111111\": \"2222222222\",\n  \"3333333333\": \"4444444444\"}\n", output);
    output = dump(builder.build(), nonSplitMap);
    assertEquals("{\"1\": \"2\", \"3\": \"4\"}\n", output);

    // Split lines disabled
    DumpSettings noSplit = builder.setSplitLines(false).build();
    output = dump(noSplit, splitMap);
    assertEquals("{\"1111111111\": \"2222222222\", \"3333333333\": \"4444444444\"}\n", output);
    output = dump(noSplit, nonSplitMap);
    assertEquals("{\"1\": \"2\", \"3\": \"4\"}\n", output);
  }

  @Test
  public void testAnchorInMaps() {
    DumpSettingsBuilder builder = DumpSettings.builder().setDefaultFlowStyle(FlowStyle.FLOW);
    Map<Object, Object> map1 = new HashMap<Object, Object>();
    Map<Object, Object> map2 = new HashMap<Object, Object>();
    map1.put("2", map2);
    map2.put("1", map1);
    String output = dump(builder.build(), map1);
    assertEquals("&id001 {'2': {'1': *id001}}\n", output);
  }

  @Test
  @DisplayName("Expected space to separate anchor from colon")
  public void testAliasAsKey() {
    DumpSettingsBuilder builder = DumpSettings.builder().setDefaultFlowStyle(FlowStyle.FLOW);
    // this is VERY BAD code
    // the map has itself as a key (no idea why it may be used except of a DoS attack)
    HashMap f = new HashMap();
    f.put(f, "a");

    String output = dump(builder.build(), f);
    // TODO FIXME this YAML is invalid, the colon will be part of Anchor and not the separator
    // key:value in the flow.
    assertEquals("&id001 {*id001: a}\n", output);
    Load load = new Load(LoadSettings.builder().build());
    try {
      load.loadFromString(output);
      fail("TODO fix anchor");
    } catch (ComposerException e) {
      assertTrue(e.getMessage().contains("found undefined alias id001:"), e.getMessage());
    }
  }

  public static class MyDumperWriter extends StringWriter implements StreamDataWriter {

  }
}
