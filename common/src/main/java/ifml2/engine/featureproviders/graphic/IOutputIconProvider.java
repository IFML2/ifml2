package ifml2.engine.featureproviders.graphic;

import ifml2.engine.featureproviders.IPlayerFeatureProvider;

import javax.swing.*;

/**
 * Output Icon provider.
 */
public interface IOutputIconProvider extends IPlayerFeatureProvider {
    /**
     * Outputs Icon.
     *
     * @param icon icon to output.
     */
    void outputIcon(Icon icon);
}
