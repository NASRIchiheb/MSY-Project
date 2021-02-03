import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Evaluation {
    // here we have to implement the Evaluation parser, similar to the Configuration parser
    private String simulation;
    private String data;

    public Evaluation(String filePath) {
        File evalFile = new File(filePath);
        try {
            Scanner myReader = new Scanner(evalFile);
            simulation = myReader.nextLine();
            data = simulation + '\n';

            while (myReader.hasNextLine()) {
                data += myReader.nextLine() + '\n';
            }
            myReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while opening the file " + filePath);
            e.printStackTrace();
        }

    }

    public String getApplicationTitle() {
        return simulation;
    }

    public String getData() {
        return data;
    }

    public void printEval() {
        System.out.println(data);
    }


}
