package com.wumpus.speech;

import java.util.List;

public final class Dictionary {
    public enum PerceptKeyword {BREEZE, STENCH, GLITTER, BUMP, SCREAM, NOTHING}

    public enum SpeleologistKeyword {PIT_NEAR, WUMPUS_NEAR, GOLD_NEAR, WALL_NEAR, WUMPUS_KILLED_NEAR, NOTHING}

    public enum ActionKeyword {TURN_LEFT, TURN_RIGHT, GO_FORWARD, GRAB, SHOOT, CLIMB}

    public enum NavigatorKeyword {GO_FORWARD, TURN_LEFT, TURN_RIGHT, SHOOT, GRAB, CLIMB}

    public static List<String> getPerceptKeywords(PerceptKeyword keyword) {
        switch (keyword) {
            case BREEZE:
                return List.of("breeze");
            case STENCH:
                return List.of("stench", "stinky", "smell");
            case GLITTER:
                return List.of("glitter", "shiny");
            case BUMP:
                return List.of("bump", "hit");
            case SCREAM:
                return List.of("scream", "hear");
            case NOTHING:
                return List.of("All clear", "I see nothing", "There is nothing", "I feel nothing", "I hear nothing");
            default:
                throw new IllegalArgumentException("Unknown keyword");
        }
    }

    public static List<String> getSpeleologistKeywords(SpeleologistKeyword keyword) {
        switch (keyword) {
            case PIT_NEAR:
                return List.of("There is a breeze", "I feel breeze", "It's breezy here", "I feel something, like a breeze", "I feel something, like a wind");
            case WUMPUS_NEAR:
                return List.of("There is a stench", "It's stinky here", "I smell something", "I smell something, like a stench", "I smell something, like a stink");
            case GOLD_NEAR:
                return List.of("There is a glitter", "I see something shiny", "It's glittery here", "I see something, like a glitter", "I see something, like a shiny");
            case WALL_NEAR:
                return List.of("There is a bump", "It's bumping here", "I hit the wall", "I feel something, like a bump", "I feel something, like a hit");
            case WUMPUS_KILLED_NEAR:
                return List.of("There is a scream", "It's screaming here", "I hear something", "I hear something, like a scream", "I hear something, like a shout");
            case NOTHING:
                return List.of("There is nothing", "All clear", "I see nothing", "I feel nothing", "I hear nothing");
            default:
                throw new IllegalArgumentException("Unknown keyword");
        }
    }

    public static List<String> getActionKeywords(ActionKeyword keyword) {
        switch (keyword) {
            case TURN_LEFT:
                return List.of("left");
            case TURN_RIGHT:
                return List.of("right");
            case GO_FORWARD:
                return List.of("forward", "ahead", "straight");
            case GRAB:
                return List.of("grab");
            case SHOOT:
                return List.of("shoot");
            case CLIMB:
                return List.of("climb");
            default:
                throw new IllegalArgumentException("Unknown keyword");
        }
    }

    public static List<String> getNavigatorKeywords(NavigatorKeyword keyword) {
        switch (keyword) {
            case GO_FORWARD:
                return List.of("Go forward", "Go straight", "Go ahead", "Go straight ahead");
            case TURN_LEFT:
                return List.of("Turn left", "Turn to the left", "Turn leftwards", "Turn to the leftwards", "You should turn left", "You should turn to the left");
            case TURN_RIGHT:
                return List.of("Turn right", "Turn to the right", "Turn rightwards", "Turn to the rightwards", "You should turn right", "You should turn to the right");
            case SHOOT:
                return List.of("Shoot", "Shoot the Wumpus", "Shoot the monster");
            case GRAB:
                return List.of("Grab", "Grab the gold", "Grab the treasure", "Grab the coins", "Grab the money", "Grab the loot");
            case CLIMB:
                return List.of("Climb", "Climb the ladder", "Climb the stairs");
            default:
                throw new IllegalArgumentException("Unknown keyword");
        }
    }
}
