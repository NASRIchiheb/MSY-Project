import java.util.logging.Logger;
import java.util.Scanner;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import java.util.ArrayList;

/**
 * 
 * Fuzzy agent is defined by:
 * 		a fuzzy set,
 *		a set of rules and
 *		a particular mechanism to handle with data and infer the final response.
 *
 * jFuzzyLogic: library that permits to create a fuzzy rule inference engine
 *
 */
public class FuzzyAgent extends Agent {
	
	private Logger LOGGER = Logger.getLogger(getClass().getName());
	private FIS fis;
	private String domain = null;
	
	/**
	 * Initialize the agent
	 */
	protected void setup() {
        // Registration with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("FuzzyAgent");
        sd.setName(getName());
        sd.setOwnership("MAS_group");
        dfd.setName(getAID());
        dfd.addServices(sd);
        addBehaviour(new FuzzyBehaviour(this));
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
        	//myLogger.log(Logger.SEVERE, "Agent " + getLocalName() + " - Cannot register with DF", e);
            doDelete();
        }
	}
	
	/**
	 * Loads the FCL and creates its particular FIS
	 * @param fileName Name of the FCL to load
	 * @return error
	 */
	public void loadFCL(String fileName) throws Exception {
		String folder = "../input/";
		//String folder = "input/";
		fileName = folder.concat(fileName).concat(".fcl");
        System.out.println(this.getLocalName() + " : loaded fuzzy setting " + fileName);

		try {
			fis = FIS.load(fileName,true);
		} catch (Error e) {
			e.printStackTrace();
			throw new Exception("ERROR: The load process failed");
		}
	}
	
	/**
	 * The fuzzy inference system (FIS) performs the inference
	 * @param rowsToEvaluate
	 * @return results
	 */
    public ArrayList<Double> performInferenceByFIS(String rowsToEvaluate) {

    	// Read the first line of the message where appears the domain
		String domain = rowsToEvaluate.substring(0, rowsToEvaluate.indexOf("\n"));

		// Read the sequence of inputs to be evaluated
		String sequenceOfInputs = rowsToEvaluate.substring(rowsToEvaluate.indexOf("\n")+1, rowsToEvaluate.length());
		Scanner scanner = new Scanner(sequenceOfInputs);

		String row;
		String[] inputValues;
		FunctionBlock functionBlock;
		ArrayList<Double> results = new ArrayList<Double>();
		double output = 0;

		switch(domain) {

			// If the domain is tipper, the rows to evaluate contain 2 input values
			case "tipper":

				double service = 0, food = 0;
				functionBlock = fis.getFunctionBlock("tipper");

				// For each row of input values
				while (scanner.hasNextLine()) {

					// Get the row to evaluate
					row = scanner.nextLine();

					// Read input values separated by commas
					inputValues = row.split(",");

					// Set input variables
					service = Double.parseDouble(inputValues[0]);
					food = Double.parseDouble(inputValues[1]);

					// Set inputs (specify the values to evaluate)
					functionBlock.setVariable("service", service);
					LOGGER.config("service is set to "+service);
					functionBlock.setVariable("food", food);
					LOGGER.config("food is set to "+food);

					// Evaluate
					functionBlock.evaluate();

					// Get output
					output = functionBlock.getVariable("tip").getLatestDefuzzifiedValue();

					// Print result
					System.out.println(String.format("service: %2.2f\tfood:%2.2f\t=> tip: %2.2f %%", service, food, output));

					// Build the response to return
					results.add(output);
				}
				break;

			// If the domain is qualityservice, the rows to evaluate contain 3 input values
			case "qualityservice":

				double commitment = 0, clarity = 0, influence = 0;
				functionBlock = fis.getFunctionBlock("MamdaniQoSFewRules");

				// For each row of input values
				while (scanner.hasNextLine()) {

					// Get the row to evaluate
					row = scanner.nextLine();

					// Read input values separated by commas
					inputValues = row.split(",");

					// Set input variables
					commitment = Double.parseDouble(inputValues[0]);
					clarity = Double.parseDouble(inputValues[1]);
					influence = Double.parseDouble(inputValues[2]);

					// Set inputs (specify the values to evaluate)
					functionBlock.setVariable("commitment", commitment);
					LOGGER.config("commitment is set to " + commitment);
					functionBlock.setVariable("clarity", clarity);
					LOGGER.config("clarity is set to " + clarity);
					functionBlock.setVariable("influence", influence);
					LOGGER.config("influence is set to " + influence);

					// Evaluate
					functionBlock.evaluate();

					// Get output
					output = functionBlock.getVariable("service_quality").getLatestDefuzzifiedValue();

					// Print result
					System.out.println(String.format("commitment: %2.2f\tclarity:%2.2f\tinfluence:%2.2f\t=> service_quality: %2.2f %%", commitment, clarity, influence, output));

					// Build the response to return
					results.add(output);
				}
				break;

			default:
				break;
		}

		scanner.close();

		// Return the results (an array of doubles)
		//ArrayList<double> arrayListOfDoubles = new ArrayList(results);
        return results;
    }

	protected void takeDown() {
		doDelete();
	}

}