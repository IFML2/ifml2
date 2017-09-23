package ifml2.editor.gui.editors;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ifml2.GUIUtils;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.om.Word;
import ifml2.om.WordLinks;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;

import static ifml2.om.Word.GramCase.DP;
import static ifml2.om.Word.GramCase.IP;
import static ifml2.om.Word.GramCase.PP;
import static ifml2.om.Word.GramCase.RP;
import static ifml2.om.Word.GramCase.TP;
import static ifml2.om.Word.GramCase.VP;

public class WordLinksEditor extends AbstractEditor<WordLinks> {
    public static final String NEW_WORD_ACTION = "Новое...";
    public static final String DELETE_WORD_ACTION = "Удалить";
    public static final String MAIN_WORD_MUST_BE_SET_ERROR_MESSAGE_DIALOG = "Основное слово не установлено. Его необходимо установить.";
    private static final String DICTIONARY_EDITOR_TITLE = "Словарь";
    private static final String CASE_DOC_PROPERTY = "case";
    private static final String WORD_IP_QUERY_PROMPT = "Именительный падеж:";
    private static final String DUPLICATED_WORD_INFO_MESSAGE = "Такое слово уже есть в словаре, оно будет использовано";
    private static final String DUPLICATED_WORD_INFO_DIALOG_TITLE = "Дубликат";
    private static final String WORD_DELETION_QUERY_PROMPT = "Вы уверены, что хотите удалить это слово из словаря?";
    private static final String WRONG_DOC_PROP_SYSTEM_ERROR = "Системная ошибка: неверное свойство case у DocumentEvent.getDocument() в wordDocListener";
    private static final String SET_MAIN_WORD_QUERY_PROMPT = "Основное слово ещё не установлено. Установить только что добавленное?";
    private static final String SET_MAIN_WORD_DIALOG_TITLE = "Установка основного слова";
    private JPanel contentPane;
    private JButton buttonOK;
    private JList wordList;
    private JTextField ipText;
    private JTextField rpText;
    private JTextField dpText;
    private JTextField vpText;
    private JTextField tpText;
    private JTextField ppText;
    private JButton newWordButton;
    private JButton delWordButton;
    private JPanel casesPanel;
    private JComboBox mainWordCombo;
    private boolean isUpdatingText = false;
    private ArrayList<Word> wordsClone = null;

