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
package org.snakeyaml.engine.v1.emitter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.v1.api.Dump;
import org.snakeyaml.engine.v1.api.DumpSettings;
import org.snakeyaml.engine.v1.api.DumpSettingsBuilder;
import org.snakeyaml.engine.v1.exceptions.YamlEngineException;

@Tag("fast")
public class FlexSimleKeyTest {

    private int len = 130;

    @Test
    public void testLongKey(TestInfo testInfo) {
        Dump dump = new Dump(createOptions(len));
        Map<String, Object> root = new HashMap();
        Map<String, String> map = new HashMap<>();
        String key = createKey(len);
        map.put(key, "v1");
        root.put("data", map);
        assertEquals("data: {? " + key + "\n  : v1}\n", dump.dumpToString(root));
    }

    @Test
    public void testForceLongKeyToBeImplicit(TestInfo testInfo) {
        Dump dump = new Dump(createOptions(len + 10));
        Map<String, Object> root = new HashMap();
        Map<String, String> map = new HashMap<>();
        String key = createKey(len);
        map.put(key, "v1");
        root.put("data", map);
        assertEquals("data: {" + key + ": v1}\n", dump.dumpToString(root));
    }

    @Test
    public void testTooLongKeyLength() {
        try {
            createOptions(1024 + 1);
            fail("Length must be restricted to 1024 chars");
        } catch (YamlEngineException e) {
            assertEquals("The simple key must not span more than 1024 stream characters. See https://yaml.org/spec/1.1/#id934537", e.getMessage());
        }
    }

    private DumpSettings createOptions(int len) {
        return new DumpSettingsBuilder().setMaxSimpleKeyLength(len).build();
    }

    private String createKey(int length) {
        StringBuffer outputBuffer = new StringBuffer(length);
        for (int i = 0; i < length; i++) {
            outputBuffer.append("" + (i + 1) % 10);
        }
        String prefix = String.valueOf(length);
        String result = prefix + "_" + outputBuffer.toString().substring(0, length - prefix.length() - 1);
        if (result.length() != length) throw new RuntimeException("It was: " + result.length());
        return result;
    }
}
