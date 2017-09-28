package ifml2.engine.featureproviders.graphic;

import ifml2.engine.featureproviders.PlayerFeatureProvider;

import javax.swing.*;

/**
 * Output Icon provider.
 */
public interface OutputIconProvider extends PlayerFeatureProvider {
    /**
     * Outputs Icon.
     *
     * @param icon icon to output.
     */
    void outputIcon(Icon icon);
}
