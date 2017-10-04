package ifml2.engine;

import ca.odell.glazedlists.BasicEventList;
import ifml2.CommonConstants;
import ifml2.Environment;
import ifml2.IFML2Exception;
import ifml2.SystemIdentifiers;
import ifml2.engine.featureproviders.PlayerFeatureProvider;
import ifml2.engine.featureproviders.text.OutputPlainTextProvider;
import ifml2.engine.saved.SavedGame;
import ifml2.om.Action;
import ifml2.om.Hook;
import ifml2.om.IFMLObject;
import ifml2.om.Item;
import ifml2.om.Location;
import ifml2.om.OMManager;
import ifml2.om.Procedure;
import ifml2.om.Property;
import ifml2.om.PropertyDefinition;
import ifml2.om.Restriction;
import ifml2.om.Role;
import ifml2.om.Story;
import ifml2.om.StoryOptions;
import ifml2.parser.FormalElement;
import ifml2.parser.IFML2ParseException;
import ifml2.parser.ParseResult;
import ifml2.parser.Parser;
import ifml2.vm.ExpressionCalculator;
import ifml2.vm.IFML2VMException;
import ifml2.vm.VirtualMachine;
import ifml2.vm.RunningContext;
import ifml2.vm.Variable;
import ifml2.vm.instructions.SetVarInstruction;
import ifml2.vm.values.BooleanValue;
import ifml2.vm.values.CollectionValue;
import ifml2.vm.values.NumberValue;
import ifml2.vm.values.ObjectValue;
import ifml2.vm.values.TextValue;
import ifml2.vm.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import static ifml2.engine.EngineImpl.SystemCommand.HELP;
import static java.lang.String.format;

public class EngineImpl implements Engine {

    public static final Logger LOG = LoggerFactory.getLogger(EngineImpl.class);

    private final HashMap<String, Value> globalVariables = new HashMap<>();

    private final Parser parser;
    private final VirtualMachine virtualMachine;
    private Environment environment;

    private final HashMap<String, Value> systemVariables = new HashMap<>();
    private final ArrayList<Item> abyss = new ArrayList<>();

    private ArrayList<Item> inventory = new ArrayList<>();
    private HashMap<String, SystemCommand> SYSTEM_COMMANDS = new HashMap<String, SystemCommand>() {
        {
            put("помощь", HELP);
            put("помоги", HELP);
            put("помогите", HELP);
            put("help", HELP);
            put("info", HELP);
            put("инфо", HELP);
            put("информация", HELP);
        }

        @Override
        public SystemCommand get(Object key) {
            return super.get(key.toString().toLowerCase());
        }

        @Override
        public boolean containsKey(Object key) {
            return super.containsKey(key.toString().toLowerCase());
        }

        @Override
        public SystemCommand put(String key, SystemCommand value) {
            return super.put(key.toLowerCase(), value);
        }
    };
    private DataHelper dataHelper = new DataHelper();
    private String storyFileName;
    private PlayerFeatureProvider playerFeatureProvider;
    private Date starTime = new Date();
    private HashMap<String, Callable<? extends Value>> ENGINE_SYMBOLS = new HashMap<String, Callable<? extends Value>>() {
        {
            Callable<CollectionValue> returnInv = () -> new CollectionValue(inventory);

            put("инвентарий", returnInv);
            put("инвентарь", returnInv);
            put("куча",
                    (Callable<TextValue>) () -> new TextValue(new CollectionValue(
                            new ArrayList<>(environment.getStory().getObjectsHeap().values())).toString()));
            put("словарь",
                    (Callable<TextValue>) () -> new TextValue(new CollectionValue(
                            new ArrayList<>(environment.getStory().getDictionary().values())).toString()));
            put("пустота", (Callable<CollectionValue>) () -> new CollectionValue(abyss));
            put("глобальные", (Callable<TextValue>) () -> new TextValue(globalVariables.entrySet().toString()));
            put("локации",
                    (Callable<TextValue>) () -> new TextValue(new CollectionValue(environment.getStory().getLocations()).toString()));
            put("предметы",
                    (Callable<TextValue>) () -> new TextValue(new CollectionValue(environment.getStory().getItems()).toString()));
            put("системные", new Callable<TextValue>() {
                @Override
                public TextValue call() throws Exception {
                    return new TextValue(format("Системные переменные: %s", ENGINE_SYMBOLS.keySet()));
                }
            });
            put("секунды", new Callable<NumberValue>() {
                @Override
                public NumberValue call() throws Exception {
                    Date now = new Date();
                    return new NumberValue((now.getTime() - starTime.getTime()) / 1000);
                }
            });
            put("минуты", new Callable<NumberValue>() {
                @Override
                public NumberValue call() throws Exception {
                    Date now = new Date();
                    return new NumberValue((now.getTime() - starTime.getTime()) / 1000 / 60);
                }
            });
        }
    };

