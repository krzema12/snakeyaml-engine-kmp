# Releasing a new version

1. Change the version in build.gradle.kts by removing the `-SNAPSHOT` suffix, and adjusting the appropriate
   version component (major, minor or patch). If it’s a patch release, the version after removing the suffix
   should be the one you want. Create a commit containing the change, give it a title like `Prepare for
   releasing version 3.2.1`.
1. Run the workflow from the main branch: https://github.com/krzema12/snakeyaml-engine-kmp/actions/workflows/release.yaml,
   and wait until it succeeds.
1. Create a tag from the tip of the main branch, in a format like `v3.2.1`.
1. Change the version in build.gradle.kts so that the next development cycle can start. Bump the patch
   component and add the `-SNAPSHOT` suffix back. Create a commit containing the change, give it a title
   like `Prepare for next dev cycle`.
1. Create a release in GitHub. If this release contains changes ported from snakeyaml-engine, mention which
   version of the upstream project this release reflects.
