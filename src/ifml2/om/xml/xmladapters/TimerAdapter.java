package ifml2.om.xml.xmladapters;


import ifml2.IFML2Exception;
import ifml2.om.ActionsTimer;
import ifml2.om.GameTimer;
import ifml2.om.RealTimeTimer;
import ifml2.om.xml.xmlobjects.XmlTimer;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class TimerAdapter extends XmlAdapter<XmlTimer, GameTimer>
{
    @Override
    public GameTimer unmarshal(XmlTimer xmlTimer) throws Exception
    {
        switch (xmlTimer.type)
        {
            case REAL_TIME:
                return new RealTimeTimer(xmlTimer.name, xmlTimer.time);
            case ACTIONS:
                return new ActionsTimer(xmlTimer.name, xmlTimer.quantity);
            default:
                throw new IFML2Exception("Неизвестный тип таймера - \"{0}\".", xmlTimer.type);
        }
    }

    @Override
    public XmlTimer marshal(GameTimer gameTimer) throws Exception
    {
        if (gameTimer instanceof RealTimeTimer)
        {
            RealTimeTimer timer = (RealTimeTimer) gameTimer;
            return new XmlTimer(timer.getName(), timer.getTime());
        }
        else if (gameTimer instanceof ActionsTimer)
        {
            ActionsTimer timer = (ActionsTimer) gameTimer;
            return new XmlTimer(timer.getName(), timer.getQuantity());
        }
        else
        {
            throw new Exception("Неизвестный класс таймера " + gameTimer.getClass());
        }
    }
}
