package ifml2.players.guiplayer;

import ifml2.GUIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;

public class PlayerThemeDialog extends JDialog {
    private final static List<PlayerTheme> defaultPlayerThemes = Arrays.asList(
            new PlayerTheme("Светлая", new Color(0x000000), new Color(0xF0F0F0), "Sitka Text", 18),
            new PlayerTheme("Тёмная", new Color(0xC0C0C0), new Color(0x303030), "Book Antiqua", 18),
            new PlayerTheme("Старая зелёная", new Color(0x00FF00), new Color(0x000000), "Lucida Console", 18)
    );
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
                return defaultPlayerThemes.size();
            }

            @Override
            public PlayerTheme getElementAt(int index) {
                return defaultPlayerThemes.get(index);
            }
        });
        themeList.addListSelectionListener(e -> {
            PlayerTheme theme = themeList.getSelectedValue();
            previewTextPane.setBackground(theme.getBackgroundColor());
            previewTextPane.setForeground(theme.getFontColor());
            previewTextPane.setFont(new Font(theme.getFontName(), Font.PLAIN, theme.getFontSize()));
        });
        themeList.setSelectedIndex(0);
    }

    PlayerTheme ShowDialog() {
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
