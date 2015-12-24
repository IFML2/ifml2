package ifml2.engine;

import ca.odell.glazedlists.BasicEventList;
import ifml2.CommonConstants;
import ifml2.FormatLogger;
import ifml2.IFML2Exception;
import ifml2.SystemIdentifiers;
import ifml2.engine.saved.SavedGame;
import ifml2.om.*;
import ifml2.parser.FormalElement;
import ifml2.parser.IFML2ParseException;
import ifml2.parser.Parser;
import ifml2.players.GameInterface;
import ifml2.vm.*;
import ifml2.vm.instructions.SetVarInstruction;
import ifml2.vm.values.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import static ifml2.engine.Engine.SystemCommand.HELP;

public class Engine
{
    public static final FormatLogger LOG = FormatLogger.getLogger(Engine.class);
    private final HashMap<String, Value> globalVariables = new HashMap<String, Value>();
    private final Parser parser = new Parser();
    private final VirtualMachine virtualMachine = new VirtualMachine();
    private final HashMap<String, Value> systemVariables = new HashMap<String, Value>();
    private GameInterface gameInterface = null;
    private Story story = null;
    private ArrayList<Item> inventory = new ArrayList<Item>();
    private ArrayList<Item> abyss = new ArrayList<Item>();
    private HashMap<String, Callable<? extends Value>> ENGINE_SYMBOLS = new HashMap<String, Callable<? extends Value>>()
    {
        {
            Callable<CollectionValue> returnInv = new Callable<CollectionValue>()
            {
                @Override
                public CollectionValue call() throws Exception
                {
                    return new CollectionValue(inventory);
                }
            };

            put("инвентарий", returnInv);
            put("инвентарь", returnInv);
            put("куча", new Callable<CollectionValue>()
            {
                @Override
                public CollectionValue call() throws Exception
                {
                    return new CollectionValue(new ArrayList<IFMLObject>(story.getObjectsHeap().values()));
                }
            });
            put("словарь", new Callable<CollectionValue>()
            {
                @Override
                public CollectionValue call() throws Exception
                {
                    return new CollectionValue(new ArrayList<Word>(story.getDictionary().values()));
                }
            });
            put("пустота", new Callable<CollectionValue>()
            {
                @Override
                public CollectionValue call() throws Exception
                {
                    return new CollectionValue(abyss);
                }
            });
            put("глобальные", new Callable<TextValue>()
            {
                @Override
                public TextValue call() throws Exception
                {
                    return new TextValue(globalVariables.entrySet().toString());
                }
            });
            put("локации", new Callable<TextValue>()
            {
                @Override
                public TextValue call() throws Exception
                {
                    return new TextValue(story.getLocations().toString());
                }
            });
            put("предметы", new Callable<TextValue>()
            {
                @Override
                public TextValue call() throws Exception
                {
                    return new TextValue(story.getItems().toString());
                }
            });
            put("системные", new Callable<TextValue>()
            {
                @Override
                public TextValue call() throws Exception
                {
                    return new TextValue(String.format("Системные переменные: %s", ENGINE_SYMBOLS.keySet()));
                }
            });
        }
    };
    private HashMap<String, SystemCommand> SYSTEM_COMMANDS = new HashMap<String, SystemCommand>()
    {
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
        public SystemCommand get(@NotNull Object key)
        {
            return super.get(key.toString().toLowerCase());
        }

        @Override
        public boolean containsKey(@NotNull Object key)
        {
            return super.containsKey(key.toString().toLowerCase());
        }

        @Override
        public SystemCommand put(@NotNull String key, SystemCommand value)
        {
            return super.put(key.toLowerCase(), value);
        }
    };
    private DataHelper dataHelper = new DataHelper();
    private String storyFileName;

    public Engine(GameInterface gameInterface)
    {
        this.gameInterface = gameInterface;
        virtualMachine.setEngine(this);
        LOG.info("Engine created.");
    }

