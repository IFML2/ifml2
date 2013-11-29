package ifml2;

import java.text.MessageFormat;

public interface CommonConstants
{
    String GAMES_DIRECTORY = "\\Games";
    String LIBRARIES_DIRECTORY = "\\libs";
    String TESTS_DIRECTORY = "\\Tests";
    String SAVES_DIRECTORY = "\\Saved Games";
    String STORY_EXTENSION = ".ifml";
    String TEST_EXTENSION = ".xml";
    String SAVE_EXTENSION = ".ifml-save";
    String STORY_FILE_FILTER_NAME = "Файл истории (*" + STORY_EXTENSION + ")";
    String TEST_FILE_FILTER_NAME = "Файл теста (*" + TEST_EXTENSION + ")";
    String SAVE_FILE_FILTER_NAME = "Файл сохранённой игры (*" + SAVE_EXTENSION + ")";
    @SuppressWarnings("SpellCheckingInspection")
    String CIPHERED_STORY_EXTENSION = ".cifml";
    String CIPHERED_STORY_FILE_FILTER_NAME = MessageFormat.format("Файл зашифрованной истории (*{0})", CIPHERED_STORY_EXTENSION);
    //String IFML_EXTENSION = ".ifml";
    String STORY_ALL_TYPES_FILE_FILTER_NAME = MessageFormat
            .format("Файлы истории (*{0},*{1})", STORY_EXTENSION, CIPHERED_STORY_EXTENSION);
}