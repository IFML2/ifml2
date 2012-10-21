package ifml2.om.xml.xmladapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProcedureCallParametersAdapter extends XmlAdapter<String, List<Integer>>
{
	@Override
	public String marshal(List<Integer> v) throws Exception
	{
        Iterator<Integer> iterator = v.iterator();
        String paramsList = null;
        while(iterator.hasNext())
        {
            Integer param = iterator.next();
            paramsList += param.toString();
            if(iterator.hasNext())
            {
                paramsList += ",";
            }
        }
        return paramsList;
	}

	@Override
	public List<Integer> unmarshal(String v) throws Exception
	{
		List<Integer> parameters = new ArrayList<Integer>();
		for(String param : v.split("\\s*,\\s*"))
		{
			parameters.add(Integer.parseInt(param));
		}
		return parameters;
	}
}
