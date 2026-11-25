# Reloot Mod - Minecraft 1.20.1 (Forge)
**Compatible Solo (Singleplayer) et Serveur Dédié (Multiplayer)**

---

## 🌟 Fonctionnalités Principales

1. **Reloot Universel de Conteneurs** :
   - Fonctionne sur **tous les blocs ouvrables** de Minecraft Vanilla et des Mods : Coffres, Coffres Piégés, Barils, Boîtes de Shulker, Coffres de mods (Iron Chests, etc.).
   - Sauvegarde indépendante des blocs via `RelootSavedData` (aucune corruption de chunks ou de mondes).

2. **6 Paliers de Rareté & Colorisation Automatique** :
   - **Commun** : `§7Gris` (40.0% de chance)
   - **Peu commun** : `§aVert clair` (30.0% de chance)
   - **Rare** : `§bBleu cyan` (18.0% de chance)
   - **Épique** : `§dRose / Violet` (8.5% de chance)
   - **Légendaire** : `§6Or / Doré` (3.0% de chance)
   - **Mythique** : `§4§lRouge foncé gras` (0.5% de chance)
   - Chaque objet généré reçoit automatiquement son **nom coloré** et un **Lore stylisé** avec sa bannière de rareté.

3. **14 Thèmes de Loot Intégrés** :
   - `Hôpital` : Bandages, kits de premiers soins, sérums de régénération.
   - `Police` : Matraques, casques anti-émeute, gilets pare-balles, tasers.
   - `Militaire` : Lames de combat, charges C4, casques thermiques, rations de combat.
   - `Boîte à munitions` : Munitions 9mm, calibre 12, 5.56mm OTAN, .50 BMG, munitions explosives.
   - `Ferme` : Paille, légumes frais, fourches et faux renforcées, miel.
   - `Cuisine` : Plats cuisinés, conserves, ragoûts chauds, rations gastronomiques.
   - `Survivant` : Briquets, boussoles, bottes de marche, cannes à pêche tactiques, parachutes.
   - `Armes à feu (Gun)` : Pistolets, fusils à pompe, fusils d'assaut, fusils de précision sniper.
   - **NOUVEAU** `Magasin de nourriture / Supermarché` : Biscuits, pommes d'étalage, chips en boîte, gâteaux emballés, canettes de soda, chocolat précieux.
   - **NOUVEAU** `Bricolage / Quincaillerie` : Vis et clous, scotch blindé, marteaux, établis, meuleuses, découpeur plasma.
   - **NOUVEAU** `Station-service / Garage` : Bidons d'huile, câbles de démarrage, pinces, batteries 24V, jerricans d'essence.
   - **NOUVEAU** `Pharmacie / Laboratoire` : Alcool à 90°, antibiotiques, stéroïdes, seringues, antidotes anti-zombies.
   - **NOUVEAU** `Chasse & Pêche` : Arbalètes à poulie, harpons, pièges à loups, peaux tannées.
   - **NOUVEAU** `Banque / Coffre-fort` : Liasses de billets, lingots d'or 24K, diamants d'Anvers, lingots de netherite.

4. **Gestion du Temps de Reloot** :
   - Par défaut : **2 Heures** (recharge cyclique).
   - Bouton rapide : **17 Ans** (pour les caches Légendaires / Mythiques qui ne doivent être pillées qu'une seule fois dans la vie du serveur).
   - Choix personnalisables (30 min, 1h, 2h, etc.).
   - Lorsqu'un joueur ouvre un conteneur en cours de recharge, un message rouge et un son de cadenas lui indiquent précisément le temps restant (`ex: 1h 24m 12s`).

5. **Outil d'Administration en Jeu** :
   - **Baguette de Reloot** (`relootmod:reloot_wand`).
   - Clic droit sur n'importe quel conteneur pour ouvrir l'interface graphique de configuration.
   - Sélection du thème en un clic, réglage du temps, sauvegarde ou suppression.

---

## 🛠️ Configuration des Loots (Ajouter des armes et items de mods)

Le fichier `config/relootmod/loot_tables.json` permet d'ajouter **n'importe quel item de mod** (ex: armes de *TACZ*, *MrCrayfish's Gun Mod*, nourriture de *Farmer's Delight*, etc.) sans toucher au code Java.

Exemple :
```json
{
  "GUN": {
    "legendaire": [
      { "id": "tacz:modern_kinetic_gun", "min": 1, "max": 1, "name": "Fusil de Sniper Lourd" }
    ]
  },
  "MAGASIN_NOURRITURE": {
    "commun": [
      { "id": "farmersdelight:cabbage", "min": 2, "max": 6, "name": "Chou Frais Emballé" }
    ]
  }
}
```

---

## 🚀 Utilisation sur CurseForge

1. Sur CurseForge, ouvre ton profil ou modpack en **Forge 1.20.1**.
2. Clique sur les trois petits points `...` à côté de Jouer -> **Ouvrir le dossier du profil**.
3. Place le fichier `.jar` du mod dans le dossier `mods/`.
4. Lance le jeu en Solo ou démarre ton serveur !
5. Prends la baguette dans l'onglet *Outils* du mode créatif ou via `/give @s relootmod:reloot_wand`.
