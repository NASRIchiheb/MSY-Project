
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Configuration implements Serializable {

    private String action;
    private String filePath;
    private String simulationTitle;
    private String applicationTitle;
    private String fuzzyAgents;
    private ArrayList<String> fuzzySettings;
    private String aggregationFunction;
    //private String file;

    /**
     * Constructor of the class. Configured only for the "initialization" task.
     * Read the file and store all the parameters of the configuration.
     * @param action the action specified by User Agent
     * @param filePath the path of the configuration file specified by the User Agent
     */
    public Configuration(String filePath) {
        this.setFilePath(filePath);

        File xmlFile = new File(filePath);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = dbFactory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            NodeList nList = document.getElementsByTagName("SimulationSettings");
            // Get root element
            Node nNode = nList.item(0);
            Element rootElement = (Element) nNode;

            this.setSimulationTitle(rootElement.getElementsByTagName("title").item(0).getTextContent());
            this.setApplicationTitle(rootElement.getElementsByTagName("application").item(0).getTextContent());
            this.setFuzzyAgents(rootElement.getElementsByTagName("fuzzyagents").item(0).getTextContent());

            // setFuzzySettings needs an arrayList, so I call parseFuzzySettings to parse the string to an arrayList
            this.setFuzzySettings(parseFuzzySettings(rootElement.getElementsByTagName("fuzzySettings").item(0).getTextContent()));
            this.setAggregationFunction(rootElement.getElementsByTagName("aggregation").item(0).getTextContent());
            //System.out.println("Config param i send from config is"+printConfig());

        } catch (Exception e) {
            System.err.println("Parsing of xml configuration file error: \n" + e.getMessage());
        }

    }


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSimulationTitle() {
        return simulationTitle;
    }

    public void setSimulationTitle(String simulationTitle) {
        this.simulationTitle = simulationTitle;
    }

    public String getApplicationTitle() {
        return applicationTitle;
    }

    public void setApplicationTitle(String applicationTitle) {
        this.applicationTitle = applicationTitle;
    }

    public Integer getFuzzyAgents() {
        return Integer.parseInt(fuzzyAgents);
    }

    public void setFuzzyAgents(String fuzzyAgents) {
        this.fuzzyAgents = fuzzyAgents;
    }

    public ArrayList<String> getFuzzySettings() {
        return fuzzySettings;
    }

    public void setFuzzySettings(ArrayList<String> fuzzySettings) {
        this.fuzzySettings = fuzzySettings;
    }

    public String getAggregationFunction() {
        return aggregationFunction;
    }

    public void setAggregationFunction(String aggregationFunction) {
        this.aggregationFunction = aggregationFunction;
    }

    /**
     * parseFuzzySettings receive the string of parameters from the file, and split the fuzzy settings in a ArrayList
     * @param settings the string of fuzzy settings file
     * @return the ArrayList with the settings parsed
     */
    private ArrayList<String> parseFuzzySettings(String settings) {
        String[] settingsArray = settings.split(",");
        return new ArrayList<String>(Arrays.asList(settingsArray));
    }

    /**
     * Method used to print all the parameters stored in the Config class
     * @return
     */
    public void printConfig(){
        String configParameters =
                "\n * Title: " + this.getSimulationTitle() + "\n" +
                " * Application: " + this.getApplicationTitle() + "\n" +
                " * Fuzzy Agents: " + this.getFuzzyAgents() + "\n" +
                " * Fuzzy Settings: " + this.getFuzzySettings() + "\n" +
                " * Aggregation Function: " + this.getAggregationFunction() + "\n";
        System.out.println(configParameters);
    }

}