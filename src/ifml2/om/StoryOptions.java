package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.vm.instructions.SetVarInstruction;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;

public class StoryOptions
{
    @XmlElement(name = "startLocationOption")
    private final StartLocationOption startLocationOption = new StartLocationOption();
    @XmlElement(name = "startProcedureOption")
    private final StartProcedureOption startProcedureOption = new StartProcedureOption();
    @XmlElement(name = "storyDescription")
    private final StoryDescription storyDescription = new StoryDescription();
    private EventList<SetVarInstruction> vars = new BasicEventList<SetVarInstruction>();

    public StartLocationOption getStartLocationOption()
    {
        return startLocationOption;
    }

    public StartProcedureOption getStartProcedureOption()
    {
        return startProcedureOption;
    }

    public StoryDescription getStoryDescription()
    {
        return storyDescription;
    }

    public EventList<SetVarInstruction> getVars()
    {
        return vars;
    }

    @XmlElementWrapper(name = "globalVars")
    @XmlElement(name = "var")
    public void setVars(EventList<SetVarInstruction> vars)
    {
        this.vars = vars;
    }

    public static class StartLocationOption
    {
        private Location location;
        private boolean showStartLocDesc;

        public Location getLocation()
        {
            return location;
        }

        @XmlAttribute(name = "location")
        @XmlIDREF
        public void setLocation(Location location)
        {
            this.location = location;
        }

        public boolean getShowStartLocDesc()
        {
            return showStartLocDesc;
        }

        @XmlAttribute(name = "showStartLocDesc")
        public void setShowStartLocDesc(boolean showStartLocDesc)
        {
            this.showStartLocDesc = showStartLocDesc;
        }
    }

    public static class StartProcedureOption
    {
        private Procedure procedure;

        public Procedure getProcedure()
        {
            return procedure;
        }

        @XmlAttribute(name = "procedure")
        @XmlIDREF
        public void setProcedure(Procedure procedure)
        {
            this.procedure = procedure;
        }
    }

    public static class StoryDescription
    {
        private String name;
        private String description;
        private String version;
        private String author;

        public String getName()
        {
            return name;
        }

        @XmlAttribute(name = "name")
        public void setName(String name)
        {
            this.name = name;
        }

        public String getDescription()
        {
            return description;
        }

        @XmlAttribute(name = "description")
        public void setDescription(String description)
        {
            this.description = description;
        }

        public String getVersion()
        {
            return version;
        }

        @XmlAttribute(name = "version")
        public void setVersion(String version)
        {
            this.version = version;
        }

        public String getAuthor()
        {
            return author;
        }

        @XmlAttribute(name = "author")
        public void setAuthor(String author)
        {
            this.author = author;
        }
    }
}
