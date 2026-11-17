# Trésors Sauvages — Forge 1.20.1

## Fonctionnalités
- Coffres placés naturellement à la surface lors de la génération des chunks.
- Quatre raretés : Commun, Rare, Épique et Légendaire.
- Les coffres communs sont très majoritaires ; les légendaires sont extrêmement rares.
- Un coffre vide peut récupérer son butin après un délai, avec une chance de réussite.
- Le contenu est tiré aléatoirement dans une réserve propre à chaque rareté.

## Fréquences actuelles
- Commun : 84,5 %
- Rare : 12 %
- Épique : 3 %
- Légendaire : 0,5 %
- Environ 1 chunk sur 12 reçoit un coffre (avec une petite chance d'en recevoir deux).

## Reloot
Après 20 minutes de jeu, un coffre vide a 35 % de chances de se recharger lorsqu'un joueur clique dessus.
En cas d'échec, une nouvelle tentative est possible 5 minutes plus tard.

## Installation / compilation
1. Installer Java 17.
2. Ouvrir ce dossier avec IntelliJ IDEA ou Eclipse en projet Gradle.
3. Laisser Gradle télécharger Forge 1.20.1-47.2.0.
4. Lancer `gradle build`.
5. Le mod compilé se trouve dans `build/libs/`.
6. Copier le fichier JAR dans le dossier `mods` de Minecraft Forge 1.20.1.

Ce projet fournit les sources. Il ne contient pas de JAR précompilé.
