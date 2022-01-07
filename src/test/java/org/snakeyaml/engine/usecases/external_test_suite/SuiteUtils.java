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
package org.snakeyaml.engine.usecases.external_test_suite;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.lowlevel.Parse;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

public class SuiteUtils {

  public static final List<String> deviationsWithSuccess = Lists.newArrayList(
      "G5U8", "S98Z", "4EJS", "9MMA", "9HCY", "9JBA", "CVW2", "U99R",
      /* sep */ "9C9N", "SU5Z", "QB6E", "QLJ7", "EB22");
  public static final List<String> deviationsWithError = Lists.newArrayList(
      "HWV9", "NB6Z", "652Z", "VJP3-01", "RHX7", "KH5V-01", "5MUD", "9SA2", "QT73", "4MUZ", "CFD4",
      "NJ66", "NKF9", "JR7V", "K3WX", "Y2GN", "5T43", "3RLN-01", "SM9W-01", "3RLN-04", "DE56-02",
      "DE56-03",
      /* sep */"CXX2", "KZN9", "DC7X", "6HB6", "2JQS", "6M2F", "S3PD", "Q5MG", "FRK4", "NHX8",
      "DBG4", "4ABK", "M7A3", "9MMW", "6BCT", "A2M4", "2SXE", "DK3J", "W5VH", "8XYN", "K54U",
      "HS5T", "UT92", "W4TN", "FP8R", "WZ62", "7Z25");


  public static final String FOLDER_NAME = "src/test/resources/comprehensive-test-suite-data";

  public static List<File> getAllFoldersIn(String folder) {
    File file = new File(folder);
    if (!file.exists()) {
      throw new RuntimeException("Folder not found: " + file.getAbsolutePath());
    }
    if (!file.isDirectory()) {
      throw new RuntimeException("Must be folder: " + file.getAbsolutePath());
    }
    return Arrays.stream(file.listFiles()).filter(f -> f.isDirectory())
        .collect(Collectors.toList());
  }

  public static SuiteData readData(File file) {
    try {
      String name = file.getName();
      String label = Files.asCharSource(new File(file, "==="), Charsets.UTF_8).read();
      String input = Files.asCharSource(new File(file, "in.yaml"), Charsets.UTF_8).read();
      List<String> events = Files.readLines(new File(file, "test.event"), Charsets.UTF_8)
          .stream().filter(line -> !line.isEmpty()).collect(Collectors.toList());
      boolean error = new File(file, "error").exists();
      return new SuiteData(name, label, input, events, error);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static List<SuiteData> getAll() {
    List<File> allSuiteFiles = getAllFoldersIn(FOLDER_NAME);
    return allSuiteFiles.stream().map(file -> readData(file)).collect(Collectors.toList());
  }

  public static SuiteData getOne(String name) {
    return readData(new File(FOLDER_NAME, name));
  }

  public static ParseResult parseData(SuiteData data) {
    Optional<Exception> error = Optional.empty();
    List<Event> list = new ArrayList();
    try {
      LoadSettings settings = LoadSettings.builder().setLabel(data.getLabel()).build();
      Iterable<Event> iterable = new Parse(settings).parseString(data.getInput());
      iterable.forEach(event -> list.add(event));
    } catch (YamlEngineException e) {
      error = Optional.of(e);
    }
    return new ParseResult(list, error);
  }
}

class ParseResult {

  private final List<Event> events;
  private final Optional<Exception> error;

  public ParseResult(List<Event> events, Optional<Exception> error) {
    this.events = events;
    this.error = error;
  }

  public List<Event> getEvents() {
    return events;
  }

  public Optional<Exception> getError() {
    return error;
  }
}
