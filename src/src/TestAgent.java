

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.introspection.AddedBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.util.Logger;

import java.io.IOException;
import java.util.ArrayList;


public class TestAgent extends Agent {
    private Logger myLogger = Logger.getMyLogger(getClass().getName());

    protected void setup() {
        System.out.println("My local name is " + getAID().getLocalName());
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                ACLMessage messages = new ACLMessage(ACLMessage.REQUEST);
                try {
                    ArrayList<String> content = new ArrayList<String>();
                    content.add("I");
                    content.add("input/configuration1.txt");
                    messages.setContentObject(content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                messages.addReceiver(new AID("ManagerAgent", AID.ISLOCALNAME));
                myAgent.send(messages);
                try {
                    System.out.println(messages.getContentObject());
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }

            }
        });

    }
}
