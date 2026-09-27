plugins {
    // Laedt ein fehlendes JDK der geforderten Version automatisch herunter, damit der Build
    // nicht davon abhaengt, welche Java-Version auf dem jeweiligen Rechner installiert ist.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "KaBoomEnchants"
