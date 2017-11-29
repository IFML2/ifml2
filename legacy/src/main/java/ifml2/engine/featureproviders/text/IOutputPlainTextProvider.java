package ifml2.engine.featureproviders.text;

import ifml2.engine.featureproviders.IPlayerFeatureProvider;

/**
 * Output plain text provider. Used by Engine for outputting plain text if a player doesn't support other text output features.
 */
public interface IOutputPlainTextProvider extends IPlayerFeatureProvider {
    /**
     * Outputs plain text.
     * @param text plain text to output.
     */
    void outputPlainText(String text);
}
