import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;

import java.io.IOException;
import java.io.Serializable;

enum FuzzyAgentState {
    CREATED,			// The fuzzy agent is created
    WAITING_REQUEST,	// The fuzzy agent waits for a request
    FAILED,
    SUCCESS
}


public class FuzzyBehaviour extends CyclicBehaviour {
    private FuzzyAgent myAgent;
    private FuzzyAgentState state;
    private ACLMessage requestMsg;
    private ArrayList<Double> result;
    private String resultString;

    public FuzzyBehaviour(FuzzyAgent a) {
        super(a);
        myAgent = a;
        state = FuzzyAgentState.CREATED;
    }

    public void action() {
        ACLMessage msg;
        switch(state) {
            case CREATED:
            	// Receives from the manager agent a message that contains the name of the FCL to read
            	msg = myAgent.blockingReceive();
            	if (msg != null) {
            		try {
                        if (msg.getPerformative() == ACLMessage.REQUEST) {
                            requestMsg = msg;

                            // Recieves the name of the file to load (example: fcl1)
                            String fileName = msg.getContent();
                            
                            // Loads the FCL and creates its particular FIS
                            try {
                                myAgent.loadFCL(fileName);

                                // Replies with an agree message
                                ACLMessage response = msg.createReply();
                                response.setPerformative(ACLMessage.INFORM);
                                response.setContent("FCL loaded");
                                myAgent.send(response);

                                // Waits for further messages (requests)
                                state = FuzzyAgentState.WAITING_REQUEST;

                            } catch (Exception e) {
                                // Replies with a refuse message
                                ACLMessage response = msg.createReply();
                                response.setPerformative(ACLMessage.FAILURE);
                                response.setContent(e.getMessage());
                                myAgent.send(response);
                                e.printStackTrace();
                            }
                        }
            		} catch (Exception e) {
                        e.printStackTrace();
                    }
            	}
                break;
            case WAITING_REQUEST:
            	// Receives from the manager agent a message that contains the set of rows to evaluate
            	msg = myAgent.blockingReceive();
            	if (msg != null) {
            		try {
                        if (msg.getPerformative() == ACLMessage.REQUEST) {
                            requestMsg = msg;
                            // Recieves the set of rows to evaluate
                            String rowsToEvaluate = msg.getContent();
                            
                            // The fuzzy inference system (FIS) performs the inference
                            result = myAgent.performInferenceByFIS(rowsToEvaluate);
                            	
                            if (result.size() != 0) {
                            	state = FuzzyAgentState.SUCCESS;
                            } else {
                            	state = FuzzyAgentState.FAILED;
                            	resultString = "ERROR";
                            	throw new Exception("performInferenceByFIS failed");
                            }
                        }
            		} catch (Exception e) {
                        e.printStackTrace();
                    }
            	}
                break;
            case FAILED:
            	// Replies to the manager agent with a failure message
                ACLMessage resultFailed = requestMsg.createReply();
                resultFailed.setPerformative(ACLMessage.FAILURE);
				resultFailed.setContent((String) resultString);
                myAgent.send(resultFailed);
                state = FuzzyAgentState.WAITING_REQUEST;
                break;
            case SUCCESS:
            	// Replies to the manager agent with an inform message that contains the results

                    ACLMessage resultSuccess = requestMsg.createReply();
                    resultSuccess.setPerformative(ACLMessage.INFORM);
                try {
                    resultSuccess.setContentObject(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                myAgent.send(resultSuccess);
                    state = FuzzyAgentState.WAITING_REQUEST;

                break;
        }
    }
}