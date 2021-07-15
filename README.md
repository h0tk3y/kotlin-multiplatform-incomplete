# Kotlin Multiplatform Incomplete Libs

This repository is a proof of concept for publishing a multiplatform library with some of its targets not contained in
the core library codebase – therefore the library is called "incomplete". The implementations ("`actual`s") for those
targets are provided by some other project – a "completing" module. 

The "consumer" library demonstrates using the incomplete library in one of the targets and the completing library's
implementations for the other targets.

## How to build

The libraries in the repo depend on each other's published artifacts.
So, to build all the modules in the correct order, follow these steps:

1. In `incomplete-lib`: `./gradlew publish`
2. In `completing-lib`: `./gradlew publish`
3. In `consumer-lib`: `./gradlew publish`

The builds will publish the artifacts in the repository root's `build/repo`.

## Working with IDE

* Import `incomplete-lib`, `completing-lib`, and `consumer-lib` as Gradle projects. 

* Don't import `incomplete-lib` and `completing-lib` in the same IntelliJ project, as the IDE will fail to include the
  `incomplete-lib`'s common sources into both projects (resulting into *missing `expect`* in `completing-lib`). Import
  the projects into two different IDE instances instead.
  
* The `incomplete-lib` will report missing `actual`s for JVM and JS; this is expected (and probably needs some
  workaround to get rid of the reported errors).

## How this works

All of the build customizations in this repo are in one of the two categories:

* Build hacks (putting the right sources or dependencies where they are needed, disabling some stuff);
* Publishing and reading additional metadata in the artifacts, which allows the consumer to check that the incomplete
  library was provided with a matching implementation.

All non-trivial build customizations are done in the `gradle-plugins`, which get applied in the library builds.

1. `incomplete-lib`:

    The Android target gets the implementation of the `expect` API right inside the `incomplete-lib`.

    We also define the targets JVM and JS, which won't have actual implementations inside `incomplete-lib`. Then, for these
    two targets, the plugin:

    * disables the platform compilations, so that the compilation tasks don't fail because of missing `actual`s;
    * include the incomplete library markers in the (otherwise empty) platform artifacts, so that the consumer can find out
      that there is an incomplete library on the classpath that needs a completing library

2. `completing-lib`:

    We define just the two targets JVM and JS (it would be OK to implement them separately, even each in a single-platform
    project, too, but here they are in a single Multiplatform project). Then the plugin does the following:

    * create a new source set `incompleteMain`, which imports the sources of `incomplete-lib` into `completing-lib`;
    * disable the compilation of `incompleteMain` to Kotlin Metadata (KLIB), because it's already compiled to Kotlin
      Metadata by `incomplete-lib`, and `completing-lib` should not redistribute the symbols of `incomplete-lib` in the
      common artifacts again;
    * add a dependency on `incomplete-lib` to `incompleteMain`, so that the project also receives the dependencies of
      `incomplete-lib` as transitive ones and compiles against them;
    * make `commonMain` depend on `incompleteMain`, so all targets must provide the actual implementations;
    * include the completing library markers into the platform artifacts of the two targets, so that the consumer can find
      them on the classpath and match them with the incomplete library's markers, verifying that the implementation is
      there;

3. `consumer-lib`:

    This is a consumer project that is written agains the incomplete library with the implementations provided by the
    completing library attached.

    The source set `commonMain` depends on just `incomplete-lib`, and its code can reference the common API of
    `incomplete-lib`. The JVM and JS targets get a dependency on `completing-lib` (their shared source set `jvmAndJsMain`
    can also use the common API from the `completing-lib`).

    The plugin checks the incomplete and completing library markers on the classpath and reports missing completing 
    libraries. You can remove the dependency on `completing-lib` from `jvmAndJsMain` (or move it to one of the 
    platform-specific source sets) to see the plugin reporting an error about an unsatisfied incomplete library. 

## Project presets

It is possible to make one more plugin that implements a preset for user libraries (like `consumer-lib`) or 
applications written against an incomplete framework and its external completing implementations. The plugin would
automatically create all the supported targets, like Android, JVM Desktop, and JS. However, a user might want to write
just common code with no `expect`s and `actual`s. In that case, the plugin could remove the source directories from the
platform source sets, so that the IDE doesn't suggest creating them.
