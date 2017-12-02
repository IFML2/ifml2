package ifml2.engine.featureproviders.graphic;

import javax.swing.Icon;

import ifml2.engine.featureproviders.IPlayerFeatureProvider;

/**
 * Output Icon provider.
 */
public interface IOutputIconProvider extends IPlayerFeatureProvider {
    /**
     * Outputs Icon.
     * 
     * @param icon
     *            icon to output.
     */
    void outputIcon(Icon icon);
}