    public void loadStory(String storyFileName, boolean isAllowedOpenCipherFiles) throws IFML2Exception
    {
        LOG.info("Loading story \"{0}\"...", storyFileName);

        if (!new File(storyFileName).exists())
        {
            throw new IFML2Exception("Файл истории не найден");
        }

        //TODO validate xml

        OMManager.LoadStoryResult loadStoryResult = OMManager.loadStoryFromFile(storyFileName, true, isAllowedOpenCipherFiles);
        story = loadStoryResult.getStory();
        this.storyFileName = storyFileName;
        inventory = loadStoryResult.getInventory();

        LOG.info("Story \"{0}\" loaded", story);
    }

    public void outText(String text)
    {
        gameInterface.outputText(text);
    }

    public void outTextLn(String text)
    {
        outText(text + "\n");
    }

    public void initGame() throws IFML2Exception
    {
        LOG.info("Initializing game...");

        if (story == null)
        {
            throw new IFML2Exception("История не загружена.");
        }

        if (story.getLocations().isEmpty())
        {
            throw new IFML2Exception("Локаций нет.");
        }

        // init and reset objects
        virtualMachine.init();
        systemVariables.clear();
        abyss.clear();

        // load global vars
        globalVariables.clear();
        for (SetVarInstruction varInstruction : story.getStoryOptions().getVars())
        {
            Value value = ExpressionCalculator.calculate(RunningContext.CreateNewContext(virtualMachine), varInstruction.getValue());
            String name = varInstruction.getName();
            if (name != null)
            {
                globalVariables.put(name.toLowerCase(), value);
            }
        }

        // find properties and evaluates its expression into value
        for (IFMLObject ifmlObject : story.getObjectsHeap().values())
        {
            //todo: check pure object properties
            // check roles:
            for (Role role : ifmlObject.getRoles())
            {
                // fill roles instances with default properties (not set in instances but defined in role definitions)
                for (PropertyDefinition propertyDefinition : role.getRoleDefinition().getPropertyDefinitions())
                {
                    Property property = role.tryFindPropertyByDefinition(propertyDefinition);
                    if (property == null)
                    {
                        // property is default (not set in role instance) -- create it in role instance
                        property = new Property(propertyDefinition, role);
                        role.getProperties().add(property);
                    }

                    // calculate property
                    property.evaluateFromPrimaryExpression(RunningContext.CreateNewContext(virtualMachine));
                }
            }
        }

        // show initial info
        StoryOptions.StoryDescription storyDescription = story.getStoryOptions().getStoryDescription();
        outTextLn(storyDescription.getName() != null ? storyDescription.getName() : "<Без имени>");
        outTextLn("**********");
        outTextLn(storyDescription.getDescription() != null ? storyDescription.getDescription() : "<Без описания>");
        outTextLn(String.format("ВЕРСИЯ: %s", storyDescription.getVersion() != null ? storyDescription.getVersion() : "<Без версии>"));
        outTextLn(String.format("АВТОР: %s", storyDescription.getAuthor() != null ? storyDescription.getAuthor() : "<Без автора>"));
        outTextLn("**********\n");

        Procedure startProcedure = story.getStartProcedure();
        if (startProcedure != null)
        {
            try
            {
                virtualMachine.runProcedureWithoutParameters(startProcedure);
            }
            catch (IFML2Exception e)  // should we catch it?
            {
                outTextLn(e.getMessage());
            }
        }

        Location startLocation = story.getStartLocation();
        if (startLocation != null)
        {
            setCurrentLocation(startLocation);
            if (story.IsShowStartLocDesc())
            {
                // show first location description
                virtualMachine.showLocation(getCurrentLocation());
            }
        }

        if (getCurrentLocation() == null)
        {
            // if current location isn't set then take any
            setCurrentLocation(story.getAnyLocation());
        }

        LOG.info("Game initialized.");
    }

