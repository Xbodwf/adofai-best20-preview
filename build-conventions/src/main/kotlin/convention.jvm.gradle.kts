plugins {
  id("convention.base")
  `java-base`
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
  }
}

tasks {
  withType<Test> {
    useJUnitPlatform()
  }
}