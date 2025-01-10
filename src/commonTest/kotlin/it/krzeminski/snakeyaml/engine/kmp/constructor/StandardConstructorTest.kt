package it.krzeminski.snakeyaml.engine.kmp.constructor

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose
import it.krzeminski.snakeyaml.engine.kmp.stringFromResources

class StandardConstructorTest: FunSpec({
    test("construct merge example") {
        val compose = Compose(LoadSettings.builder().build())
        val node = compose.compose(stringFromResources("/load/list1.yaml"))
        node.shouldNotBeNull()
        val constructor = StandardConstructor(LoadSettings.builder().build())
        val obj = constructor.construct(node)
        obj.shouldNotBeNull()
    }
})
