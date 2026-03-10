import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  jvm("desktop")

  sourceSets {
    val desktopMain by getting {
      dependencies {
        implementation(project(":nebula-core"))
        implementation(compose.desktop.currentOs)
        implementation(compose.material3)
        implementation(compose.foundation)
        implementation(compose.ui)
        implementation(compose.runtime)
        implementation(libs.kotlinx.serialization.json)
      }
    }
  }
}

compose.desktop {
  application {
    mainClass = "io.github.androidpoet.nebula.sample.MainKt"
    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "Nebula Sample"
      packageVersion = "1.0.0"
    }
  }
}
