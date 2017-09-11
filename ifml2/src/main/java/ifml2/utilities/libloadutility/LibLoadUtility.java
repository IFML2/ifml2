package ifml2.utilities.libloadutility;

import ifml2.CommonConstants;
import ifml2.GUIUtils;
import ifml2.om.Library;
import ifml2.om.LiteralTemplateElement;
import ifml2.om.OMManager;
import ifml2.om.ObjectTemplateElement;
import ifml2.om.Parameter;
import ifml2.om.Procedure;
import ifml2.om.Template;
import ifml2.vm.instructions.ShowMessageInstr;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import static ifml2.om.Word.GramCase.VP;
import static ifml2.vm.instructions.ShowMessageInstr.Type.EXPRESSION;
import static ifml2.vm.instructions.ShowMessageInstr.Type.TEXT;

/**
 * Lib load utility main frame
 *
 * @author realsonic on 15.03.14.
 */
public class LibLoadUtility extends JFrame {
    private final String currentDirectoryPath = System.getProperty("user.dir");
    private JTextField textFileTextField;
    private JButton selectTextFileButton;
    private JTextField libTextField;
    private JButton selectLibButton;
    private JTextField saveLibTextField;
    private JButton saveLibButton;
    private JButton startButton;
    private JTextPane logTextPane;
    private JPanel contentPane;
    private JButton stopButton;
    private JButton clearLogButton;
    private File textFile;
    private File libFile;
    private File saveLibFile;
    private SwingWorker<Void, String> swingWorker;

