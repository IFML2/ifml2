package ifml2.parser;

import ca.odell.glazedlists.BasicEventList;
import ifml2.CommonUtils;
import ifml2.IFML2Exception;
import ifml2.engine.Engine;
import ifml2.om.*;
import ifml2.vm.IFML2VMException;
import ifml2.vm.values.CollectionValue;
import ifml2.vm.values.Value;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Parser
{
    public Parser(Engine engine)
    {
        setEngine(engine);
    }

    Story getStory()
    {
        return story;
    }

    public void setStory(Story story)
    {
        this.story = story;
    }

    void setEngine(Engine engine)
    {
        this.engine = engine;
    }

    public class ParseResult
	{
		public Action action = null;
		public List<FormalElement> formalElements = null;

        public ParseResult(Action action, ArrayList<FormalElement> formalElements)
        {
            this.action = action;
            this.formalElements = formalElements;
        }

        public Action getAction()
        {
            return action;
        }

        public List<FormalElement> getFormalElements()
        {
            return formalElements;
        }
    }

    private class TemplateElementFitResult
    {
        public FittedFormalElement fittedFormalElement = null;
        public int usedWordsQty = 0;

        public TemplateElementFitResult(FittedFormalElement fittedSynonym, int usedWordsQty)
        {
            this.fittedFormalElement = fittedSynonym;
            this.usedWordsQty = usedWordsQty;
        }
    }

    private class FittedTemplate
    {
        public Action action = null;
        public ArrayList<FittedFormalElement> fittedFormalElements = new ArrayList<FittedFormalElement>();

        public ArrayList<FittedFormalElement> getFittedFormalElements()
        {
            return fittedFormalElements;
        }

        public FittedTemplate(Action action, ArrayList<FittedFormalElement> fittedFormalElements)
        {
            this.action = action;
            this.fittedFormalElements = fittedFormalElements;
        }
    }

    protected class FittedFormalElement
    {
        String parameter = "";
    }
    private class FittedSynonym extends FittedFormalElement
    {
        String synonym = null;

        public FittedSynonym(String synonym, String parameter)
        {
            this.synonym = synonym;
            this.parameter = parameter;
        }

        @Override
        public String toString()
        {
            return synonym;
        }
    }
    private class FittedObjects extends FittedFormalElement
    {
        ArrayList<IFMLObject> objects = new ArrayList<IFMLObject>();
        public ArrayList<IFMLObject> getObjects()
        {
            return objects;
        }

        final Word.GramCaseEnum gramCase;

        public FittedObjects(ArrayList<IFMLObject> objects, Word.GramCaseEnum gramCase, String parameter)
        {
            this.objects = objects;
            this.gramCase = gramCase;
            this.parameter = parameter;
        }

        @Override
        public String toString()
        {
            return objects.get(0).toString();
        }
    }

	private Story story = null;
	private Engine engine = null;

    public ParseResult parse(String phrase) throws IFML2Exception
    {
        if(getStory() == null)
        {
            throw new IFML2Exception("Системная ошибка: Ссылка на объектную модель (story) не задана!");
        }

        if (getStory().getAllActions() == null || getStory().getAllActions().size() == 0)
        {
            throw new IFML2Exception("Системная ошибка: В системе нет ни одного действия.");
        }

        // split phrase to array
        ArrayList<String> phraseAsList = new ArrayList<String>(Arrays.asList(phrase.split("\\s+")));

        // prepare list of all fitted templates
        ArrayList<FittedTemplate> fittedTemplates = new ArrayList<FittedTemplate>();

        IFML2Exception lastException = null;

        for(Action action : getStory().getAllActions())
        {
            for(Template template : action.templates)
            {
                try
                {
                    ArrayList<FittedFormalElement> fittedFormalElements =
                            fitPhraseWithTemplate(phraseAsList, template.getElements());

                    FittedTemplate fittedTemplate = new FittedTemplate(action, fittedFormalElements);
                    fittedTemplates.add(fittedTemplate);
                }
                catch (IFML2ParsePhraseTooLong e)
                {
                    if(lastException == null
                                || (lastException instanceof IFML2ParseException
                                    && e.isMoreFull((IFML2ParseException) lastException, template.size())))
                    {
                        lastException = new IFML2ParseException(MessageFormat.format(
                                "Я бы понял, если бы вы сказали \"{0}\", но я не понял вот эту часть фразы: \"{1}\".",
                                convertFittedToString(e.getFittedFormalElements()), e.getPhraseRest()), // convert phrase rest to normal string
                                e.getUsedWords(), template.size());
                    }
                }
                catch (IFML2ParseException e)
                {
                    if(lastException == null
                                || (lastException instanceof IFML2ParseException
                                    && e.isMoreFull((IFML2ParseException) lastException, template.size())))
                    {
                        lastException = e;
                        ((IFML2ParseException) lastException).setTemplateSize(template.size());
                    }
                }
                catch (IFML2Exception e)
                {
                    lastException = e;
                }
            }
        }

        if(fittedTemplates.size() == 0)
        {
            throw lastException;
        }

        // clean fitted templates from templates with inaccessible objects
        ArrayList<FittedTemplate> accessibleTemplates = null;
        try
        {
            accessibleTemplates = removeInaccessibleObjects(fittedTemplates);
        }
        catch (IFML2Exception e)
        {
            lastException = e; // always rewrite last exception because inaccessible objects are more important for errors
        }

        if(accessibleTemplates == null || accessibleTemplates.size() == 0)
        {
            throw lastException;
        }

        // take the first of fitted templates
        FittedTemplate firstFittedTemplate = accessibleTemplates.get(0);

        // test fitted template on variance of objects
        for(FittedFormalElement fittedFormalElement : firstFittedTemplate.fittedFormalElements)
        {
            if(fittedFormalElement instanceof FittedObjects)
            {
                List<IFMLObject> objects = ((FittedObjects) fittedFormalElement).objects;
                if(objects.size() > 1)
                {
                    throw new IFML2ParseException("Не понятно, что за " + objects.get(0).getWordLinks().getMainWord() + " имеется в виду.", phraseAsList.size());
                }
            }
        }

        ArrayList<FormalElement> formalElements = new ArrayList<FormalElement>();
        for(FittedFormalElement fittedFormalElement : firstFittedTemplate.fittedFormalElements)
        {
            FormalElement formalElement;
            if(fittedFormalElement instanceof FittedSynonym)
            {
                FittedSynonym fittedSynonym = (FittedSynonym) fittedFormalElement;
                formalElement = new FormalElement(fittedSynonym.synonym, fittedSynonym.parameter);
            }
            else
            if(fittedFormalElement instanceof FittedObjects)
            {
                FittedObjects fittedObjects = (FittedObjects) fittedFormalElement;
                formalElement = new FormalElement(fittedObjects.objects.get(0), fittedObjects.parameter);
            }
            else
            {
                throw new IFML2Exception("Системная ошибка: ПодходящийФомральныйЭлемент имеет неизвестный тип\n"
                        + "Фраза: " + phrase + "\n"
                        + "Шаблон: " + firstFittedTemplate.fittedFormalElements + ".");
            }

            formalElements.add(formalElement);
        }

        return new ParseResult(firstFittedTemplate.action, formalElements);
    }

    private String convertFittedToString(ArrayList<FittedFormalElement> fittedFormalElements) throws IFML2Exception
    {
        String result = "";

        for(FittedFormalElement fittedFormalElement : fittedFormalElements)
        {
            String element = "";
            if(fittedFormalElement instanceof FittedSynonym)
            {
                element = fittedFormalElement.toString();
            }
            else
            if(fittedFormalElement instanceof FittedObjects)
            {
                ArrayList<IFMLObject> fittedObjects = ((FittedObjects) fittedFormalElement).objects;

                if(fittedObjects.size() > 0)
                {
                    element = fittedObjects.get(0).getName(((FittedObjects) fittedFormalElement).gramCase);
                }
                else
                {
                    throw new IFML2Exception("Системная ошибка: в FittedObjects кол-во объектов = 0\n" +
                        "fittedFormalElements = " + fittedFormalElements + '\n' +
                        "fittedFormalElement = " + fittedFormalElement);
                }
            }
            result += " " + element;
        }

        return result.trim();
    }

    private ArrayList<FittedTemplate> removeInaccessibleObjects(ArrayList<FittedTemplate> fittedTemplates) throws IFML2Exception
    {
        ArrayList<FittedTemplate> result = new ArrayList<FittedTemplate>();
        IFMLObject inaccessibleObject = null;

        for (FittedTemplate fittedTemplate : fittedTemplates)
        {
            Boolean toAddTemplate = true;

            for (FittedFormalElement fittedFormalElement : fittedTemplate.getFittedFormalElements())
            {
                if (fittedFormalElement instanceof FittedObjects)
                {
                    ArrayList<IFMLObject> objectsToRemove = new ArrayList<IFMLObject>();

                    ArrayList<IFMLObject> fittedObjects = ((FittedObjects) fittedFormalElement).getObjects();

                    for (IFMLObject object : fittedObjects)
                    {
                        if (!isObjectAccessible(object))
                        {
                            objectsToRemove.add(object);

                            if (inaccessibleObject == null)
                            {
                                inaccessibleObject = object;
                            }
                        }
                    }

                    fittedObjects.removeAll(objectsToRemove);

                    if (fittedObjects.size() == 0)
                    {
                        toAddTemplate = false;
                    }
                }
            }

            if (toAddTemplate)
            {
                result.add(fittedTemplate);
            }
        }

        if (result.size() > 0)
        {
            return result;
        }
        else
        {
            if (inaccessibleObject != null)
            {
                throw new IFML2ParseException("Не вижу здесь " + inaccessibleObject.getName(Word.GramCaseEnum.VP)
                        + ".");
            }
            else
            {
                throw new IFML2Exception("Системная ошибка: inaccessibleObject = null в Parser.removeInaccessibleObjects() при result.size() = 0.");
            }
            // it doesn't require word count because it's outstanding exception
        }
    }

    /***
     * Checks if object is inaccessible for player's actions
     * @param object IFMLObject for check
     * @return true if object is inaccessible
     * @throws IFML2Exception when tested objects neither location or item
     */
    private boolean isObjectAccessible(IFMLObject object) throws IFML2Exception
    {
        Location currentLocation = engine.getCurrentLocation();

        // test locations
        if(object instanceof Location)
        {
            return object.equals(currentLocation);
        }
        else

        // test items
        if(object instanceof Item)
        {
            Item item = (Item) object;

            // test if object is in current location or player's inventory
            boolean isInLocOrInv = currentLocation.contains(item) || engine.getInventory().contains(item);
            if(isInLocOrInv)
            {
                return isInLocOrInv;
            }
            else
            {
                // test contents of current location's items using item triggers
                return checkDeepContent(item, currentLocation.getItems());
            }
        }
        else
        {
            throw new IFML2Exception("Системная ошибка: Неизвестный тип объекта: \"{0}\".", object);
        }
    }

    private boolean checkDeepContent(Item item, List<Item> items) throws IFML2Exception
    {
        for (Item locItem : items)
        {
            Value itemContents = locItem.getAccessibleContent(engine.getVirtualMachine());
            if(itemContents != null)
            {
                if (!(itemContents instanceof CollectionValue))
                {
                    throw new IFML2VMException("Триггер доступного содержимого у предмета \"{0}\" вернул не коллекцию, а \"{1}\"!",
                            item, itemContents.getTypeName());
                }

                List itemContentsList = ((CollectionValue) itemContents).getValue();
                List<Item> itemContentsItemList = new BasicEventList<Item>();
                for (Object object : itemContentsList)
                {
                    if(!(object instanceof Item))
                    {
                        throw new IFML2VMException("Триггер доступного содержимого у предмета \"{0}\" вернул в коллекции не предмет, а \"{1}\"!",
                                item, object);
                    }

                    itemContentsItemList.add((Item) object);
                }

                if(itemContentsList.contains(item) || checkDeepContent(item, itemContentsItemList))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private ArrayList<FittedFormalElement> fitPhraseWithTemplate(ArrayList<String> phraseAsList,
                                                                     List<TemplateElement> template) throws IFML2Exception
    {
        // get vars into local copy
        ArrayList<String> phraseRest = new ArrayList<String>(phraseAsList);
        ArrayList<TemplateElement> templateRest = new ArrayList<TemplateElement>(template);

        // take the first element of template
        TemplateElement firstTemplateElement = templateRest.get(0);

        // try to fit template element with beginning of phrase
        TemplateElementFitResult result = fitTemplateElementWithPhrase(firstTemplateElement, phraseRest);

        ArrayList<FittedFormalElement> fittedFormalElements = new ArrayList<FittedFormalElement>();

        fittedFormalElements.add(result.fittedFormalElement);

        // cut template and phrase
        templateRest.remove(0);
        for(int i = 1; i <= result.usedWordsQty; i++)
        {
            phraseRest.remove(0);
        }

        if(templateRest.size() == 0 && phraseRest.size() == 0)
        {
            return fittedFormalElements;
        }
        else
        if(templateRest.size() > 0 && phraseRest.size() == 0)
        {
            throw new IFML2ParseException(makeQuestionsForTemplate(templateRest) + " (пишите ответ полностью)", result.usedWordsQty);
        }
        else
        if(templateRest.size() == 0 && phraseRest.size() > 0)
        {
            throw new IFML2ParsePhraseTooLong(fittedFormalElements, phraseRest, result.usedWordsQty);
        }
        else
        {
            try
            {
                ArrayList<FittedFormalElement> nextElements = fitPhraseWithTemplate(phraseRest, templateRest);
                fittedFormalElements.addAll(nextElements);
                return fittedFormalElements;
            }
            catch (IFML2ParsePhraseTooLong e)
            {
                e.getFittedFormalElements().add(0, result.fittedFormalElement);
                e.setUsedWords(e.getUsedWords() + result.usedWordsQty);
                throw e;
            }
            catch (IFML2ParseException e)
            {
                e.setUsedWords(e.getUsedWords() + result.usedWordsQty);
                throw e;
            }
        }
    }

    private TemplateElementFitResult fitTemplateElementWithPhrase(TemplateElement templateElement, ArrayList<String> phrase) throws IFML2Exception
    {
        IFML2Exception lastException = null;

        if(templateElement instanceof LiteralTemplateElement)
        {
            for(String synonym : ((LiteralTemplateElement) templateElement).synonyms)
            {
                FittedFormalElement fittedFormalElement = new FittedSynonym(synonym, templateElement.parameter);
                try
                {
                    int usedWordsQty = fitSynonymWithPhrase(synonym, phrase);
                    return new TemplateElementFitResult(fittedFormalElement, usedWordsQty);
                }
                catch (IFML2ParseException e)
                {
                    if(lastException == null || ((e.getUsedWords() > ((IFML2ParseException) lastException).getUsedWords())))
                    {
                        lastException = e;
                    }
                }
            }

            if(lastException != null)
            {
                throw lastException;
            }
            else
            {
                throw new IFML2Exception("Системная ошибка: при сопоставлении ЭлементаШаблона с началом фразы проверка синонимов ни к чему не привел.");
            }
        }

        else if(templateElement instanceof ObjectTemplateElement)
        {
            Word.GramCaseEnum gramCase = ((ObjectTemplateElement) templateElement).gramCase;

            FitObjectWithPhraseResult fitObjectWithPhraseResult = fitObjectWithPhrase(gramCase, phrase);
            ArrayList<IFMLObject> objects = fitObjectWithPhraseResult.getObjects();
            int usedWordsQty = fitObjectWithPhraseResult.getUsedWordsQty();

            /*
            ArrayList<IFMLObject> objects;
            String word = phrase.get(0);

            Word.GramCaseEnum gramCase = ((ObjectTemplateElement) templateElement).gramCase;

            objects = fitObjectWithWord(gramCase, word);
            */

            /*if(objects == null || objects.size() == 0)
            {
                throw new IFML2ParseException("Не знаю, что такое \"" + word + "\".", 1);
            }
            else
            {*/
            return new TemplateElementFitResult(new FittedObjects(objects, gramCase, templateElement.parameter), usedWordsQty);
            /*}*/
        }
        else
        {
            throw new IFML2Exception("Системная ошибка: ЭлементШаблона неизвестного типа.");
        }
    }

    private FitObjectWithPhraseResult fitObjectWithPhrase(Word.GramCaseEnum gramCase, ArrayList<String> phrase) throws IFML2Exception
    {
        List<String> restPhrase = new ArrayList<String>(phrase);
        ArrayList<Word> fittedWords = new ArrayList<Word>();
        int allUsedWords = 0;

        // Stage I

        while (true)
        {
            if (restPhrase.size() == 0)
            {
                break;
            }

            boolean wordIsFound = false;

            for (Word dictWord : getStory().getDictionary().values())
            {
                int usedWords = fitWordWithPhrase(dictWord, gramCase, restPhrase);

                allUsedWords += usedWords;

                if (usedWords > 0)
                {
                    // case when dict word has no links to objects
                    if(dictWord.linkerObjects.size() == 0)
                    {
                        throw new IFML2ParseException(MessageFormat.format("Нигде не вижу {0}.", dictWord.getFormByGramCase(Word.GramCaseEnum.VP)), allUsedWords);
                    }

                    if (fittedWords.contains(dictWord))
                    {
                        String usedPhrase = "";
                        for (String word : phrase.subList(0, allUsedWords - 1))
                        {
                            usedPhrase += " " + word;
                        }

                        throw new IFML2ParseException(MessageFormat.format("Я бы понял фразу, если бы вы сказали \"{0}\"", usedPhrase.trim()), allUsedWords);
                    }

                    fittedWords.add(dictWord);
                    restPhrase = restPhrase.subList(usedWords, restPhrase.size());
                    wordIsFound = true;
                    break;
                }
            }

            if (!wordIsFound)
            {
                if (fittedWords.size() > 0)
                {
                    break;
                }
                else
                {
                    throw new IFML2ParseException(MessageFormat.format("Не знаю слово \"{0}\".", restPhrase.get(0)), 1);
                }
            }
        }

        // Stage II

        ArrayList<IFMLObject> objects = new ArrayList<IFMLObject>();
        objects.addAll(fittedWords.get(0).linkerObjects);

        if (fittedWords.size() == 1)
        {
            return new FitObjectWithPhraseResult(objects, allUsedWords);
        }

        for (Word word : fittedWords.subList(1, fittedWords.size()))
        {
            for (Iterator<IFMLObject> iterator = objects.iterator(); iterator.hasNext();)
            {
                IFMLObject object = iterator.next();
                if (!word.linkerObjects.contains(object))
                {
                    if (objects.size() > 1)
                    {
                        iterator.remove();
                    }
                    else
                    {
                        throw new IFML2ParseException(MessageFormat.format("Не знаю такого предмета – \"{0}\"", fittedWords));
                    }
                }
            }
        }

        return new FitObjectWithPhraseResult(objects, allUsedWords);
    }

    private int fitWordWithPhrase(Word word, Word.GramCaseEnum gramCase, List<String> restPhrase)
    {
        String casedDictWord = word.getFormByGramCase(gramCase);
        List<String> casedDictWordArray = Arrays.asList(casedDictWord.split("\\s+"));

        if (casedDictWordArray.size() > 0 && casedDictWordArray.size() <= restPhrase.size())
        {
            int currentWord = 0;

            for (String dictWordPart : casedDictWordArray)
            {
                String phraseWord = restPhrase.get(currentWord);
                if (!dictWordPart.equalsIgnoreCase(phraseWord))
                {
                    break;
                }
                currentWord++;
            }

            return currentWord;
        }

        return 0;
    }

    private int fitSynonymWithPhrase(String synonym, ArrayList<String> phrase) throws IFML2ParseException
    {
        ArrayList<String> synonymWords = new ArrayList<String>(Arrays.asList(synonym.split("\\s+")));

        if(synonymWords.size() > phrase.size())
        {
            throw new IFML2ParseException("Я бы понял, если бы вы упомянули \"" + synonym + "\".", 0);
        }

        int wordNum = 0;
        for (String synonymWord : synonymWords)
        {
            String phraseWord = phrase.get(wordNum);
            if(!synonymWord.equalsIgnoreCase(phraseWord))
            {
                throw new IFML2ParseException("Не знаю, что такое \"" + phraseWord + "\".", wordNum);
            }
            wordNum++;
        }

        return wordNum;
    }

    private String makeQuestionsForTemplate(ArrayList<TemplateElement> templateRest) throws IFML2Exception
    {
        String result = "";

        for(TemplateElement templateElement : templateRest)
        {
            if(templateElement instanceof LiteralTemplateElement)
            {
                result += ' ' + ((LiteralTemplateElement) templateElement).synonyms.get(0);
            }
            else
            if(templateElement instanceof ObjectTemplateElement)
            {
                result += ' ' + ((ObjectTemplateElement) templateElement).gramCase.getQuestionWord();
            }
            else
            {
                throw new IFML2Exception("Системная ошибка: ЭлементШаблона неизвестного типа.");
            }
        }

        return CommonUtils.uppercaseFirstLetter(result.trim()) + "?";
    }

    public class FitObjectWithPhraseResult
    {
        private final ArrayList<IFMLObject> objects;

        public int getUsedWordsQty()
        {
            return usedWordsQty;
        }

        private final int usedWordsQty;

        public FitObjectWithPhraseResult(ArrayList<IFMLObject> objects, int usedWordsQty)
        {
            this.objects = objects;
            this.usedWordsQty = usedWordsQty;
        }

        public ArrayList<IFMLObject> getObjects()
        {
            return objects;
        }

    }
}