    public boolean executeGamerCommand(String gamerCommand)
    {
        String trimmedCommand = gamerCommand.trim();

        StoryOptions.SystemCommandsDisableOption systemCommandsDisableOption = story.getStoryOptions().getSystemCommandsDisableOption();

        // check system commands
        if (SYSTEM_COMMANDS.containsKey(trimmedCommand))
        {
            SystemCommand systemCommand = SYSTEM_COMMANDS.get(trimmedCommand);
            switch (systemCommand)
            {
                case HELP:
                    if (!systemCommandsDisableOption.isDisableHelp())
                    {
                        outTextLn("Попробуйте одну из команд: " + story.getAllActions());
                        return true;
                    }
            }
        }

        // check debug command
        if (trimmedCommand.length() > 0 && trimmedCommand.charAt(0) == '?' && !systemCommandsDisableOption.isDisableDebug())
        {
            String expression = trimmedCommand.substring(1);
            try
            {
                Value value = ExpressionCalculator.calculate(RunningContext.CreateNewContext(virtualMachine), expression);
                outTextLn(MessageFormat.format("[ОТЛАДКА] ({0}) {1}", value.getTypeName(), value));
            }
            catch (IFML2Exception e)
            {
                outTextLn("[ОТЛАДКА] Ошибка при вычислении выражения: " + e.getMessage());
            }
            return true;
        }

        Parser.ParseResult parseResult;
        try
        {
            parseResult = parser.parse(trimmedCommand, story.getDataHelper(), dataHelper);
            Action action = parseResult.getAction();
            List<FormalElement> formalElements = parseResult.getFormalElements();

            // check hooks & run procedure
            HashMap<Hook.Type, List<Hook>> objectHooks = collectObjectHooks(action, formalElements);
            HashMap<Hook.Type, List<Hook>> locationHooks = collectLocationHooks(action);

            List<Variable> parameters = convertFormalElementsToParameters(formalElements);

            // if there are INSTEAD hooks then fire them and finish
            if (objectHooks.get(Hook.Type.INSTEAD).size() > 0 || locationHooks.get(Hook.Type.INSTEAD).size() > 0)
            {
                // fire object hooks
                for (Hook hook : objectHooks.get(Hook.Type.INSTEAD))
                {
                    virtualMachine.runHook(hook, parameters);
                }
                // fire location hooks
                for (Hook hook : locationHooks.get(Hook.Type.INSTEAD))
                {
                    virtualMachine.runHook(hook, parameters);
                }

                // ... and finish
                return true;
            }

            // check restrictions
            if (checkActionRestrictions(action, parameters))
            {
                return true;
            }

            // fire BEFORE hooks
            fireBeforeHooks(parameters, objectHooks, locationHooks);
            // fire action
            virtualMachine.runAction(action, parameters);
            // fire AFTER hooks
            fireAfterHooks(parameters, objectHooks, locationHooks);
        }
        catch (IFML2ParseException e)
        {
            handleParseError(trimmedCommand, e);
        }
        catch (IFML2VMException e)
        {
            outTextLn("[Ошибка!] " + e.getMessage());
        }
        catch (IFML2Exception e)
        {
            outTextLn(e.getMessage());
        }

        return true;
    }

    private void handleParseError(String trimmedCommand, IFML2ParseException parseException)
    {
        Procedure parseErrorHandler = dataHelper.getParseErrorHandler();
        if (parseErrorHandler != null)
        {
            try
            {
                // prepare parameters
                List<Variable> parameters = Arrays
                        .asList(new Variable(CommonConstants.PARSE_ERROR_HANDLER_PRM_PHRASE, new TextValue(trimmedCommand)),
                                new Variable(CommonConstants.PARSE_ERROR_HANDLER_PRM_ERROR, new TextValue(parseException.getMessage())));

                // call handler
                Value returnValue = virtualMachine.callProcedureWithParameters(parseErrorHandler, parameters);

                // check return value
                if (returnValue != null && returnValue instanceof BooleanValue)
                {
                    Boolean isErrorHandled = ((BooleanValue) returnValue)
                            .getValue(); // error handler should return false if he can't handle error
                    if (!isErrorHandled)
                    {
                        // show original parser error
                        outTextLn(parseException.getMessage());
                    }
                }
            }
            catch (IFML2Exception e)
            {
                outTextLn(e.getMessage());
            }
        }
        else
        {
            outTextLn(parseException.getMessage());
        }
    }

