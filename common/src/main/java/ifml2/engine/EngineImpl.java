package ifml2.engine;

import ca.odell.glazedlists.BasicEventList;
import ifml2.CommonConstants;
import ifml2.Environment;
import ifml2.IFML2Exception;
import ifml2.SystemIdentifiers;
import ifml2.engine.saved.XmlSavedGame;
import ifml2.om.Action;
import ifml2.om.Hook;
import ifml2.om.IFMLObject;
import ifml2.om.Item;
import ifml2.om.Location;
import ifml2.om.Procedure;
import ifml2.om.Property;
import ifml2.om.PropertyDefinition;
import ifml2.om.Restriction;
import ifml2.om.Role;
import ifml2.om.RoleDefinition;
import ifml2.om.Story;
import ifml2.om.StoryOptions;
import ifml2.parser.FormalElement;
import ifml2.parser.FormalObject;
import ifml2.parser.IFML2ParseException;
import ifml2.parser.ParseResult;
import ifml2.parser.Parser;
import ifml2.storage.Storage;
import ifml2.storage.StorageImpl;
import ifml2.storage.StoryDTO;
import ifml2.storage.domain.SavedGame;
import ifml2.storage.domain.SavedItem;
import ifml2.storage.domain.SavedLocation;
import ifml2.storage.domain.SavedProperty;
import ifml2.storage.domain.SavedRole;
import ifml2.storage.domain.SavedVariable;
import ifml2.vm.ExpressionCalculator;
import ifml2.vm.IFML2VMException;
import ifml2.vm.RunningContext;
import ifml2.vm.Variable;
import ifml2.vm.VirtualMachine;
import ifml2.vm.values.BooleanValue;
import ifml2.vm.values.CollectionValue;
import ifml2.vm.values.NumberValue;
import ifml2.vm.values.ObjectValue;
import ifml2.vm.values.TextValue;
import ifml2.vm.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ifml2.engine.SystemCommand.HELP;
import static java.lang.String.format;

public class EngineImpl implements Engine {

    public static final Logger LOG = LoggerFactory.getLogger(EngineImpl.class);

    private final TypedMap<Value> globalVariables = new TypedMap<Value>();

    private Environment environment;
    private final VirtualMachine virtualMachine;
    private final Parser parser;
    private final StorageImpl storage;

    private final TypedMap<Value> systemVariables = new TypedMap<Value>();
    private final ArrayList<Item> abyss = new ArrayList<>();

    private List<Item> inventory = new ArrayList<>();
    private TypedMap<SystemCommand> SYSTEM_COMMANDS = new TypedMap<SystemCommand>()
            .preSet("помощь", HELP)
            .preSet("помоги", HELP)
            .preSet("помогите", HELP)
            .preSet("инфо", HELP)
            .preSet("информация", HELP)
            .preSet("help", HELP)
            .preSet("info", HELP);
    private DataHelper dataHelper = new DataHelper();
    private String storyFileName;
    private Date starTime = new Date();
    private TypedMap<Callable<? extends Value>> ENGINE_SYMBOLS = new TypedMap<>();

    {
        Callable<CollectionValue> returnInv = () -> new CollectionValue(inventory);

        ENGINE_SYMBOLS.preSet("инвентарий", returnInv)
                .preSet("инвентарь", returnInv)
                .preSet("куча",         (Callable<TextValue>) ()        -> new TextValue(new CollectionValue(new ArrayList<>(environment.getStory().getObjectsHeap().values())).toString()))
                .preSet("словарь",      (Callable<TextValue>) ()        -> new TextValue(new CollectionValue(new ArrayList<>(environment.getStory().getDictionary().values())).toString()))
                .preSet("пустота",      (Callable<CollectionValue>) ()  -> new CollectionValue(abyss))
                .preSet("глобальные",   (Callable<TextValue>) ()        -> new TextValue(globalVariables.entrySet().toString()))
                .preSet("локации",      (Callable<TextValue>) ()        -> new TextValue(new CollectionValue(environment.getStory().getLocations()).toString()))
                .preSet("предметы",     (Callable<TextValue>) ()        -> new TextValue(new CollectionValue(environment.getStory().getItems()).toString()))
                .preSet("системные",    (Callable<TextValue>) ()        -> new TextValue(format("Системные переменные: %s", ENGINE_SYMBOLS.keySet())))
                .preSet("секунды",      (Callable<NumberValue>) ()      -> { return new NumberValue((System.currentTimeMillis() - starTime.getTime()) / 1000); })
                .preSet("минуты",       (Callable<NumberValue>) ()      -> { return new NumberValue((System.currentTimeMillis() - starTime.getTime()) / 1000 / 60); });
    }

