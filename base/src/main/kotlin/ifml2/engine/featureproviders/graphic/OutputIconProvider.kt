package ifml2.engine.featureproviders.graphic

import ifml2.engine.featureproviders.PlayerFeatureProvider
import javax.swing.Icon

interface OutputIconProvider : PlayerFeatureProvider {
    fun outputIcon(icon: Icon)
}