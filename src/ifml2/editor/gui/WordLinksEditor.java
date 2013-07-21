package ifml2.editor.gui;

import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.om.Word;
import ifml2.om.WordLinks;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

public class WordLinksEditor extends JDialog
{
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

    private static final String DICTIONARY_EDITOR_TITLE = "Словарь";
    private static final String CASE_DOC_PROPERTY = "case";
    private static final String WORD_IP_QUERY_PROMPT = "Именительный падеж:";
    private static final String DUPLICATED_WORD_ERROR_MESSAGE = "Такое слово уже есть в словаре, оно будет использовано";
    private static final String DUPLICATED_WORD_ERROR_DIALOG_TITLE = "Дубликат";
    private static final String WORD_DELETION_QUERY_PROMPT = "Вы уверены, что хотите удалить это слово из словаря?";
    private static final String WRONG_DOC_PROP_SYSTEM_ERROR = "Системная ошибка: неверное свойство case у DocumentEvent.getDocument() в wordDocListener";

    private boolean isUpdatingText = false;
    private HashMap<String, Word> dictionary = null;
    private ArrayList<Word> wordsClone = null;
    private boolean isOk = false;

    public WordLinksEditor(Window owner)
    {
        super(owner, DICTIONARY_EDITOR_TITLE,  ModalityType.DOCUMENT_MODAL);

        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        GUIUtils.packAndCenterWindow(this);

        buttonOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onOK();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


        wordList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                repaintCurrentWord((Word) wordList.getSelectedValue());
            }
        });

        DocumentListener wordDocListener = new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                try
                {
                    updateCurrentWord(e.getDocument());
                }
                catch (IFML2EditorException e1)
                {
                    GUIUtils.showErrorMessage(WordLinksEditor.this, e1);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                try
                {
                    updateCurrentWord(e.getDocument());
                }
                catch (IFML2EditorException e1)
                {
                    GUIUtils.showErrorMessage(WordLinksEditor.this, e1);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                //do nothing
            }
        };

        ipText.getDocument().putProperty(CASE_DOC_PROPERTY, Word.GramCaseEnum.IP);
        ipText.getDocument().addDocumentListener(wordDocListener);

        rpText.getDocument().putProperty(CASE_DOC_PROPERTY, Word.GramCaseEnum.RP);
        rpText.getDocument().addDocumentListener(wordDocListener);

        dpText.getDocument().putProperty(CASE_DOC_PROPERTY, Word.GramCaseEnum.DP);
        dpText.getDocument().addDocumentListener(wordDocListener);

        vpText.getDocument().putProperty(CASE_DOC_PROPERTY, Word.GramCaseEnum.VP);
        vpText.getDocument().addDocumentListener(wordDocListener);

        tpText.getDocument().putProperty(CASE_DOC_PROPERTY, Word.GramCaseEnum.TP);
        tpText.getDocument().addDocumentListener(wordDocListener);

        ppText.getDocument().putProperty(CASE_DOC_PROPERTY, Word.GramCaseEnum.PP);
        ppText.getDocument().addDocumentListener(wordDocListener);

        newWordButton.setAction(new AbstractAction("Новое...", GUIUtils.ADD_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String newWordIp = JOptionPane.showInputDialog(WORD_IP_QUERY_PROMPT);
                if (newWordIp != null && !"".equals(newWordIp))
                {
                    Word word = new Word(newWordIp);
                    if (dictionary.containsKey(newWordIp))
                    {
                        JOptionPane.showMessageDialog(WordLinksEditor.this, DUPLICATED_WORD_ERROR_MESSAGE, DUPLICATED_WORD_ERROR_DIALOG_TITLE,
                                JOptionPane.INFORMATION_MESSAGE);
                        word = dictionary.get(newWordIp);
                    }
                    else
                    {
                        dictionary.put(newWordIp, word);
                    }
                    wordsClone.add(word);
                    updateLinksAndMain(word);
                }
            }
        });

        delWordButton.setAction(new AbstractAction("Удалить", GUIUtils.DEL_ELEMENT_ICON)
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Word word = (Word) wordList.getSelectedValue();
                if (word != null)
                {
                    int answer = JOptionPane.showConfirmDialog(getContentPane(), WORD_DELETION_QUERY_PROMPT);
                    if (answer == JOptionPane.YES_OPTION)
                    {
                        //dictionary.values().remove(word);
                        wordsClone.remove(word);
                        updateLinksAndMain(word);
                    }
                }
            }
        });
    }

    private void updateLinksAndMain(Word word)
    {
        updateWordLinks(word);
        updateMainWord();
    }

    private void updateMainWord()
    {
        Object selectedMainWord = mainWordCombo.getSelectedItem();
        mainWordCombo.setModel(new DefaultComboBoxModel(wordsClone.toArray()));
        mainWordCombo.setSelectedItem(selectedMainWord);
    }

    private void updateWordLinks(Word word)
    {
        updateWordLinks();
        wordList.setSelectedValue(word, true);
    }

    private void updateCurrentWord(Document document) throws IFML2EditorException
    {
        if (isUpdatingText)
        {
            return;
        }

        Word word = (Word) wordList.getSelectedValue();
        if(word != null)
        {

            String text = null;
            try
            {
                text = document.getText(0, document.getLength());
            }
            catch (BadLocationException e)
            {
                GUIUtils.showErrorMessage(WordLinksEditor.this, e);
            }

            if (text != null)
            {
                text = text.trim();
            }

            Word.GramCaseEnum gramCase = (Word.GramCaseEnum) document.getProperty(CASE_DOC_PROPERTY);
            switch (gramCase)
            {
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

    private void repaintCurrentWord(Word word)
    {
        isUpdatingText = true;
        try
        {
            if(word != null)
            {
                casesPanel.setBorder(new TitledBorder(word.ip));
            }
            ipText.setText(word != null ? word.ip : "");
            rpText.setText(word != null ? word.rp : "");
            dpText.setText(word != null ? word.dp : "");
            vpText.setText(word != null ? word.vp : "");
            tpText.setText(word != null ? word.tp : "");
            ppText.setText(word != null ? word.pp : "");
        }
        finally
        {
            isUpdatingText = false;
        }
    }

    private void onOK()
    {
        isOk = true;
        dispose();
    }

    private void onCancel()
    {
        isOk = false;
        dispose();
    }

    public void setAllData(HashMap<String, Word> dictionary, WordLinks wordLinks)
    {
        this.dictionary = dictionary;

        wordsClone = new ArrayList<Word>(wordLinks.getWords());
        updateWordLinks(null);
        updateMainWord();
        mainWordCombo.setSelectedItem(wordLinks.getMainWord());
    }

    private void updateWordLinks()
    {
        DefaultListModel wordLinksListModel = new DefaultListModel();
        for(Word word : wordsClone)
        {
            wordLinksListModel.addElement(word);
        }
        wordList.setModel(wordLinksListModel);
    }

    //TODO: привести редактор в порядок; сделать транзакционным! позволить редактировать ИП - через обновление HashMap словаря

    public void getData(WordLinks wordLinks)
    {
        wordLinks.getWords().clear();
        wordLinks.getWords().addAll(wordsClone);
        wordLinks.setMainWord((Word) mainWordCombo.getSelectedItem());
    }

    public boolean showDialog()
    {
        setVisible(true);
        return isOk;
    }
}
