package me.simon.schratter.word.analysis;

import javax.swing.*;
import java.awt.*;

public class CreateAndShowGUI {
    public static void createAndShowGUI() {

        JFrame frame = new JFrame();
        frame.setTitle("File Reader and Word occurrence Counter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 500));
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        JComponent ContentPane = new MainPanel();
        ContentPane.setOpaque(true);
        frame.setContentPane(ContentPane);

        frame.pack();
        frame.setVisible(true);
    }
}
