pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
}

plugins {
  id("com.gradle.enterprise") version "3.13"
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

rootProject.name = "adofai-best20-preview"
includeBuild("./build-conventions/")
include(":tests:test-utils")

include(
  ":modules:template-kmp-library-core",
  ":tests:template-kmp-library-core-tests"
)

include(
  ":modules:template-kmp-library-dsl",
  ":tests:template-kmp-library-dsl-tests"
)

include(
  ":modules:preview-api",
  ":modules:preview-image",
  ":modules:preview-collage",
  ":modules:preview-core"
)

//include(":apps:preview-ui")