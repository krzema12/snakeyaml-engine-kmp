package it.krzeminski.snakeyaml.engine.kmp.internal

/**
 * Public because otherwise, KMP cannot find the annotated classes.
 * Not a part of the library's API.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class CopyDsl
