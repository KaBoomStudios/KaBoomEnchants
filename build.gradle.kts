// Ohne diesen Import zeigt "java" in dieser Datei auf die Java-Erweiterung von Gradle
// und nicht auf das gleichnamige Paket der Standardbibliothek.
import java.util.Properties

plugins {
    id("java")
    // Startet einen echten Paper-Testserver ueber die Aufgabe "runServer".
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

group = "de.kaboomstudios"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
}

dependencies {
    // Fest auf den Build genagelt, der auch auf dem Server laeuft: so kompiliert das Plugin
    // gegen genau die API, gegen die es spaeter ausgefuehrt wird. Das ist hier besonders
    // wichtig, weil die Registry-API fuer Verzauberungen als experimentell markiert ist.
    // Seit 26.1 lautet das Versionsschema {VERSION}.build.{NUMMER}-{stable|beta|alpha};
    // das frueher uebliche -R0.1-SNAPSHOT gibt es nicht mehr.
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.74-stable")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 25
}

tasks.processResources {
    // Die Plugin-Version steht nur an einer Stelle, naemlich oben in dieser Datei.
    // ${version} in der paper-plugin.yml wird beim Bauen daraus gefuellt.
    val buildProperties = mapOf("version" to project.version.toString())
    inputs.properties(buildProperties)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(buildProperties)
    }
}

// Der Pfad zum Testserver gehoert nicht ins Repository, weil der Quelltext veroeffentlicht wird.
// Er steht in gradle-local.properties, die in der .gitignore aufgefuehrt ist. Fehlt die Datei
// (etwa auf einem frisch geklonten Arbeitsplatz), laeuft der Server im Ordner "run" im Projekt.
// Das Einlesen geschieht zur Konfigurationszeit und wird vom Konfigurations-Cache nicht ueberwacht;
// bei einer Datei, die sich praktisch nie aendert, ist das vertretbar.
val localBuildSettings = Properties().apply {
    val file = rootProject.file("gradle-local.properties")
    if (file.isFile) file.inputStream().use(::load)
}
val testServerDirectory = localBuildSettings.getProperty("kbenchants.runDirectory") ?: "run"

// Weitere Plugins fuer den Testserver, etwa ein Belohnungs-Plugin fuer einen gemeinsamen Test.
// Sie werden beim Start eingebunden, ohne im plugins-Ordner des Servers zu liegen. Die Pfade sind
// ebenfalls rechnerbezogen und stehen deshalb in gradle-local.properties, mehrere durch Komma getrennt.
val extraPluginJars = localBuildSettings.getProperty("kbenchants.extraPluginJars")
    ?.split(',')?.map(String::trim)?.filter(String::isNotEmpty).orEmpty()

tasks.runServer {
    minecraftVersion("26.1.2")
    runDirectory = file(testServerDirectory)
    pluginJars.from(extraPluginJars)
}
