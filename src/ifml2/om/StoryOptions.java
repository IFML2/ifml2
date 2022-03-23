package ifml2.om;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ifml2.vm.instructions.SetVarInstruction;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

import static javax.xml.bind.annotation.XmlAccessType.NONE;

@XmlAccessorType(NONE)
public class StoryOptions
{
    @XmlElement(name = "startLocationOption")
    private final StartLocationOption startLocationOption = new StartLocationOption();

    public StartLocationOption getStartLocationOption()
    {
        return startLocationOption;
    }

    @XmlElement(name = "startProcedureOption")
    private final StartProcedureOption startProcedureOption = new StartProcedureOption();

    public StartProcedureOption getStartProcedureOption()
    {
        return startProcedureOption;
    }

    @XmlElement(name = "storyDescription")
    private final StoryDescription storyDescription = new StoryDescription();

    public StoryDescription getStoryDescription()
    {
        return storyDescription;
    }

    @XmlElementWrapper(name = "globalVars")
    @XmlElement(name = "var")
    private EventList<SetVarInstruction> vars = new BasicEventList<SetVarInstruction>();

    public EventList<SetVarInstruction> getVars()
    {
        return vars;
    }

    public void setVars(EventList<SetVarInstruction> vars)
    {
        this.vars = vars;
    }

    @XmlElement(name = "systemCommandsDisableOption")
    private final SystemCommandsDisableOption systemCommandsDisableOption = new SystemCommandsDisableOption();

    public SystemCommandsDisableOption getSystemCommandsDisableOption()
    {
        return systemCommandsDisableOption;
    }

    @XmlElementWrapper(name = "musicList")
    @XmlElement(name = "music")
    private ArrayList<Music> musicList = new ArrayList<>();

    public ArrayList<Music> getMusicList() {
        return musicList;
    }

    public void setMusicList(ArrayList<Music> musicList) { this.musicList = musicList; }

    @XmlAccessorType(NONE)
    public static class StartLocationOption
    {
        @XmlIDREF
        @XmlAttribute(name = "location")
        private Location location;

        public Location getLocation()
        {
            return location;
        }

        public void setLocation(Location location)
        {
            this.location = location;
        }

        @XmlAttribute(name = "showStartLocDesc")
        private boolean showStartLocDesc;

        public boolean getShowStartLocDesc()
        {
            return showStartLocDesc;
        }

        public void setShowStartLocDesc(boolean showStartLocDesc)
        {
            this.showStartLocDesc = showStartLocDesc;
        }
    }

    @XmlAccessorType(NONE)
    public static class StartProcedureOption
    {
        @XmlAttribute(name = "procedure")
        @XmlIDREF
        private Procedure procedure;

        public Procedure getProcedure()
        {
            return procedure;
        }

        public void setProcedure(Procedure procedure)
        {
            this.procedure = procedure;
        }
    }

    @XmlAccessorType(NONE)
    public static class StoryDescription
    {
        @XmlAttribute(name = "name")
        private String name;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        @XmlAttribute(name = "description")
        private String description;

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        @XmlAttribute(name = "version")
        private String version;

        public String getVersion()
        {
            return version;
        }

        public void setVersion(String version)
        {
            this.version = version;
        }

        @XmlAttribute(name = "author")
        private String author;

        public String getAuthor()
        {
            return author;
        }

        public void setAuthor(String author)
        {
            this.author = author;
        }
    }

    @XmlAccessorType(NONE)
    public static class SystemCommandsDisableOption
    {
        @XmlAttribute(name = "DisableHelp")
        private boolean disableHelp = false;

        public boolean isDisableHelp()
        {
            return disableHelp;
        }

        public void setDisableHelp(boolean disableHelp)
        {
            this.disableHelp = disableHelp;
        }

        @XmlAttribute(name = "DisableDebug")
        private boolean disableDebug = false;

        public boolean isDisableDebug()
        {
            return disableDebug;
        }

        public void setDisableDebug(boolean disableDebug)
        {
            this.disableDebug = disableDebug;
        }
    }

    @XmlAccessorType(NONE)
    private static class Music {
        @XmlAttribute(name = "name")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @XmlAttribute(name = "fileName")
        private String fileName;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
    }
}
