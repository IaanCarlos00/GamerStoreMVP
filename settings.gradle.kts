pluginManagement {
    repositories {
        // --- ESTA ES LA VERSIÓN SIMPLIFICADA Y CORRECTA ---
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Esto ya lo tenías bien, le dice a la app dónde buscar
        google()
        mavenCentral()
    }
}

rootProject.name = "GamerStoreMVP"
include(":app")