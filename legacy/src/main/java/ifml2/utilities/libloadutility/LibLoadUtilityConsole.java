package ifml2.utilities.libloadutility;

import static ifml2.om.Word.GramCase.VP;
import static ifml2.vm.instructions.ShowMessageInstr.Type.EXPRESSION;
import static ifml2.vm.instructions.ShowMessageInstr.Type.TEXT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import ifml2.IFML2Exception;
import ifml2.om.Action;
import ifml2.om.Library;
import ifml2.om.LiteralTemplateElement;
import ifml2.om.OMManager;
import ifml2.om.ObjectTemplateElement;
import ifml2.om.Parameter;
import ifml2.om.Procedure;
import ifml2.om.Template;
import ifml2.vm.instructions.ShowMessageInstr;

public class LibLoadUtilityConsole {
    private static void log(String message, Object... args) {
        System.out.println(MessageFormat.format(message, args));
    }

    public static void main(String[] args) throws IFML2Exception, IOException, JAXBException {
        log("Утилита запущена.");

        // load text file
        String currentDirectoryPath = System.getProperty("user.dir");
        JFileChooser openTextDialog = new JFileChooser(currentDirectoryPath);
        if (openTextDialog.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(null, "Файл для загрузки не выбран. Завершение работы.");
            return;
        }
        File textFile = openTextDialog.getSelectedFile();
        if (!textFile.exists()) {
            JOptionPane.showMessageDialog(null, "Файл для загрузки не существует. Завершение работы.");
            return;
        }

        // choose lib from folder
        File libFolder = new File(currentDirectoryPath + "\\libs\\");
        File[] libs = libFolder.listFiles();
        File selectedLib = (File) JOptionPane.showInputDialog(null, "Выберите библиотеку для заполнения:",
                "Библиотека ЯРИЛ", JOptionPane.QUESTION_MESSAGE, null, libs, null);
        if (selectedLib == null) {
            JOptionPane.showMessageDialog(null, "Библиотека ЯРИЛ для заполнения не выбрана. Завершение работы.");
            return;
        }

        // load library
        log("Грузим либу {0}...", selectedLib);
        Library library = OMManager.loadLibrary(selectedLib.getName());
        log("Загружена.");

        // process text file...
        log("\nОбработка файла...");

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
            Action action = new Action();
            action.setName(procedureName);
            action.getTemplates().add(template);
            action.getProcedureCall().setProcedure(procedure);
            library.actions.add(action);
            log("Добавлено действие \"{0}\" с шаблоном {1} и ссылкой на процу \"{2}\".", action.getName(), template,
                    action.getProcedureCall().getProcedure().getName());
        }
        log("\nФайл закончился.");

        String libNewFile = "filled_" + selectedLib.getName();
        log("\nСохраняем библиотеку под именем {0}.", libNewFile);
        OMManager.saveLib(library, new File(libNewFile));
    }
}
