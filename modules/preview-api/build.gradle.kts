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
        implementation(libs.ktor.client.core)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.ktor.serialization.kotlinx.json)
        implementation(libs.kotlinx.serialization.json)
      }
    }

    /*
    val jvmMain by creating {
      dependsOn(commonMain.get())
      dependencies {
        implementation(libs.ktor.client.cio)
      }
    }
    
    val mingwMain by creating {
      dependsOn(commonMain.get())
      dependencies {
        implementation(libs.ktor.client.cio)
      }
    }
    
    val jsMain by creating {
      dependsOn(commonMain.get())
      dependencies {
        implementation(libs.ktor.client.js)
      }
    }
     */
  }
}
