package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.vm.instructions.SetVarInstruction;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;

import static javax.xml.bind.annotation.XmlAccessType.NONE;

@XmlAccessorType(NONE)
public class StoryOptions {
    @XmlElement(name = "startLocationOption")
    private final StartLocationOption startLocationOption = new StartLocationOption();
    @XmlElement(name = "startProcedureOption")
    private final StartProcedureOption startProcedureOption = new StartProcedureOption();
    @XmlElement(name = "storyDescription")
    private final StoryDescription storyDescription = new StoryDescription();
    @XmlElement(name = "systemCommandsDisableOption")
    private final SystemCommandsDisableOption systemCommandsDisableOption = new SystemCommandsDisableOption();
    @XmlElementWrapper(name = "globalVars")
    @XmlElement(name = "var")
    private EventList<SetVarInstruction> vars = new BasicEventList<SetVarInstruction>();

    public StartLocationOption getStartLocationOption() {
        return startLocationOption;
    }

    public StartProcedureOption getStartProcedureOption() {
        return startProcedureOption;
    }

    public StoryDescription getStoryDescription() {
        return storyDescription;
    }

    public EventList<SetVarInstruction> getVars() {
        return vars;
    }

    public void setVars(EventList<SetVarInstruction> vars) {
        this.vars = vars;
    }

    public SystemCommandsDisableOption getSystemCommandsDisableOption() {
        return systemCommandsDisableOption;
    }

    @XmlAccessorType(NONE)
    public static class StartLocationOption {
        @XmlIDREF
        @XmlAttribute(name = "location")
        private Location location;
        @XmlAttribute(name = "showStartLocDesc")
        private boolean showStartLocDesc;

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public boolean getShowStartLocDesc() {
            return showStartLocDesc;
        }

        public void setShowStartLocDesc(boolean showStartLocDesc) {
            this.showStartLocDesc = showStartLocDesc;
        }
    }

    @XmlAccessorType(NONE)
    public static class StartProcedureOption {
        @XmlAttribute(name = "procedure")
        @XmlIDREF
        private Procedure procedure;

        public Procedure getProcedure() {
            return procedure;
        }

        public void setProcedure(Procedure procedure) {
            this.procedure = procedure;
        }
    }

    @XmlAccessorType(NONE)
    public static class StoryDescription {
        @XmlAttribute(name = "name")
        private String name;
        @XmlAttribute(name = "description")
        private String description;
        @XmlAttribute(name = "version")
        private String version;
        @XmlAttribute(name = "author")
        private String author;

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

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }
    }

    @XmlAccessorType(NONE)
    public static class SystemCommandsDisableOption {
        @XmlAttribute(name = "DisableHelp")
        private boolean disableHelp = false;
        @XmlAttribute(name = "DisableDebug")
        private boolean disableDebug = false;

        public boolean isDisableHelp() {
            return disableHelp;
        }

        public void setDisableHelp(boolean disableHelp) {
            this.disableHelp = disableHelp;
        }

        public boolean isDisableDebug() {
            return disableDebug;
        }

        public void setDisableDebug(boolean disableDebug) {
            this.disableDebug = disableDebug;
        }
    }
}
