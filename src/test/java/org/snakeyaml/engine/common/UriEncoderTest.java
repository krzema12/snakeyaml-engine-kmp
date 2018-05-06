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
package org.snakeyaml.engine.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

@Tag("fast")
class UriEncoderTest {

    @Test
    @DisplayName("Encode-decode")
    void encodeDecodeString(TestInfo testInfo) {
        String encoded = UriEncoder.encode(" +%");
        assertEquals("%20%2B%25", encoded);
        String decoded = UriEncoder.decode(encoded);
        assertEquals(" +%", decoded);
    }

    @Test
    @DisplayName("Invalid decode")
    void testInvalidDecode(TestInfo testInfo) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                UriEncoder.decode("%2"));
        assertEquals("URLDecoder: Incomplete trailing escape (%) pattern", exception.getMessage());
    }
}
