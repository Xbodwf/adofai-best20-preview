import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.compose)
  application
}

group = "com.tuforums.preview"
version = "1.0.0"

repositories {
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  google()
}

dependencies {
  implementation(compose.desktop.currentOs)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.serialization.json)
  
  // Preview library modules
  implementation(project(":modules:preview-core"))
  implementation(project(":modules:preview-image"))
  implementation(project(":modules:preview-collage"))
  implementation(project(":modules:preview-api"))
  
  // Compose Desktop (included via currentOs, but explicitly for clarity)
  implementation(compose.ui)
  implementation(compose.foundation)
  implementation(compose.material)
  implementation(compose.runtime)
  implementation(compose.uiTooling)
}

compose.desktop {
  application {
    mainClass = "com.tuforums.preview.ui.MainKt"
    
      nativeDistributions {
      targetFormats(TargetFormat.Msi)
      packageName = "Preview Generator"
      packageVersion = "1.0.0"
      description = "ADOFAI Best 20 Preview Generator"
      copyright = "© 2025"
      
      windows {
        menuGroup = "Preview Generator"
        upgradeUuid = "18159995-d967-4cd2-8885-77BFA97CFA9F"
        dirChooser = true
        
        // 添加Windows特定的配置
        msiPackageVersion = "1.0.0"
      }
    }
  }
}