    public EngineImpl(final Environment environment, final VirtualMachine virtualMachine, final Parser parser) {
        this.environment = environment;
        this.virtualMachine = virtualMachine;
        this.parser = parser;
        virtualMachine.setEngine(this);
        LOG.info("Engine created.");
    }

    public void loadStory(String storyFileName, boolean isAllowedOpenCipherFiles) throws IFML2Exception {
        LOG.info("Loading story \"{0}\"...", storyFileName);

        if (!new File(storyFileName).exists()) {
            throw new IFML2Exception("Файл истории не найден");
        }

        //TODO validate xml

        OMManager.LoadStoryResult loadStoryResult = OMManager.loadStoryFromFile(storyFileName, true, isAllowedOpenCipherFiles);
        environment.setStory(loadStoryResult.getStory());
        this.storyFileName = storyFileName;
        inventory = loadStoryResult.getInventory();

        LOG.info("Story \"{0}\" loaded", environment.getStory());
    }

    public void outText(String text, Object... args) {
        environment.outText(text);
    }

    private void outputPlainText(String text) {
        if (playerFeatureProvider instanceof OutputPlainTextProvider) {
            ((OutputPlainTextProvider) playerFeatureProvider).outputPlainText(text);
        }
    }

    public void outTextLn(String text, Object... args) {
        outText(text + "\n", args);
    }

    public void initGame() throws IFML2Exception {
        LOG.info("Initializing game...");

        Story story = environment.getStory();

        if (story == null) {
            throw new IFML2Exception("История не загружена.");
        }

        if (story.getLocations().isEmpty()) {
            throw new IFML2Exception("Локаций нет.");
        }

        // init and reset objects
        virtualMachine.init();
        systemVariables.clear();
        abyss.clear();
        starTime = new Date(); // reset time counter to now

        // load global vars
        globalVariables.clear();
        for (SetVarInstruction varInstruction : story.getStoryOptions().getVars()) {
            Value value = ExpressionCalculator.calculate(RunningContext.CreateNewContext(virtualMachine), varInstruction.getValue());
            String name = varInstruction.getName();
            if (name != null) {
                globalVariables.put(name.toLowerCase(), value);
            }
        }

        // find properties and evaluates its expression into value
        for (IFMLObject ifmlObject : story.getObjectsHeap().values()) {
            //todo: check pure object properties
            // check roles:
            for (Role role : ifmlObject.getRoles()) {
                // fill roles instances with default properties (not set in instances but defined in role definitions)
                for (PropertyDefinition propertyDefinition : role.getRoleDefinition().getPropertyDefinitions()) {
                    Property property = role.tryFindPropertyByDefinition(propertyDefinition);
                    if (property == null) {
                        // property is default (not set in role instance) -- create it in role instance
                        property = new Property(propertyDefinition, role);
                        role.getProperties().add(property);
                    }

                    // calculate property
                    property.evaluateFromPrimaryExpression(RunningContext.CreateNewContext(virtualMachine));
                }
            }
        }

        // add all "hanging" items to abyss
        story.getItems().parallelStream().filter(item -> item.getContainer() == null).forEach(item -> {
            synchronized (abyss) {
                abyss.add(item);
                item.setContainer(abyss);
            }
        });

        // show initial info
        showInitInfo(story.getStoryOptions().getStoryDescription());
        runStartProcedure(story.getStartProcedure());
        initStartLocation(story.getStartLocation());

        LOG.info("Game initialized.");
    }

