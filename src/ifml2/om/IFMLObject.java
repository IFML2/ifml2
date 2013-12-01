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
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.*;
import java.text.MessageFormat;

import static ifml2.om.Word.GramCaseEnum;
import static ifml2.om.xml.XmlSchemaConstants.*;

public class IFMLObject implements Cloneable
{
    private static final String NAME_PROPERTY_LITERAL = "имя";
    private static final String DESCRIPTION_PROPERTY_LITERAL = "описание";
    @XmlElementWrapper(name = ITEM_HOOKS_ELEMENT)
    @XmlElement(name = ITEM_HOOK_ELEMENT)
    public EventList<Hook> hooks = new BasicEventList<Hook>();
    @XmlElementWrapper(name = OBJECT_PROPERTIES_ELEMENT)
    @XmlElement(name = OBJECT_PROPERTY_ELEMENT)
    private EventList<Property> properties = new BasicEventList<Property>();
    private String id;
    private WordLinks wordLinks = new WordLinks();
    private String name;
    private String description;
    private EventList<Attribute> attributes = new BasicEventList<Attribute>();
    @XmlElementWrapper(name = IFML_OBJECT_ROLES_ELEMENT)
    @XmlElement(name = IFML_OBJECT_ROLE_ELEMENT)
    protected EventList<Role> roles = new BasicEventList<Role>();

    @Override
    public IFMLObject clone() throws CloneNotSupportedException
    {
        IFMLObject ifmlObject = (IFMLObject) super.clone();
        ifmlObject.wordLinks = wordLinks.clone();
        ifmlObject.attributes = GlazedLists.eventList(attributes);
        return ifmlObject;
    }

    public String getId()
    {
        return id;
    }

    @XmlAttribute(name = "id")
    @XmlID
    public void setId(String id)
    {
        this.id = id;
    }

    public WordLinks getWordLinks()
    {
        return wordLinks;
    }

    @XmlElement(name = OBJECT_WORDS_TAG)
    public void setWordLinks(@NotNull WordLinks wordLinks) throws IFML2Exception
    {
        this.wordLinks = wordLinks;
    }

    public String getName()
    {
        return name;
    }

    @XmlAttribute(name = "name")
    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    @XmlElement(name = "description")
    public void setDescription(String description)
    {
        this.description = description;
    }

    public EventList<Attribute> getAttributes()
    {
        return attributes;
    }

    @XmlElementWrapper(name = IFML_OBJECT_ATTRIBUTES_ELEMENT)
    @XmlElement(name = IFML_OBJECT_ATTRIBUTE_ELEMENT)
    @XmlIDREF
    public void setAttributes(EventList<Attribute> attributes)
    {
        this.attributes = attributes;
    }

    public EventList<Role> getRoles()
    {
        return roles;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public String getName(GramCaseEnum gramCase) throws IFML2Exception
    {
        if (wordLinks == null)
        {
            throw new IFML2Exception(MessageFormat.format("Ссылка на словарь пустая у объекта {0}!", name));
        }
        else if (wordLinks.getMainWord() == null)
        {
            throw new IFML2Exception(MessageFormat.format("Ссылка на главное слово пустая у объекта {0}!", name));
        }

        return wordLinks.getMainWord().getFormByGramCase(gramCase);
    }

    public Value getMemberValue(String propertyName, RunningContext runningContext) throws IFML2Exception
    {
        // test system properties
        if (NAME_PROPERTY_LITERAL.equalsIgnoreCase(propertyName))
        {
            return new TextValue(name);
        }
        if (DESCRIPTION_PROPERTY_LITERAL.equalsIgnoreCase(propertyName))
        {
            return new TextValue(description);
        }

        // test gram cases
        GramCaseEnum caseEnum = GramCaseEnum.getValueByAbbr(propertyName);
        if (caseEnum != null)
        {
            return new TextValue(wordLinks.getMainWord().getFormByGramCase(caseEnum));
        }

        // test attributes
        for (Attribute attribute : attributes)
        {
            if (attribute.getName().equalsIgnoreCase(propertyName))
            {
                return new BooleanValue(true);
            }
        }

        // test roles and properties
        for (Role role : roles)
        {
            // test role as boolean
            if (role.getName().equalsIgnoreCase(propertyName))
            {
                return new BooleanValue(true);
            }

            // test attributes of role
            for (Attribute attribute : role.getRoleDefinition().getAttributes())
            {
                if (attribute.getName().equalsIgnoreCase(propertyName))
                {
                    return new BooleanValue(true);
                }
            }

            // test properties of role
            for (Property property : role.getProperties())
            {
                if (property.getName().equalsIgnoreCase(propertyName))
                {
                    return property.getValue();
                }
            }
        }

        // test principal possibility of attribute
        for (Attribute attribute : runningContext.getStory().getAllAttributes())
        {
            if (attribute.getName().equalsIgnoreCase(propertyName))
            {
                // there is such attribute but object hasn't it - take false and exit
                return new BooleanValue(false);
            }
        }

        // test principal possibility of role
        for (RoleDefinition roleDefinition : runningContext.getStory().getAllRoleDefinitions())
        {
            if (roleDefinition.getName().equalsIgnoreCase(propertyName))
            {
                // there is such role but object hasn't it - take false and exit
                return new BooleanValue(false);
            }
        }

        throw new IFML2VMException("У объекта \"{0}\" нет свойства \"{1}\", а также в игре нет признаков и ролей с таким названием.", this,
                                   propertyName);
    }

    public Value tryGetMemberValue(String symbol, RunningContext runningContext)
    {
        try
        {
            return getMemberValue(symbol, runningContext);
        }
        catch (IFML2Exception e)
        {
            return null;
        }
    }

    public Property findPropertyByName(String name)
    {
        // search in local properties
        for (Property property : properties)
        {
            if (property.getName().equalsIgnoreCase(name))
            {
                return property;
            }
        }

        // search in roles' properties
        for (Role role : roles)
        {
            Property property = role.findPropertyByName(name);
            if (property != null)
            {
                return property;
            }
        }

        return null;
    }

    public Role findRoleByName(String name)
    {
        for (Role role : roles)
        {
            if (role.getName().equalsIgnoreCase(name))
            {
                return role;
            }
        }

        return null;
    }
}
