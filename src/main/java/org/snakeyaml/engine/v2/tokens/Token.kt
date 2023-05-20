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
import java.util.*

/**
 * A unit of YAML data
 */
sealed class Token(
  val startMark: Optional<Mark>,
  val endMark: Optional<Mark>,
) {

  /**
   * For error reporting.
   *
   * @return [ID] of this token
   */
  abstract val tokenId: ID

  override fun toString(): String = tokenId.toString()

  enum class ID(
    private val description: String
  ) {
    Alias("<alias>"),
    Anchor("<anchor>"),
    BlockEnd("<block end>"),
    BlockEntry("-"),
    BlockMappingStart("<block mapping start>"),
    BlockSequenceStart("<block sequence start>"),
    Directive("<directive>"),
    DocumentEnd("<document end>"),
    DocumentStart("<document start>"),
    FlowEntry(","),
    FlowMappingEnd("}"),
    FlowMappingStart("{"),
    FlowSequenceEnd("]"),
    FlowSequenceStart("["),
    Key("?"),
    Scalar("<scalar>"),
    StreamEnd("<stream end>"),
    StreamStart("<stream start>"),
    Tag("<tag>"),
    Comment("#"),
    Value(":");

    override fun toString(): String = description
  }
}