    private void showInitInfo(final StoryOptions.StoryDescription storyDescription) {
        final StringBuilder sb = new StringBuilder();
        sb
            .append(storyDescription.getName() != null ? storyDescription.getName() : "<Без имени>").append('\n')
            .append("**********").append('\n')
            .append(storyDescription.getDescription() != null ? environment.getStory().getStoryOptions().getStoryDescription() : "<Без имени>")
            .append(format("ВЕРСИЯ: %s", storyDescription.getVersion() != null ? storyDescription.getVersion() : "<Без версии>"))
            .append(format("АВТОР: %s", storyDescription.getAuthor() != null ? storyDescription.getAuthor() : "<Без автора>"))
            .append("**********").append('\n');
        environment.outText(sb.toString());
    }

    private void runStartProcedure(final Procedure startProcedure) {
        if (startProcedure != null) {
            try {
                virtualMachine.runProcedureWithoutParameters(startProcedure);
            } catch (IFML2Exception e)  // should we catch it?
            {
                outTextLn(e.getMessage());
            }
        }
    }

    private void initStartLocation(final Location startLocation) throws IFML2Exception {
        if (startLocation != null) {
            setCurrentLocation(startLocation);
            if (environment.getStory().IsShowStartLocDesc()) {
                // show first location description
                virtualMachine.showLocation(getCurrentLocation());
            }
        }

        if (getCurrentLocation() == null) {
            // if current location isn't set then take any
            setCurrentLocation(environment.getStory().getAnyLocation());
        }
    }

    public boolean executeGamerCommand(String gamerCommand) {
        String trimmedCommand = gamerCommand.trim();

        StoryOptions.SystemCommandsDisableOption systemCommandsDisableOption = environment.getStory().getStoryOptions().getSystemCommandsDisableOption();

        // check system commands
        if (SYSTEM_COMMANDS.containsKey(trimmedCommand)) {
            SystemCommand systemCommand = SYSTEM_COMMANDS.get(trimmedCommand);
            switch (systemCommand) {
                case HELP:
                    if (!systemCommandsDisableOption.isDisableHelp()) {
                        outTextLn("Попробуйте одну из команд: " + environment.getStory().getAllActions());
                        return true;
                    }
            }
        }

        // check debug command
        if (environment.isDebug() && trimmedCommand.length() > 0 && trimmedCommand.charAt(0) == '?') {
            String expression = trimmedCommand.substring(1);
            try {
                Value value = ExpressionCalculator.calculate(RunningContext.CreateNewContext(virtualMachine), expression);
                environment.debug("({0}) {1}", value.getTypeName(), value);
            } catch (IFML2Exception e) {
                environment.debug("Ошибка при вычислении выражения: {0}", e.getMessage());
            }
            return true;
        }

        // switch debug mode
        if ("!отладка".equalsIgnoreCase(trimmedCommand) && !systemCommandsDisableOption.isDisableDebug()) {
            environment.debugToggle();
            environment.debug("Режим отладки {}.", environment.isDebug() ? "включен" : "выключен");
            return true;
        }

        ParseResult parseResult;
        try {
            outEngDebug("Анализируем команду игрока \"{0}\"...", trimmedCommand);
            parseResult = parser.parse(trimmedCommand, environment.getStory().getDataHelper(), dataHelper);
            outEngDebug("Анализ завершился успешно. Результат анализа команды: {0}.", parseResult);

            Action action = parseResult.getAction();
            List<FormalElement> formalElements = parseResult.getFormalElements();

            // check hooks & run procedure
            outEngDebug("Поиск перехватов для действия \"{0}\"...", action);

            HashMap<Hook.Type, List<Hook>> objectHooks = collectObjectHooks(action, formalElements);
            //outEngDebug("Кол-во найденных перехватов на предмете - {0}.", objectHooks.size()); // нужно выводить не кол-во списков перехватов,
            // а само кол-во перехватов

            HashMap<Hook.Type, List<Hook>> locationHooks = collectLocationHooks(action);
            //outEngDebug("Кол-во найденных перехватов в локации - {0}.", locationHooks.size()); // нужно выводить не кол-во списков перехватов,
            // а само кол-во перехватов

            List<Variable> parameters = convertFormalElementsToParameters(formalElements);

            // if there are INSTEAD hooks then fire them and finish
            int itemInsteadHooksQty = objectHooks.get(Hook.Type.INSTEAD).size();
            int locInsteadHooksQty = locationHooks.get(Hook.Type.INSTEAD).size();
            if (itemInsteadHooksQty > 0 || locInsteadHooksQty > 0) {
                outEngDebug("Найдено перехватов типа \"ВМЕСТО\": на предмете - {0}, в локации - {1}.", itemInsteadHooksQty, locInsteadHooksQty);

                // fire object hooks
                for (Hook hook : objectHooks.get(Hook.Type.INSTEAD)) {
                    outEngDebug("Запуск перехвата \"{0}\" на предмете...", hook);
                    virtualMachine.runHook(hook, parameters);
                    outEngDebug("Перехват выполнен.");
                }
                // fire location hooks
                for (Hook hook : locationHooks.get(Hook.Type.INSTEAD)) {
                    outEngDebug("Запуск перехвата \"{0}\" в локации...", hook);
                    virtualMachine.runHook(hook, parameters);
                    outEngDebug("Перехват выполнен.");
                }

                // ... and finish
                outEngDebug("Завершение работы команды.");
                return true;
            }

            // check restrictions
            outEngDebug("Проверка ограничений действия...");
            if (checkActionRestrictions(action, parameters)) {
                outEngDebug("Сработало ограничение, команда завершается.");
                return true;
            }

            // fire BEFORE hooks
            outEngDebug("Выполнение перехватов \"ДО\"...");
            fireBeforeHooks(parameters, objectHooks, locationHooks);


            // fire action
            outEngDebug("Выполнение самого действия \"{0}\"...", action);
            virtualMachine.runAction(action, parameters);

            // fire AFTER hooks
            outEngDebug("Выполнение перехватов \"ПОСЛЕ\"...");
            fireAfterHooks(parameters, objectHooks, locationHooks);
        } catch (IFML2ParseException e) {
            handleParseError(trimmedCommand, e);
        } catch (IFML2VMException e) {
            outTextLn("[Ошибка!] {0}", e.getMessage());
        } catch (IFML2Exception e) {
            outEngDebug("Анализ завершился неудачно.");
            outTextLn(e.getMessage());
        }

        return true;
    }

