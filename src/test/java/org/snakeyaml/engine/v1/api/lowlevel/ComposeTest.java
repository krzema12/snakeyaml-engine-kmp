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
package org.snakeyaml.engine.v1.api.lowlevel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.v1.api.LoadSettingsBuilder;
import org.snakeyaml.engine.v1.nodes.Node;

import com.google.common.io.CharSource;

@Tag("fast")
class ComposeTest {

    @Test
    void composeEmptyReader(TestInfo testInfo) throws IOException {
        Compose compose = new Compose(new LoadSettingsBuilder().build());
        Optional<Node> node = compose.composeReader(CharSource.wrap("").openStream());
        assertEquals(Optional.empty(), node);
    }

    @Test
    void composeEmptyInputStream(TestInfo testInfo) {
        Compose compose = new Compose(new LoadSettingsBuilder().build());
        Optional<Node> node = compose.composeInputStream(new ByteArrayInputStream("".getBytes()));
        assertEquals(Optional.empty(), node);
    }

    @Test
    void composeAllFromEmptyReader(TestInfo testInfo) throws IOException {
        Compose compose = new Compose(new LoadSettingsBuilder().build());
        Iterable<Node> nodes = compose.composeAllFromReader(CharSource.wrap("").openStream());
        assertFalse(nodes.iterator().hasNext());
    }

    @Test
    void composeAllFromEmptyInputStream(TestInfo testInfo) {
        Compose compose = new Compose(new LoadSettingsBuilder().build());
        Iterable<Node> nodes = compose.composeAllFromInputStream(new ByteArrayInputStream("".getBytes()));
        assertFalse(nodes.iterator().hasNext());
    }
}
