package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ifml2.IFML2Exception;
import ifml2.vm.IFML2VMException;
import ifml2.vm.RunningContext;
import ifml2.vm.values.BooleanValue;
import ifml2.vm.values.TextValue;
import ifml2.vm.values.Value;

import javax.xml.bind.annotation.*;
import java.text.MessageFormat;

import static ifml2.om.Word.GramCaseEnum;
import static ifml2.om.xml.XmlSchemaConstants.*;

public class IFMLObject implements Cloneable
{
    @Override
    public IFMLObject clone() throws CloneNotSupportedException
    {
        IFMLObject ifmlObject = (IFMLObject) super.clone();
        ifmlObject.wordLinks = wordLinks.clone();
        ifmlObject.attributes = GlazedLists.eventList(attributes);
        return ifmlObject;
    }

    private static final String NAME_PROPERTY_LITERAL = "имя";
    private static final String DESCRIPTION_PROPERTY_LITERAL = "описание";

    private String id;
    @XmlAttribute(name = "id")
	@XmlID
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

    private WordLinks wordLinks = new WordLinks();
    @XmlElement(name = OBJECT_WORDS_TAG)
    public WordLinks getWordLinks() { return wordLinks; }
    public void setWordLinks(WordLinks wordLinks) { this.wordLinks = wordLinks; }

    private String name;
	@XmlAttribute(name = "name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    private String description;
	@XmlElement(name = "description")
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    private EventList<Attribute> attributes = new BasicEventList<Attribute>();
    @XmlElementWrapper(name = IFML_OBJECT_ATTRIBUTES_ELEMENT)
    @XmlElement(name = IFML_OBJECT_ATTRIBUTE_ELEMENT)
    @XmlIDREF
    public EventList<Attribute> getAttributes() { return attributes; }
    public void setAttributes(EventList<Attribute> attributes) { this.attributes = attributes; }

    @XmlElementWrapper(name = IFML_OBJECT_ROLES_ELEMENT)
    @XmlElement(name = IFML_OBJECT_ROLE_ELEMENT)
    private EventList<Role> roles = new BasicEventList<Role>();

	@Override
	public String toString()
	{
		return getName();
	}

    public String getName(GramCaseEnum gramCase) throws IFML2Exception
    {
        if(wordLinks == null)
        {
            throw new IFML2Exception(MessageFormat.format("Ссылка на словарь пустая у объекта {0}!", getName()));
        }
        else if(wordLinks.getMainWord() == null)
        {
            throw new IFML2Exception(MessageFormat.format("Ссылка на главное слово пустая у объекта {0}!", getName()));
        }

        return wordLinks.getMainWord().getFormByGramCase(gramCase);
    }

    public Value getPropertyValue(String propertyName, RunningContext runningContext) throws IFML2Exception
    {
        // test system properties
        if(NAME_PROPERTY_LITERAL.equalsIgnoreCase(propertyName))
        {
            return new TextValue(getName());
        }
        if(DESCRIPTION_PROPERTY_LITERAL.equalsIgnoreCase(propertyName))
        {
            return new TextValue(getDescription());
        }

        // test cases
        String upperPropertyName = propertyName.toUpperCase();
        GramCaseEnum caseEnum = GramCaseEnum.getValueByAbbr(upperPropertyName);
        if(caseEnum != null)
        {
            return new TextValue(wordLinks.getMainWord().getFormByGramCase(caseEnum));
        }

        // test attributes
        for(Attribute attribute : attributes)
        {
            if(attribute != null && attribute.getName().equalsIgnoreCase(propertyName))
            {
                return new BooleanValue(true);
            }
        }

        // test principal possibility of attribute
        for(Attribute attribute : runningContext.getStory().getAllAttributes())
        {
            if(attribute.getName().equalsIgnoreCase(propertyName))
            {
                // there is such attribute but object hasn't it - take false and exit
                return new BooleanValue(false);
            }
        }

        throw new IFML2VMException("У объекта \"{0}\" нет свойства \"{1}\", а также в игре нет признаков с таким названием.", this, propertyName);
    }
}