    public void outIcon(String iconFilePath, int maxHeight, int maxWidth) {
        environment.outIcon(iconFilePath, maxHeight, maxWidth);
    }

    private void handleParseError(String trimmedCommand, IFML2ParseException parseException) {
        Procedure parseErrorHandler = dataHelper.getParseErrorHandler();
        if (parseErrorHandler != null) {
            try {
                // prepare parameters
                List<Variable> parameters = Arrays
                        .asList(new Variable(CommonConstants.PARSE_ERROR_HANDLER_PRM_PHRASE, new TextValue(trimmedCommand)),
                                new Variable(CommonConstants.PARSE_ERROR_HANDLER_PRM_ERROR, new TextValue(parseException.getMessage())));

                // call handler
                Value returnValue = virtualMachine.callProcedureWithParameters(parseErrorHandler, parameters);

                // check return value
                if (returnValue != null && returnValue instanceof BooleanValue) {
                    Boolean isErrorHandled = ((BooleanValue) returnValue)
                            .getValue(); // error handler should return false if he can't handle error
                    if (!isErrorHandled) {
                        // show original parser error
                        outTextLn(parseException.getMessage());
                    }
                }
            } catch (IFML2Exception e) {
                outTextLn(e.getMessage());
            }
        } else {
            outTextLn(parseException.getMessage());
        }
    }

    private List<Variable> convertFormalElementsToParameters(/*@NotNull*/ List<FormalElement> formalElements) throws IFML2Exception {
        List<Variable> parameters = new ArrayList<>(formalElements.size());
        for (FormalElement formalElement : formalElements) {
            Value value;
            FormalElement.Type formalElementType = formalElement.getType();
            switch (formalElementType) {
                case LITERAL:
                    value = new TextValue(formalElement.getLiteral());
                    break;
                case OBJECT:
                    value = new ObjectValue(formalElement.getObject());
                    break;
                default:
                    throw new IFML2Exception("Внутренняя ошибка: Неизвестный тип формального элемента: {0}", formalElementType);
            }
            parameters.add(new Variable(formalElement.getParameterName(), value));
        }

        return parameters;
    }

