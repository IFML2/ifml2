package ifml2.editor.gui.editors;

import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.om.Word;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

public class DictionaryEditor extends JDialog {
    private static final String DICTIONARY_EDITOR_TITLE = "Словарь";
    private static final String CASE_DOC_PROPERTY = "case";
    private static final String WORD_IP_QUERY_PROMPT = "Именительный падеж:";
    private static final String DUPLICATED_WORD_ERROR_MESSAGE = "Такое слово уже есть в словаре!";
    private static final String DUPLICATED_WORD_ERROR_DIALOG_TITLE = "Дубликат";
    private static final String WORD_DELETION_QUERY_PROMPT = "Вы уверены, что хотите удалить это слово из словаря?";
    private static final String WRONG_DOC_PROP_SYSTEM_ERROR = "Системная ошибка: неверное свойство case у DocumentEvent.getDocument() в wordDocListener";
    private JPanel contentPane;
    private JButton buttonOK;
    private JList dictList;
    private JTextField ipText;
    private JTextField rpText;
    private JTextField dpText;
    private JTextField vpText;
    private JTextField tpText;
    private JTextField ppText;
    private JButton newWordButton;
    private JButton delWordButton;
    private boolean isUpdatingText = false;
    private HashMap<String, Word> dictionary = null;

    public DictionaryEditor(Frame owner) {
        super(owner, DICTIONARY_EDITOR_TITLE, ModalityType.DOCUMENT_MODAL);

        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        GUIUtils.packAndCenterWindow(this);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


        dictList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                repaintCurrentWord((Word) dictList.getSelectedValue());
            }
        });

        DocumentListener wordDocListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    updateCurrentWord(e.getDocument());
                } catch (IFML2EditorException ex) {
                    GUIUtils.showErrorMessage(DictionaryEditor.this, ex);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                try {
                    updateCurrentWord(e.getDocument());
                } catch (IFML2EditorException ex) {
                    GUIUtils.showErrorMessage(DictionaryEditor.this, ex);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                //do nothing
            }
        };

        ipText.getDocument().putProperty(CASE_DOC_PROPERTY, Word.GramCase.IP);
        ipText.getDocument().addDocumentListener(wordDocListener);

        rpText.getDocument().putProperty(CASE_DOC_PROPERTY, Word.GramCase.RP);
        rpText.getDocument().addDocumentListener(wordDocListener);

        dpText.getDocument().putProperty(CASE_DOC_PROPERTY, Word.GramCase.DP);
        dpText.getDocument().addDocumentListener(wordDocListener);

        vpText.getDocument().putProperty(CASE_DOC_PROPERTY, Word.GramCase.VP);
        vpText.getDocument().addDocumentListener(wordDocListener);

        tpText.getDocument().putProperty(CASE_DOC_PROPERTY, Word.GramCase.TP);
        tpText.getDocument().addDocumentListener(wordDocListener);

        ppText.getDocument().putProperty(CASE_DOC_PROPERTY, Word.GramCase.PP);
        ppText.getDocument().addDocumentListener(wordDocListener);

        newWordButton.setAction(new AbstractAction("Новое...", GUIUtils.ADD_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newWordIp = JOptionPane.showInputDialog(WORD_IP_QUERY_PROMPT);
                if (newWordIp != null && !"".equals(newWordIp)) {
                    Word word = new Word(newWordIp);
                    if (dictionary.containsKey(newWordIp)) {
                        JOptionPane.showMessageDialog(DictionaryEditor.this, DUPLICATED_WORD_ERROR_MESSAGE, DUPLICATED_WORD_ERROR_DIALOG_TITLE,
                                JOptionPane.ERROR_MESSAGE);
                        dictList.setSelectedValue(dictionary.get(newWordIp), true);
                        return;
                    }
                    dictionary.put(newWordIp, word);
                    setAllData(dictionary);
                    dictList.setSelectedValue(word, true);
                }
            }
        });

        delWordButton.setAction(new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Word word = (Word) dictList.getSelectedValue();
                if (word != null) {
                    int answer = JOptionPane.showConfirmDialog(getContentPane(), WORD_DELETION_QUERY_PROMPT);
                    if (answer == JOptionPane.YES_OPTION) {
                        dictionary.values().remove(word);
                        setAllData(dictionary);
                        dictList.setSelectedValue(word, true);
                    }
                }
            }
        });
    }

    private void updateCurrentWord(Document document) throws IFML2EditorException {
        Word word = (Word) dictList.getSelectedValue();
        if (word != null) {
            if (isUpdatingText) {
                return;
            }

            Word.GramCase gramCase = (Word.GramCase) document.getProperty(CASE_DOC_PROPERTY);
            switch (gramCase) {
                case IP:
                    word.ip = ipText.getText();
                    break;
                case RP:
                    word.rp = rpText.getText();
                    break;
                case DP:
                    word.dp = dpText.getText();
                    break;
                case VP:
                    word.vp = vpText.getText();
                    break;
                case TP:
                    word.tp = tpText.getText();
                    break;
                case PP:
                    word.pp = ppText.getText();
                    break;
                default:
                    throw new IFML2EditorException(WRONG_DOC_PROP_SYSTEM_ERROR);
            }
        }
    }

    private void repaintCurrentWord(Word word) {
        try {
            isUpdatingText = true;
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

    private void onOK() {
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    void setAllData(HashMap<String, Word> dictionary) {
        this.dictionary = dictionary;
        DefaultListModel dictListModel = new DefaultListModel();
        for (Word word : dictionary.values()) {
            dictListModel.addElement(word);
        }
        dictList.setModel(dictListModel);
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

}
