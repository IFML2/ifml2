package ifml2.editor.gui;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import ifml2.GUIUtils;

public class ShowMemoDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextArea memoText;

    public ShowMemoDialog(Window owner, String prompt, String message) {
        super(owner, ModalityType.DOCUMENT_MODAL);
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        // show texts
        setTitle(prompt);
        memoText.setText(message);

        GUIUtils.packAndCenterWindow(this);

        setVisible(true);
    }

    private void onOK() {
        // add your code here
        dispose();
    }
}