    private void outEngDebug(String message, Object... args) {
        outDebug(this.getClass(), message, args);
    }

    public void outDebug(Class reporter, String message, Object... args) {
        String reporterName = genReporterName(reporter);
        environment.debug(reporterName + message, args);
    }

    private String genReporterName(Class reporter) {
        String reporterName;
        if (Engine.class.equals(reporter)) {
            reporterName = "Движок";
        } else if (Parser.class.equals(reporter)) {
            reporterName = "Парсер";
        } else if (VirtualMachine.class.equals(reporter)) {
            reporterName = "ВиртуальнаяМашина";
        } else {
            reporterName = reporter != null ? reporter.getClass().getSimpleName() : "";
        }
        return '[' + reporterName + "] ";
    }

    private void fireAfterHooks(List<Variable> parameters, HashMap<Hook.Type, List<Hook>> objectHooks,
                                HashMap<Hook.Type, List<Hook>> locationHooks) throws IFML2Exception {
        // ... object hooks
        for (Hook hook : objectHooks.get(Hook.Type.AFTER)) {
            virtualMachine.runHook(hook, parameters);
        }
        // ... and location hooks
        for (Hook hook : locationHooks.get(Hook.Type.AFTER)) {
            virtualMachine.runHook(hook, parameters);
        }
    }

    private void fireBeforeHooks(List<Variable> parameters, HashMap<Hook.Type, List<Hook>> objectHooks,
                                 HashMap<Hook.Type, List<Hook>> locationHooks) throws IFML2Exception {
        // ... object hooks
        for (Hook hook : objectHooks.get(Hook.Type.BEFORE)) {
            virtualMachine.runHook(hook, parameters);
        }
        // ... and location hooks
        for (Hook hook : locationHooks.get(Hook.Type.BEFORE)) {
            virtualMachine.runHook(hook, parameters);
        }
    }

    private HashMap<Hook.Type, List<Hook>> collectLocationHooks(Action action) throws IFML2Exception {
        // create HashMap with all location hooks
        HashMap<Hook.Type, List<Hook>> locationHooks = new HashMap<Hook.Type, List<Hook>>() {
            {
                put(Hook.Type.BEFORE, new ArrayList<>());
                put(Hook.Type.INSTEAD, new ArrayList<>());
                put(Hook.Type.AFTER, new ArrayList<>());
            }
        };

        Location currentLocation = getCurrentLocation();
        if (currentLocation == null) {
            throw new IFML2Exception("Системная ошибка: Текущая локация не задана!");
        }

        // collect current location hooks
        currentLocation.getHooks().stream().filter(hook -> action.equals(hook.getAction()))
                .forEach(hook -> locationHooks.get(hook.getType()).add(hook));
        return locationHooks;
    }

    private HashMap<Hook.Type, List<Hook>> collectObjectHooks(Action action, List<FormalElement> formalElements) {
        // create HashMap with all object hooks
        HashMap<Hook.Type, List<Hook>> objectHooks = new HashMap<Hook.Type, List<Hook>>() {
            {
                put(Hook.Type.BEFORE, new ArrayList<>());
                put(Hook.Type.INSTEAD, new ArrayList<>());
                put(Hook.Type.AFTER, new ArrayList<>());
            }
        };

        // collect all object hooks
        formalElements.stream().filter(formalElement ->
                FormalElement.Type.OBJECT.equals(formalElement.getType()) &&
                        formalElement.getObject() instanceof Item).forEach(formalElement -> {
            Item item = (Item) formalElement.getObject();
            item.getHooks().stream().filter(hook -> action.equals(hook.getAction()) &&
                    formalElement.getParameterName()
                            .equalsIgnoreCase(
                                    hook.getObjectElement()))
                    .forEach(hook -> objectHooks.get(hook.getType()).add(hook));
        });
        return objectHooks;
    }

