package ifml2.platform.core;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainUI mainUI = new MainUI();
                mainUI.open();
            }
        });
    }

}
