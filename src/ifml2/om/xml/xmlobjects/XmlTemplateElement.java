package ifml2.om.xml.xmlobjects;

import javax.xml.bind.annotation.XmlAttribute;

//@XmlRootElement(name="element")
class XmlTemplateElement
{
	/*
    public enum XmlTemplateTypeEnum
	{
		@XmlEnumValue(value="literal")
		LITERAL,
		@XmlEnumValue(value="object")
		OBJECT
	}
	*/

	@XmlAttribute(name="position")
	public int position;
	
	/*
    @XmlAttribute(name="type")
	public XmlTemplateTypeEnum type;

	@XmlValue
	public String value;

    @XmlElement(name="synonym")
    public List<String> synonyms;
	*/
}
