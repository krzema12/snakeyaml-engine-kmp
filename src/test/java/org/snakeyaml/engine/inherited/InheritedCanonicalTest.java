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
package org.snakeyaml.engine.inherited;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.events.Event;
import org.snakeyaml.engine.tokens.Token;

@org.junit.jupiter.api.Tag("fast")
public class InheritedCanonicalTest extends InheritedImportTest {

    @Test
    @DisplayName("Canonical scan")
    public void testCanonicalScanner() throws IOException {
        File[] files = getStreamsByExtension(".canonical");
        assertTrue(files.length > 0, "No test files found.");
        for (int i = 0; i < files.length; i++) {
            InputStream input = new FileInputStream(files[i]);
            List<Token> tokens = canonicalScan(input);
            input.close();
            assertFalse(tokens.isEmpty());
        }
    }

    private List<Token> canonicalScan(InputStream input) throws IOException {
        int ch = input.read();
        StringBuilder buffer = new StringBuilder();
        while (ch != -1) {
            buffer.append((char) ch);
            ch = input.read();
        }
        CanonicalScanner scanner = new CanonicalScanner(buffer.toString());
        List<Token> result = new ArrayList<Token>();
        while (scanner.peekToken() != null) {
            result.add(scanner.getToken());
        }
        return result;
    }

    @Test
    @DisplayName("Canonical parse")
    public void testCanonicalParser() throws IOException {
        File[] files = getStreamsByExtension(".canonical");
        assertTrue(files.length > 0, "No test files found.");
        for (int i = 0; i < files.length; i++) {
            InputStream input = new FileInputStream(files[i]);
            List<Event> tokens = canonicalParse(input);
            input.close();
            assertFalse(tokens.isEmpty());
        }
    }
}
