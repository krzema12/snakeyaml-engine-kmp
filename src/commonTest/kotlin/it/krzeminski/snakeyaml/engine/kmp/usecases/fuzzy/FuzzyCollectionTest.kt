package it.krzeminski.snakeyaml.engine.kmp.usecases.fuzzy

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldStartWith
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings

class FuzzyCollectionTest : FunSpec({
    test("fuzzy input") {
        /**
         * https://bitbucket.org/snakeyaml/snakeyaml/issues/1064 This is different from SnakeYAML - the
         * YAML looks valid.
         */
        val datastring = " ? - - ? - - ? ? - - ? ? ? - - ? ? - - ? ? ? - - ? ? - ? ? - - ? - - ? ? ? - - ? ? - ?  -? - ? ? - - ? - - ? ? ? - - ? ? - ?  -? - ? ? - - ? - "

        val settings = LoadSettings.builder()
            .setAllowRecursiveKeys(true)
            .setMaxAliasesForCollections(1000)
            .setAllowDuplicateKeys(true)
            .build()
        val yamlProcessor = Load(settings)
        val fuzzy = yamlProcessor.loadOne(datastring)
        fuzzy.toString().shouldStartWith("{[[{[[{{[[{{{[[{{[[{{{[[{{[{{[[{[[{{{[[{{[{-?")
    }
})
