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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("fast")
public class ReferencesTest {

    private String createDump(DumpSettings dumpSettings) {
        HashMap root = new HashMap();
        HashMap s1, s2, t1, t2;
        s1 = root;
        s2 = new HashMap();
        /*
        the time to parse grows very quickly
        SIZE -> time to parse in seconds
        25 -> 1
        26 -> 2
        27 -> 3
        28 -> 8
        29 -> 13
        30 -> 28
        31 -> 52
        32 -> 113
        33 -> 245
        34 -> 500
         */
        int SIZE = 25;
        for (int i = 0; i < SIZE; i++) {

            t1 = new HashMap();
            t2 = new HashMap();
            t1.put("foo", "1");
            t2.put("bar", "2");

            s1.put("a", t1);
            s1.put("b", t2);
            s2.put("a", t1);
            s2.put("b", t2);

            s1 = t1;
            s2 = t2;
        }

        //FIXME
        // this is VERY BAD code
        // the map has itself as a key (no idea why it may be used)
        HashMap f = new HashMap();
        f.put(f, "a");
        f.put("g", root);

        Dump dump = new Dump(dumpSettings);
        String output = dump.dumpToString(f);
        return output;
    }


    @Test
    public void referencesWithRecursiveKeysNotAllowedByDefault() {
        String output = createDump(DumpSettings.builder().build());
        //System.out.println(output);

        // Load
        LoadSettings settings = LoadSettings.builder().build();
        Load load = new Load(settings);
        try {
            load.loadFromString(output);
            fail();
        } catch (Exception e) {
            assertEquals("Recursive key for mapping is detected but it is not configured to be allowed.", e.getMessage());
        }
    }

    @Test
    public void referencesWithAllowRecursiveKeys() {
        String output = createDump(DumpSettings.builder().build());
        //System.out.println(output);

        // Load
        long time1 = System.currentTimeMillis();
        LoadSettings settings = LoadSettings.builder().setAllowRecursiveKeys(true).build();
        Load load = new Load(settings);
        load.loadFromString(output);
        long time2 = System.currentTimeMillis();
        System.out.println("Time was " + ((time2 - time1) / 1000) + " seconds.");
    }
}