    private boolean checkActionRestrictions(Action action, List<Variable> parameters) throws IFML2Exception {
        for (Restriction restriction : action.getRestrictions()) {
            try {
                RunningContext runningContext = RunningContext.CreateNewContext(virtualMachine);
                runningContext.populateParameters(parameters);
                Value isRestricted = ExpressionCalculator.calculate(runningContext, restriction.getCondition());
                if (!(isRestricted instanceof BooleanValue)) {
                    throw new IFML2Exception("Выражение (%s) условия ограничения действия \"%s\" не логического типа.",
                            restriction.getCondition(), action);
                }
                if (((BooleanValue) isRestricted).getValue()) // if condition is true, run reaction
                {
                    virtualMachine.runInstructionList(restriction.getReaction(), runningContext);
                    return true;
                }
            } catch (IFML2Exception e) {
                throw new IFML2Exception(e, "{0}\n  при вычислении ограничения \"{1}\" действия \"{2}\"", e.getMessage(),
                        restriction.getCondition(), action);
            }
        }
        return false;
    }

    public Story getStory() {
        return environment.getStory();
    }

    /*@Nullable*/
    public Location getCurrentLocation() {
        ObjectValue object = (ObjectValue) systemVariables.get(SystemIdentifiers.CURRENT_LOCATION_SYSTEM_VARIABLE.toLowerCase());
        return object != null ? (Location) object.getValue() : null;
    }

    public void setCurrentLocation(Location currentLocation) {
        systemVariables.put(SystemIdentifiers.CURRENT_LOCATION_SYSTEM_VARIABLE.toLowerCase(), new ObjectValue(currentLocation));
    }

    public ArrayList<Item> getInventory() {
        return inventory;
    }

    public Value resolveSymbol(String symbol) throws IFML2VMException {
        String loweredSymbol = symbol.toLowerCase();

        try {
            if (ENGINE_SYMBOLS.containsKey(loweredSymbol)) {
                return ENGINE_SYMBOLS.get(loweredSymbol).call();
            }
        } catch (Exception e) {
            throw new IFML2VMException(e, "  во время вычисления переменной движка {0}", symbol);
        }

        if (systemVariables.containsKey(loweredSymbol)) {
            return systemVariables.get(loweredSymbol);
        }

        if (environment.getStory().getObjectsHeap().containsKey(loweredSymbol)) {
            return new ObjectValue(environment.getStory().getObjectsHeap().get(loweredSymbol));
        }

        throw new IFML2VMException("Неизвестный идентификатор \"{0}\"", symbol);
    }

    public VirtualMachine getVirtualMachine() {
        return virtualMachine;
    }

    public void saveGame(String saveFileName) throws IFML2Exception {
        SavedGame savedGame = new SavedGame(dataHelper, environment.getStory().getDataHelper());
        OMManager.saveGame(saveFileName, savedGame);
        outTextLn("Игра сохранена в файл {0}.", saveFileName);
    }

    public void loadGame(String saveFileName) throws IFML2Exception {
        try {
            SavedGame savedGame = OMManager.loadGame(saveFileName);
            savedGame.restoreGame(dataHelper, environment.getStory().getDataHelper());
            outTextLn("Игра восстановлена из файла {0}.", saveFileName);
        } catch (IFML2Exception e) {
            String errorText = "Ошибка при загрузке игры! " + e.getMessage();
            outTextLn(errorText);
            LOG.error(errorText);
        }
    }

