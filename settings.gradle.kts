pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
}

dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
    maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
}

rootProject.name = "Nebula"
include(":nebula-core")
include(":nebula-protocol")
include(":nebula-protocol-creation")
include(":nebula-protocol-player")
include(":sample")
