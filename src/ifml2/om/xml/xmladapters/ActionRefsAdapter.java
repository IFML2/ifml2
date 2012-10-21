package ifml2.om.xml.xmladapters;

import ifml2.om.Action;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ActionRefsAdapter extends XmlAdapter<String, Action>
{
    @Override
    public Action unmarshal(String v) throws Exception {
        // pre load just reference names ... to post load by OMManager later

        Action action = new Action();
        action.setName(v);

        return action;
    }

    @Override
    public String marshal(Action v) throws Exception {
        // marshal only names as references

        return v.getName();
    }
}
