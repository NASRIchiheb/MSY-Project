import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


enum UserAgentState {
    READING,
    WAITING,
}


public class UserBehaviour extends CyclicBehaviour {
    private UserAgent myAgent;
    private UserAgentState state = UserAgentState.READING;

    public UserBehaviour(UserAgent a) {
        super(a);
        myAgent = a;
    }

    public void action() {
        ACLMessage msg;
        switch (state) {
            case READING:

                String userInput = myAgent.readUserInput();
                String[] words = userInput.split("_");
                List<String> wordsList = Arrays.asList(words);
                ArrayList<String> command = new ArrayList<String>(wordsList);

                try {
                    myAgent.sendCommand(command);
                    state = UserAgentState.WAITING;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case WAITING:
                msg = myAgent.blockingReceive();
                if (msg != null) {
                    // manager agent confirm that all fuzzy agents are started
                    if (msg.getPerformative() == ACLMessage.CONFIRM) {
                        state = UserAgentState.READING;


                    }
                    if (msg.getPerformative() == ACLMessage.INFORM) {
                        // user agent gets final respons
                        System.out.println("\n\nEnd of the simulation, name of decision file is: "+ msg.getContent());
                        state = UserAgentState.READING;
                        

                    }
                    // Other agents report a problem to user agent
                    else if(msg.getPerformative() == ACLMessage.FAILURE){
                        System.out.println("A problem occurred please verify the command");
                        state = UserAgentState.READING;
                    }
                }
                break;

            default:
                break;
        }

    }
}