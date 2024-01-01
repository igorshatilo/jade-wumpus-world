package com.wumpus.speech;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import wumpusworld.core.environment.wumpusworld.WumpusAction;
import wumpusworld.core.environment.wumpusworld.WumpusPercept;

public class SpeleologistSpeech {
    private final Random randomGenerator;
    private final Map<List<String>, WumpusAction> actionKeyWords;

    public SpeleologistSpeech() {
        randomGenerator = new Random();

        actionKeyWords = new HashMap<>();
        actionKeyWords.put(Dictionary.getActionKeywords(Dictionary.ActionKeyword.TURN_LEFT), WumpusAction.TURN_LEFT);
        actionKeyWords.put(Dictionary.getActionKeywords(Dictionary.ActionKeyword.TURN_RIGHT), WumpusAction.TURN_RIGHT);
        actionKeyWords.put(Dictionary.getActionKeywords(Dictionary.ActionKeyword.GO_FORWARD), WumpusAction.FORWARD);
        actionKeyWords.put(Dictionary.getActionKeywords(Dictionary.ActionKeyword.SHOOT), WumpusAction.SHOOT);
        actionKeyWords.put(Dictionary.getActionKeywords(Dictionary.ActionKeyword.GRAB), WumpusAction.GRAB);
        actionKeyWords.put(Dictionary.getActionKeywords(Dictionary.ActionKeyword.CLIMB), WumpusAction.CLIMB);
    }

    public WumpusAction recognize(String speech) {
        String finalSpeech = speech.toLowerCase();

        return actionKeyWords.keySet().stream()
                .filter(keyWords -> keyWords.stream().anyMatch(finalSpeech::contains))
                .findFirst()
                .map(actionKeyWords::get)
                .orElseThrow();
    }

    public String tellPercept(WumpusPercept percept) {
        List<String> feelings = new ArrayList<>();

        if (percept.isBreeze()) {
            feelings.add(getSentence(Dictionary.getSpeleologistKeywords(Dictionary.SpeleologistKeyword.PIT_NEAR)));
        }

        if (percept.isStench()) {
            feelings.add(getSentence(Dictionary.getSpeleologistKeywords(Dictionary.SpeleologistKeyword.WUMPUS_NEAR)));
        }

        if (percept.isGlitter()) {
            feelings.add(getSentence(Dictionary.getSpeleologistKeywords(Dictionary.SpeleologistKeyword.GOLD_NEAR)));
        }

        if (percept.isBump()) {
            feelings.add(getSentence(Dictionary.getSpeleologistKeywords(Dictionary.SpeleologistKeyword.WALL_NEAR)));
        }

        if (percept.isScream()) {
            feelings.add(getSentence(Dictionary.getSpeleologistKeywords(Dictionary.SpeleologistKeyword.WUMPUS_KILLED_NEAR)));
        }

        if (feelings.isEmpty()) {
            feelings.add(getSentence(Dictionary.getSpeleologistKeywords(Dictionary.SpeleologistKeyword.NOTHING)));
        }

        return String.join(". ", feelings);
    }

    private String getSentence(List<String> sentences) {
        int index = randomGenerator.nextInt(sentences.size());
        return sentences.get(index);
    }
}

