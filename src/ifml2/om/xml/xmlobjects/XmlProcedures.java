package ifml2.om.xml.xmlobjects;

import ifml2.om.Procedure;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="procedures")
public class XmlProcedures
{
	@XmlElement(name="procedure")
	public List<Procedure> procedures = new ArrayList<Procedure>();

    @Override
    public String toString()
    {
        return String.format("{procedures = %s}", procedures.toString());
    }
}
