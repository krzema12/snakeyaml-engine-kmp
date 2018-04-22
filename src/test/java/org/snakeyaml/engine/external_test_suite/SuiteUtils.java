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
package org.snakeyaml.engine.external_test_suite;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class SuiteUtils {

    public static List<File> getAllFoldersIn(String folder) {
        File file = new File(folder);
        if (!file.exists()) {
            throw new RuntimeException("Folder not found: " + file.getAbsolutePath());
        }
        if (!file.isDirectory()) {
            throw new RuntimeException("Must be folder: " + file.getAbsolutePath());
        }
        return Arrays.stream(file.listFiles()).filter(f -> f.isDirectory()).collect(Collectors.toList());
    }

    public static SuiteData readData(File file) {
        try {
            String name = file.getName();
            String label = Files.asCharSource(new File(file, "==="), Charsets.UTF_8).read();
            String input = Files.asCharSource(new File(file, "in.yaml"), Charsets.UTF_8).read();
            List<String> events = Files.readLines(new File(file, "test.event"), Charsets.UTF_8)
                    .stream().filter(line -> !line.isEmpty()).collect(Collectors.toList());
            boolean error = new File(file, "error").exists();
            return new SuiteData(name, label, input, events, error);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<SuiteData> getAll() {
        List<File> allSuiteFiles = getAllFoldersIn("src/test/resources/comprehensive-test-suite-data");
        return allSuiteFiles.stream().map(file -> readData(file)).collect(Collectors.toList());
    }


}
