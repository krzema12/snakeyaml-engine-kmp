/*
 * Copyright (c) 2018, http://www.snakeyaml.org
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
package org.snakeyaml.engine.issues.issue17;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * https://yaml.org/spec/1.2/spec.html#id2774608
 */
@org.junit.jupiter.api.Tag("fast")
public class WindowsTest {
    Load loader = new Load(LoadSettings.builder().build());

    @Test
    @DisplayName("Check that Windows style line endings handled the same as Unix style ones")
    void testGetLineNumberOnWindows() {
        LoadSettings settings = LoadSettings.builder().build();
        StreamReader reader1 = new StreamReader(settings, "foo\r\nbar");
        StreamReader reader2 = new StreamReader(settings, "foo\nbar");
        reader1.forward(100);
        reader2.forward(100);
        assertEquals(reader1.getLine(), reader2.getLine());
    }

    @Test
    void countLinesCRLF() {
        try {
            loader.loadFromString("\r\n[");
            fail();
        } catch (ParserException e) {
            assertTrue(e.getMessage().contains("line 2,"), e.getMessage());
        }
    }

    @Test
    void countLinesCRCR() {
        try {
            loader.loadFromString("\r\r[");
            fail();
        } catch (ParserException e) {
            assertTrue(e.getMessage().contains("line 3,"));
        }
    }

    @Test
    void countLinesLFLF() {
        try {
            loader.loadFromString("\n\n[");
            fail();
        } catch (ParserException e) {
            assertTrue(e.getMessage().contains("line 3,"));
        }
    }
}
