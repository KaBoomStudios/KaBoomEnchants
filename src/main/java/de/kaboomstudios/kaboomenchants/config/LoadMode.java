package de.kaboomstudios.kaboomenchants.config;

/** Wie mit einer unlesbaren Datei umgegangen wird. */
public enum LoadMode {

    /**
     * Beim Serverstart: melden und mit den mitgelieferten Werten weitermachen. Ein Tippfehler in
     * der Konfiguration darf den Server nicht am Start hindern; eine Ausnahme in der
     * Bootstrap-Phase würde ihn sofort wieder herunterfahren.
     */
    STARTUP,

    /**
     * Bei {@code /kbe reload}: abbrechen. Der bisherige Stand bleibt aktiv, statt still auf die
     * Standardwerte zu springen und damit etwa alle Sperren zu ändern.
     */
    RELOAD
}