    public WordLinksEditor(Window owner, final HashMap<String, Word> dictionary, WordLinks wordLinks) {
        super(owner);
        initializeEditor(DICTIONARY_EDITOR_TITLE, contentPane, buttonOK, null);

        // -- init form --

        wordList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                repaintCurrentWord((Word) wordList.getSelectedValue());
            }
        });

        DocumentListener wordDocListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    updateCurrentWord(e.getDocument());
                } catch (IFML2EditorException ex) {
                    GUIUtils.showErrorMessage(WordLinksEditor.this, ex);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                try {
                    updateCurrentWord(e.getDocument());
                } catch (IFML2EditorException ex) {
                    GUIUtils.showErrorMessage(WordLinksEditor.this, ex);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                //do nothing
            }
        };

        ipText.getDocument().putProperty(CASE_DOC_PROPERTY, IP);
        ipText.getDocument().addDocumentListener(wordDocListener);

        rpText.getDocument().putProperty(CASE_DOC_PROPERTY, RP);
        rpText.getDocument().addDocumentListener(wordDocListener);

        dpText.getDocument().putProperty(CASE_DOC_PROPERTY, DP);
        dpText.getDocument().addDocumentListener(wordDocListener);

        vpText.getDocument().putProperty(CASE_DOC_PROPERTY, VP);
        vpText.getDocument().addDocumentListener(wordDocListener);

        tpText.getDocument().putProperty(CASE_DOC_PROPERTY, TP);
        tpText.getDocument().addDocumentListener(wordDocListener);

        ppText.getDocument().putProperty(CASE_DOC_PROPERTY, PP);
        ppText.getDocument().addDocumentListener(wordDocListener);

        newWordButton.setAction(new AbstractAction(NEW_WORD_ACTION, GUIUtils.ADD_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newWordIp = JOptionPane.showInputDialog(WORD_IP_QUERY_PROMPT);
                if (newWordIp != null && !"".equals(newWordIp)) {
                    Word word = new Word(newWordIp);
                    if (dictionary.containsKey(newWordIp)) {
                        JOptionPane.showMessageDialog(WordLinksEditor.this, DUPLICATED_WORD_INFO_MESSAGE, DUPLICATED_WORD_INFO_DIALOG_TITLE,
                                JOptionPane.INFORMATION_MESSAGE);
                        word = dictionary.get(newWordIp);
                    } else {
                        dictionary.put(newWordIp, word);
                    }
                    wordsClone.add(word);
                    updateLinksAndMain(word);

                    // set main word in case it isn't set
                    if (mainWordCombo.getSelectedItem() == null) {
                        if (JOptionPane.showConfirmDialog(WordLinksEditor.this, SET_MAIN_WORD_QUERY_PROMPT, SET_MAIN_WORD_DIALOG_TITLE,
                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                            mainWordCombo.setSelectedItem(word);
                        }
                    }
                }
            }
        });

        delWordButton.setAction(new AbstractAction(DELETE_WORD_ACTION, GUIUtils.DEL_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Word word = (Word) wordList.getSelectedValue();
                if (word != null && JOptionPane.showConfirmDialog(getContentPane(), WORD_DELETION_QUERY_PROMPT) == JOptionPane.YES_OPTION) {
                    wordsClone.remove(word);
                    updateLinksAndMain(word);
                }
            }
        });

        wordsClone = new ArrayList<Word>(wordLinks.getWords());

        Word firstWord = wordsClone.size() > 0 ? wordsClone.get(0) : null;
        updateWordLinks(firstWord);

        updateMainWord();
        mainWordCombo.setSelectedItem(wordLinks.getMainWord());
    }

    private void updateLinksAndMain(Word word) {
        updateWordLinks(word);
        updateMainWord();
    }

    private void updateMainWord() {
        Object selectedMainWord = mainWordCombo.getSelectedItem();
        mainWordCombo.setModel(new DefaultComboBoxModel(wordsClone.toArray()));
        mainWordCombo.setSelectedItem(selectedMainWord);
    }

    private void updateWordLinks(Word word) {
        updateWordLinks();
        wordList.setSelectedValue(word, true);
    }

    private void updateCurrentWord(Document document) throws IFML2EditorException {
        if (isUpdatingText) {
            return;
        }

        Word word = (Word) wordList.getSelectedValue();
        if (word != null) {

            String text = null;
            try {
                text = document.getText(0, document.getLength());
            } catch (BadLocationException e) {
                GUIUtils.showErrorMessage(WordLinksEditor.this, e);
            }

            if (text != null) {
                text = text.trim();
            }

            Word.GramCase gramCase = (Word.GramCase) document.getProperty(CASE_DOC_PROPERTY);
            switch (gramCase) {
                case IP:
                    word.ip = text;
                    break;
                case RP:
                    word.rp = text;
                    break;
                case DP:
                    word.dp = text;
                    break;
                case VP:
                    word.vp = text;
                    break;
                case TP:
                    word.tp = text;
                    break;
                case PP:
                    word.pp = text;
                    break;
                default:
                    throw new IFML2EditorException(WRONG_DOC_PROP_SYSTEM_ERROR);
            }
        }
    }

    private void repaintCurrentWord(Word word) {
        isUpdatingText = true;
        try {
            if (word != null) {
                casesPanel.setBorder(new TitledBorder(word.ip));
            }
            ipText.setText(word != null ? word.ip : "");
            rpText.setText(word != null ? word.rp : "");
            dpText.setText(word != null ? word.dp : "");
            vpText.setText(word != null ? word.vp : "");
            tpText.setText(word != null ? word.tp : "");
            ppText.setText(word != null ? word.pp : "");
        } finally {
            isUpdatingText = false;
        }
    }

    private void updateWordLinks() {
        DefaultListModel wordLinksListModel = new DefaultListModel();
        for (Word word : wordsClone) {
            wordLinksListModel.addElement(word);
        }
        wordList.setModel(wordLinksListModel);
    }

    //TODO: привести редактор в порядок; сделать транзакционным! позволить редактировать ИП - через обновление HashMap словаря

    @Override
    public void updateData(@NotNull WordLinks wordLinks) {
        wordLinks.getWords().clear();
        wordLinks.getWords().addAll(wordsClone);
        wordLinks.setMainWord((Word) mainWordCombo.getSelectedItem());
    }

    @Override
    protected void validateData() throws DataNotValidException {
        // check if main word is set
        if (mainWordCombo.getSelectedItem() == null) {
            throw new DataNotValidException(MAIN_WORD_MUST_BE_SET_ERROR_MESSAGE_DIALOG, mainWordCombo);
        }
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        casesPanel = new JPanel();
        casesPanel.setLayout(new GridLayoutManager(6, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(casesPanel, new GridConstraints(0, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        ipText = new JTextField();
        casesPanel.add(ipText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("И.П.");
        casesPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Р.П.");
        casesPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rpText = new JTextField();
        casesPanel.add(rpText, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Д.П.");
        casesPanel.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dpText = new JTextField();
        casesPanel.add(dpText, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("В.П.");
        casesPanel.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Т.П.");
        casesPanel.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("П.П.");
        casesPanel.add(label6, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        vpText = new JTextField();
        casesPanel.add(vpText, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        tpText = new JTextField();
        casesPanel.add(tpText, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        ppText = new JTextField();
        casesPanel.add(ppText, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(223, 128), null, 0, false));
        wordList = new JList();
        wordList.setSelectionMode(0);
        scrollPane1.setViewportView(wordList);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        panel1.add(toolBar1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        newWordButton = new JButton();
        newWordButton.setText("Новое...");
        newWordButton.setMnemonic('Н');
        newWordButton.setDisplayedMnemonicIndex(0);
        toolBar1.add(newWordButton);
        delWordButton = new JButton();
        delWordButton.setText("Удалить...");
        delWordButton.setMnemonic('У');
        delWordButton.setDisplayedMnemonicIndex(0);
        toolBar1.add(delWordButton);
        final JLabel label7 = new JLabel();
        label7.setText("Основное слово:");
        panel1.add(label7, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mainWordCombo = new JComboBox();
        panel1.add(mainWordCombo, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        buttonOK.setMnemonic('O');
        buttonOK.setDisplayedMnemonicIndex(0);
        panel3.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label1.setLabelFor(ipText);
        label2.setLabelFor(rpText);
        label3.setLabelFor(dpText);
        label4.setLabelFor(vpText);
        label5.setLabelFor(tpText);
        label6.setLabelFor(ppText);
        label7.setLabelFor(mainWordCombo);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
