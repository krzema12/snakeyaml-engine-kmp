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
package it.krzeminski.snakeyaml.engine.kmp.comments

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.StringOutputStream
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.StreamDataWriter
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.composer.Composer
import it.krzeminski.snakeyaml.engine.kmp.emitter.Emitter
import it.krzeminski.snakeyaml.engine.kmp.events.*
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import it.krzeminski.snakeyaml.engine.kmp.serializer.Serializer

class EmitterWithCommentEnabledTest : FunSpec({
    fun runEmitterWithCommentsEnabled(data: String): String {
        val output: StreamDataWriter = StringOutputStream()

        val dumpSettings = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.PLAIN)
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setDumpComments(true)
            .build()
        val serializer = Serializer(dumpSettings, Emitter(dumpSettings, output))

        serializer.emitStreamStart()
        val loadSettings = LoadSettings.builder()
            .setParseComments(true)
            .build()
        val composer = Composer(
            loadSettings,
            ParserImpl(loadSettings, StreamReader(loadSettings, data))
        )
        for (node in composer) {
            serializer.serializeDocument(node)
        }
        serializer.emitStreamEnd()

        return output.toString()
    }

    fun producePrettyFlowEmitter(output: StreamDataWriter): Emitter {
        val dumpSettings = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.PLAIN)
            .setDefaultFlowStyle(FlowStyle.FLOW)
            .setDumpComments(true)
            .setMultiLineFlow(true)
            .build()
        return Emitter(dumpSettings, output)
    }

    test("empty") {
        val data = ""

        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe data
    }

    test("with only comment") {
        val data = "# Comment\n\n"

        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe data
    }

    test("comment ending a line") {
        val data = """key: # Comment
  value
"""

        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe data
    }

    test("multiline comment") {
        val data = """key: # Comment
     # lines
  value
"""

        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe data
    }

    test("blank line") {
        val data = "" +
                "\n"

        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe data
    }

    test("blank line comments") {
        val data = """

            abc: def # comment



            """.trimIndent()

        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe data
    }

    test("block scalar") {
        val data = """abc: | # Comment
  def
  hij
"""

        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe data
    }

    test("directive line end comment") {
        val data = "%YAML 1.1 #Comment\n---\n"

        val result = runEmitterWithCommentsEnabled(data)
        // We currently strip Directive comments
        result shouldBe data
    }

    test("sequence") {
        val data = """# Comment
list: # InlineComment1
  - # Block Comment
    item # InlineComment2
# Comment
"""

        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe data
    }

    test("all comments 1") {
        val data = """# Block Comment1
# Block Comment2
key: # Inline Comment1a
     # Inline Comment1b
  # Block Comment3a
  # Block Comment3b
  value # Inline Comment2
# Block Comment4
list: # InlineComment3a
      # InlineComment3b
  - # Block Comment5
    item1 # InlineComment4
  - item2: [value2a, value2b] # InlineComment5
  - item3: {key3a: [value3a1, value3a2], key3b: value3b} # InlineComment6
# Block Comment6
---
# Block Comment7
"""

        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe data
    }

    test("multidoc") {
        val data = """
            key: value
            # Block Comment
            ---
            # Block Comment
            key: value

            """.trimIndent()

        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe data
    }

    test("all comments 2") {
        val data = """key:
  key:
    key:
    - # Block Comment1
      item1a
    - # Block Comment2
    - item1b
    - # Block Comment3
      MapKey_1: MapValue1
      MapKey_2: MapValue2
key2:
- # Block Comment4
  # Block Comment5
  item1 # Inline Comment1a
        # Inline Comment1b
- # Block Comment6a
  # Block Comment6b
  item2: value # Inline Comment2
# Block Comment7
"""

        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe data
    }

    test("all comments 3") {
        val data = """
            # Block Comment1
            [item1, {item2: value2}, {item3: value3}] # Inline Comment1
            # Block Comment2

            """.trimIndent()

        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe data
    }

    test("keeping new line inside sequence") {
        val data = """

            key:
            - item1
            - item2
            - item3

            key2: value2

            key3: value3


            """.trimIndent()

        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe data
    }

    test("keeping new line inside sequence 2") {
        val data = """
        apiVersion: kustomize.config.k8s.io/v1beta1
        kind: Kustomization

        namePrefix: acquisition-gateway-

        bases:
        - https://github.intuit.com/dev-patterns/intuit-kustomize//intuit-service-canary-appd-noingress-base?ref=v3.2.0
        - https://github.intuit.com/dev-patterns/intuit-kustomize//intuit-service-rollout-hpa-base?ref=v3.2.0
        # resources:
        # - Nginx-ConfigMap.yaml

        resources:
        - ConfigMap-v1-splunk-sidecar-config.yaml
        - CronJob-patch.yaml

        patchesStrategicMerge:
        - app-rollout-patch.yaml
        - Service-patch.yaml
        - Service-metrics-patch.yaml
        - Hpa-patch.yaml
        #- SignalSciences-patch.yaml

        # Uncomment HPA-patch when you need to enable HPA
        #- Hpa-patch.yaml
        # Uncomment SignalSciences-patch when you need to enable Signal Sciences
        #- SignalSciences-patch.yaml

        """.trimIndent()

        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe data
    }

    test("comments, indent, first line blank") {
        val data = """# Comment 1
key1:
  
  # Comment 2
  # Comment 3
  key2: value1
# "Fun" options
key3:
  # Comment 4
  # Comment 5
  key4: value2
key5:
  key6: value3
"""

        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe data
    }

    test("comments, line blank") {
        val data = """# Comment 1
key1:
  
  # Comment 2

  # Comment 3

  key2: value1
# "Fun" options
key3:
  # Comment 4
  # Comment 5
  key4: value2
key5:
  key6: value3
"""

        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe data
    }

    test("multiline string") {
        val data = """# YAML load and save bug with keep block chomping indicator
example:
  description: |+
    These lines have a carrage return after them.
    And the carrage return will be duplicated with each save if the
    block chomping indicator + is used. ("keep": keep the line feed, keep trailing blank lines.)

successfully-loaded: test
"""
        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe data
    }

    test("100 comments") {
        val commentBuilder = buildString {
            for (i in 0..99) {
                append("# Comment ").append(i).append("\n")
            }
        }
        val data = "" + commentBuilder + "simpleKey: simpleValue\n" + "\n"

        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe data
    }

    test("comments on reference") {
        val data = """dummy: &a test
conf:
- # comment not ok here
  *a #comment not ok here
"""
        val expected = """
            dummy: &a test
            conf:
            - *a

            """.trimIndent()
        val result = runEmitterWithCommentsEnabled(data)
        result shouldBe expected
    }

    test("comments at data window break") {
        val complexConfig = """# Core configurable options for LWC
core:

    # The language LWC will use, specified by the shortname. For example, English = en, French = fr, German = de,
    # and so on
    locale: en

    # How often updates are batched to the database (in seconds). If set to a higher value than 10, you may have
    # some unexpected results, especially if your server is prone to crashing.
    flushInterval: 10

    # LWC regularly caches protections locally to prevent the database from being queried as often. The default is 10000
    # and for most servers is OK. LWC will also fill up to <precache> when the server is started automatically.
    cacheSize: 10000

    # How many protections are precached on startup. If set to -1, it will use the cacheSize value instead and precache
    # as much as possible
    precache: -1

    # If true, players will be sent a notice in their chat box when they open a protection they have access to, but
    # not their own unless <showMyNotices> is set to true
    showNotices: true

    # If true, players will be sent a notice in their chat box when they open a protection they own.
    showMyNotices: false
"""

        val loadSettings =
            LoadSettings.builder().setMaxAliasesForCollections(Int.MAX_VALUE).build()

        val load = Load(loadSettings)
        load.loadAll(complexConfig)
    }

    test("comments in flow mapping") {
        val output: StreamDataWriter = StringOutputStream()
        val emitter = producePrettyFlowEmitter(output)

        emitter.emit(StreamStartEvent(null, null))
        emitter.emit(
            DocumentStartEvent(
                false, null, HashMap(), null,
                null
            )
        )
        emitter.emit(
            MappingStartEvent(
                null, "yaml.org,2002:map", true,
                FlowStyle.FLOW
            )
        )
        emitter.emit(
            CommentEvent(CommentType.BLOCK, " I'm first", null, null)
        )
        val allImplicit = ImplicitTuple(true, true)
        emitter.emit(
            ScalarEvent(
                null, "yaml.org,2002:str", allImplicit,
                "a", ScalarStyle.PLAIN, null, null
            )
        )
        emitter.emit(
            ScalarEvent(
                null, "yaml.org,2002:str", allImplicit,
                "Hello", ScalarStyle.PLAIN, null, null
            )
        )
        emitter.emit(
            ScalarEvent(
                null, "yaml.org,2002:str", allImplicit,
                "b", ScalarStyle.PLAIN, null, null
            )
        )
        emitter.emit(
            MappingStartEvent(
                null, "yaml.org,2002:map", true,
                FlowStyle.FLOW, null, null
            )
        )
        emitter.emit(
            ScalarEvent(
                null, "yaml.org,2002:str", allImplicit,
                "one", ScalarStyle.PLAIN, null, null
            )
        )
        emitter.emit(
            ScalarEvent(
                null, "yaml.org,2002:str", allImplicit,
                "World", ScalarStyle.PLAIN, null, null
            )
        )
        emitter
            .emit(CommentEvent(CommentType.BLOCK, " also me", null, null))
        emitter.emit(
            ScalarEvent(
                null, "yaml.org,2002:str", allImplicit,
                "two", ScalarStyle.PLAIN, null, null
            )
        )
        emitter.emit(
            ScalarEvent(
                null, "yaml.org,2002:str", allImplicit,
                "eee", ScalarStyle.PLAIN, null, null
            )
        )
        emitter.emit(MappingEndEvent(null, null))
        emitter.emit(MappingEndEvent(null, null))
        emitter.emit(DocumentEndEvent(false, null, null))
        emitter.emit(StreamEndEvent(null, null))

        val result = output.toString()
        val data = """{
  # I'm first
  a: Hello,
  b: {
    one: World,
    # also me
    two: eee
  }
}
"""

        result shouldBe data
    }

    test("comment in empty flow mapping") {
        val output: StreamDataWriter = StringOutputStream()
        val emitter = producePrettyFlowEmitter(output)

        emitter.emit(StreamStartEvent(null, null))
        emitter.emit(
            DocumentStartEvent(
                false, null, HashMap(), null,
                null
            )
        )
        emitter.emit(
            MappingStartEvent(
                null, "yaml.org,2002:map", true,
                FlowStyle.FLOW, null, null
            )
        )
        emitter.emit(
            CommentEvent(CommentType.BLOCK, " nobody home", null, null)
        )
        emitter.emit(MappingEndEvent(null, null))
        emitter.emit(DocumentEndEvent(false, null, null))
        emitter.emit(StreamEndEvent(null, null))

        val result = output.toString()
        val data = """{
  # nobody home
}
"""

        result shouldBe data
    }

    test("comments in flow sequence") {
        val output: StreamDataWriter = StringOutputStream()
        val emitter = producePrettyFlowEmitter(output)
        val allImplicit = ImplicitTuple(true, true)

        emitter.emit(StreamStartEvent(null, null))
        emitter.emit(
            DocumentStartEvent(
                false, null, HashMap(), null,
                null
            )
        )
        emitter.emit(
            SequenceStartEvent(
                null, "yaml.org,2002:seq", true,
                FlowStyle.FLOW, null, null
            )
        )
        emitter.emit(CommentEvent(CommentType.BLOCK, " red", null, null))
        emitter.emit(
            ScalarEvent(
                null, "yaml.org,2002:str", allImplicit,
                "one", ScalarStyle.PLAIN, null, null
            )
        )
        emitter.emit(CommentEvent(CommentType.BLOCK, " blue", null, null))
        emitter.emit(
            ScalarEvent(
                null, "yaml.org,2002:str", allImplicit,
                "two", ScalarStyle.PLAIN, null, null
            )
        )
        emitter.emit(SequenceEndEvent(null, null))
        emitter.emit(DocumentEndEvent(false, null, null))
        emitter.emit(StreamEndEvent(null, null))

        val result = output.toString()
        val data = """[
  # red
  one,
  # blue
  two
]
"""

        result shouldBe data
    }

    test("comment in empty sequence") {
        val output = StringOutputStream()
        val emitter = producePrettyFlowEmitter(output)

        emitter.emit(StreamStartEvent(null, null))
        emitter.emit(
            DocumentStartEvent(
                false, null, HashMap(), null,
                null
            )
        )
        emitter.emit(
            SequenceStartEvent(
                null, "yaml.org,2002:seq", true,
                FlowStyle.FLOW, null, null
            )
        )
        emitter.emit(
            CommentEvent(CommentType.BLOCK, " nobody home", null, null)
        )
        emitter.emit(SequenceEndEvent(null, null))
        emitter.emit(DocumentEndEvent(false, null, null))
        emitter.emit(StreamEndEvent(null, null))

        val result = output.toString()
        val data = """[
  # nobody home
]
"""

        result shouldBe data
    }

})
