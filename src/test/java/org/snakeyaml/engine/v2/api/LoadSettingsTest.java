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
package org.snakeyaml.engine.v2.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.v2.common.SpecVersion;
import org.snakeyaml.engine.v2.exceptions.DuplicateKeyException;
import org.snakeyaml.engine.v2.resolver.JsonScalarResolver;

import java.util.Map;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("fast")
class LoadSettingsTest {

    @Test
    @DisplayName("Accept only YAML 1.2")
    void acceptOnly12(TestInfo testInfo) {
        UnaryOperator<SpecVersion> strict12 = t -> {
            if (t.getMajor() != 1 || t.getMinor() != 2) {
                throw new IllegalArgumentException("Only 1.2 is supported.");
            } else {
                return t;
            }
        };

        LoadSettings settings = LoadSettings.builder()
                .setVersionFunction(strict12)
                .build();
        Load load = new Load(settings);
        try {
            load.loadFromString("%YAML 1.1\n...\nfoo");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Only 1.2 is supported.", e.getMessage());
        }
    }

    @Test
    @DisplayName("Do not allow duplicate keys")
    void doNotAllowDuplicateKeys(TestInfo testInfo) {
        LoadSettings settings = LoadSettings.builder()
                .setAllowDuplicateKeys(false)
                .build();
        Load load = new Load(settings);
        try {
            load.loadFromString("{a: 1, a: 2}");
            fail("Duplicate keys must not be allowed.");
        } catch (DuplicateKeyException e) {
            assertTrue(e.getMessage().contains("found duplicate key a"));
        }
    }

    @Test
    @DisplayName("Do not allow duplicate keys by default")
    void doNotAllowDuplicateKeysByDefault(TestInfo testInfo) {
        LoadSettings settings = LoadSettings.builder().build();
        Load load = new Load(settings);
        try {
            load.loadFromString("{a: 1, a: 2}");
            fail("Duplicate keys must not be allowed.");
        } catch (DuplicateKeyException e) {
            assertTrue(e.getMessage().contains("found duplicate key a"));
        }
    }

    @Test
    @DisplayName("Allow duplicate keys")
    void allowDuplicateKeysWhenSpecified(TestInfo testInfo) {
        LoadSettings settings = LoadSettings.builder()
                .setAllowDuplicateKeys(true)
                .build();
        Load load = new Load(settings);
        Map<String, Integer> map = (Map<String, Integer>) load.loadFromString("{a: 1, a: 2}");
        assertEquals(new Integer(2), map.get("a"));
    }

    @Test
    @DisplayName("Set and get custom property")
    void customProperty(TestInfo testInfo) {
        SomeKey key = new SomeKey();
        LoadSettings settings = LoadSettings.builder()
                .setCustomProperty(key, "foo")
                .setCustomProperty(SomeStatus.DELIVERED, "bar")
                .build();
        assertEquals("foo", settings.getCustomProperty(key));
        assertEquals("bar", settings.getCustomProperty(SomeStatus.DELIVERED));
    }

    public static final class SomeKey implements SettingKey {
    }

    public enum SomeStatus implements SettingKey {
        ORDERED,
        DELIVERED
    }

    @Test
    @DisplayName("Set and get custom I/O buffer size")
    void bufferSize(TestInfo testInfo) {
        LoadSettings settings = LoadSettings.builder()
                .setBufferSize(4096)
                .build();
        assertEquals(new Integer(4096), settings.getBufferSize());
    }

    @Test
    @DisplayName("Use custom ScalarResolver")
    void customScalarResolver(TestInfo testInfo) {
        LoadSettings settings = LoadSettings.builder()
                .setScalarResolver(new SomeScalarResolver())
                .build();
        Load load = new Load(settings);
        assertEquals("false", load.loadFromString("false"));
        assertEquals(new Integer(1024), settings.getBufferSize());
    }

    public static final class SomeScalarResolver extends JsonScalarResolver {

        @Override
        public org.snakeyaml.engine.v2.nodes.Tag resolve(String value, Boolean implicit) {
            if ("false".equals(value)) {
                return org.snakeyaml.engine.v2.nodes.Tag.STR;
            } else {
                return super.resolve(value, implicit);
            }
        }
    }
}
