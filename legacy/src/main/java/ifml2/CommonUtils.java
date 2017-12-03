package ifml2;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class CommonUtils {
    private static String getCurrentDirectory() {
        return System.getProperty("user.dir");
    }

    public static String getGamesDirectory() {
        return getCurrentDirectory() + CommonConstants.GAMES_DIRECTORY;
    }

    public static String getLibrariesDirectory() {
        return getCurrentDirectory() + CommonConstants.LIBRARIES_DIRECTORY;
    }

    public static String uppercaseFirstLetter(String s) {
        return s == null || s.isEmpty() ? "" : s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String getTestsDirectory() {
        return getCurrentDirectory() + CommonConstants.TESTS_DIRECTORY;
    }

    public static String getSavesDirectory() {
        return getCurrentDirectory() + CommonConstants.SAVES_DIRECTORY;
    }

    public static Cipher createCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
        return Cipher.getInstance("DES/ECB/PKCS5Padding");
    }
}