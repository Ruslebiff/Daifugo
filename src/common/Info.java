package common;

import javax.swing.*;
import java.util.Arrays;

public class Info {
    private static double version;
    private static String[] contributors;
    private final int year;


    public Info() {
        this.contributors = new String[]{"person 1", "person 2", "person 3"}; // TODO: get contributors
        this.version = 1.0;
        this.year = 2020;
    }

    public static double getVersion() {
        return version;
    }
    public static String getContributors() {
        return Arrays.toString(contributors);
    }

    public void showAboutWindow() {
        JFrame f = new JFrame("About");//creating instance of JFrame
        int SCREEN_HEIGHT = 150;
        int SCREEN_WIDTH = 200;

        JTextArea textArea = new JTextArea(40, 20);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setEditable(false);

        textArea.setText("Version: " + getVersion() + "\n" + "Contributors: " + getContributors()); // TODO: make contributors text better

        f.setSize(SCREEN_WIDTH,SCREEN_HEIGHT);  // Frame size
        f.add(textArea);
        f.setLocationRelativeTo(null);          // Place in middle of screen
        f.setVisible(true);                     // Frame visible
    }

    public void showRulesWindow() {
        JFrame f = new JFrame("Rules");//creating instance of JFrame
        int SCREEN_HEIGHT = 150;
        int SCREEN_WIDTH = 200;

        JTextArea textArea = new JTextArea(40, 20);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setEditable(false);

        textArea.setText("Rules be here"); // TODO: read rules

        f.setSize(SCREEN_WIDTH,SCREEN_HEIGHT);  // Frame size
        f.add(textArea);
        f.setLocationRelativeTo(null);          // Place in middle of screen
        f.setVisible(true);                     // Frame visible
    }
}
