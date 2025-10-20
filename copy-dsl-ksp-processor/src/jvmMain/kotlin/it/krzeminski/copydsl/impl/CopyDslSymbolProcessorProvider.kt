package it.krzeminski.copydsl.impl

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class CopyDslSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        CopyDslSymbolProcessor(
            codeGenerator = environment.codeGenerator,
            copyAnnotationFqn = environment.options["copy-annotation-fqn"]
                ?: error("Missing 'copyAnnotationFqn' processor option!"),
        )
}
