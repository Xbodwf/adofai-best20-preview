import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
  id("convention.jvm")
  id("convention.kotlin-mpp")
}

// Android support removed - Windows only

kotlin {
  js {
    useCommonJs()
    browser {
      commonWebpackConfig {
        cssSupport { enabled.set(true) }
        scssSupport { enabled.set(true) }
      }
      testTask { useKarma() }
    }
  }

  jvm()
}
