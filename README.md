# KaBoomEnchants - Custom Minecraft Enchantments Plugin

Ein leichtgewichtiges, performantes Spigot-Plugin für Minecraft (Java), das dem Spiel maßgeschneiderte und einzigartige Verzauberungen hinzufügt.

## Motivation und Hintergrund

Dieses Projekt wurde von mir gestartet, um mein theoretisches Java-Wissen aus der Umschulung sofort praktisch anzuwenden und zu vertiefen. Gleichzeitig bietet es mir die perfekte Gelegenheit, die internen Mechaniken, APIs und die Struktur meines Lieblingsspiels Minecraft besser kennenzulernen.

Die Entwicklung dieses Plugins fordert mich heraus, mich eigenständig in komplexe, fremde Code-Bibliotheken einzuarbeiten und reale, logische Programmierprobleme im Spielkontext zu lösen.

---

## Features

* **Custom Enchantment 1: Bohrer**
  Erlaubt es Spielern, mit Spitzhacken oder Schaufeln größere Bereiche (z. B. 3x3) abzubauen. Die Abbautiefe skaliert dynamisch mit der Stufe der Verzauberung und passt sich automatisch der Blickrichtung des Spielers an.

* **Custom Enchantment 2: Baumesser (In Arbeit)**
  Eine spezielle Verzauberung für Äxte, die das Fällen ganzer Bäume erleichtert, indem alle verbundenen Stammblöcke über einen Breitensuche-Algorithmus (BFS) im Event-Handler erfasst und abgebaut werden.

* **Event-Driven Architecture**
  Nutzung des Spigot Event-Systems zur performanten Erkennung von Spieler- und Blockaktionen.

* **Sichere Datenhaltung**
  Verwendung des PersistentDataContainer (PDC) von Bukkit, um Verzauberungsdaten sicher, unsichtbar und robuster auf den Items zu speichern (kein Verlust durch Umbenennungen).

---

## Tech-Stack

* Programmiersprache: Java (JDK 21)
* API: Paper / Spigot API (1.21.x)
* Build-System: Maven (Dependency Management)
* Versionierung: Git & GitHub

---

## Projektstatus: Work in Progress

Dieses Plugin befindet sich in der aktiven Entwicklung.

Aktuelle Meilensteine:
- [x] Projekt-Setup und Maven-Strukturierung mit Paper-API
- [x] Entwicklung des "Bohrer"-Enchantments (inkl. Level-Kombination am Amboss)
- [x] Integration in den Zaubertisch (EnchantmentTable) und Dorfbewohner-Handel (VillagerTrades)
- [x] Behebung des Z-Achsen-Suchfehlers bei der Baumesser-Suche
- [ ] Integration einer Konfigurationsdatei (config.yml) zur dynamischen Werteanpassung

---

## Kern-Lerneffekte in diesem Projekt

Durch die eigenständige Entwicklung dieses Projekts habe ich gelernt:
1. Systematische Einarbeitung in eine umfangreiche, ereignisgesteuerte API (Spigot/Paper).
2. Anwendung von Algorithmen (Breitensuche / BFS) auf dreidimensionale Blockgitter.
3. Sicheres Exception- und Null-Pointer-Handling bei der Verarbeitung von Item-Metadaten.
4. Versionskontrolle komplexer Java-Projekte mittels Git.
