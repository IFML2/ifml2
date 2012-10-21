package ifml2;

public class CommonUtils
{
    private static String getCurrentDirectory()
    {
        return System.getProperty("user.dir");
    }

    public static String getSamplesDirectory()
    {
        return getCurrentDirectory() + CommonConstants.SAMPLES_DIRECTORY;
    }

    public static String getLibrariesDirectory()
    {
        return getCurrentDirectory() + CommonConstants.LIBRARIES_DIRECTORY;
    }

    public static String uppercaseFirstLetter(String s)
    {
        if(s == null || "".equals(s))
        {
            return "";
        }
        else
        {
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
    }

    public static String getTestsDirectory()
    {
        return getCurrentDirectory() + CommonConstants.TESTS_DIRECTORY;
    }
}