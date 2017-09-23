package ifml2.tests;

import ifml2.IFML2Exception;
import ifml2.engine.Engine;
import ifml2.engine.featureproviders.text.IOutputPlainTextProvider;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;

public class TestManager {
    private final ArrayList<IFMLTestPlan> testPlans = new ArrayList<>();
    private final ArrayList<ListDataListener> testsListDataListeners = new ArrayList<>();
    private IOutputPlainTextProvider outputPlainTextProvider;

    ArrayList<IFMLTestPlan> getTestPlans() {
        return testPlans;
    }

    public void loadTestsFromFiles(File[] files) throws Exception {
        try {
            testPlans.clear();
            JAXBContext context = JAXBContext.newInstance(IFMLTestPlan.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            for (File file : files) {
                try {
                    IFMLTestPlan testPlan = (IFMLTestPlan) unmarshaller.unmarshal(file);
                    testPlan.testFile = file.getAbsolutePath();
                    testPlans.add(testPlan);
                } catch (JAXBException e) {
                    throw new Exception(e);
                }
            }
        } finally {
            fireTestsListListeners();
        }
    }

    private void fireTestsListListeners() {
        for (ListDataListener listDataListener : testsListDataListeners) {
            listDataListener.contentsChanged(new ListDataEvent(testPlans, ListDataEvent.CONTENTS_CHANGED, 0, testPlans.size() - 1));
        }
    }

    public int getTestsListSize() {
        return testPlans.size();
    }

    public IFMLTestPlan getTestsListElementAt(int index) {
        return (index <= testPlans.size() - 1) ? testPlans.get(index) : null;
    }

    public void addTestsListDataListener(ListDataListener listDataListener) {
        testsListDataListeners.add(listDataListener);
    }

    public void removeTestsListDataListener(ListDataListener listDataListener) {
        testsListDataListeners.remove(listDataListener);
    }

    public void run(IOutputPlainTextProvider outputPlainTextProvider) {
        this.outputPlainTextProvider = outputPlainTextProvider;

        log("=== Запуск тестов ===");
        String[] outText = {""};
        Engine engine = new Engine((IOutputPlainTextProvider) text -> outText[0] += ((outText[0].length() > 0) ? '\n' : "") + text);

        int plansSuccess = 0;
        for (IFMLTestPlan testPlan : getTestPlans()) {
            try {
                String testFile = testPlan.testFile;
                String storyLink = testPlan.storyLink;
                String storyFile = new File(testFile).getParent() + storyLink;
                log("--- Загрузка теста {0} для файла истории {1} ---", testPlan.name, storyFile);
                engine.loadStory(storyFile, false);
                engine.initGame();

                int cmdCnt = 0;
                int cmdSuccess = 0;
                for (IFMLTestIteration testIteration : testPlan.test.testIterations) {
                    log("Тест команды №{0}: \"{1}\" > \"{2}\"", ++cmdCnt, testIteration.command, testIteration.answer);

                    outText[0] = "";
                    engine.executeGamerCommand(testIteration.command);

                    String outString = outText[0].trim();
                    //outString = outString.replaceAll("\t|\n|\r", "");
                    String answer = (testIteration.answer != null) ? testIteration.answer.trim()/*.replaceAll("\t|\n|\r", "")*/ : "";
                    if (answer.equalsIgnoreCase(outString)) {
                        cmdSuccess++;
                        log("Ответ ожидаемый.");
                    } else {
                        log("Ответ не соответствует ожидаемому (\"{0}\").", outString);
                    }
                }
                if (cmdSuccess == testPlan.test.testIterations.size()) {
                    plansSuccess++;
                }
                log("--- Завершено успешно {0} из {1} команд ---", cmdSuccess, testPlan.test.testIterations.size());
            } catch (IFML2Exception e) {
                log("Ошибка! " + e.getMessage());
            }
            log("=== Завершено успешно {0} из {1} тестов ===", plansSuccess, testPlans.size());
        }
    }

    private void log(String message, Object... arguments) {
        String formattedMessage = MessageFormat.format(message, arguments);
        outputPlainTextProvider.outputPlainText(MessageFormat.format("[{0}] {1}\n", new Date(), formattedMessage));
    }
}
