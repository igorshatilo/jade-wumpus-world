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
import wumpusworld.core.environment.wumpusworld.HybridWumpusAgent;
import wumpusworld.core.environment.wumpusworld.WumpusAction;
import wumpusworld.core.environment.wumpusworld.WumpusCave;
import wumpusworld.core.environment.wumpusworld.WumpusEnvironment;
import wumpusworld.core.environment.wumpusworld.WumpusPercept;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

public class EnvironmentAgent extends Agent {
    private WumpusEnvironment wumpusEnvironment;
    private AID speleologistAID;
    private HybridWumpusAgent speleologist;
    private WumpusPercept percept;

    @Override
    protected void takeDown() {
        System.out.println("Environment-agent " + getAID().getName() + " terminating.");
    }

    @Override
    protected void setup() {
        register();
        WumpusCave cave = new WumpusCave(4, 4, ""
                + ". . . P "
                + "W G P . "
                + ". . . . "
                + "S . P . ");
        wumpusEnvironment = new WumpusEnvironment(cave);
        speleologist = new EfficientHybridWumpusAgent(4, 4, new AgentPosition(1, 1, AgentPosition.Orientation.FACING_NORTH));
        percept = new WumpusPercept();
        wumpusEnvironment.addAgent(speleologist);

        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("speleologist");
                template.addServices(sd);

                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);

                    if (result.length == 0) {
                        return;
                    }

                    speleologistAID = result[0].getName();
                    myAgent.addBehaviour(new ListenBehavior());
                    System.out.println("Environment-agent " + getAID().getName() + " is ready.");
                    this.stop();
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });
    }

    private void register() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("environment");
        sd.setName("wumpus-world");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private void println(String msg) {
        System.out.println(ConsoleColors.GREEN + "Environment: " + ConsoleColors.RESET + msg);
    }

    private class ListenBehavior extends CyclicBehaviour {
        public void action() {
            //query - propose
            MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchPerformative(ACLMessage.CFP));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                if (ACLMessage.REQUEST == msg.getPerformative()) {
                    addBehaviour(new QueryBehaviour());
                } else if (ACLMessage.CFP == msg.getPerformative()) {
                    String move = msg.getContent();
                    wumpusEnvironment.execute(speleologist, WumpusAction.fromString(move));
                    addBehaviour(new AcceptBehaviour());
                } else {
                    block();
                }
            } else {
                block();
            }
        }
    }

    private class QueryBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            AgentPosition agentPosition = wumpusEnvironment.getAgentPosition(speleologist);
            percept = wumpusEnvironment.getPerceptSeenBy(speleologist);
            ACLMessage report = new ACLMessage(ACLMessage.INFORM);

            Properties props = new Properties();
            StringWriter writer = new StringWriter();
            props.setProperty("percept", percept.toString());

            try {
                props.store(writer, null);
            } catch (IOException e) {
                e.printStackTrace();
            }

            report.setContent(writer.toString());

            report.addReceiver(speleologistAID);
            report.addReplyTo(speleologistAID);
            myAgent.send(report);

            println("Percept sent to Speleologist " + speleologistAID.getName());
            println("Current Speleologist position: " + agentPosition);
        }
    }

    private class AcceptBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            ACLMessage report = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
            report.setContent("OK");
            report.addReceiver(speleologistAID);
            report.addReplyTo(speleologistAID);
            println("Step performed.");
            myAgent.send(report);
        }
    }
}
