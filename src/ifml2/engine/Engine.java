package ifml2.engine;

import ca.odell.glazedlists.BasicEventList;
import ifml2.FormatLogger;
import ifml2.IFML2Exception;
import ifml2.SystemIdentifiers;
import ifml2.engine.saved.SavedGame;
import ifml2.om.*;
import ifml2.parser.FormalElement;
import ifml2.parser.Parser;
import ifml2.players.GameInterface;
import ifml2.vm.ExpressionCalculator;
import ifml2.vm.IFML2VMException;
import ifml2.vm.RunningContext;
import ifml2.vm.VirtualMachine;
import ifml2.vm.instructions.SetVarInstruction;
import ifml2.vm.values.BooleanValue;
import ifml2.vm.values.CollectionValue;
import ifml2.vm.values.ObjectValue;
import ifml2.vm.values.Value;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

public class Engine
{
    public static final String ENGINE_VERSION = "Хоббит, глава 4";
    public static final FormatLogger LOG = FormatLogger.getLogger(Engine.class);
    private final HashMap<String, Value> globalVariables = new HashMap<String, Value>();
    private final Parser parser = new Parser(this);
    private final VirtualMachine virtualMachine = new VirtualMachine();
    private final HashMap<String, Value> systemVariables = new HashMap<String, Value>();
    private GameInterface gameInterface = null;
    private Story story = null;
    private ArrayList<Item> inventory = new ArrayList<Item>();
    private ArrayList<Item> abyss = new ArrayList<Item>();
    private HashMap<String, Callable<? extends Value>> ENGINE_SYMBOLS = new HashMap<String, Callable<? extends Value>>()
    {
        {
            Callable<? extends Value> returnInv = new Callable<Value>()
            {
                @Override
                public Value call() throws Exception
                {
                    return new CollectionValue(inventory);
                }
            };

            put("инвентарий", returnInv);
            put("инвентарь", returnInv);
            put("куча", new Callable<Value>()
            {
                @Override
                public Value call() throws Exception
                {
                    return new CollectionValue(new ArrayList<IFMLObject>(story.getObjectsHeap().values()));
                }
            });
            put("словарь", new Callable<Value>()
            {
                @Override
                public Value call() throws Exception
                {
                    return new CollectionValue(new ArrayList<Word>(story.getDictionary().values()));
                }
            });
            put("пустота", new Callable<Value>()
            {
                @Override
                public Value call() throws Exception
                {
                    return new CollectionValue(abyss);
                }
            });
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
        parser.setStory(story);
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

        // load global vars
        globalVariables.clear();
        for (SetVarInstruction varInstruction : story.getStoryOptions().getVars())
        {
            Value value = ExpressionCalculator.calculate(virtualMachine.createGlobalRunningContext(), varInstruction.getValue());
            globalVariables.put(varInstruction.getName(), value);
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
                    }

                    // calculate property
                    property.evaluateFromPrimaryExpression(virtualMachine.createGlobalRunningContext());
                }
            }
        }

        // show initial info
        StoryOptions.StoryDescription storyDescription = getStory().getStoryOptions().getStoryDescription();
        outTextLn(storyDescription.getName() != null ? storyDescription.getName() : "<Без имени>");
        outTextLn("**********");
        outTextLn(storyDescription.getDescription() != null ? storyDescription.getDescription() : "<Без описания>");
        outTextLn(String.format("ВЕРСИЯ: %s", storyDescription.getVersion() != null ? storyDescription.getVersion() : "<Без версии>"));
        outTextLn(String.format("АВТОР: %s", storyDescription.getAuthor() != null ? storyDescription.getAuthor() : "<Без автора>"));
        outTextLn("**********\n");

        if (story.getStartProcedure() != null)
        {
            try
            {
                virtualMachine.runProcedure(getStory().getStartProcedure());
            }
            catch (IFML2Exception e)  // should we catch it?
            {
                outTextLn(e.getMessage());
            }
        }

        if (story.getStartLocation() != null)
        {
            setCurrentLocation(story.getStartLocation());
        }
        else
        {
            setCurrentLocation(story.getAnyLocation());
        }

        if (story.IsShowStartLocDesc())
        {
            // show first location description
            virtualMachine.showLocName(getCurrentLocation());
        }

        LOG.info("Game initialized.");
    }

    public boolean executeGamerCommand(String gamerCommand)
    {
        String trimmedCommand = gamerCommand.trim();

        // check help command
        if ("помощь".equalsIgnoreCase(trimmedCommand) || "помоги".equalsIgnoreCase(trimmedCommand) ||
            "помогите".equalsIgnoreCase(trimmedCommand) || "help".equalsIgnoreCase(trimmedCommand) ||
            "info".equalsIgnoreCase(trimmedCommand) ||
            "инфо".equalsIgnoreCase(trimmedCommand)) // todo refactor to List.contains() or something similar
        {
            outTextLn("Попробуйте одну из команд: " + story.getAllActions());
            return true;
        }

        // check debug command
        if (trimmedCommand.length() > 0 && trimmedCommand.charAt(0) == '?')
        {
            String expression = trimmedCommand.substring(1);
            try
            {
                Value value = ExpressionCalculator.calculate(virtualMachine.createGlobalRunningContext(), expression);
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
            parseResult = parser.parse(trimmedCommand);
            Action action = parseResult.getAction();
            List<FormalElement> formalElements = parseResult.getFormalElements();

            // check hooks & run procedure
            HashMap<Hook.HookTypeEnum, List<Hook>> objectHooks = collectObjectHooks(action, formalElements);
            HashMap<Hook.HookTypeEnum, List<Hook>> locationHooks = collectLocationHooks(action);

            // if there are INSTEAD hooks then fire them and finish
            if (objectHooks.get(Hook.HookTypeEnum.INSTEAD).size() > 0 || locationHooks.get(Hook.HookTypeEnum.INSTEAD).size() > 0)
            {
                // fire object hooks
                for (Hook hook : objectHooks.get(Hook.HookTypeEnum.INSTEAD))
                {
                    virtualMachine.runHook(hook, formalElements);
                }
                // fire location hooks
                for (Hook hook : locationHooks.get(Hook.HookTypeEnum.INSTEAD))
                {
                    virtualMachine.runHook(hook, formalElements);
                }

                // ... and finish
                return true;
            }

            // check restrictions
            if (checkActionRestrictions(action, formalElements))
            {
                return true;
            }

            // fire BEFORE hooks
            fireBeforeHooks(formalElements, objectHooks, locationHooks);
            // fire action
            virtualMachine.runAction(action, formalElements);
            // fire AFTER hooks
            fireAfterHooks(formalElements, objectHooks, locationHooks);
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

    private void fireAfterHooks(List<FormalElement> formalElements, HashMap<Hook.HookTypeEnum, List<Hook>> objectHooks, HashMap<Hook.HookTypeEnum, List<Hook>> locationHooks) throws IFML2Exception
    {
        // ... object hooks
        for (Hook hook : objectHooks.get(Hook.HookTypeEnum.AFTER))
        {
            virtualMachine.runHook(hook, formalElements);
        }
        // ... and location hooks
        for (Hook hook : locationHooks.get(Hook.HookTypeEnum.AFTER))
        {
            virtualMachine.runHook(hook, formalElements);
        }
    }

    private void fireBeforeHooks(List<FormalElement> formalElements, HashMap<Hook.HookTypeEnum, List<Hook>> objectHooks, HashMap<Hook.HookTypeEnum, List<Hook>> locationHooks) throws IFML2Exception
    {
        // ... object hooks
        for (Hook hook : objectHooks.get(Hook.HookTypeEnum.BEFORE))
        {
            virtualMachine.runHook(hook, formalElements);
        }
        // ... and location hooks
        for (Hook hook : locationHooks.get(Hook.HookTypeEnum.BEFORE))
        {
            virtualMachine.runHook(hook, formalElements);
        }
    }

    private HashMap<Hook.HookTypeEnum, List<Hook>> collectLocationHooks(Action action)
    {
        // create HashMap with all location hooks
        HashMap<Hook.HookTypeEnum, List<Hook>> locationHooks = new HashMap<Hook.HookTypeEnum, List<Hook>>()
        {
            {
                put(Hook.HookTypeEnum.BEFORE, new ArrayList<Hook>());
                put(Hook.HookTypeEnum.INSTEAD, new ArrayList<Hook>());
                put(Hook.HookTypeEnum.AFTER, new ArrayList<Hook>());
            }
        };

        // collect current location hooks
        for (Hook hook : getCurrentLocation().hooks)
        {
            if (action.equals(hook.getAction()))
            {
                locationHooks.get(hook.getType()).add(hook);
            }
        }
        return locationHooks;
    }

    private HashMap<Hook.HookTypeEnum, List<Hook>> collectObjectHooks(Action action, List<FormalElement> formalElements)
    {
        // create HashMap with all object hooks
        HashMap<Hook.HookTypeEnum, List<Hook>> objectHooks = new HashMap<Hook.HookTypeEnum, List<Hook>>()
        {
            {
                put(Hook.HookTypeEnum.BEFORE, new ArrayList<Hook>());
                put(Hook.HookTypeEnum.INSTEAD, new ArrayList<Hook>());
                put(Hook.HookTypeEnum.AFTER, new ArrayList<Hook>());
            }
        };

        // collect all object hooks
        for (FormalElement formalElement : formalElements)
        {
            if (FormalElement.FormalElementTypeEnum.OBJECT.equals(formalElement.getType()) && formalElement.getObject() instanceof Item)
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

    private boolean checkActionRestrictions(Action action, List<FormalElement> formalElements) throws IFML2Exception
    {
        for (Restriction restriction : action.getRestrictions())
        {
            try
            {
                RunningContext runningContext = new RunningContext(formalElements, virtualMachine);
                Value isRestricted = ExpressionCalculator.calculate(runningContext, restriction.getCondition());
                if (!(isRestricted instanceof BooleanValue))
                {
                    throw new IFML2Exception("Выражение (%s) условия ограничения действия \"%s\" не логического типа.",
                                             restriction.getCondition(), action);
                }
                if (((BooleanValue) isRestricted).getValue()) // if condition is true, run reaction
                {
                    virtualMachine.runInstructionList(restriction.getReaction(), runningContext, false, false);
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

    public Location getCurrentLocation()
    {
        return (Location) ((ObjectValue) systemVariables.get(SystemIdentifiers.CURRENT_LOCATION_SYSTEM_VARIABLE.toLowerCase())).value;
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

    public HashMap<String, Value> getGlobalVariables()
    {
        return globalVariables;
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

        // test locations
        if (object instanceof Location)
        {
            return object.equals(currentLocation);
        }
        else if (object instanceof Item)   // test items
        {
            Item item = (Item) object;

            // test if object is in current location or player's inventory
            boolean isInLocOrInv = currentLocation.contains(item) || inventory.contains(item);
            return isInLocOrInv || checkDeepContent(item, currentLocation.getItems()) || checkDeepContent(item, inventory);
        }
        else
        {
            throw new IFML2Exception("Системная ошибка: Неизвестный тип объекта: \"{0}\".", object);
        }
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

        public void setGlobalVariable(String name, String expression) throws IFML2Exception
        {
            Value value = ExpressionCalculator.calculate(virtualMachine.createGlobalRunningContext(), expression);
            globalVariables.put(name, value);
        }

        public void setSystemVariable(String name, String expression) throws IFML2Exception
        {
            Value value = ExpressionCalculator.calculate(virtualMachine.createGlobalRunningContext(), expression);
            systemVariables.put(name, value);
        }

        public
        @NotNull
        String getStoryFileName()
        {
            return new File(storyFileName).getName();
        }
    }
}