    private List<Variable> convertFormalElementsToParameters(@NotNull List<FormalElement> formalElements) throws IFML2Exception
    {
        List<Variable> parameters = new ArrayList<Variable>(formalElements.size());
        for (FormalElement formalElement : formalElements)
        {
            Value value;
            FormalElement.Type formalElementType = formalElement.getType();
            switch (formalElementType)
            {
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

    private void fireAfterHooks(List<Variable> parameters, HashMap<Hook.Type, List<Hook>> objectHooks,
            HashMap<Hook.Type, List<Hook>> locationHooks) throws IFML2Exception
    {
        // ... object hooks
        for (Hook hook : objectHooks.get(Hook.Type.AFTER))
        {
            virtualMachine.runHook(hook, parameters);
        }
        // ... and location hooks
        for (Hook hook : locationHooks.get(Hook.Type.AFTER))
        {
            virtualMachine.runHook(hook, parameters);
        }
    }

    private void fireBeforeHooks(List<Variable> parameters, HashMap<Hook.Type, List<Hook>> objectHooks,
            HashMap<Hook.Type, List<Hook>> locationHooks) throws IFML2Exception
    {
        // ... object hooks
        for (Hook hook : objectHooks.get(Hook.Type.BEFORE))
        {
            virtualMachine.runHook(hook, parameters);
        }
        // ... and location hooks
        for (Hook hook : locationHooks.get(Hook.Type.BEFORE))
        {
            virtualMachine.runHook(hook, parameters);
        }
    }

    private HashMap<Hook.Type, List<Hook>> collectLocationHooks(Action action) throws IFML2Exception
    {
        // create HashMap with all location hooks
        HashMap<Hook.Type, List<Hook>> locationHooks = new HashMap<Hook.Type, List<Hook>>()
        {
            {
                put(Hook.Type.BEFORE, new ArrayList<Hook>());
                put(Hook.Type.INSTEAD, new ArrayList<Hook>());
                put(Hook.Type.AFTER, new ArrayList<Hook>());
            }
        };

        Location currentLocation = getCurrentLocation();
        if (currentLocation == null)
        {
            throw new IFML2Exception("Системная ошибка: Текущая локация не задана!");
        }

        // collect current location hooks
        for (Hook hook : currentLocation.getHooks())
        {
            if (action.equals(hook.getAction()))
            {
                locationHooks.get(hook.getType()).add(hook);
            }
        }
        return locationHooks;
    }

    private HashMap<Hook.Type, List<Hook>> collectObjectHooks(Action action, List<FormalElement> formalElements)
    {
        // create HashMap with all object hooks
        HashMap<Hook.Type, List<Hook>> objectHooks = new HashMap<Hook.Type, List<Hook>>()
        {
            {
                put(Hook.Type.BEFORE, new ArrayList<Hook>());
                put(Hook.Type.INSTEAD, new ArrayList<Hook>());
                put(Hook.Type.AFTER, new ArrayList<Hook>());
            }
        };

        // collect all object hooks
        for (FormalElement formalElement : formalElements)
        {
            if (FormalElement.Type.OBJECT.equals(formalElement.getType()) && formalElement.getObject() instanceof Item)
            {
                Item item = (Item) formalElement.getObject();
                for (Hook hook : item.getHooks())
                {
                    if (action.equals(hook.getAction()) && formalElement.getParameterName().equalsIgnoreCase(hook.getObjectElement()))
                    {
                        objectHooks.get(hook.getType()).add(hook);
                    }
                }
            }
        }
        return objectHooks;
    }

    private boolean checkActionRestrictions(Action action, List<Variable> parameters) throws IFML2Exception
    {
        for (Restriction restriction : action.getRestrictions())
        {
            try
            {
                RunningContext runningContext = RunningContext.CreateNewContext(virtualMachine);
                runningContext.populateParameters(parameters);
                Value isRestricted = ExpressionCalculator.calculate(runningContext, restriction.getCondition());
                if (!(isRestricted instanceof BooleanValue))
                {
                    throw new IFML2Exception("Выражение (%s) условия ограничения действия \"%s\" не логического типа.",
                            restriction.getCondition(), action);
                }
                if (((BooleanValue) isRestricted).getValue()) // if condition is true, run reaction
                {
                    virtualMachine.runInstructionList(restriction.getReaction(), runningContext);
                    return true;
                }
            }
            catch (IFML2Exception e)
            {
                throw new IFML2Exception(e, "{0}\n  при вычислении ограничения \"{1}\" действия \"{2}\"", e.getMessage(),
                        restriction.getCondition(), action);
            }
        }
        return false;
    }

    public Story getStory()
    {
        return story;
    }

    @Nullable
    public Location getCurrentLocation()
    {
        ObjectValue object = (ObjectValue) systemVariables.get(SystemIdentifiers.CURRENT_LOCATION_SYSTEM_VARIABLE.toLowerCase());
        return object != null ? (Location) object.getValue() : null;
    }

    public void setCurrentLocation(Location currentLocation)
    {
        systemVariables.put(SystemIdentifiers.CURRENT_LOCATION_SYSTEM_VARIABLE.toLowerCase(), new ObjectValue(currentLocation));
    }

    public ArrayList<Item> getInventory()
    {
        return inventory;
    }

    public Value resolveSymbol(@NotNull String symbol) throws IFML2VMException
    {
        String loweredSymbol = symbol.toLowerCase();

        try
        {
            if (ENGINE_SYMBOLS.containsKey(loweredSymbol))
            {
                return ENGINE_SYMBOLS.get(loweredSymbol).call();
            }
        }
        catch (Exception e)
        {
            throw new IFML2VMException(e, "  во время вычисления переменной движка {0}", symbol);
        }

        if (systemVariables.containsKey(loweredSymbol))
        {
            return systemVariables.get(loweredSymbol);
        }

        if (story.getObjectsHeap().containsKey(loweredSymbol))
        {
            return new ObjectValue(story.getObjectsHeap().get(loweredSymbol));
        }

        throw new IFML2VMException("Неизвестный идентификатор \"{0}\"", symbol);
    }

    public VirtualMachine getVirtualMachine()
    {
        return virtualMachine;
    }

    public void saveGame(String saveFileName) throws IFML2Exception
    {
        SavedGame savedGame = new SavedGame(dataHelper, story.getDataHelper());
        OMManager.saveGame(saveFileName, savedGame);
        outTextLn(MessageFormat.format("Игра сохранена в файл {0}.", saveFileName));
    }

    public void loadGame(String saveFileName) throws IFML2Exception
    {
        try
        {
            SavedGame savedGame = OMManager.loadGame(saveFileName);
            savedGame.restoreGame(dataHelper, story.getDataHelper());
            outTextLn(MessageFormat.format("Игра восстановлена из файла {0}.", saveFileName));
        }
        catch (IFML2Exception e)
        {
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
    public boolean checkDeepContent(Item itemToCheck, List<Item> items) throws IFML2Exception
    {
        for (Item item : items)
        {
            Value itemContents = item.getAccessibleContent(getVirtualMachine());
            if (itemContents != null)
            {
                if (!(itemContents instanceof CollectionValue))
                {
                    throw new IFML2VMException("Триггер доступного содержимого у предмета \"{0}\" вернул не коллекцию, а \"{1}\"!",
                            itemToCheck, itemContents.getTypeName());
                }

                List itemContentsList = ((CollectionValue) itemContents).getValue();
                List<Item> itemContentsItemList = new BasicEventList<Item>();
                for (Object object : itemContentsList)
                {
                    if (!(object instanceof Item))
                    {
                        throw new IFML2VMException(
                                "Триггер доступного содержимого у предмета \"{0}\" вернул в коллекции не предмет, а \"{1}\"!", itemToCheck,
                                object);
                    }

                    itemContentsItemList.add((Item) object);
                }

                if (itemContentsList.contains(itemToCheck) || checkDeepContent(itemToCheck, itemContentsItemList))
                {
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
    public boolean isObjectAccessible(IFMLObject object) throws IFML2Exception
    {
        Location currentLocation = getCurrentLocation();

        if (currentLocation == null)
        {
            throw new IFML2Exception("Системная ошибка: Текущая локация не задана!");
        }

        // test locations
        if (object instanceof Location)
        {
            return object.equals(currentLocation);
        }
        else if (object instanceof Item)   // test items
        {
            Item item = (Item) object;

            // test if object is in current location or player's inventory
            return currentLocation.contains(item) || inventory.contains(item) || checkDeepContent(item, currentLocation.getItems()) ||
                   checkDeepContent(item, inventory);
        }
        else
        {
            throw new IFML2Exception("Системная ошибка: Неизвестный тип объекта: \"{0}\".", object);
        }
    }

    public Variable searchGlobalVariable(@Nullable String name)
    {
        if (name == null)
        {
            return null;
        }

        String loweredName = name.toLowerCase();

        if (globalVariables.containsKey(loweredName))
        {
            return new GlobalVariableProxy(globalVariables, name, globalVariables.get(loweredName));
        }

        return null;
    }

    /**
     * Helper for saved games data.
     */
    public class DataHelper
    {
        public HashMap<String, Value> getGlobalVariables()
        {
            return globalVariables;
        }

        public HashMap<String, Value> getSystemVariables()
        {
            return systemVariables;
        }

        public ArrayList<Item> getInventory()
        {
            return inventory;
        }

        public List<Location> getLocations()
        {
            return story.getLocations();
        }

        public void setGlobalVariable(@NotNull String name, String expression) throws IFML2Exception
        {
            Value value = ExpressionCalculator.calculate(RunningContext.CreateNewContext(virtualMachine), expression);
            globalVariables.put(name.toLowerCase(), value);
        }

        public void setSystemVariable(String name, String expression) throws IFML2Exception
        {
            Value value = ExpressionCalculator.calculate(RunningContext.CreateNewContext(virtualMachine), expression);
            systemVariables.put(name, value);
        }

        public
        @NotNull
        String getStoryFileName()
        {
            return new File(storyFileName).getName();
        }

        public boolean isObjectAccessible(IFMLObject ifmlObject) throws IFML2Exception
        {
            return Engine.this.isObjectAccessible(ifmlObject);
        }

        public
        @Nullable
        Procedure getParseErrorHandler()
        {
            return story.getInheritedSystemProcedures().getParseErrorHandler();
        }
    }

    private class GlobalVariableProxy extends Variable
    {
        private final HashMap<String, Value> globalVariables;

        public GlobalVariableProxy(@NotNull HashMap<String, Value> globalVariables, @NotNull String name, Value value)
        {
            super(name.toLowerCase(), value);
            this.globalVariables = globalVariables;
        }

        @Override
        public Value getValue()
        {
            if (globalVariables.containsKey(name))
            {
                return globalVariables.get(name);
            }

            return null;
        }

        @Override
        public void setValue(Value value)
        {
            if (globalVariables.containsKey(name))
            {
                globalVariables.put(name, value);
            }
        }

        @Override
        public void setName(String name)
        {
            throw new RuntimeException("Внутренняя ошибка: Запрещено менять имена переменных");
        }
    }

    public enum SystemCommand
    {
        HELP
    }
}