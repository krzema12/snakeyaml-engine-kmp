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
package org.snakeyaml.engine.v2.api.lowlevel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import com.google.common.io.CharSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.StreamDataWriter;
import org.snakeyaml.engine.v2.emitter.Emitter;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.events.StreamEndEvent;
import org.snakeyaml.engine.v2.events.StreamStartEvent;
import org.snakeyaml.engine.v2.utils.TestUtils;

/**
 * Test from https://github.com/yaml/yaml-runtimes
 * @see <a href"https://github.com/yaml/yaml-runtimes/blob/master/docker/java/utils/java/src/main/java/org/yaml/editor/Snake2Yaml.java">Snake2Yaml.java</a>
 */
@Tag("fast")
class ParseEmitTest {

  @Test
  void parseAndEmitList() throws IOException {
    ByteArrayOutputStream uu = new ByteArrayOutputStream();
    final PrintStream sw = new PrintStream(uu);
    String input = "- 1\n- 2\n- 3";
    yamlToYaml(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), sw);
    assertEquals("- 1\n- 2\n- 3\n", uu.toString());
  }

  @Test
  void parseAndEmitMap() throws IOException {
    ByteArrayOutputStream uu = new ByteArrayOutputStream();
    final PrintStream sw = new PrintStream(uu);
    String input = "---\nfoo: bar\n";
    yamlToYaml(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), sw);
    assertEquals("---\nfoo: bar\n", uu.toString());
  }

  /**
   * Convert a YAML character stream into events and then emit events back to YAML.
   *
   * @param in  Stream to read YAML from
   * @param out Stream to write YAML to
   */
  void yamlToYaml(final InputStream in, final PrintStream out) throws IOException {
    Parse parser = new Parse(LoadSettings.builder().build());
    Emitter emitter = new Emitter(DumpSettings.builder().build(), new MyDumperWriter(out));
    for (Event event : parser.parseInputStream(in)) {
      emitter.emit(event);
    }
  }

  class MyDumperWriter implements StreamDataWriter {
    private PrintStream out;

    public MyDumperWriter(PrintStream out) {
      this.out = out;
    }

    @Override
    public void flush() {
    }

    @Override
    public void write(String s) {
      out.print(s);
    }

    @Override
    public void write(String s, int offset, int len) {
      out.append(s, offset, offset + len);
    }
  }
}
