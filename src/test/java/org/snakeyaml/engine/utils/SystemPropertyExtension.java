/*
 * Copyright (c) 2018, SnakeYAML
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
package org.snakeyaml.engine.utils;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class SystemPropertyExtension implements AfterEachCallback, BeforeEachCallback {

  @Override
  public void afterEach(ExtensionContext extensionContext) throws Exception {
    SystemProperty annotation = extensionContext.getTestMethod().get()
        .getAnnotation(SystemProperty.class);
    System.clearProperty(annotation.key());
  }

  @Override
  public void beforeEach(ExtensionContext extensionContext) throws Exception {
    SystemProperty annotation = extensionContext.getTestMethod().get()
        .getAnnotation(SystemProperty.class);
    System.setProperty(annotation.key(), annotation.value());
  }
}
