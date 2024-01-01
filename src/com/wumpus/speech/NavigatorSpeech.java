package com.wumpus.speech;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import wumpusworld.core.environment.wumpusworld.WumpusAction;
import wumpusworld.core.environment.wumpusworld.WumpusPercept;

public class NavigatorSpeech {
    private final Random randomGenerator;
    private final Map<WumpusAction, List<String>> actionSentences;

    public NavigatorSpeech() {
        randomGenerator = new Random();

        actionSentences = new HashMap<>();
        actionSentences.put(WumpusAction.TURN_LEFT, Dictionary.getNavigatorKeywords(Dictionary.NavigatorKeyword.TURN_LEFT));
        actionSentences.put(WumpusAction.TURN_RIGHT, Dictionary.getNavigatorKeywords(Dictionary.NavigatorKeyword.TURN_RIGHT));
        actionSentences.put(WumpusAction.FORWARD, Dictionary.getNavigatorKeywords(Dictionary.NavigatorKeyword.GO_FORWARD));
        actionSentences.put(WumpusAction.SHOOT, Dictionary.getNavigatorKeywords(Dictionary.NavigatorKeyword.SHOOT));
        actionSentences.put(WumpusAction.GRAB, Dictionary.getNavigatorKeywords(Dictionary.NavigatorKeyword.GRAB));
        actionSentences.put(WumpusAction.CLIMB, Dictionary.getNavigatorKeywords(Dictionary.NavigatorKeyword.CLIMB));
    }

    public String tellAction(WumpusAction action) {
        List<String> sentences = actionSentences.get(action);
        int index = randomGenerator.nextInt(sentences.size());

        return sentences.get(index);
    }

    public WumpusPercept recognize(String speech) {
        List<String> feelings = Arrays.stream(speech.split(". ")).map(String::toLowerCase).toList();
        WumpusPercept percept = new WumpusPercept();

        for (String feeling : feelings) {
            for (String word : Dictionary.getPerceptKeywords(Dictionary.PerceptKeyword.STENCH)) {
                if (feeling.contains(word)) {
                    percept.setStench();
                }
                break;
            }

            for (String word : Dictionary.getPerceptKeywords(Dictionary.PerceptKeyword.BREEZE)) {
                if (feeling.contains(word)) {
                    percept.setBreeze();
                }
                break;
            }

            for (String word : Dictionary.getPerceptKeywords(Dictionary.PerceptKeyword.GLITTER)) {
                if (feeling.contains(word)) {
                    percept.setGlitter();
                }
                break;
            }

            for (String word : Dictionary.getPerceptKeywords(Dictionary.PerceptKeyword.BUMP)) {
                if (feeling.contains(word)) {
                    percept.setBump();
                }
                break;
            }

            for (String word : Dictionary.getPerceptKeywords(Dictionary.PerceptKeyword.SCREAM)) {
                if (feeling.contains(word)) {
                    percept.setScream();
                }
                break;
            }
        }

        return percept;
    }
}
