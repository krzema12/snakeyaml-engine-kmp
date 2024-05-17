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
package org.snakeyaml.engine.v2.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.krzeminski.snakeyaml.engine.kmp.api.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("fast")
class LoadTest {

  @Test
  @DisplayName("String 'a' is parsed")
  void parseString() {
    Load load = new Load();
    String str = (String) load.loadOne("a");
    assertEquals("a", str);
  }

  @Test
  @DisplayName("Integer 1 is parsed")
  void parseInteger() {
    Load load = new Load();
    Integer integer = (Integer) load.loadOne("1");
    assertEquals(Integer.valueOf(1), integer);
  }

  @Test
  @DisplayName("Boolean true is parsed")
  void parseBoolean() {
    Load load = new Load();
    assertTrue((Boolean) load.loadOne("true"));
  }

  @Test
  @DisplayName("null is parsed")
  void parseNull() {
    Load load = new Load();
    assertNull(load.loadOne(""));
  }

  @Test
  @DisplayName("null tag is parsed")
  void parseNullTag() {
    Load load = new Load();
    assertNull(load.loadOne("!!null"));
  }

  @Test
  @DisplayName("Float is parsed")
  void parseFloat() {
    Load load = new Load();
    Double doubleValue = (Double) load.loadOne("1.01");
    assertEquals(Double.valueOf(1.01), doubleValue);
  }

  @Test
  @DisplayName("Load from InputStream")
  void loadFromInputStream() {
    Load load = new Load();
    String v = (String) load.loadOne(new ByteArrayInputStream("aaa".getBytes()));
    assertEquals("aaa", v);
  }

  @Test
  @DisplayName("Load from Reader")
  void loadFromReader() {
    Load load = new Load();
    String v = (String) load.loadOne(new StringReader("bbb"));
    assertEquals("bbb", v);
  }

  @Test
  @DisplayName("Load all from String")
  void loadAll() {
    Load load = new Load();
    InputStream input = new ByteArrayInputStream("bbb\n---\nccc\n---\nddd".getBytes());
    Iterable<Object> v = load.loadAll(input);
    Iterator<Object> iter = v.iterator();
    assertTrue(iter.hasNext(), "expect iterator has next");
    Object o1 = iter.next();
    assertEquals("bbb", o1);

    assertTrue(iter.hasNext());
    Object o2 = iter.next();
    assertEquals("ccc", o2);

    assertTrue(iter.hasNext());
    Object o3 = iter.next();
    assertEquals("ddd", o3);

    assertFalse(iter.hasNext());
  }

  @Test
  @DisplayName("Load all from String")
  void loadIterableFromString() {
    Load load = new Load();
    Iterable<Object> v = load.loadAll("1\n---\n2\n---\n3");
    int counter = 1;
    for (Object o : v) {
      // System.out.println("O: " + o);
      assertEquals(counter, o);
      counter++;
    }
    assertEquals(4, counter);
  }

  @Test
  @DisplayName("Load all from String which has only 1 document")
  void loadIterableFromString2() {
    Load load = new Load();

    Iterable<Object> iterable = load.loadAll("1\n");
    int counter = 1;
    for (Object o : iterable) {
      // System.out.println("O: " + o);
      assertEquals(counter, o);
      counter++;
    }
    assertEquals(2, counter);

    Iterator<Object> iter = load.loadAll("1\n").iterator();
    iter.hasNext();
    Object o1 = iter.next();
    assertEquals(1, o1);
    assertFalse(iter.hasNext());
  }

  @Test
  @DisplayName("Load all from Reader")
  void loadAllFromReader() {
    Load load = new Load();
    Iterable<Object> v = load.loadAll(new StringReader("bbb"));
    Iterator<Object> iter = v.iterator();
    assertTrue(iter.hasNext());
    Object o1 = iter.next();
    assertEquals("bbb", o1);
    assertFalse(iter.hasNext());
  }

  @Test
  @DisplayName("Load a lot of documents from the same Load instance (not recommended)")
  void loadManyFromTheSameInstance() {
    Load load = new Load();
    for (int i = 0; i < 100000; i++) {
      Iterable<Object> v = load.loadAll(new StringReader("{foo: bar, list: [1, 2, 3]}"));
      Iterator<Object> iter = v.iterator();
      assertTrue(iter.hasNext());
      Object o1 = iter.next();
      assertNotNull(o1);
      assertFalse(iter.hasNext());
    }
  }

  @Test
  @DisplayName("Throw UnsupportedOperationException if try to remove from iterator")
  void loadAllWithUnsupportedOperationException() {
    Load load = new Load();
    Iterable<Object> v = load.loadAll("bbb");
    UnsupportedOperationException exception =
        assertThrows(UnsupportedOperationException.class, () -> v.iterator().remove());
    assertEquals("Operation is not supported for read-only collection", exception.getMessage());
  }
}
