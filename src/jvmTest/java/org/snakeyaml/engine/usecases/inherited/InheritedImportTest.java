/*
 * Copyright (c) 2018, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.snakeyaml.engine.usecases.inherited;

import okio.Okio;
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;
import it.krzeminski.snakeyaml.engine.kmp.api.YamlUnicodeReader;
import it.krzeminski.snakeyaml.engine.kmp.events.Event;
import it.krzeminski.snakeyaml.engine.kmp.parser.Parser;
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl;
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader;
import org.snakeyaml.engine.v2.utils.TestUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


public abstract class InheritedImportTest {

  public static final String PATH = "/inherited_yaml_1_1";


  protected String getResource(String theName) {
    //noinspection InjectedReferences
    return TestUtils.getResource(PATH + "/" + theName);
  }

  protected File[] getStreamsByExtension(String extension) {
    return getStreamsByExtension(extension, false);
  }

  protected File[] getStreamsByExtension(String extension, boolean onlyIfCanonicalPresent) {
    File file = new File("src/jvmTest/resources" + PATH);
    assertTrue(file.exists(), "Folder not found: " + file.getAbsolutePath());
    assertTrue(file.isDirectory());
    return file.listFiles(new InheritedFilenameFilter(extension, onlyIfCanonicalPresent));
  }

  protected File getFileByName(String name) {
    File file = new File("src/jvmTest/resources" + PATH + "/" + name);
    assertTrue(file.exists(), "Folder not found: " + file.getAbsolutePath());
    assertTrue(file.isFile());
    return file;
  }

  protected List<Event> canonicalParse(InputStream input2, String label) throws IOException {
    LoadSettings setting = LoadSettings.builder().setLabel(label).build();
    StreamReader reader = new StreamReader(setting, new YamlUnicodeReader(Okio.source(input2)));
    StringBuilder buffer = new StringBuilder();
    while (reader.peek() != '\0') {
      buffer.appendCodePoint(reader.peek());
      reader.forward();
    }
    CanonicalParser parser =
        new CanonicalParser(buffer.toString().replace(System.lineSeparator(), "\n"), label);
    List<Event> result = new ArrayList<>();
    while (parser.hasNext()) {
      result.add(parser.next());
    }
    input2.close();
    return result;
  }

  protected List<Event> parse(InputStream input) throws IOException {
    LoadSettings settings = LoadSettings.builder().build();
    StreamReader reader = new StreamReader(settings, new YamlUnicodeReader(Okio.source(input)));
    Parser parser = new ParserImpl(settings, reader);
    List<Event> result = new ArrayList<>();
    while (parser.hasNext()) {
      result.add(parser.next());
    }
    input.close();
    return result;
  }

  private static class InheritedFilenameFilter implements FilenameFilter {

    private final String extension;
    private final boolean onlyIfCanonicalPresent;

    public InheritedFilenameFilter(String extension, boolean onlyIfCanonicalPresent) {
      this.extension = extension;
      this.onlyIfCanonicalPresent = onlyIfCanonicalPresent;
    }

    public boolean accept(File dir, String name) {
      int position = name.lastIndexOf('.');
      String canonicalFileName = name.substring(0, position) + ".canonical";
      File canonicalFile = new File(dir, canonicalFileName);
      if (onlyIfCanonicalPresent && !canonicalFile.exists()) {
        return false;
      } else {
        return name.endsWith(extension);
      }
    }
  }
}
