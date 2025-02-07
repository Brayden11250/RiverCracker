import java.util.Arrays;
import javax.swing.SwingUtilities;

public class RiverCracker {

    public static void main(String[] args) {
        if (args.length == 0) {
            SwingUtilities.invokeLater(() -> RiverGUI.showGUI());
            return;
        }
        String mode = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        if (mode.equals("generate")) {
            RiverGUI.runRiverGeneration(subArgs);
        } else if (mode.equals("bruteforce")) {
            Bruteforce.runBruteforce(subArgs);
        } else if (mode.equals("gui")) {
            SwingUtilities.invokeLater(() -> RiverGUI.showGUI());
        } else {
            System.out.println("Unknown mode: " + mode);
            System.out.println("Usage:");
            System.out.println("To launch GUI: java RiverCracker gui");
            System.out.println("To generate a river image: java RiverCracker generate <seed> <output.png>");
            System.out.println("To bruteforce seeds: java RiverCracker bruteforce [bounding box numbers and/or seedfile.txt]");
        }
    }
}
