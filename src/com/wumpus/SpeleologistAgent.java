package com.wumpus;

import com.wumpus.core.WumpusPercept;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ControllerException;
import wumpusworld.core.environment.wumpusworld.WumpusAction;
import com.wumpus.speech.SpeleologistSpeech;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class SpeleologistAgent extends Agent {
    private AID environmentAid;
    private AID navigatorAid;
    SpeleologistSpeech speech;

    @Override
    protected void setup() {
        speech = new SpeleologistSpeech();
        register();
        System.out.println("Speleologist-agent " + getAID().getName() + " is ready.");
        searchEnvironmentAndNavigator();
    }

    @Override
    protected void takeDown() {
        System.out.println("Speleologist-agent " + getAID().getName() + " terminating.");
        try {
            getContainerController().getAgent(navigatorAid.getLocalName()).kill();
            getContainerController().getAgent(environmentAid.getLocalName()).kill();
        } catch (ControllerException e) {
            throw new RuntimeException(e);
        }
        System.exit(0);
    }

    private void register() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("speleologist");
        sd.setName("wumpus-world");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private void searchEnvironmentAndNavigator() {
        // Search environment
        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("environment");
                template.addServices(sd);

                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);

                    if (result.length > 0) {
                        environmentAid = result[0].getName();
                        println("Environment found.");
                        this.stop();
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });

        // Search navigator
        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                DFAgentDescription template2 = new DFAgentDescription();
                ServiceDescription sd2 = new ServiceDescription();
                sd2.setType("navigator");
                template2.addServices(sd2);

                try {
                    DFAgentDescription[] result2 = DFService.search(myAgent, template2);

                    if (result2.length > 0) {
                        navigatorAid = result2[0].getName();
                        println("Navigator found.");
                        this.stop();
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });

        // Start speleologist behaviour when environment and navigator are found
        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                if (environmentAid != null && navigatorAid != null) {
                    addBehaviour(new SpeleologistBehaviour());
                    this.stop();
                }
            }
        });
    }

    private void println(String msg) {
        System.out.println(ConsoleColors.YELLOW + "Speleologist: " + ConsoleColors.RESET + msg);
    }

    class SpeleologistBehaviour extends Behaviour {
        WumpusAction wumpusAction;
        private MessageTemplate mt;
        private WumpusPercept currentPercept;
        private int step = 0;

        @Override
        public void action() {
            switch (step) {
                case 0 -> {
                    // Request for current state
                    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                    request.setContent("Current state");
                    request.addReceiver(environmentAid);
                    myAgent.send(request);
                    mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                            MessageTemplate.MatchReplyTo(new AID[]{myAgent.getAID()}));
                    println("Request for current state sent.");
                    step = 1;
                }
                case 1 -> {
                    // Receive answer from environment
                    ACLMessage reply = myAgent.receive(mt);

                    if (reply == null) {
                        block();
                        break;
                    }

                    if (reply.getPerformative() == ACLMessage.INFORM) {
                        String state = reply.getContent();
                        println("Info about state received from environment.");

                        try {
                            Properties props = new Properties();
                            StringReader reader = new StringReader(state);
                            props.load(reader);
                            currentPercept = WumpusPercept.fromString(props.getProperty("percept"));
                            step = 2;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                case 2 -> {
                    // Send percept to navigator
                    ACLMessage state = new ACLMessage(ACLMessage.INFORM);
                    state.setLanguage("English");
                    state.setOntology("WumpusWorld");
                    state.addReceiver(navigatorAid);
                    String feelings = speech.tellPercept(currentPercept);
                    state.setContent(feelings);
                    myAgent.send(state);
                    mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
                            MessageTemplate.MatchReplyTo(new AID[]{myAgent.getAID()}));
                    println("State sent to navigator.");
                    step = 3;
                }
                case 3 -> {
                    // Receive answer from navigator
                    ACLMessage reply2 = myAgent.receive(mt);
                    if (reply2 != null) {
                        // Reply received
                        String action = reply2.getContent();
                        wumpusAction = speech.recognize(action);
                        println("Action received from navigator: " + action);
                        step = 4;
                    } else {
                        block();
                    }
                }
                case 4 -> {
                    // Send action to environment
                    ACLMessage action = new ACLMessage(ACLMessage.CFP);
                    action.setConversationId("environment");
                    action.addReceiver(environmentAid);
                    action.setContent(wumpusAction.getSymbol());
                    myAgent.send(action);
                    mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
                            MessageTemplate.MatchReplyTo(new AID[]{myAgent.getAID()}));
                    step = 5;
                }
                case 5 -> {
                    // Receive answer from environment
                    ACLMessage envReply = myAgent.receive(mt);
                    if (envReply != null) {
                        // Reply received
                        if (envReply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                            if (WumpusAction.CLIMB.equals(wumpusAction)) {
                                println("Climbed out of the cave.");
                                step = 6;
                            } else {
                                println("Going to step 0.");
                                step = 0;
                            }

                        }
                    } else {
                        block();
                    }
                }
                case 6 -> {
                    System.out.println("Game over");
                    step = 7;
                    myAgent.doDelete();
                }
            }
        }

        @Override
        public boolean done() {
            return step == 7;
        }
    }
}
