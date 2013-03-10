package ifml2.tests.gui;

import ifml2.CommonConstants;
import ifml2.CommonUtils;
import ifml2.GUIUtils;
import ifml2.engine.Engine;
import ifml2.interfaces.Interface;
import ifml2.tests.IFMLTestPlan;
import ifml2.tests.TestManager;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;

public class TestRunner extends JFrame
{
    private JList testsList;
    private JList commandsList;
    private JTextArea logText;
    private JButton loadTestsButton;
    private JButton startButton;
    private JPanel mainPanel;

    private final TestManager testManager = new TestManager();
    private final ArrayList<ListDataListener> commandsListDataListeners = new ArrayList<ListDataListener>();

    public TestRunner()
    {
        super("ЯРИЛ 2.0 Тестер " + Engine.ENGINE_VERSION);
        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        GUIUtils.packAndCenterWindow(this);

        testsList.setModel(new ListModel()
        {
            @Override
            public int getSize()
            {
                return testManager.getTestsListSize();
            }

            @Override
            public Object getElementAt(int index)
            {
                return testManager.getTestsListElementAt(index);
            }

            @Override
            public void addListDataListener(ListDataListener l)
            {
                testManager.addTestsListDataListener(l);
            }

            @Override
            public void removeListDataListener(ListDataListener l)
            {
                testManager.removeTestsListDataListener(l);
            }
        });
        testsList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    for (ListDataListener listener : commandsListDataListeners)
                    {
                        listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSelectedTestSize()));
                    }
                }
            }
        });
        commandsList.setModel(new ListModel()
        {
            @Override
            public int getSize()
            {
                return getSelectedTestSize();
            }

            @Override
            public Object getElementAt(int index)
            {
                return ((IFMLTestPlan) testsList.getSelectedValue()).getCommandWithAnswer(index);
            }

            @Override
            public void addListDataListener(ListDataListener l)
            {
                commandsListDataListeners.add(l);
            }

            @Override
            public void removeListDataListener(ListDataListener l)
            {
                commandsListDataListeners.remove(l);
            }
        });
        loadTestsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                JFileChooser testFileChooser = new JFileChooser(CommonUtils.getTestsDirectory());
                testFileChooser.setFileFilter(new FileFilter()
                {
                    @Override
                    public String getDescription()
                    {
                        return CommonConstants.TEST_FILE_FILTER_NAME;
                    }

                    @Override
                    public boolean accept(File file)
                    {
                        return file.isDirectory() || file.getName().toLowerCase().endsWith(CommonConstants.TEST_EXTENSION);
                    }
                });
                testFileChooser.setMultiSelectionEnabled(true);

                if (testFileChooser.showOpenDialog(TestRunner.this) != JFileChooser.APPROVE_OPTION)
                {
                    return;
                }

                File[] testFiles = testFileChooser.getSelectedFiles();

                try
                {
                    loadTestsFromFiles(testFiles);
                }
                catch (Throwable e)
                {
                    showError(e);
                }
            }
        });
        startButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                loadTestsButton.setEnabled(false);
                try
                {
                    startButton.setEnabled(false);
                    try
                    {
                        testManager.run(new Interface()
                        {
                            @Override
                            public void outputText(String text)
                            {
                                logText.append(text);
                            }

                            @Override
                            public String inputText()
                            {
                                return null;
                            }
                        });
                    }
                    finally
                    {
                        startButton.setEnabled(true);
                    }
                }
                finally
                {
                    loadTestsButton.setEnabled(true);
                }
            }
        });
    }

    private int getSelectedTestSize()
    {
        return testsList.getSelectedValue() != null ? ((IFMLTestPlan) testsList.getSelectedValue()).getSize() : 0;
    }

    private void showError(Throwable exception)
    {
        JOptionPane.showMessageDialog(this, "Произошла ошибка:\n" + exception.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args)
    {
        TestRunner testRunner = new TestRunner();
        testRunner.setVisible(true);
        if (args.length > 0)
        {
            testRunner.loadTestsFromPaths(args);
        }
    }

    private void loadTestsFromPaths(String[] testsToLoad)
    {
        try
        {
            ArrayList<File> files = new ArrayList<File>();
            for (String fileToLoad : testsToLoad)
            {
                File testFile = new File(fileToLoad);
                if (testFile.exists())
                {
                    files.add(testFile);
                }
                else
                {
                    throw new Exception(MessageFormat.format("Файл {0}, переданный в параметрах, не существует", fileToLoad));
                }
            }
            File[] filesArray = new File[files.size()];
            filesArray = files.toArray(filesArray);
            testManager.loadTestsFromFiles(filesArray);
        }
        catch (Throwable e)
        {
            showError(e);
        }
    }

    private void loadTestsFromFiles(File[] files)
    {
        try
        {
            testManager.loadTestsFromFiles(files);
        }
        catch (Throwable e)
        {
            showError(e);
        }
    }
}