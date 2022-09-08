package ifml2;

import ifml2.engine.EngineVersion;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class CommonUtils
{
    private static String getCurrentDirectory()
    {
        return System.getProperty("user.dir");
    }

    public static String getGamesDirectory()
    {
        return getCurrentDirectory() + CommonConstants.GAMES_DIRECTORY;
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

    public static String getSavesDirectory()
    {
        return getCurrentDirectory() + CommonConstants.SAVES_DIRECTORY;
    }

    public static Cipher createCipher() throws NoSuchAlgorithmException, NoSuchPaddingException
    {
        return Cipher.getInstance("DES/ECB/PKCS5Padding");
    }

    /*public static String getExtendedStackTrace(Throwable e)
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        StringBuilder stringBuilder = new StringBuilder(stringWriter.toString());
        Throwable cause = e;
        while(cause.getCause() != null && cause != cause.getCause())
        {
            cause = cause.getCause();
            stringBuilder.append("\n[Extended] Caused by:\n");
            cause.printStackTrace(printWriter);
            stringBuilder.append(stringWriter.toString());
        }
        return stringBuilder.toString();
    }*/

    public static Date getBuildDate(){
        try {
            String jarPath = CommonUtils.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
            File jarFile = new File(jarPath);
            return new Date(jarFile.lastModified());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static String getVersion(){
        String versionStr = EngineVersion.VERSION;
        if (EngineVersion.IS_DEVELOPER_VERSION){
            versionStr += String.format("[📅%1$td/%1$tb/%1$ty]", getBuildDate());
        }
        return versionStr;
    }
}