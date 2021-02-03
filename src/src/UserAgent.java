import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class UserAgent extends Agent {

	private String CONFIG_FILE_PATH = "UserAgent/input/";
	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	private Configuration config = null;
	private boolean actionPending = false;

	protected void setup() {
		// Registration with the DF
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("UserAgent");
		sd.setName(getName());
		sd.setOwnership("MAS_group");
		dfd.setName(getAID());
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			myLogger.log(Logger.SEVERE, "Agent " + getLocalName() + " - Cannot register with DF", e);
			doDelete();
		}
		addBehaviour(new UserBehaviour(this));
	}

	protected void takeDown() {
		//DF unregistration
		//Close any open/required resources
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		super.takeDown();
	}



	public String readUserInput() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		boolean validAction = false;
		String instructions =
				"\n\n***************************** FA-DSS 2020 ****************************" +
				"\n*****************************   TEAM 02   ****************************" +
				"\n********** Please input one of the following action request **********\n" +
				"'I_configuration_name.txt': \tconfigure the MAS\n" +
				"'D_simulation_file.txt': \tstart the simulation \n";
		String action = "";
		System.out.println(instructions);
		try {
			while (!validAction) {
				// Read user input
				action = reader.readLine();
				if (action != null && (action.startsWith("I") || action.startsWith("D"))) {
					validAction = true;
				} else {
					myLogger.log(Logger.WARNING, "Wrong action" );
				}
			}
		} catch (Exception e) {
			myLogger.log(Logger.SEVERE, "Error during user interaction");
			e.printStackTrace();
			return "";
		}
		return action;
	}

	public void sendCommand(ArrayList<String> command) throws IOException {
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		AID receiver = new AID("ManagerAgent", AID.ISLOCALNAME);
		message.addReceiver(receiver);
		message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		message.setContentObject(command);
		this.send(message);
	}
}
