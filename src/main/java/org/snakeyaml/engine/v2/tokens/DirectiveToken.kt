/*
 * Copyright (c) 2018, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.snakeyaml.engine.v2.tokens

import org.snakeyaml.engine.v2.exceptions.Mark
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import java.util.*

class DirectiveToken<T>(
  name: String, value: Optional<List<T>>, startMark: Optional<Mark>,
  endMark: Optional<Mark>
) :
  Token(startMark, endMark) {
  val name: String
  val value: Optional<List<T>>

  init {
    Objects.requireNonNull(name)
    this.name = name
    Objects.requireNonNull(value)
    if (value.isPresent && value.get().size != 2) {
      throw YamlEngineException(
        "Two strings/integers must be provided instead of " + value.get().size
      )
    }
    this.value = value
  }

  override val tokenId: ID
    get() = ID.Directive

  companion object {
    const val YAML_DIRECTIVE = "YAML"
    const val TAG_DIRECTIVE = "TAG"
  }
}
