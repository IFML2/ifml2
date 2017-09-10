package ifml2;

import java.text.MessageFormat;

public interface CommonConstants {
    String GAMES_DIRECTORY = "\\Games";
    String LIBRARIES_DIRECTORY = "\\libs";
    String TESTS_DIRECTORY = "\\Tests";
    String SAVES_DIRECTORY = "\\Saved Games";
    String STORY_EXTENSION = ".ifml";
    String STORY_FILE_FILTER_NAME = MessageFormat.format("Файл истории (*{0})", STORY_EXTENSION);
    String TEST_EXTENSION = ".xml";
    String TEST_FILE_FILTER_NAME = MessageFormat.format("Файл теста (*{0})", TEST_EXTENSION);
    String SAVE_EXTENSION = ".ifml-save";
    String SAVE_FILE_FILTER_NAME = MessageFormat.format("Файл сохранённой игры (*{0})", SAVE_EXTENSION);
    String CIPHERED_STORY_EXTENSION = ".cifml";
    String STORY_ALL_TYPES_FILE_FILTER_NAME = MessageFormat.format("Файл истории (*{0}, *{1})", STORY_EXTENSION, CIPHERED_STORY_EXTENSION);
    String CIPHERED_STORY_FILE_FILTER_NAME = MessageFormat.format("Файл зашифрованной истории (*{0})", CIPHERED_STORY_EXTENSION);
    String LIBS_FOLDER = "libs"; //todo merge with LIBRARIES_DIRECTORY constant
    String LIBRARY_EXTENSION = ".xml";
    String LIBRARY_FILE_FILTER_NAME = MessageFormat.format("Файл библиотеки (*{0})", LIBRARY_EXTENSION);
    String PARSE_ERROR_HANDLER_PRM_PHRASE = "Фраза";
    String PARSE_ERROR_HANDLER_PRM_ERROR = "Ошибка";
    String RUSSIAN_PRODUCT_NAME = "ЯРИЛ 2.0";
}