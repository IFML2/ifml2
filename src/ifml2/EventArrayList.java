package ifml2;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Collection;

public class EventArrayList<T> extends ArrayList<T>
{
    @XmlTransient
    private ArrayList<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
    public void addChangeListener(ChangeListener listener) { changeListeners.add(listener); }
    public void removeChangeListener(ChangeListener listener) { changeListeners.remove(listener); }
    private void fireChangeEvent(ChangeEvent event)
    {
        for(ChangeListener listener : changeListeners)
        {
            listener.stateChanged(event);
        }
    }

    @Override
    public T set(int index, T element)
    {
        T t = super.set(index, element);
        fireChangeEvent(new ChangeEvent(this));
        return t;
    }

    @Override
    public boolean add(T t)
    {
        boolean res = super.add(t);
        fireChangeEvent(new ChangeEvent(this));
        return res;
    }

    @Override
    public void add(int index, T element)
    {
        super.add(index, element);
        fireChangeEvent(new ChangeEvent(this));
    }

    @Override
    public void clear()
    {
        super.clear();
        fireChangeEvent(new ChangeEvent(this));
    }

    @Override
    public T remove(int index)
    {
        T t = super.remove(index);
        fireChangeEvent(new ChangeEvent(this));
        return t;
    }

    @Override
    public boolean remove(Object o)
    {
        boolean res = super.remove(o);
        fireChangeEvent(new ChangeEvent(this));
        return res;
    }

    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        boolean res = super.addAll(c);
        fireChangeEvent(new ChangeEvent(this));
        return res;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c)
    {
        boolean res = super.addAll(index, c);
        fireChangeEvent(new ChangeEvent(this));
        return res;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex)
    {
        super.removeRange(fromIndex, toIndex);
        fireChangeEvent(new ChangeEvent(this));
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        boolean res = super.removeAll(c);
        fireChangeEvent(new ChangeEvent(this));
        return res;
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        boolean res = super.retainAll(c);
        fireChangeEvent(new ChangeEvent(this));
        return res;
    }

    public EventArrayList(int initialCapacity)
    {
        super(initialCapacity);
    }

    public EventArrayList()
    {
        super();
    }

    public EventArrayList(Collection<? extends T> c)
    {
        super(c);
    }
}
