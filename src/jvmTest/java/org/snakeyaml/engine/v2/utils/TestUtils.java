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
package org.snakeyaml.engine.v2.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.intellij.lang.annotations.Language;
import it.krzeminski.snakeyaml.engine.kmp.events.Event;

public abstract class TestUtils {

  public static String getResource(@Language("file-reference") String theName) {
    InputStream inputStream = getResourceAsStream(theName);
    return new BufferedReader(new InputStreamReader(inputStream)).lines()
        .collect(Collectors.joining("\n", "", "\n"));
  }

  public static InputStream getResourceAsStream(@Language("file-reference") String theName) {
    InputStream inputStream = TestUtils.class.getResourceAsStream(theName);
    if (inputStream == null) {
      throw new RuntimeException("Resource not found: " + theName);
    }
    return inputStream;
  }

  public static void compareEvents(List<Event> list1, List<Event> list2) {
    assertEquals(list1.size(), list2.size());
    for (Event event1 : list1) {
      Event event2 = list2.remove(0);
      String ev1 = event1.toString();
      String ev2 = event2.toString();
      assertEquals(ev1, ev2);
    }
  }

}
