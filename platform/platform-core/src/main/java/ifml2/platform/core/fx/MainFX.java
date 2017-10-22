package ifml2.platform.core.fx;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public class MainFX {

    public static void main(final String[] args) {
        // Two ways to start 
        // (it is not trivial, as we want to show Swing components
        // too)
//        startAWT(args);
        startJFXPanel(args);
    }

    private static void startJFXPanel(final String[] args) {
        System.setProperty("javafx.macosx.embedded", "true");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    final JFrame mainFrame = new JFrame("gui tool (s/fx)");
                    final JFXPanel jfxPanel = new JFXPanel();
                    mainFrame.getContentPane().add(jfxPanel);
                    mainFrame.setTitle("OSGi Snippets GUI Tool (s/fx)");
                    mainFrame.setSize(800, 600);
                    mainFrame.setLocationRelativeTo(null);
                    mainFrame.setVisible(true);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            MainFXUI fxui = new MainFXUI();
                            try {
                                fxui.start(jfxPanel, mainFrame);
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void startAWT(String[] args) {
        System.setProperty("javafx.macosx.embedded", "true");
        java.awt.Toolkit.getDefaultToolkit();
        Application.launch(MainFXUI.class, args);
    }

}
