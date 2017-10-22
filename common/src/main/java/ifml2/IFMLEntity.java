package ifml2;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import java.text.MessageFormat;

public class IFMLEntity implements Cloneable {
    protected static <T extends IFMLEntity> EventList<T> deepCloneEventList(
            EventList<T> eventList,
            Class<T> clazz
    ) throws CloneNotSupportedException {
        EventList<T> cloneList = new BasicEventList<T>();
        for (T element : eventList) {
            IFMLEntity clone = element.clone();
            if (clazz.isInstance(clone)) {
                cloneList.add(clazz.cast(clone));
            } else {
                throw new IllegalArgumentException(MessageFormat.format("EventList doesn't hold objects of {0} class", clazz.getSimpleName()));
            }
        }
        return cloneList;
    }

    protected IFMLEntity clone() throws CloneNotSupportedException {
        return (IFMLEntity) super.clone();
    }
}
