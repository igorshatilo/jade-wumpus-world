package com.wumpus.core;

public class WumpusPercept extends wumpusworld.core.environment.wumpusworld.WumpusPercept {
    public static WumpusPercept fromString(String percept) {
        WumpusPercept wp = new WumpusPercept();
        String[] percepts = processString(percept);

        for (String p : percepts) {
            if (p.equals("Stench")) {
                wp.setStench();
            } else if (p.equals("Breeze")) {
                wp.setBreeze();
            } else if (p.equals("Glitter")) {
                wp.setGlitter();
            } else if (p.equals("Bump")) {
                wp.setBump();
            } else if (p.equals("Scream")) {
                wp.setScream();
            }
        }
        return wp;
    }

    private static String[] processString(String inputString) {
        String cleanedString = inputString.replace("{", "").replace("}", "");

        String[] array = cleanedString.split(", ");

        return array;
    }
}
