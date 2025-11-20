plugins {
  id("convention.kotlin-mpp-tier1")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(kotlin("test"))
        api(libs.kotlinx.coroutines.test)
        api(libs.bundles.kotest.assertions)
      }
    }
    jvmMain {
      dependencies {
        api(kotlin("test-junit5"))
      }
    }

    jsMain {
      dependencies {
        api(kotlin("test-js"))
      }
    }
  }
}