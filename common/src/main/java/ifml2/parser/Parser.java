package ifml2.parser;

import ifml2.IFML2Exception;
import ifml2.engine.EngineImpl;
import ifml2.om.Story;

public interface Parser {

    ParseResult parse(String phrase, Story.DataHelper storyDataHelper, EngineImpl.DataHelper engineDataHelper) throws IFML2Exception;

}
