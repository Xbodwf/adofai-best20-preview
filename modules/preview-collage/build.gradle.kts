plugins {
  id("convention.kotlin-mpp-tier3")
  id("convention.library-mpp")
  id("convention.publishing-mpp")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
        api(project(":modules:preview-image"))
        api(project(":modules:preview-api"))
      }
    }
  }
}
