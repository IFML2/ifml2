package ifml2.engine.saved;

import ifml2.FormatLogger;
import ifml2.om.*;
import ifml2.vm.values.CollectionValue;
import ifml2.vm.values.Value;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class SavedProperty
{
    private static final FormatLogger LOG = FormatLogger.getLogger(SavedProperty.class);
    @XmlAttribute(name = "name")
    private String name;
    @XmlElement(name = "item")
    private ArrayList<String> items = new ArrayList<String>();

    @SuppressWarnings("UnusedDeclaration")
    public SavedProperty()
    {
        // JAXB
    }

    public SavedProperty(@NotNull Property property)
    {
        name = property.getName();
        Value value = property.getValue();
        if (value instanceof CollectionValue)
        {
            List<?> collection = ((CollectionValue) value).getValue();
            for (Object obj : collection)
            {
                if (obj instanceof Item)
                {
                    items.add(((Item) obj).getId());
                }
            }
        }
        else
        {
            LOG.error("Системная ошибка: свойство \"{0}\" помечено как коллекция, но хранит значение другого типа - \"{1)\".", name,
                      value.getTypeName());
        }
    }

    public void restore(@NotNull Role role, @NotNull Story.DataHelper dataHelper)
    {
        Property property = role.findPropertyByName(name);
        if (property != null)
        {
            RoleDefinition roleDefinition = role.getRoleDefinition();
            PropertyDefinition propertyDefinition = roleDefinition.findPropertyDefinitionByName(name);
            if (propertyDefinition != null)
            {
                // restore only collections
                if (PropertyDefinition.PropertyTypeEnum.COLLECTION.equals(propertyDefinition.getType()))
                {
                    List<Item> propItems = new ArrayList<Item>();
                    for (String itemId : items)
                    {
                        Item propItem = dataHelper.findItemById(itemId);
                        if (propItem != null)
                        {
                            propItem.moveTo(propItems);
                        }
                        else
                        {
                            LOG.warn("[Game loading] Location items loading: there is no item with id \"{0}\".", itemId);
                        }
                    }
                    property.setValue(new CollectionValue(propItems));
                }
                else
                {
                    LOG.error("Системная ошибка: свойство \"{0}\" в роли \"{1}\"" +
                              "не помечено как коллекция, но сохранено в сохранённой игре как коллекция.", name, role.getName());
                }
            }
            else
            {
                LOG.error("Системная ошибка: в роли {0} не найдено свойство {1}.", role, name);
            }
        }
        else
        {
            LOG.warn("Не найдено свойство по имени \"{0}\" в роли \"{1}\".", name, role.getName());
        }
    }
}
