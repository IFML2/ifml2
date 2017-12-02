package ifml2.editor.gui.editors;

import static ifml2.om.Word.GramCase.DP;
import static ifml2.om.Word.GramCase.IP;
import static ifml2.om.Word.GramCase.PP;
import static ifml2.om.Word.GramCase.RP;
import static ifml2.om.Word.GramCase.TP;
import static ifml2.om.Word.GramCase.VP;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.jetbrains.annotations.NotNull;

import ifml2.GUIUtils;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.om.Word;
import ifml2.om.WordLinks;

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
                // do nothing
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
                        JOptionPane.showMessageDialog(WordLinksEditor.this, DUPLICATED_WORD_INFO_MESSAGE,
                                DUPLICATED_WORD_INFO_DIALOG_TITLE, JOptionPane.INFORMATION_MESSAGE);
                        word = dictionary.get(newWordIp);
                    } else {
                        dictionary.put(newWordIp, word);
                    }
                    wordsClone.add(word);
                    updateLinksAndMain(word);

                    // set main word in case it isn't set
                    if (mainWordCombo.getSelectedItem() == null) {
                        if (JOptionPane.showConfirmDialog(WordLinksEditor.this, SET_MAIN_WORD_QUERY_PROMPT,
                                SET_MAIN_WORD_DIALOG_TITLE, JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
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
                if (word != null && JOptionPane.showConfirmDialog(getContentPane(),
                        WORD_DELETION_QUERY_PROMPT) == JOptionPane.YES_OPTION) {
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

    // TODO: привести редактор в порядок; сделать транзакционным! позволить
    // редактировать ИП - через обновление HashMap словаря

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
}
