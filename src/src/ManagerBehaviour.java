import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;


enum ManagerAgentState {
    IDLE,
    WAITING_INIT_FUZZY,
    WAITING_EVAL_FUZZY
}

public class ManagerBehaviour extends CyclicBehaviour {
    private ManagerAgent myAgent;
    private ManagerAgentState state = ManagerAgentState.IDLE;
    private int fuzzyAgentCounter = 0;
    private ArrayList<ArrayList<Double>> evalStorage = new ArrayList<ArrayList<Double>>();

    public ManagerBehaviour(ManagerAgent a) {
        super(a);
        myAgent = a;
        //state = ManagerBehaviourState.IDLE;
    }

    public void action() {
        ACLMessage msg;
        //wait until receive message
        switch (state) {
            case IDLE:
                msg = myAgent.blockingReceive();
                if (msg != null) {
                    try {
                        if (msg.getPerformative() == ACLMessage.REQUEST) {

                            ArrayList<String> message = (ArrayList<String>) msg.getContentObject();

                            try {
                                myAgent.checkAction(message);
                                String action = message.get(0);
                                myAgent.performAction(action);

                                // if the message is I:
                                // change WAITING_INIT_FUZZY
                                // if the message is D:
                                // change WAITING_EVAL_FUZZY
                                switch (action) {
                                    case "I":
                                        state = ManagerAgentState.WAITING_INIT_FUZZY;
                                        fuzzyAgentCounter = myAgent.getFuzzyAgentCounter();
                                        break;
                                    case "D":
                                        state = ManagerAgentState.WAITING_EVAL_FUZZY;
                                        fuzzyAgentCounter = myAgent.getFuzzyAgentCounter();
                                        break;
                                    default:
                                        // it's impossible to finish here
                                        break;
                                }

                            } catch (Exception e) {
                                // error: response with the error
                                ACLMessage response = msg.createReply();
                                response.setPerformative(ACLMessage.FAILURE);
                                response.setContent(e.getMessage());
                                myAgent.send(response);
                            }
                        }
                    } catch (UnreadableException e) {
                        System.out.println("error");
                        e.printStackTrace();
                    }
                }
                break;

            case WAITING_INIT_FUZZY:
                msg = myAgent.blockingReceive();
                if (msg != null) {

                    if (msg.getPerformative() == ACLMessage.INFORM) {

                        if (fuzzyAgentCounter == 1) { // last CONFIRM response by the fuzzy agents
                            // confirm to the user agent that all fuzzy agents are ready
                            ACLMessage message = new ACLMessage(ACLMessage.CONFIRM);
                            AID receiver = new AID("UserAgent", AID.ISLOCALNAME);
                            message.addReceiver(receiver);
                            message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                            message.setPerformative(ACLMessage.CONFIRM);
                            message.setContent("Fuzzy Agents ready");
                            myAgent.send(message);

                            // change state
                            state = ManagerAgentState.IDLE;
                        }

                        fuzzyAgentCounter--;

                    } else {
                        // error: a fuzzy agent sent a response with the error
                        ACLMessage message = new ACLMessage(ACLMessage.FAILURE);
                        AID receiver = new AID("UserAgent", AID.ISLOCALNAME);
                        message.addReceiver(receiver);
                        message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                        message.setPerformative(ACLMessage.FAILURE);
                        message.setContent("Error: Fuzzy Agent not created");
                        myAgent.send(message);

                        // change state
                        state = ManagerAgentState.IDLE;

                        // reset fuzzy agents counter
                        fuzzyAgentCounter = 0;
                    }

                }
                break;

            case WAITING_EVAL_FUZZY:
                msg = myAgent.blockingReceive();
                if (msg != null) {

                    if (msg.getPerformative() == ACLMessage.INFORM) {

                        // add the result
                        ArrayList<Double> result = null;
                        try {
                            result = (ArrayList<Double>) msg.getContentObject();
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                        evalStorage.add(result);

                        if (fuzzyAgentCounter == 1) { // last CONFIRM response by the fuzzy agents

                            // aggregate results, create the file and return the name of the file
                            String outputFile = myAgent.aggregateResults(evalStorage);

                            // confirm to the user agent that all fuzzy agents are ready
                            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                            AID receiver = new AID("UserAgent", AID.ISLOCALNAME);
                            message.addReceiver(receiver);
                            message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                            message.setPerformative(ACLMessage.INFORM);
                            message.setContent(outputFile);
                            myAgent.send(message);

                            // change state
                            state = ManagerAgentState.IDLE;

                            // reset storage
                            evalStorage = new ArrayList<ArrayList<Double>>();
                        }

                        fuzzyAgentCounter--;

                    } else {
                        // error: a fuzzy agent sent a response with the error
                        ACLMessage message = new ACLMessage(ACLMessage.FAILURE);
                        AID receiver = new AID("UserAgent", AID.ISLOCALNAME);
                        message.addReceiver(receiver);
                        message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                        message.setPerformative(ACLMessage.FAILURE);
                        message.setContent("Error: Fuzzy Agent not created");
                        myAgent.send(message);

                        // change state
                        state = ManagerAgentState.IDLE;

                        // reset fuzzy agents counter e storage
                        fuzzyAgentCounter = 0;
                        evalStorage = new ArrayList<ArrayList<Double>>();
                    }
                }
                break;

            default:
                break;
        }

    }
}