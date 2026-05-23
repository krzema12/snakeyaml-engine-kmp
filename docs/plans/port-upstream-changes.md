# Plan: Port upstream snakeyaml-engine changes to KMP

## Last analyzed commit

```
be40e57ffc1b5bac2d485d77c1af2da7e35adc7d
```

## Upstream commits since then (graph)

```
* 5503e17 Issue 80: Fix L24T test in test suite
* 8838fbf Issue 80: fix DK95 in test suite
* 4ce68cb Issue 80: fix test code for the test suite
* edae5eb Issue 80: update the test suite to data-2022-01-17
* 75442fd Add test for Issue 79: fix test
* f90c8cf Add test for Issue 79: float values may loose precision
*   37a9288 Merge pull request #3 from altro3/update-libs
|\  
| * 23a1bd9 Updating plugin versions. Updating lib versions.
|/  
* 655f721 Update changes
*   d6cc5db Merge branch 'master' into issue-68
|\  
| *   68d31df Merge branch 'master' into issue-75
| |\  
| | * 8a30b31 Update changes
| | * cedf62b handle boundary condition related to surrogate chars
* | | bc68379 Fix issue 68 when comments after anchor/tag
|/ /  
* | c1d4d3c Allow 0x7F in quoted scalar
* | e63aa32 Merge branch 'master' into issue-75
|\| 
| *   9c726fa Merge remote-tracking branch 'origin/master' into issue-76-parse-json
| |\  
| | * 623230a Ignore CLAUDE.md
| * | 9810a28 Fix parsing of JSON with multiple consecutive TABs
| |/  
| * 5f7eff2 Update version for JDK 11
| * 0ed8f4f Update JUnit
| * c463a58 Update maven-resources-plugin
| * 93608b6 Update some Maven plugins
* | 2c85c8c Allow 7F
|/  
* 06b1c92 Add a test for issue 75
* 2070eb4 [maven-release-plugin] prepare for next development iteration
* db85065 [maven-release-plugin] prepare release snakeyaml-engine-3.0.1
* 243f992 Prepare 3.0.1
* 37a144f Update changes
* e8c75a9 Issue 74: Refactor packages to run test with module-info.java
* 0280e0e Add module-info.java
*   33680c3 Merge remote-tracking branch 'origin/master'
|\  
| * 636de54 [maven-release-plugin] prepare for next development iteration
| * 8bebbda [maven-release-plugin] prepare release snakeyaml-engine-3.0
| * c2eb39a Update changes
* | b3b6627 Remove unused cobertura plugin
|/  
*   84e55e7 Merge branch 'arrays-test'
|\  
| * 957a804 Improve test coverage for StandardRepresenter (for Set)
| * 9a01a3c Improve test coverage for StandardRepresenter
| * 7a1ecac Minor improvement for Java 11
| * 7abcc03 Cover by tests arrays of primitives
| * 555467d Reformat test
* | 5ea32df Reformat test
|/  
* 6b27f3e The next version will be 3
* 8876c5c Issue 72: Enforce mapping keys to be scalars
```

## All commits analysis

| # | Commit | Worth porting | Reason |
|---|--------|:---:|--------|
| 1 | `8876c5c` | YES | Enforce mapping keys to be scalars (Composer + LoadSettings API) |
| 2 | `6b27f3e` | NO | Version bump to 3 |
| 3 | `5ea32df` | NO | Reformat test (whitespace) |
| 4 | `555467d` | NO | Reformat test (whitespace) |
| 5 | `7abcc03` | NO | Test-only: arrays of primitives |
| 6 | `7a1ecac` | NO | Java 11 minor cleanup (isEmpty()) |
| 7 | `9a01a3c` | YES | Remove byte[] special handling in StandardRepresenter |
| 8 | `957a804` | NO | Test-only: Set coverage |
| 9 | `84e55e7` | NO | Merge commit |
| 10 | `b3b6627` | NO | Build: remove cobertura plugin |
| 11 | `c2eb39a` | NO | Changelog |
| 12 | `8bebbda` | NO | Release preparation |
| 13 | `636de54` | NO | Next dev iteration |
| 14 | `33680c3` | NO | Merge commit |
| 15 | `0280e0e` | NO | module-info.java (JPMS, irrelevant for KMP) |
| 16 | `e8c75a9` | NO | JPMS package refactoring |
| 17 | `37a144f` | NO | Changelog |
| 18 | `243f992` | NO | Version bump |
| 19 | `db85065` | NO | Release preparation |
| 20 | `2070eb4` | NO | Next dev iteration |
| 21 | `06b1c92` | NO | Test-only: issue 75 |
| 22 | `2c85c8c` | YES | Allow 0x7F in printable range (StreamReader) |
| 23 | `93608b6` | NO | Build: Maven plugins |
| 24 | `c463a58` | NO | Build: maven-resources-plugin |
| 25 | `0ed8f4f` | NO | Build: JUnit version |
| 26 | `5f7eff2` | NO | Build: JDK 11 version |
| 27 | `623230a` | NO | .gitignore |
| 28 | `9810a28` | YES | Fix JSON with multiple consecutive TABs (ScannerImpl) |
| 29 | `9c726fa` | NO | Merge commit |
| 30 | `e63aa32` | NO | Merge commit |
| 31 | `c1d4d3c` | YES | YAML 1.2 0x7F compliance in quoted/plain/comment/block (ScannerImpl) |
| 32 | `bc68379` | YES | Fix comments after anchor/tag (ParserImpl) |
| 33 | `cedf62b` | YES | Surrogate char boundary bug fix (StreamReader) |
| 34 | `8a30b31` | NO | Changelog |
| 35 | `68d31df` | NO | Merge commit |
| 36 | `d6cc5db` | NO | Merge commit |
| 37 | `655f721` | NO | Changelog |
| 38 | `23a1bd9` | NO | Build: plugin/lib versions |
| 39 | `37a9288` | NO | Merge commit |
| 40 | `f90c8cf` | NO | Test-only: issue 79 (float precision, no production fix) |
| 41 | `75442fd` | NO | Test-only: fix issue 79 test |
| 42 | `edae5eb` | NO | Test data: update test suite |
| 43 | `4ce68cb` | NO | Test infrastructure: SuiteUtils changes |
| 44 | `8838fbf` | YES | Fix DK95: tab handling in block context (ScannerImpl) |
| 45 | `5503e17` | YES | Fix L24T: final newline in block scalars (ScannerImpl) |

