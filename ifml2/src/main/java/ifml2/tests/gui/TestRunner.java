package ifml2.tests.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import ifml2.CommonConstants;
import ifml2.CommonUtils;
import ifml2.GUIUtils;
import ifml2.engine.EngineVersion;
import ifml2.tests.IFMLTestPlan;
import ifml2.tests.TestManager;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;

import static ifml2.CommonConstants.RUSSIAN_PRODUCT_NAME;
import static java.lang.String.format;

public class TestRunner extends JFrame {
    private final TestManager testManager = new TestManager();
    private final ArrayList<ListDataListener> commandsListDataListeners = new ArrayList<>();
    private JList<IFMLTestPlan> testsList;
    private JList<String> commandsList;
    private JTextArea logText;
    private JButton loadTestsButton;
    private JButton startButton;
    private JPanel mainPanel;

    public TestRunner() {
        super(format("%s Тестер %s", RUSSIAN_PRODUCT_NAME, EngineVersion.VERSION));
        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        GUIUtils.packAndCenterWindow(this);

        testsList.setModel(new ListModel<IFMLTestPlan>() {
            @Override
            public int getSize() {
                return testManager.getTestsListSize();
            }

            @Override
            public IFMLTestPlan getElementAt(int index) {
                return testManager.getTestsListElementAt(index);
            }

            @Override
            public void addListDataListener(ListDataListener l) {
                testManager.addTestsListDataListener(l);
            }

            @Override
            public void removeListDataListener(ListDataListener l) {
                testManager.removeTestsListDataListener(l);
            }
        });
        testsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    for (ListDataListener listener : commandsListDataListeners) {
                        listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSelectedTestSize()));
                    }
                }
            }
        });
        commandsList.setModel(new ListModel<String>() {
            @Override
            public int getSize() {
                return getSelectedTestSize();
            }

            @Override
            public String getElementAt(int index) {
                return testsList.getSelectedValue().getCommandWithAnswer(index);
            }

            @Override
            public void addListDataListener(ListDataListener l) {
                commandsListDataListeners.add(l);
            }

            @Override
            public void removeListDataListener(ListDataListener l) {
                commandsListDataListeners.remove(l);
            }
        });
        loadTestsButton.addActionListener(event -> {
            JFileChooser testFileChooser = new JFileChooser(CommonUtils.getTestsDirectory());
            testFileChooser.setFileFilter(new FileFilter() {
                @Override
                public String getDescription() {
                    return CommonConstants.TEST_FILE_FILTER_NAME;
                }

                @Override
                public boolean accept(File file) {
                    return file.isDirectory() || file.getName().toLowerCase().endsWith(CommonConstants.TEST_EXTENSION);
                }
            });
            testFileChooser.setMultiSelectionEnabled(true);

            if (testFileChooser.showOpenDialog(TestRunner.this) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File[] testFiles = testFileChooser.getSelectedFiles();

            try {
                loadTestsFromFiles(testFiles);
            } catch (Throwable e) {
                showError(e);
            }
        });
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                loadTestsButton.setEnabled(false);
                try {
                    startButton.setEnabled(false);
                    try {
                        testManager.run(text -> logText.append(text));
                    } finally {
                        startButton.setEnabled(true);
                    }
                } finally {
                    loadTestsButton.setEnabled(true);
                }
            }
        });
    }

    public static void main(String[] args) {
        TestRunner testRunner = new TestRunner();
        testRunner.setVisible(true);
        if (args.length > 0) {
            testRunner.loadTestsFromPaths(args);
        }
    }

    private int getSelectedTestSize() {
        return testsList.getSelectedValue() != null ? testsList.getSelectedValue().getSize() : 0;
    }

    private void showError(Throwable exception) {
        JOptionPane.showMessageDialog(this, "Произошла ошибка:\n" + exception.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private void loadTestsFromPaths(String[] testsToLoad) {
        try {
            ArrayList<File> files = new ArrayList<>();
            for (String fileToLoad : testsToLoad) {
                File testFile = new File(fileToLoad);
                if (testFile.exists()) {
                    files.add(testFile);
                } else {
                    throw new Exception(MessageFormat.format("Файл {0}, переданный в параметрах, не существует", fileToLoad));
                }
            }
            File[] filesArray = new File[files.size()];
            filesArray = files.toArray(filesArray);
            testManager.loadTestsFromFiles(filesArray);
        } catch (Throwable e) {
            showError(e);
        }
    }

    private void loadTestsFromFiles(File[] files) {
        try {
            testManager.loadTestsFromFiles(files);
        } catch (Throwable e) {
            showError(e);
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
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        mainPanel.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        loadTestsButton = new JButton();
        loadTestsButton.setText("Загрузить тесты...");
        loadTestsButton.setMnemonic('З');
        loadTestsButton.setDisplayedMnemonicIndex(0);
        toolBar1.add(loadTestsButton);
        startButton = new JButton();
        startButton.setText("Запустить тесты");
        startButton.setMnemonic('Т');
        startButton.setDisplayedMnemonicIndex(10);
        toolBar1.add(startButton);
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation(200);
        splitPane1.setOrientation(0);
        mainPanel.add(splitPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(600, 400), null, 0, false));
        final JSplitPane splitPane2 = new JSplitPane();
        splitPane1.setLeftComponent(splitPane2);
        final JScrollPane scrollPane1 = new JScrollPane();
        splitPane2.setLeftComponent(scrollPane1);
        scrollPane1.setBorder(BorderFactory.createTitledBorder("Тесты"));
        testsList = new JList();
        Font testsListFont = this.$$$getFont$$$("Segoe UI", -1, 14, testsList.getFont());
        if (testsListFont != null) testsList.setFont(testsListFont);
        scrollPane1.setViewportView(testsList);
        final JScrollPane scrollPane2 = new JScrollPane();
        splitPane2.setRightComponent(scrollPane2);
        scrollPane2.setBorder(BorderFactory.createTitledBorder("Команды выбранного теста"));
        commandsList = new JList();
        Font commandsListFont = this.$$$getFont$$$("Segoe UI", -1, 14, commandsList.getFont());
        if (commandsListFont != null) commandsList.setFont(commandsListFont);
        scrollPane2.setViewportView(commandsList);
        final JScrollPane scrollPane3 = new JScrollPane();
        splitPane1.setRightComponent(scrollPane3);
        scrollPane3.setBorder(BorderFactory.createTitledBorder("Лог выполнения теста"));
        logText = new JTextArea();
        logText.setEditable(false);
        Font logTextFont = this.$$$getFont$$$("Segoe UI", -1, 14, logText.getFont());
        if (logTextFont != null) logText.setFont(logTextFont);
        logText.setLineWrap(true);
        logText.setWrapStyleWord(true);
        scrollPane3.setViewportView(logText);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}