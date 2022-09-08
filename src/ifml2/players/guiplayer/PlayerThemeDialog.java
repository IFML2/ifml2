package ifml2.players.guiplayer;

import ifml2.GUIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PlayerThemeDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList<PlayerTheme> themeList;
    private JTextPane previewTextPane;

    private boolean isOk;

    PlayerThemeDialog(Window owner) {
        super(owner, ModalityType.DOCUMENT_MODAL);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        GUIUtils.packAndCenterWindow(this);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // SET UP LIST
        themeList.setModel(new AbstractListModel<PlayerTheme>() {
            @Override
            public int getSize() {
                return PlayerTheme.DEFAULT_PLAYER_THEMES.size();
            }

            @Override
            public PlayerTheme getElementAt(int index) {
                return (PlayerTheme) PlayerTheme.DEFAULT_PLAYER_THEMES.values().toArray()[index];
            }
        });
        themeList.addListSelectionListener(e -> {
            PlayerTheme theme = themeList.getSelectedValue();
            previewTextPane.setBackground(theme.getBackgroundColor());
            previewTextPane.setForeground(theme.getFontColor());
            previewTextPane.setFont(new Font(theme.getFontName(), Font.PLAIN, theme.getFontSize()));
        });
    }

    PlayerTheme ShowDialog(PlayerTheme playerTheme) {
        if (playerTheme != null)
        {
            themeList.setSelectedValue(playerTheme, true);
        }
        setVisible(true);
        return isOk ? themeList.getSelectedValue() : null;
    }

    private void onOK() {
        isOk = true;
        dispose();
    }

    private void onCancel() {
        isOk = false;
        dispose();
    }
}
