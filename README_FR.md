# Windy: Configurable — Forge 1.18.2 port (experimental)

Adaptation du JAR `Windy_Forge-1.20.1-v1.2.1.jar` pour Minecraft **1.18.2 / Forge 40.3.12**.

**Important :** portage *fonctionnellement simplifié*, et non portage exact du code original. Les textures des particules proviennent du JAR LGPL-3.0 fourni. La logique Java a été réécrite pour 1.18.2 avec les événements Forge et ne requiert pas Mixin.

Fonctions prévues : particules wind/strong_wind, influence météo, configuration de densité et des altitudes, accès au ciel. La configuration détaillée par biome, intégration LivingWind et écran de configuration de la version 1.20.1 ne sont pas encore reproduits.

## Compilation

1. Envoie le contenu de cette archive **à la racine** d'un dépôt GitHub.
2. Actions > Build Windy Forge 1.18.2 > Run workflow (ou push sur main).
3. Télécharge l'artifact Windy-Forge-1.18.2 et utilise son JAR dans `mods/` du client Minecraft.
4. Paramètres : `config/windy-1.18.2.properties` après premier démarrage.

Aucun JAR n'a été compilé ni testé dans Minecraft dans cet environnement. Signaler la première erreur `error:` si GitHub échoue.

Crédits : BonfireMC et Pathmakers Studio. Licence LGPL-3.0-only, consulter les auteurs originaux.
