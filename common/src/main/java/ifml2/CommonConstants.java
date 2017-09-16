package ifml2;

import java.text.MessageFormat;

public enum CommonConstants {
    ;

    public static final String GAMES_DIRECTORY = "\\Games";
    public static final String LIBRARIES_DIRECTORY = "\\libs";
    public static final String TESTS_DIRECTORY = "\\Tests";
    public static final String SAVES_DIRECTORY = "\\Saved Games";
    public static final String STORY_EXTENSION = ".ifml";
    public static final String STORY_FILE_FILTER_NAME = MessageFormat.format("Файл истории (*{0})", STORY_EXTENSION);
    public static final String TEST_EXTENSION = ".xml";
    public static final String TEST_FILE_FILTER_NAME = MessageFormat.format("Файл теста (*{0})", TEST_EXTENSION);
    public static final String SAVE_EXTENSION = ".ifml-save";
    public static final String SAVE_FILE_FILTER_NAME = MessageFormat.format("Файл сохранённой игры (*{0})", SAVE_EXTENSION);
    public static final String CIPHERED_STORY_EXTENSION = ".cifml";
    public static final String STORY_ALL_TYPES_FILE_FILTER_NAME = MessageFormat.format("Файл истории (*{0}, *{1})", STORY_EXTENSION, CIPHERED_STORY_EXTENSION);
    public static final String CIPHERED_STORY_FILE_FILTER_NAME = MessageFormat.format("Файл зашифрованной истории (*{0})", CIPHERED_STORY_EXTENSION);
    public static final String LIBS_FOLDER = "libs"; //todo merge with LIBRARIES_DIRECTORY constant
    public static final String LIBRARY_EXTENSION = ".xml";
    public static final String LIBRARY_FILE_FILTER_NAME = MessageFormat.format("Файл библиотеки (*{0})", LIBRARY_EXTENSION);
    public static final String PARSE_ERROR_HANDLER_PRM_PHRASE = "Фраза";
    public static final String PARSE_ERROR_HANDLER_PRM_ERROR = "Ошибка";
    public static final String RUSSIAN_PRODUCT_NAME = "ЯРИЛ 2.0";
}