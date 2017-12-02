package ifml2.om;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

@XmlAccessorType(XmlAccessType.NONE)
public class Action {
    @XmlElementWrapper(name = "templates")
    @XmlElement(name = "template")
    private final EventList<Template> templates = new BasicEventList<Template>();

    public EventList<Template> getTemplates() {
        return templates;
    }

    @XmlElement(name = "procedureCall")
    public ProcedureCall procedureCall = new ProcedureCall();

    public ProcedureCall getProcedureCall() {
        return procedureCall;
    }

    @XmlElementWrapper(name = "restrictions")
    @XmlElement(name = "restriction")
    private EventList<Restriction> restrictions = new BasicEventList<Restriction>();

    public EventList<Restriction> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(EventList<Restriction> restrictions) {
        this.restrictions = restrictions;
    }

    @XmlAttribute(name = "name")
    @XmlID
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name = "description")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name;
    }

    public Object[] getAllObjectParameters() {
        List<Object> parameters = new ArrayList<>();

        for (Template template : templates) {
            for (TemplateElement element : template.getElements()) {
                if (element instanceof ObjectTemplateElement && element.getParameter() != null) {
                    parameters.add(element.getParameter());
                }
            }
        }

        return parameters.toArray();
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class ProcedureCall {
        @XmlAttribute(name = "procedure")
        @XmlIDREF
        private Procedure procedure;

        public Procedure getProcedure() {
            return procedure;
        }

        public void setProcedure(Procedure procedure) {
            this.procedure = procedure;
        }

        @Override
        public String toString() {
            return procedure != null ? procedure.toString() : "";
        }
    }
}