    public LibLoadUtility() {
        super("Утилита заполнения библиотек");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setContentPane(contentPane);
        GUIUtils.packAndCenterWindow(this);

        selectTextFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // load text file
                JFileChooser openTextDialog = new JFileChooser(currentDirectoryPath);
                if (openTextDialog.showOpenDialog(LibLoadUtility.this) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = openTextDialog.getSelectedFile();
                    if (selectedFile.exists()) {
                        setTextFile(selectedFile);
                    } else {
                        JOptionPane.showMessageDialog(LibLoadUtility.this, "Файл для загрузки не существует.");
                    }
                }
            }
        });
        selectLibButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // load lib file
                String libPath = new File(currentDirectoryPath, CommonConstants.LIBS_FOLDER).getPath();
                JFileChooser openLibDialog = new JFileChooser(libPath);
                if (openLibDialog.showOpenDialog(LibLoadUtility.this) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = openLibDialog.getSelectedFile();
                    if (selectedFile.exists()) {
                        setLibFile(selectedFile);
                    } else {
                        JOptionPane.showMessageDialog(LibLoadUtility.this, "Файл библиотеки не существует.");
                    }
                }
            }
        });
        saveLibButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // choose filled lib file
                JFileChooser saveLibDialog = new JFileChooser(currentDirectoryPath);
                if (libFile != null) {
                    saveLibDialog.setSelectedFile(new File("filled_" + libFile.getName()));
                }
                if (saveLibDialog.showOpenDialog(LibLoadUtility.this) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = saveLibDialog.getSelectedFile();
                    if (selectedFile.exists()) {
                        if (JOptionPane.showConfirmDialog(LibLoadUtility.this, "Файл библиотеки уже существует. Перезаписать?",
                                "Уже существует", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) ==
                                JOptionPane.NO_OPTION) {
                            return;
                        }
                    }
                    setSaveLibFile(selectedFile);
                }
            }
        });
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textFile == null) {
                    JOptionPane.showMessageDialog(LibLoadUtility.this, "Не выбран файл загрузки", "Нет файла загрузки",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (libFile == null) {
                    JOptionPane.showMessageDialog(LibLoadUtility.this, "Не выбран файл библиотеки", "Нет библиотеки",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (saveLibFile == null) {
                    JOptionPane.showMessageDialog(LibLoadUtility.this, "Не задан файл сохранения библиотеки", "Некуда сохранять",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                swingWorker = new SwingWorker<Void, String>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        try {
                            log("Старт...");

                            // load library
                            log("Грузим либу {0}...", libFile.getAbsolutePath());
                            Library library = OMManager.loadLibrary(libFile);
                            log("Загружена.");

                            // process text file...
                            log("\nОбработка файла {0}...", textFile.getAbsolutePath());

                            BufferedReader bufferedReader = new BufferedReader(
                                    new InputStreamReader(new FileInputStream(textFile), "UTF8"));
                            int lineNo = 0;
                            String lineStr;
                            while ((lineStr = bufferedReader.readLine()) != null) {
                                lineNo++;
                                log("\nОбработка строки №{0}: {1}", lineNo, lineStr);

                                String[] parts = lineStr.split("\\|");
                                if (parts.length < 3) {
                                    log("В строке меньше 3х частей => строка отброшена.");
                                    continue;
                                } else if (parts.length > 3) {
                                    log("В строке больше 3х частей => строка отброшена.");
                                    continue;
                                }
                                String verb = parts[0];
                                String type = parts[1];
                                String message = parts[2];
                                log("Разбивка строки:\n\tглагол: {0}\n\tтип: {1}\n\tсообщение: {2}", verb, type, message);

                                ShowMessageInstr.Type messageType;
                                if ("текст".equalsIgnoreCase(type)) {
                                    messageType = TEXT;
                                } else if ("выражение".equalsIgnoreCase(type)) {
                                    messageType = EXPRESSION;
                                } else {
                                    log("\tТип не текст и не выражение => строка отброшена.");
                                    continue;
                                }

                                // create instr
                                ShowMessageInstr showMessageInstr = new ShowMessageInstr();
                                showMessageInstr.setType(messageType);
                                showMessageInstr.setBeginWithCap(true);
                                if (messageType == TEXT) {
                                    showMessageInstr.setMessageExpr(message);
                                } else {
                                    // get constant and var
                                    String[] messParts = message.split("\\+");
                                    if (messParts.length < 2) {
                                        log("В сообщении не найдена запятая, невозможно найти выражение => строка отброшена.");
                                        continue;
                                    } else if (messParts.length > 2) {
                                        log("В сообщении больше одной запятой, невозможно найти выражение => строка отброшена.");
                                        continue;
                                    }
                                    String constPart = messParts[0];
                                    String exprPart = messParts[1];

                                    // set expr
                                    showMessageInstr.setMessageExpr("'" + constPart + " ' + " + exprPart);
                                }
                                // create procedure
                                String procedureName = messageType == TEXT ? verb : verb + "Предмет";
                                Procedure procedure = new Procedure(procedureName);
                                if (messageType == EXPRESSION) {
                                    // add parameter to procedure
                                    Parameter parameter = new Parameter();
                                    parameter.setName("предмет");
                                    procedure.getParameters().add(parameter);
                                }
                                procedure.getInstructions().add(showMessageInstr);
                                library.procedures.add(procedure);
                                log("Добавлена/заменена процедура \"{0}\".", procedure.getName());

                                // create Template
                                Template template = new Template();
                                LiteralTemplateElement verbLit = new LiteralTemplateElement();
                                verbLit.getSynonyms().add(verb);
                                template.getElements().add(verbLit);
                                if (messageType == EXPRESSION) {
                                    // add object element
                                    ObjectTemplateElement itemTem = new ObjectTemplateElement();
                                    itemTem.setGramCase(VP);
                                    itemTem.setParameter("предмет");
                                    template.getElements().add(itemTem);
                                }

                                // create action
                                ifml2.om.Action action = new ifml2.om.Action();
                                String actionName = messageType == TEXT ? verb : verb + " [что]";
                                action.setName(actionName);
                                action.setDescription(procedureName);
                                action.getTemplates().add(template);
                                action.getProcedureCall().setProcedure(procedure);
                                library.actions.add(action);
                                log("Добавлено действие \"{0}\" с шаблоном {1} и ссылкой на процу \"{2}\".", action.getName(), template,
                                        action.getProcedureCall().getProcedure().getName());
                            }
                            log("\nФайл закончился.");

                            //String libNewFile = "filled_" + selectedLib.getName();
                            log("\nСохраняем библиотеку под именем {0}.", saveLibFile.getAbsolutePath());
                            OMManager.saveLib(library, saveLibFile);
                            log("Библиотека сохранёна.");

                            log("Стоп.");

                            return null;
                        } catch (Throwable e) {
                            log("ОШИБКА!!! {0}\n{1}", e.toString(), Arrays.toString(e.getStackTrace()));
                            return null;
                        }
                    }

                    private void log(String text, Object... args) {
                        publish(MessageFormat.format(text, args));
                    }

                    @Override
                    protected void process(List<String> chunks) {
                        for (String text : chunks) {
                            logMessage(text);
                        }
                    }

                    @Override
                    protected void done() {
                        // switch buttons on (stop - off)
                        selectTextFileButton.setEnabled(true);
                        selectLibButton.setEnabled(true);
                        saveLibButton.setEnabled(true);
                        startButton.setEnabled(true);
                        stopButton.setEnabled(false);
                    }
                };

                // switch buttons off (stop - on)
                selectTextFileButton.setEnabled(false);
                selectLibButton.setEnabled(false);
                saveLibButton.setEnabled(false);
                startButton.setEnabled(false);
                stopButton.setEnabled(true);

                // start worker
                swingWorker.execute();
            }
        });
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                swingWorker.cancel(true);
            }
        });
        clearLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Document document = logTextPane.getDocument();
                try {
                    document.remove(0, document.getLength());
                } catch (BadLocationException ex) {
                    JOptionPane.showMessageDialog(LibLoadUtility.this, ex.getMessage());
                }
            }
        });
    }

    public static void main(String[] args) {
        LibLoadUtility libLoadUtility = new LibLoadUtility();
        libLoadUtility.setVisible(true);
    }

    private void logMessage(String text) {
        //logTextArea.append(text + "\n\r");
        Document document = logTextPane.getDocument();
        try {
            document.insertString(document.getLength(), text + "\n\r", null);
        } catch (BadLocationException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    public void setTextFile(@NotNull File textFile) {
        this.textFile = textFile;

        // update view
        textFileTextField.setText(textFile.getAbsolutePath());
    }

    public void setLibFile(@NotNull File libFile) {
        this.libFile = libFile;

        // update view
        libTextField.setText(libFile.getAbsolutePath());
    }

    public void setSaveLibFile(@NotNull File saveLibFile) {
        this.saveLibFile = saveLibFile;

        // update view
        saveLibTextField.setText(saveLibFile.getAbsolutePath());
    }
}
