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
package org.snakeyaml.engine.v2.emitter;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("fast")
public class UseUnicodeEncodingTest {

    @Test
    public void testEmitUnicode(TestInfo testInfo) {
        DumpSettings settings = DumpSettings.builder().build();
        Dump dump = new Dump(settings);
        String russianUnicode = "Пушкин - это наше всё! 😊";
        assertEquals(russianUnicode + "\n", dump.dumpToString(russianUnicode));
    }

    @Test
    public void testEscapeUnicode(TestInfo testInfo) {
        DumpSettings settings = DumpSettings.builder()
                .setUseUnicodeEncoding(false)
                .build();
        Dump dump = new Dump(settings);
        String russianUnicode = "Пушкин - это наше всё! 😊";
        assertEquals("\"\\u041f\\u0443\\u0448\\u043a\\u0438\\u043d - \\u044d\\u0442\\u043e \\u043d\\u0430\\u0448\\u0435\\\n" +
                "  \\ \\u0432\\u0441\\u0451! \\U0001f60a\"\n", dump.dumpToString(russianUnicode));
    }
}
