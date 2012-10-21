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
    public final StartLocationOption startLocationOption = new StartLocationOption();

    public StartLocationOption getStartLocationOption()
    {
        return startLocationOption;
    }

    @XmlElement(name = "startProcedureOption")
    public final StartProcedureOption startProcedureOption = new StartProcedureOption();

    public StartProcedureOption getStartProcedureOption()
    {
        return startProcedureOption;
    }

    @XmlElement(name = "storyDescription")
    public final StoryDescription storyDescription = new StoryDescription();

    public StoryDescription getStoryDescription()
    {
        return storyDescription;
    }

    @XmlElementWrapper(name = "globalVars")
    @XmlElement(name = "var")
    public final EventList<SetVarInstruction> vars = new BasicEventList<SetVarInstruction>();

    public EventList<SetVarInstruction> getVars()
    {
        return vars;
    }

    public static class StartLocationOption
    {
        @XmlAttribute(name = "location")
        @XmlIDREF
        public Location location;

        public Location getLocation()
        {
            return location;
        }

        @XmlAttribute(name = "showStartLocDesc")
        public boolean showStartLocDesc;

        public boolean getShowStartLocDesc()
        {
            return showStartLocDesc;
        }
    }

    public static class StartProcedureOption
    {
        @XmlAttribute(name = "procedure")
        @XmlIDREF
        public Procedure procedure;

        public Procedure getProcedure()
        {
            return procedure;
        }
    }

    public static class StoryDescription
    {
        @XmlAttribute(name = "name")
        public String name;

        public String getName()
        {
            return name;
        }

        @XmlAttribute(name = "description")
        public String description;

        public String getDescription()
        {
            return description;
        }

        @XmlAttribute(name = "version")
        public String version;

        public String getVersion()
        {
            return version;
        }

        @XmlAttribute(name = "author")
        public String author;

        public String getAuthor()
        {
            return author;
        }
    }
}
