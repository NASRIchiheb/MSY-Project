
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.KillAgent;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class ManagerAgent extends Agent {
    private Logger myLogger = Logger.getMyLogger(getClass().getName());
    private Configuration config = null;
    private Evaluation eval = null;
    private String simulation = null;
    private HashMap<String, ArrayList<String>> fuzzyAgentsCreated = new HashMap<String, ArrayList<String>>();
    private HashMap<String, String> aggregations = new HashMap<String, String>();
    private HashMap<String, Integer> fuzzyAgentsHistory = new HashMap<String, Integer>();

    protected void setup() {
        // Registration with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("ManagerAgent");
        sd.setName(getName());
        sd.setOwnership("MAS_group");
        dfd.setName(getAID());
        dfd.addServices(sd);

        addBehaviour(new ManagerBehaviour(this));

        try {
            //getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL);
            //getContentManager().registerOntology(JADEManagementOntology.getInstance());

            DFService.register(this, dfd);
        } catch (FIPAException e) {
            myLogger.log(Logger.SEVERE, "Agent " + getLocalName() + " - Cannot register with DF", e);
            doDelete();
        }
    }


    public void checkAction(ArrayList<String> action) throws Exception {

        if (action.size() == 2) {
            // action[0] is the command
            // action[1] is the file
            switch (action.get(0)) {
                case "I":
                    // initialization
                    config = new Configuration(action.get(1));
                    config.printConfig();
                    break;

                case "D":
                    //evaluation
                    eval = new Evaluation(action.get(1));
                    System.out.println("\n\nEvaluation File : " + action.get(1));
                    eval.printEval();
                    break;

                default:
                    throw new Exception("Wrong command action");
            }

        } else throw new Exception("Too many parameters in the command");
    }


    public void performAction(String action) throws Exception {

        switch (action) {
            case "I":
                // initialization
                try {
                    simulation = config.getApplicationTitle();
                    aggregations.put(simulation, config.getAggregationFunction());
                    createFuzzyAgents(simulation);
                    sendFuzzySettings(simulation);
                } catch (ControllerException | IOException e) {
                    // manage this exception
                }
                break;

            case "D":
                // evaluation
                simulation = eval.getApplicationTitle();
                sendEvaluation(simulation);
                break;
            default: throw new Exception("Wrong command action");
        }
    }


    public String aggregateResults(ArrayList<ArrayList<Double>> results) {
        String aggregationFunction = aggregations.get(simulation);
        String outputFile = simulation + "_output.txt";
        int numResults = results.get(0).size();
        int numAgents = results.size();
        Double[] finalResult = new Double[numResults];
        Arrays.fill(finalResult, 0.0);

        switch (aggregationFunction) {
            case "average":

                for (int i = 0; i < numAgents; i++) {
                    for (int j = 0; j < numResults; j++) {
                        // for each row add the result in the corresponding aggregation bucket
                        finalResult[j] += results.get(i).get(j);
                    }

                }
                for (int k = 0; k < numResults; k++) {
                    finalResult[k] /= numAgents;
                }

                outputFile = createResultFile(finalResult, outputFile);

                break;
            default:
                break;
        }

        return outputFile;
    }

    private String createResultFile(Double[] finalResult, String fileName) {

        File outputFile = new File(fileName);

        try {
            if (outputFile.createNewFile()) {
                System.out.println("\n\nFile created: " + outputFile.getName());
            } else {
                System.out.println("\n\nFile already exists.");
            }

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try {
            FileWriter myWriter = new FileWriter(fileName);
            myWriter.write("Input data:\n");
            myWriter.write(eval.getData());
            myWriter.write("\nAggregation function: "+ aggregations.get(simulation));
            myWriter.write("\n\nResults:");
            for (int i = 0; i < finalResult.length; i++) {
                myWriter.write("\n"+finalResult[i].toString());
            }
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return fileName;
    }

    public Integer getFuzzyAgentCounter() {
        return fuzzyAgentsCreated.get(simulation).size();
    }

    private void sendEvaluation(String simulation) {
        ArrayList<String> fuzzyAgents = fuzzyAgentsCreated.get(simulation);
        for(int i = 0; i < fuzzyAgents.size() ; i++) {
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            AID receiver = new AID(fuzzyAgents.get(i), AID.ISLOCALNAME);
            message.addReceiver(receiver);
            message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            message.setContent(eval.getData());
            message.setPerformative(ACLMessage.REQUEST);
            this.send(message);
        }
    }


    private void createFuzzyAgents(String simulation) throws ControllerException {
        ContainerController containerController = getContainerController();

        // remove all previous fuzzy agents
        // check if there is a simulation with this name
        if(fuzzyAgentsCreated.containsKey(simulation)) {
            ArrayList<String> fuzzyAgentsToRemove = fuzzyAgentsCreated.get(simulation);

            // remove all the agents in the previous simulation
            while(fuzzyAgentsToRemove.size() > 0) {
                String agentName = fuzzyAgentsToRemove.remove(0);
                containerController.getAgent(agentName).kill();
                //killFuzzyAgent(new AID(agentName, AID.ISLOCALNAME));
            }
            // remove the simulation as entry key
            fuzzyAgentsCreated.remove(simulation);
        }

        // instantiate new fuzzy agents
        Integer fuzzyAgentToInstantiate = config.getFuzzyAgents();
        ArrayList<String> fuzzyAgentsCreatedList = new ArrayList<String>();

        while(fuzzyAgentToInstantiate > 0) {
            // create an agent and add the name to the fuzzyAgentCreated registry
            String fuzzyAID = generateFuzzyAID();

            String agentName = "FuzzyAgent" + fuzzyAID;
            fuzzyAgentsCreatedList.add(agentName);
            containerController.createNewAgent(agentName, "FuzzyAgent", null).start();
            fuzzyAgentToInstantiate--;
        }
        // map the fuzzyAgentsCreatedList with the corresponding simulation
        fuzzyAgentsCreated.put(simulation, fuzzyAgentsCreatedList);

        // fuzzy agents in the system
        System.out.println("\nFuzzy Agents created: " + fuzzyAgentsCreatedList + "\n");

    }

    private void sendFuzzySettings(String simulation) throws IOException {
        ArrayList<String> fuzzyAgents = fuzzyAgentsCreated.get(simulation);

        for(int i = 0; i < fuzzyAgents.size() ; i++) {
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            AID receiver = new AID(fuzzyAgents.get(i), AID.ISLOCALNAME);
            message.addReceiver(receiver);
            message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            message.setContent(config.getFuzzySettings().get(i));
            message.setPerformative(ACLMessage.REQUEST);
            this.send(message);
        }
    }

    /**
     * @deprecated : this functions is not useful anymore since
     * I have created a different mechanism to create / kill agents
     *
     */
    private void killFuzzyAgent(AID fuzzyAgentAID) {
        KillAgent ka = new KillAgent();
        ka.setAgent(fuzzyAgentAID); // AID of the agent you want to kill
        Action action = new Action(getAMS(), ka);
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.setLanguage(new SLCodec().getName());
        request.setOntology(JADEManagementOntology.NAME);
        try {
            getContentManager().fillContent(request, new Action(getAMS(), action));
            request.addReceiver(action.getActor());
            send(request);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String generateFuzzyAID() {
        Random rand = new Random();
        String fuzzyAID = "";
        boolean uniqueID = false;

        while (!uniqueID) {
            Integer AgentID = rand.nextInt(10000);
            if (fuzzyAgentsHistory.containsKey(AgentID.toString())) {
                // not good, we have to generate a unique AID
            } else {
                uniqueID = true;
                fuzzyAID = AgentID.toString();
                fuzzyAgentsHistory.put(fuzzyAID, 1);
            }
        }
        return fuzzyAID;
    }
}