    /**
     * Checks if item is in deep content of other items
     *
     * @param itemToCheck item to check
     * @param items       items with deep content
     * @return true if item is in deep content of items
     * @throws ifml2.IFML2Exception
     */
    public boolean checkDeepContent(Item itemToCheck, List<Item> items) throws IFML2Exception {
        for (Item item : items) {
            Value itemContents = item.getAccessibleContent(getVirtualMachine());
            if (itemContents != null) {
                if (!(itemContents instanceof CollectionValue)) {
                    throw new IFML2VMException("Триггер доступного содержимого у предмета \"{0}\" вернул не коллекцию, а \"{1}\"!",
                            itemToCheck, itemContents.getTypeName());
                }

                List itemContentsList = ((CollectionValue) itemContents).getValue();
                List<Item> itemContentsItemList = new BasicEventList<>();
                for (Object object : itemContentsList) {
                    if (!(object instanceof Item)) {
                        throw new IFML2VMException(
                                "Триггер доступного содержимого у предмета \"{0}\" вернул в коллекции не предмет, а \"{1}\"!", itemToCheck,
                                object);
                    }

                    itemContentsItemList.add((Item) object);
                }

                if (itemContentsList.contains(itemToCheck) || checkDeepContent(itemToCheck, itemContentsItemList)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if object is inaccessible for player's actions
     *
     * @param object IFMLObject for check
     * @return true if object is accessible including deep content
     * @throws ifml2.IFML2Exception when tested objects neither location or item
     */
    public boolean isObjectAccessible(IFMLObject object) throws IFML2Exception {
        Location currentLocation = getCurrentLocation();

        if (currentLocation == null) {
            throw new IFML2Exception("Системная ошибка: Текущая локация не задана!");
        }

        // test locations
        if (object instanceof Location) {
            return object.equals(currentLocation);
        } else if (object instanceof Item)   // test items
        {
            Item item = (Item) object;

            // test if object is in current location or player's inventory
            return currentLocation.contains(item) || inventory.contains(item) || checkDeepContent(item, currentLocation.getItems()) ||
                    checkDeepContent(item, inventory);
        } else {
            throw new IFML2Exception("Системная ошибка: Неизвестный тип объекта: \"{0}\".", object);
        }
    }

    @SuppressWarnings("unused")
    public void outDebug(Class reporter, int level, String message, Object... args) {
        outDebug(level, genReporterName(reporter) + message, args);
    }

    private void outDebug(int level, String message, Object... args) {
        String tab = "";
        for (int i = 1; i <= level; i++) {
            tab += "  ";
        }
        environment.debug(tab + message, args);
    }

    public Variable searchGlobalVariable(String name) {
        if (name == null) {
            return null;
        }

        String loweredName = name.toLowerCase();

        if (globalVariables.containsKey(loweredName)) {
            return new GlobalVariableProxy(globalVariables, name, globalVariables.get(loweredName));
        }

        return null;
    }

    public enum SystemCommand {
        HELP
    }

    /**
     * Helper for saved games data.
     */
    public class DataHelper {
        public HashMap<String, Value> getGlobalVariables() {
            return globalVariables;
        }

        public HashMap<String, Value> getSystemVariables() {
            return systemVariables;
        }

        public ArrayList<Item> getInventory() {
            return inventory;
        }

        public List<Location> getLocations() {
            return environment.getStory().getLocations();
        }

        public void setGlobalVariable(/*@NotNull*/ String name, String expression) throws IFML2Exception {
            Value value = ExpressionCalculator.calculate(RunningContext.CreateNewContext(virtualMachine), expression);
            globalVariables.put(name.toLowerCase(), value);
        }

        public void setSystemVariable(String name, String expression) throws IFML2Exception {
            Value value = ExpressionCalculator.calculate(RunningContext.CreateNewContext(virtualMachine), expression);
            systemVariables.put(name, value);
        }

        public String getStoryFileName() {
            return new File(storyFileName).getName();
        }

        public boolean isObjectAccessible(IFMLObject ifmlObject) throws IFML2Exception {
            return EngineImpl.this.isObjectAccessible(ifmlObject);
        }

        public void outDebug(Class reporter, int level, String message, Object... args) {
            EngineImpl.this.outDebug(level, genReporterName(reporter) + message, args);
        }

        public Procedure getParseErrorHandler() {
            return environment.getStory().getInheritedSystemProcedures().getParseErrorHandler();
        }
    }

    private class GlobalVariableProxy extends Variable {
        private final HashMap<String, Value> globalVariables;

        public GlobalVariableProxy(HashMap<String, Value> globalVariables, String name, Value value) {
            super(name.toLowerCase(), value);
            this.globalVariables = globalVariables;
        }

        @Override
        public Value getValue() {
            return globalVariables.containsKey(name) ? globalVariables.get(name) : null;
        }

        @Override
        public void setValue(Value value) {
            if (globalVariables.containsKey(name)) {
                globalVariables.put(name, value);
            }
        }

        @Override
        public void setName(String name) {
            throw new RuntimeException("Внутренняя ошибка: Запрещено менять имена переменных");
        }
    }

}
