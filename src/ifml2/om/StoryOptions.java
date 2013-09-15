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
    @XmlElementWrapper(name = "globalVars")
    @XmlElement(name = "var")
    private final EventList<SetVarInstruction> vars = new BasicEventList<SetVarInstruction>();

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
        @XmlAttribute(name = "name")
        public String name;
        @XmlAttribute(name = "description")
        public String description;
        @XmlAttribute(name = "version")
        public String version;
        @XmlAttribute(name = "author")
        public String author;

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public String getVersion()
        {
            return version;
        }

        public String getAuthor()
        {
            return author;
        }
    }
}
