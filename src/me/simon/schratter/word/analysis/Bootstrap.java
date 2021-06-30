package me.simon.schratter.word.analysis;

import javax.swing.*;

public class Bootstrap {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(CreateAndShowGUI::createAndShowGUI);
    }
}
