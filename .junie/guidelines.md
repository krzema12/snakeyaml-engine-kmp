# Project Guidelines

## Frequent tasks in the project

### Rewriting/porting Java tests to Kotlin

* place a new file in the common source set (`commonTest`). If a given test cannot be placed there because of depending
  on Java API that cannot be replaced with Kotlin API, it should be placed in the `jvmTest` source set, with an explicit
  message to the user. The file's package should start with `it.krzeminski.snakeyaml.engine.kmp` instead of
  `org.snakeyaml.engine.v2`
* delete the legal header from the file
* use kotest, with `FunSpec` API
* place all tests as `test("...")` calls in the `FunSpec` initializer, but any other helper methods should be placed
  as top-level private functions below the test class
* remove any calls to `System.out.println` and any methods that are related to debug-printing
* move test resources as well, if they're used in a given test, to `src/commonTest/resources`
* to load test resources, use `CommonTestResourceAccess.stringFromResources`
* to test cases where something should fail, use `shouldThrow<SomeException> { ... }`, and then use `.also { ... }` to
  assert on the exception message
* if unsure how to proceed with any piece to port, look at other existing tests in Kotlin
* delete the Java class for the test
