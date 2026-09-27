package de.kaboomstudios.kaboomenchants;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;

/**
 * Einstieg in der Bootstrap-Phase, also bevor der Server Registries einfriert und Welten lädt.
 *
 * <p>Nur hier können neue Verzauberungen in die Registry eingetragen und Vanilla-Tags
 * ergänzt werden. Alles, was erst zur Laufzeit gebraucht wird, gehört in
 * {@link KaBoomEnchants}.</p>
 */
@SuppressWarnings("UnstableApiUsage")
public final class KaBoomEnchantsBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(final BootstrapContext context) {
    }
}
