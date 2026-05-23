---
description: Port changes from snakeyaml-engine project (this project's upstream project, written in Java).
---

1. Check what changes were made in the upstream project since we last ported changes. To do it:
   * clone https://bitbucket.org/snakeyaml/snakeyaml-engine.git - called further as upstream project. Do it into a 'tmp'
     dir in the project's dir
   * from this project (snakeyaml-engine-kmp), check the value of file `latest-analyzed-upstream-commit.txt`
   * check which commits were made since the latest analyzed commit, until current `master` of the upstream project
   * display the commits to the user in the agent's TUI, as a tree using `git log --graph`

2. For each commit, infer if this change is actually worth porting to the Kotlin Multiplatform version (this project).
   You need to check the diff of each commit. Usually it's worth porting when it contains logic/test change. Filter out
   commits that e.g. bump version or configure build system, or are in another way irrelevant in the context of porting.
   Present a list of all commits, as a table, with a column showing a green "YES" if it's a candidate for porting or
   a red "NO" if it's not.

3. For commits worth porting, cluster them into logical changes that e.g. fix a given issue (there may be a commit with)
   tweaking some tests first, then the actual fix, then some refactoring). Think: we need to figure out which commits
   after porting should go into logical PRs to snakeyaml-engine-kmp.
