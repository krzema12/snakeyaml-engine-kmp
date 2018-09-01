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
package org.snakeyaml.engine.v1.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

@Tag("fast")
class YamlUnicodeReaderTest {

    @Test
    @DisplayName("Detect UTF-8 dy default")
    void noBom(TestInfo testInfo) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream("1".getBytes());
        YamlUnicodeReader reader = new YamlUnicodeReader(input);
        reader.init();
        assertEquals(Charset.forName("UTF-8"), reader.getEncoding(), "no BOM must be detected as UTF-8");
    }

    @Test
    @DisplayName("Detect UTF-8 - EF BB BF")
    void utf8(TestInfo testInfo) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, (byte) 49});
        YamlUnicodeReader reader = new YamlUnicodeReader(input);
        reader.init();
        assertEquals(Charset.forName("UTF-8"), reader.getEncoding(), "no BOM must be detected as UTF-8");
        assertEquals('1', reader.read(), "BOM must be skipped, #49 -> 1");
    }

    @Test
    @DisplayName("Detect 00 00 FE FF, UTF-32, big-endian")
    void feff32(TestInfo testInfo) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF,
                (byte) 0, (byte) 0, (byte) 0, (byte) 49,});
        YamlUnicodeReader reader = new YamlUnicodeReader(input);
        reader.init();
        assertEquals(Charset.forName("UTF-32BE"), reader.getEncoding());
        assertEquals('1', reader.read(), "BOM must be skipped, #49 -> 1");
    }

    @Test
    @DisplayName("Detect FF FE 00 00, UTF-32, little-endian")
    void fffe32(TestInfo testInfo) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00,
                (byte) 49, (byte) 0, (byte) 0, (byte) 0});
        YamlUnicodeReader reader = new YamlUnicodeReader(input);
        reader.init();
        assertEquals(Charset.forName("UTF-32LE"), reader.getEncoding());
        assertEquals('1', reader.read(), "BOM must be skipped, #49 -> 1");
    }

    @Test
    @DisplayName("Detect FE FF, UTF-16, big-endian")
    void feff16(TestInfo testInfo) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[]{(byte) 0xFE, (byte) 0xFF,
                (byte) 0, (byte) 49,});
        YamlUnicodeReader reader = new YamlUnicodeReader(input);
        reader.init();
        assertEquals(Charset.forName("UTF-16BE"), reader.getEncoding());
        assertEquals('1', reader.read(), "BOM must be skipped, #49 -> 1");
    }

    @Test
    @DisplayName("Detect FF FE, UTF-16, little-endian")
    void fffe16(TestInfo testInfo) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[]{(byte) 0xFF, (byte) 0xFE,
                (byte) 49, (byte) 0});
        YamlUnicodeReader reader = new YamlUnicodeReader(input);
        reader.init();
        assertEquals(Charset.forName("UTF-16LE"), reader.getEncoding());
        assertEquals('1', reader.read(), "BOM must be skipped, #49 -> 1");
        reader.close();
    }

}

