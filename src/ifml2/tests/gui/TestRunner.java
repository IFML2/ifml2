package ifml2.tests.gui;

import ifml2.CommonConstants;
import ifml2.CommonUtils;
import ifml2.GUIUtils;
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

import static ifml2.CommonConstants.RUSSIAN_PRODUCT_NAME;
import static java.lang.String.format;

public class TestRunner extends JFrame
{
    private final TestManager testManager = new TestManager();
    private final ArrayList<ListDataListener> commandsListDataListeners = new ArrayList<>();
    private JList<IFMLTestPlan> testsList;
    private JList<String> commandsList;
    private JTextArea logText;
    private JButton loadTestsButton;
    private JButton startButton;
    private JPanel mainPanel;

    public TestRunner()
    {
        super(format("%s Тестер %s", RUSSIAN_PRODUCT_NAME, CommonUtils.getVersion()));
        setContentPane(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        GUIUtils.packAndCenterWindow(this);

        testsList.setModel(new ListModel<IFMLTestPlan>()
        {
            @Override
            public int getSize()
            {
                return testManager.getTestsListSize();
            }

            @Override
            public IFMLTestPlan getElementAt(int index)
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
        commandsList.setModel(new ListModel<String>()
        {
            @Override
            public int getSize()
            {
                return getSelectedTestSize();
            }

            @Override
            public String getElementAt(int index)
            {
                return testsList.getSelectedValue().getCommandWithAnswer(index);
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
        loadTestsButton.addActionListener(event -> {
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
                        testManager.run(text -> logText.append(text));
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

    public static void main(String[] args)
    {
        TestRunner testRunner = new TestRunner();
        testRunner.setVisible(true);
        if (args.length > 0)
        {
            testRunner.loadTestsFromPaths(args);
        }
    }

    private int getSelectedTestSize()
    {
        return testsList.getSelectedValue() != null ? testsList.getSelectedValue().getSize() : 0;
    }

    private void showError(Throwable exception)
    {
        JOptionPane.showMessageDialog(this, "Произошла ошибка:\n" + exception.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private void loadTestsFromPaths(String[] testsToLoad)
    {
        try
        {
            ArrayList<File> files = new ArrayList<>();
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