**Total: 9 commits worth porting**

## Proposed PRs

### PR 1: Fix Issue 75 - Character handling / YAML 1.2 printable range compliance

Ports improvements to character handling to comply with YAML 1.2 spec for the 0x7F (DEL) character and surrogate pair boundary conditions.

| Commit | GitHub link | Files touched | Description |
|--------|-------------|---------------|-------------|
| `cedf62b` | [link](https://github.com/snakeyaml/snakeyaml-engine/commit/cedf62b) | `StreamReader.java` | Handle boundary condition related to surrogate chars |
| `2c85c8c` | [link](https://github.com/snakeyaml/snakeyaml-engine/commit/2c85c8c) | `StreamReader.java` | Allow 0x7F in printable range |
| `c1d4d3c` | [link](https://github.com/snakeyaml/snakeyaml-engine/commit/c1d4d3c) | `ScannerImpl.java` | Allow 0x7F in quoted scalar, reject in plain/comment/block |

**Rationale**: All three commits are part of the same issue (Issue 75). They are complementary: `cedf62b` and `2c85c8c` fix `StreamReader` to properly handle character boundaries and the 0x7F character, while `c1d4d3c` fixes `ScannerImpl` to correctly allow 0x7F in quoted scalars but reject it in plain scalars, comments, and block contexts.

---

### PR 2: Fix Issue 80 - YAML test suite compliance (DK95 + L24T)

Ports two fixes needed to pass the updated YAML test suite (data-2022-01-17).

| Commit | GitHub link | Files touched | Description |
|--------|-------------|---------------|-------------|
| `8838fbf` | [link](https://github.com/snakeyaml/snakeyaml-engine/commit/8838fbf) | `ScannerImpl.java` | Fix DK95: handle tabs in block context as separator whitespace |
| `5503e17` | [link](https://github.com/snakeyaml/snakeyaml-engine/commit/5503e17) | `ScannerImpl.java` | Fix L24T: ensure final newline in block scalars per YAML spec |

**Rationale**: Both are logic changes in `ScannerImpl` needed to pass the YAML test suite (Issue 80). They are independent fixes but both relate to the same test suite compliance effort.

---

### PR 3: Fix Issue 68 - Comments after anchor/tag

| Commit | GitHub link | Files touched | Description |
|--------|-------------|---------------|-------------|
| `bc68379` | [link](https://github.com/snakeyaml/snakeyaml-engine/commit/bc68379) | `ParserImpl.java` | Fix issue 68 when comments after anchor/tag |

**Rationale**: Self-contained fix adding new production states in `ParserImpl` to handle comments appearing after anchor/tag.

---

### PR 4: Fix Issue 72 - Enforce mapping keys to be scalars

| Commit | GitHub link | Files touched | Description |
|--------|-------------|---------------|-------------|
| `8876c5c` | [link](https://github.com/snakeyaml/snakeyaml-engine/commit/8876c5c) | `Composer.java`, `LoadSettings.java`, `LoadSettingsBuilder.java` | Enforce mapping keys to be scalars with `allowNonScalarKeys` setting |

**Rationale**: Adds a new API option `allowNonScalarKeys` to `LoadSettings` (default `true` for backward compatibility). Logic change in `Composer` to enforce this setting.

---

### PR 5: Fix Issue 76 - JSON parsing with multiple consecutive TABs

| Commit | GitHub link | Files touched | Description |
|--------|-------------|---------------|-------------|
| `9810a28` | [link](https://github.com/snakeyaml/snakeyaml-engine/commit/9810a28) | `ScannerImpl.java` | Fix parsing of JSON with multiple consecutive TABs |

**Rationale**: Self-contained fix changing `if` to `while` in `ScannerImpl` to correctly skip multiple consecutive TABs in JSON parsing context.

---

### PR 6: StandardRepresenter - Remove byte[] special handling

| Commit | GitHub link | Files touched | Description |
|--------|-------------|---------------|-------------|
| `9a01a3c` | [link](https://github.com/snakeyaml/snakeyaml-engine/commit/9a01a3c) | `StandardRepresenter.java` | Remove special byte[] array handling in StandardRepresenter |

**Rationale**: Simplifies `StandardRepresenter` by removing special-case handling for `byte[]` arrays, letting them fall through to the generic array representation.
