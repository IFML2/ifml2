package ifml2.om;

import ifml2.IFMLEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class TemplateElement extends IFMLEntity {
    /*@XmlAttribute(name="position")
    public int position;*/

    @XmlAttribute(name = "parameter")
    protected String parameter;

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    @Override
    public TemplateElement clone() throws CloneNotSupportedException {
        return (TemplateElement) super.clone(); // just falt clone
    }

    public abstract String getSimpleView();
}
