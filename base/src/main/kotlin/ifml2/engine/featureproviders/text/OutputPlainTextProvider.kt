package ifml2.engine.featureproviders.text

import ifml2.engine.featureproviders.PlayerFeatureProvider

interface OutputPlainTextProvider : PlayerFeatureProvider {
    fun outputPlainText(text: String)
}