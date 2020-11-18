package common;

import client.ClientMain;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Info {
    private double version;
    private static ArrayList<String> contributors;
    private int year;

    public Info() {
        this.contributors = getContributors(); // TODO: get contributors
        this.version = 1.0;
        this.year = 2020;
    }

    public static ArrayList<String> getContributors() {
        ArrayList<String> c = new ArrayList<>();
        // Read the file
        try (BufferedReader textInput = new BufferedReader(new InputStreamReader(ClientMain.class.getResourceAsStream("/contributors.txt"), Charset.defaultCharset()))) {
            String line = textInput.readLine();
            while (line != null) {
                c.add(line);
                line = textInput.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return c;
    }

    public void showAboutWindow() {
        JFrame f = new JFrame("About Daifugo");
        int SCREEN_HEIGHT = 200;
        int SCREEN_WIDTH = 300;
        f.setLayout(new FlowLayout());

        JPanel panel = new JPanel();
        panel.setBounds(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        f.add(panel);

        JLabel labelVersion = new JLabel( "Daifugo - version " + this.version);
        labelVersion.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel labelYear = new JLabel("Copyright © " + this.year + " Daifugo creators");
        labelYear.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel emptyLine = new JLabel(" ");
        JLabel labelContributors = new JLabel("Contributors:");
        labelContributors.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel labelGithub = new JLabel("github.com/Ruslebiff/Daifugo");
        labelGithub.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(labelVersion);
        panel.add(labelYear);
        panel.add(labelGithub);
        panel.add(emptyLine);
        panel.add(labelContributors);
        for (String c : contributors) {
            JLabel cLabel = new JLabel(c);
            cLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(cLabel);
        }

        f.setSize(SCREEN_WIDTH,SCREEN_HEIGHT);
        f.setLocationRelativeTo(null);          // Place in middle of screen
        f.setVisible(true);                     // Frame visible
    }

    public void showRulesWindow() {
        JFrame f=new JFrame("Rules");
        int SCREEN_HEIGHT = 600;
        int SCREEN_WIDTH = 600;

        JTextArea textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setEditable(false);

        // Read the file
        try (BufferedReader textInput = new BufferedReader(new InputStreamReader(ClientMain.class.getResourceAsStream("/rules.txt"), Charset.defaultCharset()))) {
            textArea.read(textInput, "File");
        } catch (IOException e) {
            e.printStackTrace();
        }

        textArea.setBackground(Color.lightGray);

        f.setSize(SCREEN_WIDTH,SCREEN_HEIGHT);
        f.add(scrollPane);
        f.setLocationRelativeTo(null);          // Place in middle of screen
        f.setVisible(true);                     // Frame visible
    }
}
