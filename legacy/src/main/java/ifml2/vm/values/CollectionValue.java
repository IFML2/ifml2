package ifml2.vm.values;

import ifml2.IFMLEntity;
import ifml2.om.IFMLObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;

public class CollectionValue extends Value<List<? extends IFMLEntity>> {
    public CollectionValue(List<? extends IFMLEntity> value) {
        super(value);
    }

    @Override
    public String toString() {
        return format("[%s]", String.join(", ", () -> {
            return new Iterator<CharSequence>() {
                public Iterator<? extends IFMLEntity> iterator = value.iterator();

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public CharSequence next() {
                    IFMLEntity next = iterator.next();
                    if (next instanceof IFMLObject) {
                        IFMLObject object = (IFMLObject) next;
                        return format("%s \"%s\"", object.getId(), object.getName());
                    }
                    else {
                        return next.toString();
                    }
                }
            };
        }));
    }

    @Override
    public String getTypeName() {
        return "коллекция";
    }

    @Override
    public String toLiteral() {
        throw new NotImplementedException(); //todo toLiteral
    }
}