    public EngineImpl(
            final Environment environment,
            final VirtualMachine virtualMachine,
            final Parser parser,
            final Storage storage
    ) {
        this.environment = environment;
        this.virtualMachine = virtualMachine;
        this.parser = parser;
        this.storage = (StorageImpl) storage;
        virtualMachine.setEngine(this);
        LOG.info("Engine created.");
    }

    public void loadStory(String storyFileName, boolean isAllowedOpenCipherFiles) throws IFML2Exception {
        StoryDTO loadStoryResult = storage.loadStory(storyFileName);
        //TODO validate xml
        if (loadStoryResult != null) {
            environment.setStory(loadStoryResult.getStory());
            this.storyFileName = storyFileName;
            inventory = loadStoryResult.getInventory();
        }
        LOG.info("Story \"{0}\" loaded", environment.getStory());
    }

    public void outText(String text, Object... args) {
        environment.outText(text);
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
        story.getStoryOptions().getVars().forEach(varInstruction -> {
            String name = varInstruction.getName();
            if (name != null) {
                try {
                    globalVariables.put(name, ExpressionCalculator.calculate(virtualMachine.createRunningContext(), varInstruction.getValue()));
                } catch (IFML2Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

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
                    property.evaluateFromPrimaryExpression(virtualMachine.createRunningContext());
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
                .append("**********\n")
                .append(storyDescription.getDescription() != null ? environment.getStory().getStoryOptions().getStoryDescription() : "<Без имени>")
                .append(format("ВЕРСИЯ: %s", storyDescription.getVersion() != null ? storyDescription.getVersion() : "<Без версии>"))
                .append(format("АВТОР: %s", storyDescription.getAuthor() != null ? storyDescription.getAuthor() : "<Без автора>"))
                .append("**********\n");
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
            switch (SYSTEM_COMMANDS.get(trimmedCommand)) {
                case HELP:
                    if (!systemCommandsDisableOption.isDisableHelp()) {
                        outTextLn("Попробуйте одну из команд: " + environment.getStory().getAllActions());
                        return true;
                    }
                    break;
                default:
                    // Nothing to do
            }
        }

        // check debug command
        if (environment.isDebug() && trimmedCommand.length() > 0 && trimmedCommand.charAt(0) == '?') {
            String expression = trimmedCommand.substring(1);
            try {
                Value value = ExpressionCalculator.calculate(virtualMachine.createRunningContext(), expression);
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

            HookMap objectHooks = collectObjectHooks(action, formalElements);
            //outEngDebug("Кол-во найденных перехватов на предмете - {0}.", objectHooks.size()); // нужно выводить не кол-во списков перехватов,
            // а само кол-во перехватов

            HookMap locationHooks = collectLocationHooks(action);
            //outEngDebug("Кол-во найденных перехватов в локации - {0}.", locationHooks.size()); // нужно выводить не кол-во списков перехватов,
            // а само кол-во перехватов

            List<Variable> parameters = convertFormalElementsToParameters(formalElements);

            // if there are INSTEAD hooks then fire them and finish
            int itemInsteadHooksQty = objectHooks.sizeByType(Hook.Type.INSTEAD);
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

    private List<Variable> convertFormalElementsToParameters(List<FormalElement> formalElements) throws IFML2Exception {
        return formalElements.stream().map(FormalElement::toVariable).collect(Collectors.toList());
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

    private void runHook(final Hook hook, List<Variable> parameters) {
        try {
            virtualMachine.runHook(hook, parameters);
        } catch (IFML2Exception ex) {
            LOG.error("Unable run hook {}", hook, ex);
        }
    }

    private void applyHooks(final Hook.Type type, final List<Variable> parameters, final HookMap objectHooks, final HookMap locationHooks) {
        objectHooks.get(type).forEach(hook -> runHook(hook, parameters));
        locationHooks.get(type).forEach(hook -> runHook(hook, parameters));
    }

    private void fireAfterHooks(List<Variable> parameters, HookMap objectHooks, HookMap locationHooks) throws IFML2Exception {
        applyHooks(Hook.Type.AFTER, parameters, objectHooks, locationHooks);
    }

    private void fireBeforeHooks(List<Variable> parameters, HookMap objectHooks, HookMap locationHooks) throws IFML2Exception {
        applyHooks(Hook.Type.BEFORE, parameters, objectHooks, locationHooks);
    }

    private HookMap collectLocationHooks(Action action) throws IFML2Exception {
        // create HashMap with all location hooks
        HookMap locationHooks = new HookMapImpl();

        Location currentLocation = getCurrentLocation();
        if (currentLocation == null) {
            throw new IFML2Exception("Системная ошибка: Текущая локация не задана!");
        }

        // collect current location hooks
        currentLocation.getHooks().stream()
                .filter(hook -> hook.canDo(action))
                .forEach(locationHooks::add);
        return locationHooks;
    }

    private HookMap collectObjectHooks(Action action, List<FormalElement> formalElements) {
        // create HashMap with all object hooks
        HookMap objectHooks = new HookMapImpl();

        // collect all object hooks
        formalElements.stream()
            .filter(formalElement -> formalElement instanceof FormalObject)
            .filter(formalElement -> ((FormalObject) formalElement).getObject() instanceof Item)
                .forEach(formalElement -> {
            Item item = (Item) ((FormalObject) formalElement).getObject();
            item.getHooks().stream()
                    .filter(hook -> hook.canDo(action))
                    .filter(hook -> formalElement.getParameterName().equalsIgnoreCase(hook.getObjectElement()))
                    .forEach(objectHooks::add);
        });
        return objectHooks;
    }

    private boolean checkActionRestrictions(Action action, List<Variable> parameters) throws IFML2Exception {
        for (Restriction restriction : action.getRestrictions()) {
            try {
                RunningContext runningContext = virtualMachine.createRunningContext();
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

    public Location getCurrentLocation() {
        ObjectValue object = (ObjectValue) systemVariables.get(SystemIdentifiers.CURRENT_LOCATION_SYSTEM_VARIABLE.toLowerCase());
        return object != null ? (Location) object.getValue() : null;
    }

    public void setCurrentLocation(Location currentLocation) {
        systemVariables.put(SystemIdentifiers.CURRENT_LOCATION_SYSTEM_VARIABLE.toLowerCase(), new ObjectValue(currentLocation));
    }

    public List<Item> getInventory() {
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
        XmlSavedGame savedGame = new XmlSavedGame(dataHelper, environment.getStory().getDataHelper());
        storage.saveGame(saveFileName, savedGame);
        outTextLn("Игра сохранена в файл {0}.", saveFileName);
    }

    public void loadGame(String saveFileName) throws IFML2Exception {
        try {
            XmlSavedGame savedGame = (XmlSavedGame) storage.loadGame(saveFileName);
            savedGame.restoreGame(dataHelper, environment.getStory().getDataHelper());
            outTextLn("Игра восстановлена из файла {0}.", saveFileName);
        } catch (IFML2Exception e) {
            String errorText = "Ошибка при загрузке игры! " + e.getMessage();
            outTextLn(errorText);
            LOG.error(errorText);
        }
    }

    private void applyToStory(SavedGame savedGame) throws IFML2Exception {
        Story.DataHelper storyHelper = environment.getStory().getDataHelper();
        String engineStoryFileName = dataHelper.getStoryFileName();
        if (!savedGame.getStoryFileName().equalsIgnoreCase(engineStoryFileName)) {
            throw new IFML2Exception("Saved game from {} not acceptable to story {}", savedGame.getStoryFileName(), engineStoryFileName);
        }
        // restoreGlobalVars
        savedGame.getGlobalVars().forEach(this::setGlobalVarFromSaved);
        // restoreSystemVars
        savedGame.getSystemVars().forEach(this::setSystemVarFromSaved);
        // restoreInventory
        List<Item> inventory = dataHelper.getInventory();
        inventory.clear();
        savedGame.getSavedInventory().forEach(id -> {
            Item item = storyHelper.findItemById(id);
            if (item != null) {
                inventory.add(item);
                item.setContainer(inventory);
            } else {
                LOG.warn("[Game loading] Inventory loading: there is no item with id '{}'.", id);
            }
        });
        // restoreLocations
        savedGame.getSavedLocations().forEach(savedLocation -> restoreSavedLocation(savedLocation, storyHelper));
        // restoreSavedItems
        savedGame.getSavedItems().forEach(savedItem -> restoreSavedItem(savedItem, storyHelper));
    }

    private void setGlobalVarFromSaved(SavedVariable savedVariable) {
        try {
            dataHelper.setGlobalVariable(savedVariable.getName(), savedVariable.getValue());
        } catch (IFML2Exception ex) {
            LOG.error("Unable set global variable '{}'", savedVariable.getName(), ex);
        }
    }

    private void setSystemVarFromSaved(SavedVariable savedVariable) {
        try {
            dataHelper.setSystemVariable(savedVariable.getName(), savedVariable.getValue());
        } catch (IFML2Exception ex) {
            LOG.error("Unable set system variable '{}'", savedVariable.getName(), ex);
        }
    }

    private void restoreSavedLocation(SavedLocation savedLocation, Story.DataHelper storyHelper) {
        Location location = storyHelper.findLocationById(savedLocation.getId());
        if (location != null) {
            List<Item> locationItems = location.getItems();
            locationItems.clear();
            savedLocation.getItems().forEach(itemId -> {
                Item item = storyHelper.findItemById(itemId);
                if (item != null) {
                    locationItems.add(item);
                    item.setContainer(locationItems);
                } else {
                    LOG.warn("[Game loading] Location items loading: there is no item with id '{}'.", itemId);
                }
            });
        } else {
            LOG.warn("Location with ID '{}' not found", savedLocation.getId());
        }
    }

    private void restoreSavedItem(SavedItem savedItem, Story.DataHelper storyHelper) {
        Item item = storyHelper.findItemById(savedItem.getId());
        if (item != null) {
            savedItem.getRoles().forEach(savedRole -> restoreSavedRole(savedRole, item, storyHelper));
        } else {
            LOG.warn("Item with ID '{}' not found", savedItem.getId());
        }
    }

    private void restoreSavedRole(SavedRole savedRole, Item item, Story.DataHelper storyHelper) {
        Role role = item.findRoleByName(savedRole.getName());
        if (role != null) {
            savedRole.getProperties().forEach(savedProperty -> restoreSavedProperty(savedProperty, role, storyHelper));
        } else {
            LOG.warn("Role '{}' not found for '{}'", savedRole.getName(), item.getId());
        }
    }

    private void restoreSavedProperty(SavedProperty savedProperty, Role role, Story.DataHelper storyHelper) {
        Property property = role.findPropertyByName(savedProperty.getName());
        if (property != null) {
            RoleDefinition roleDefinition = role.getRoleDefinition();
            PropertyDefinition propertyDefinition = roleDefinition.findPropertyDefinitionByName(savedProperty.getName());
            if (propertyDefinition != null) {
                if (PropertyDefinition.Type.COLLECTION == propertyDefinition.getType()) {
                    List<Item> propItems = new ArrayList<>();
                    savedProperty.getItems().forEach(itemId -> {
                        Item propItem = storyHelper.findItemById(itemId);
                        if (propItem != null) {
                            propItem.moveTo(propItems);
                        } else {
                            LOG.warn("[Game loading] Location items loading: there is no item with id '{}'.", itemId);
                        }
                    });
                    property.setValue(new CollectionValue(propItems));
                } else {
                    LOG.error("SYSTEM ERROR: Property '{}' into the role '{}' not marked as collection, but into the saved game it's marked");
                }
            } else {
                LOG.error("SYSTEM ERROR: Property '{}' not found into the role '{}'", savedProperty.getName(), role.getName());
            }
        } else {
            LOG.warn("Property '{}' not found into the role '{}'", savedProperty.getName(), role.getName());
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
                    throw new IFML2VMException("Триггер доступного содержимого у предмета \"{0}\" вернул не коллекцию, а \"{1}\"!", itemToCheck, itemContents.getTypeName());
                }

                List itemContentsList = ((CollectionValue) itemContents).getValue();
                List<Item> itemContentsItemList = new BasicEventList<>();
                for (Object object : itemContentsList) {
                    if (!(object instanceof Item)) {
                        throw new IFML2VMException("Триггер доступного содержимого у предмета \"{0}\" вернул в коллекции не предмет, а \"{1}\"!", itemToCheck, object);
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
        final StringBuilder sb = new StringBuilder();
        IntStream.range(1, level).forEach(i -> sb.append("  "));
        environment.debug(sb.append(message).toString(), args);
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

    /**
     * Helper for saved games data.
     */
    public class DataHelper {
        public TypedMap<Value> getGlobalVariables() {
            return globalVariables;
        }

        public TypedMap<Value> getSystemVariables() {
            return systemVariables;
        }

        public List<Item> getInventory() {
            return inventory;
        }

        public List<Location> getLocations() {
            return environment.getStory().getLocations();
        }

        public void setGlobalVariable(String name, String expression) throws IFML2Exception {
            Value value = ExpressionCalculator.calculate(virtualMachine.createRunningContext(), expression);
            globalVariables.put(name.toLowerCase(), value);
        }

        public void setSystemVariable(String name, String expression) throws IFML2Exception {
            Value value = ExpressionCalculator.calculate(virtualMachine.createRunningContext(), expression);
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
        private final TypedMap<Value> globalVariables;

        public GlobalVariableProxy(TypedMap<Value> globalVariables, String name, Value value) {
            super(name.toLowerCase(), value);
            this.globalVariables = globalVariables;
        }

        @Override
        public Value getValue() {
            return globalVariables.get(name);
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
