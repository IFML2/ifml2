package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.IFMLEntity;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@XmlAccessorType(XmlAccessType.NONE)
public class Action extends IFMLEntity {
    @XmlElement(name = "procedureCall")
    public ProcedureCall procedureCall = new ProcedureCall();
    @XmlElementWrapper(name = "templates")
    @XmlElement(name = "template")
    private EventList<Template> templates = new BasicEventList<>();
    @XmlElementWrapper(name = "restrictions")
    @XmlElement(name = "restriction")
    private EventList<Restriction> restrictions = new BasicEventList<>();
    @XmlAttribute(name = "name")
    @XmlID
    private String name;
    @XmlAttribute(name = "description")
    private String description;

    public EventList<Template> getTemplates() {
        return templates;
    }

    public ProcedureCall getProcedureCall() {
        return procedureCall;
    }

    public EventList<Restriction> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(EventList<Restriction> restrictions) {
        this.restrictions = restrictions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public Object[] retrieveAllObjectParameters() {
        ArrayList<Object> parameters = new ArrayList<>();

        final List<String> allParams = templates.parallelStream()
                .flatMap(t -> t.getElements().stream())
                .filter(te -> te instanceof ObjectTemplateElement && te.HasParameter())
                .map(TemplateElement::getParameter)
                .collect(Collectors.toList());
        parameters.addAll(allParams);

        return parameters.toArray();
    }

    @Override
    public Action clone() throws CloneNotSupportedException {
        Action clone = (Action) super.clone(); // flat clone

        // deep clone
        clone.procedureCall = procedureCall.clone();
        clone.restrictions = deepCloneEventList(restrictions, Restriction.class);
        clone.templates = deepCloneEventList(templates, Template.class);

        return clone;
    }

    public void copyTo(@NotNull Action action) throws CloneNotSupportedException {
        action.name = name;
        action.description = description;
        action.procedureCall = procedureCall.clone();
        action.restrictions = deepCloneEventList(restrictions, Restriction.class);
        action.templates = deepCloneEventList(templates, Template.class);
    }

    public Procedure getProcedure() {
        return procedureCall.getProcedure();
    }

    public void setProcedure(Procedure procedure) {
        procedureCall.setProcedure(procedure);
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class ProcedureCall implements Cloneable {
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
        protected ProcedureCall clone() throws CloneNotSupportedException {
            return (ProcedureCall) super.clone(); // just flat clone, cause procedure is reference
        }

        @Override
        public String toString() {
            return procedure != null ? procedure.toString() : "";
        }
    }
}
