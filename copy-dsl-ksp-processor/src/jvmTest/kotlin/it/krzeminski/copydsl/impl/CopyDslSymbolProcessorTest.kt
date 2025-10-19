package it.krzeminski.copydsl.impl

import com.lordcodes.turtle.shellRun
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@OptIn(ExperimentalCompilerApi::class)
class CopyDslSymbolProcessorTest : FunSpec({
    test("success case") {
        // Given
        val source = SourceFile.kotlin(
            name = "Main.kt",
            contents = $$"""
                import it.krzeminski.copydsl.api.CopyDsl

                @CopyDsl
                class MyClass(
                    val foo: String,
                    val bar: Int?,
                    val baz: Long = 5,
                )

                fun main() {
                    val myObject = MyClass(
                        foo = "Goo",
                        bar = 123,
                        baz = 456L,
                    )
                    val newObject = myObject.copy {
                        foo = "Zoo"
                        bar = 789
                        baz = 101L
                    }
                    println("myObject.foo: ${myObject.foo}")
                    println("myObject.bar: ${myObject.bar}")
                    println("myObject.baz: ${myObject.baz}")
                    println("newObject.foo: ${newObject.foo}")
                    println("newObject.bar: ${newObject.bar}")
                    println("newObject.baz: ${newObject.baz}")
                }
            """.trimIndent(),
        )
        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            inheritClassPath = true

            configureKsp {
                symbolProcessorProviders.add(CopyDslSymbolProcessorProvider())
            }
        }

        // When
        val result = compilation.compile()

        // Then
        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
        val jvmClassFile = result.generatedFiles
            .first { it.extension == "class" }
            .parentFile
        println("jvmClassFile: $jvmClassFile")
        println("jvmClassFile.absoluteFile: ${jvmClassFile.absoluteFile}")
        println("jvmClassFile.absolutePath: ${jvmClassFile.absolutePath}")
        println("jvmClassFile.absoluteFile.invariantSeparatorsPath: ${jvmClassFile.absoluteFile.invariantSeparatorsPath}")
        println("Files inside:")
        jvmClassFile.walkTopDown().forEach { println("- $it") }
        val classpathSeparator = if (":" in jvmClassFile.absolutePath) ";" else ":"
        val args = listOf(
            "-cp",
            listOf(jvmClassFile.absolutePath, compilation.kotlinStdLibJar!!.absolutePath).joinToString(classpathSeparator),
            "MainKt",
        )
        println("Args: $args")
        val stdout = shellRun(
            "java",
            args,
        )
        stdout.replace("\r\n", "\n") shouldBe """
            myObject.foo: Goo
            myObject.bar: 123
            myObject.baz: 456
            newObject.foo: Zoo
            newObject.bar: 789
            newObject.baz: 101
        """.trimIndent()
    }
})
