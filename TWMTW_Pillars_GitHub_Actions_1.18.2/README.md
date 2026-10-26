# TWMTW Pillars – Forge 1.18.2

Portage expérimental du générateur de piliers rocheux de TWMTW, inspiré de BWG, avec les tailles d'origine mais une forme procédurale approximative.

## Compiler sans installer Gradle (GitHub Actions)

1. Crée un dépôt GitHub vide (public ou privé), sans README généré.
2. Sur sa page, utilise **Add file → Upload files**, puis dépose **le CONTENU extrait de ce ZIP** à la racine du dépôt, y compris le dossier `.github` (dossier caché). Sinon passe par GitHub Desktop pour importer tous les fichiers.
3. Clique sur **Commit changes**.
4. Ouvre l'onglet **Actions** et sélectionne **Build TWMTW Pillars (Forge 1.18.2)**. Si le push a déjà démarré le build, attends le résultat; sinon clique **Run workflow**.
5. Si le build réussit, clique sur l'exécution, rubrique **Artifacts**, et télécharge **TWMTW-Pillars-Forge-1.18.2**. Dézippe l'archive d'artifacts pour trouver le `.jar`.
6. Si le build échoue, ouvre l'étape **Build and reobfuscate Forge mod**, copie la fin des erreurs et partage-les. Le projet n'a pas été compilé/testé dans Minecraft à ce stade.

## À ne pas installer prématurément

- Le datapack `TWMTW_Islands_Pillars_1.18.2_DATAPACK.zip` dépend du mod: conserver FIX 3 tant que le `.jar` n'est pas compilé.
- Garder Tectonic 2.3.5a et Regions Unexplored activés.
- Tester dans un nouveau monde, avec une copie de sauvegarde.

## Notes techniques

- Minecraft 1.18.2, Forge 40.3.12, JDK 17, Gradle 7.6.1 et ForgeGradle 5.1.
- Le générateur est une réimplémentation expérimentale, pas l'algorithme BWG exact.
- Il n'est pas garanti que le premier build réussisse; utiliser le journal Actions pour corriger les incompatibilités Java/Forge.
