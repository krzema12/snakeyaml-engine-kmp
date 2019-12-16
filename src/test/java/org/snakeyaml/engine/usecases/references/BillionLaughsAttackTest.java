/**
 * Copyright (c) 2018, http://www.snakeyaml.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snakeyaml.engine.usecases.references;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * https://en.wikipedia.org/wiki/Billion_laughs_attack#Variations
 */
@Tag("fast")
public class BillionLaughsAttackTest {
    public static final String data = "a: &a [\"lol\",\"lol\",\"lol\",\"lol\",\"lol\",\"lol\",\"lol\",\"lol\",\"lol\"]\n" +
            "b: &b [*a,*a,*a,*a,*a,*a,*a,*a,*a]\n" +
            "c: &c [*b,*b,*b,*b,*b,*b,*b,*b,*b]\n" +
            "d: &d [*c,*c,*c,*c,*c,*c,*c,*c,*c]\n" +
            "e: &e [*d,*d,*d,*d,*d,*d,*d,*d,*d]\n" +
            "f: &f [*e,*e,*e,*e,*e,*e,*e,*e,*e]\n" +
            "g: &g [*f,*f,*f,*f,*f,*f,*f,*f,*f]\n" +
            "h: &h [*g,*g,*g,*g,*g,*g,*g,*g,*g]\n" +
            "i: &i [*h,*h,*h,*h,*h,*h,*h,*h,*h]";

    @Test
    @DisplayName("Billion_laughs_attack")
    public void billionLaughsAttack() {
        LoadSettings settings = LoadSettings.builder().build();
        Load load = new Load(settings);
        Map map = (Map) load.loadFromString(data);
        assertNotNull(map);
    }

    @Test
    @DisplayName("Billion_laughs_attack if data expanded")
    public void billionLaughsAttackExpanded() {
        LoadSettings settings = LoadSettings.builder().build();
        Load load = new Load(settings);
        Map map = (Map) load.loadFromString(data);
        assertNotNull(map);
        try {
            map.toString();
            fail();
        } catch (Throwable e) {
            assertTrue(e.getMessage().contains("heap"));
        }
    }
}