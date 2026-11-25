package com.relootmod.data;

public enum LootTheme {
    HOPITAL("Hôpital", "Soins, trousses médicales et bandages"),
    POLICE("Police", "Gilets pare-balles, menottes et pistolets"),
    MILITAIRE("Militaire", "Armures tactiques, explosifs et fusils d'assaut"),
    MUNITIONS("Boîte à munitions", "Cartouches, chargeurs et flèches"),
    FERME("Ferme", "Nourriture agricole, graines et outils"),
    CUISINE("Cuisine", "Rations chaudes, plats préparés et conserves"),
    SURVIVANT("Survivant", "Kits de survie, torches, canifs et boussoles"),
    GUN("Armes à feu", "Armes diverses, pistolets et snipers"),
    MAGASIN_NOURRITURE("Magasin de nourriture", "Supermarché, sodas, conserves, snacks et gâteaux"),
    BRICOLAGE("Bricolage / Quincaillerie", "Clous, vis, câbles, marteaux et métaux"),
    STATION_SERVICE("Station-service / Garage", "Essence, batteries, huile et pièces mécaniques"),
    PHARMACIE("Pharmacie / Labo", "Comprimés, seringues, antibiotiques et fioles"),
    CHASSE_PECHE("Chasse & Pêche", "Arbalètes, fusils de chasse, cannes et gibier"),
    BANQUE("Banque / Coffre-fort", "Lingots d'or, diamants, liasses de billets et bijoux");

    private final String displayName;
    private final String description;

    LootTheme(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static LootTheme fromString(String name) {
        for (LootTheme theme : values()) {
            if (theme.name().equalsIgnoreCase(name)) {
                return theme;
            }
        }
        return MAGASIN_NOURRITURE;
    }
}
