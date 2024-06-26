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

import it.krzeminski.snakeyaml.engine.kmp.api.*;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.utils.TestUtils;

@Tag("fast")
class LoadSequenceTest {

  @Test
  @DisplayName("Empty list [] is parsed")
  void parseEmptyList() {
    Load load = new Load();
    List<Integer> list = (List<Integer>) load.loadOne("[]");
    assertEquals(new ArrayList<>(), list);
  }

  @Test
  @DisplayName("list [2] is parsed")
  void parseList1() {
    Load load = new Load();
    List<Integer> list = (List<Integer>) load.loadOne("[2]");
    assertEquals(Collections.singletonList(Integer.valueOf(2)), list);
  }

  @Test
  @DisplayName("list [2,3] is parsed")
  void parseList2() {
    Load load = new Load();
    List<Integer> list = (List<Integer>) load.loadOne("[2,3]");
    assertEquals(Lists.newArrayList(2, 3), list);
  }

  @Test
  @DisplayName("list [2,a,true] is parsed")
  void parseList3() {
    Load load = new Load();
    List<Object> list = (List<Object>) load.loadOne("[2,a,true]");
    assertEquals(Lists.newArrayList(2, "a", Boolean.TRUE), list);
  }

  @Test
  @DisplayName("list is parsed")
  void parseList4() {
    Load load = new Load();
    List<Object> list =
        (List<Object>) load.loadOne(TestUtils.getResource("/load/list1.yaml"));
    assertEquals(Lists.newArrayList("a", "bb", "ccc", "dddd"), list);
  }
}
