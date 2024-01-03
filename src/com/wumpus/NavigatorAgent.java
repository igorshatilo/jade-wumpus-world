package com.wumpus;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import wumpusworld.core.environment.wumpusworld.AgentPosition;
import wumpusworld.core.environment.wumpusworld.EfficientHybridWumpusAgent;
import wumpusworld.core.environment.wumpusworld.WumpusAction;
import wumpusworld.core.environment.wumpusworld.WumpusPercept;
import com.wumpus.speech.NavigatorSpeech;

public class NavigatorAgent extends Agent {
    EfficientHybridWumpusAgent agent;
    NavigatorSpeech speech;
    private AID speleologistAid;

    @Override
    protected void takeDown() {
        System.out.println("Navigator-agent " + getAID().getName() + " terminating.");
    }

    @Override
    protected void setup() {
        agent = new EfficientHybridWumpusAgent(4, 4, new AgentPosition(1, 1, AgentPosition.Orientation.FACING_NORTH));
        speech = new NavigatorSpeech();
        register();
        searchSpeleologist();

        System.out.println("Navigator-agent " + getAID().getName() + " is ready.");
        addBehaviour(new ListenBehavior());
    }

    private void register() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("navigator");
        sd.setName("wumpus-world");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private void searchSpeleologist() {
        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("speleologist");
                template.addServices(sd);

                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);

                    if (result.length > 0) {
                        speleologistAid = result[0].getName();
                        this.stop();
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });
    }

    private void println(String msg) {
        System.out.println(ConsoleColors.BLUE + "Navigator: " + ConsoleColors.RESET + msg);
    }

    private class ListenBehavior extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String state = msg.getContent();
                WumpusPercept percept = speech.recognize(state);
                println("Received percept: " + state);
                addBehaviour(new FindActionBehaviour(percept));
            } else {
                block();
            }
        }
    }

    private class FindActionBehaviour extends OneShotBehaviour {
        WumpusPercept percept;

        FindActionBehaviour(WumpusPercept percept) {
            this.percept = percept;
        }

        @Override
        public void action() {
            WumpusAction action = agent.act(percept).orElseThrow();
            ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
            println("Decided on action: " + action);
            String actionSentence = speech.tellAction(action);
            reply.setLanguage("English");
            reply.setContent(actionSentence);
            reply.addReplyTo(speleologistAid);
            reply.addReceiver(speleologistAid);
            myAgent.send(reply);
        }
    }
}
