package ifml2.feature

import javafx.scene.image.Image
import javax.swing.Icon

interface OutputImageProvider : PlayerFeatureProvider {
    fun outputImage(image: Icon) // TODO: Need to be change to javafx.scene.image.Image
}