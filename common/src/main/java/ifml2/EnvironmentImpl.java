package ifml2;

import java.awt.Image;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Optional;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ifml2.engine.featureproviders.graphic.OutputIconProvider;
import ifml2.engine.featureproviders.text.OutputPlainTextProvider;
import ifml2.om.Story;

public class EnvironmentImpl implements Environment {

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentImpl.class);

    private static final String DEBUG_PREFIX = "    [ОТЛАДКА] ";
    private static boolean debugMode = false;

    private Story story;
    private File storyFile;

    private final Optional<OutputPlainTextProvider> textProvider;
    private final Optional<OutputIconProvider> iconProvider;

    public EnvironmentImpl(final OutputPlainTextProvider textProvider, final OutputIconProvider iconProvider) {
        this.textProvider = Optional.of(textProvider);
        this.iconProvider = Optional.of(iconProvider);
    }

    public Story getStory() {
        return story;
    }

    public void setStory(final Story story) {
        this.story = story;
    }

    @Override
    public void outText(final String text) {
        textProvider.ifPresent(textProvider -> textProvider.outputPlainText(text));
    }

    @Override
    public void outText(final String text, final Object... args) {
        outText(args.length > 0 ? MessageFormat.format(text, args) : text);
    }

    @Override
    public void outIcon(String iconFilePath, int maxHeight, int maxWidth) {
        iconProvider.ifPresent(iconProvider -> {
            Path storyFolder = Paths.get(storyFile.getAbsolutePath()).normalize().getParent();
            Path iconPath = Paths.get(iconFilePath);
            Path iconFullPath = storyFolder.resolve(iconPath);

            // load and resize icon
            ImageIcon imageIcon = new ImageIcon(iconFullPath.toAbsolutePath().toString());
            int needHeight = maxHeight > 0 ? Math.min(maxHeight, imageIcon.getIconHeight()) : imageIcon.getIconHeight();
            int needWidth = maxWidth > 0 ? Math.min(maxWidth, imageIcon.getIconWidth()) : imageIcon.getIconWidth();
            Image image = imageIcon.getImage();
            Image resizedImage = image.getScaledInstance(needWidth, needHeight, java.awt.Image.SCALE_SMOOTH);
            Icon icon = new ImageIcon(resizedImage);

            iconProvider.outputIcon(icon);
        });
    }

    @Override
    public boolean isDebug() {
        return debugMode;
    }

    @Override
    public void debugOn() {
        this.debugMode = true;
    }

    @Override
    public void debugOff() {
        this.debugMode = false;
    }

    @Override
    public void debugToggle() {
        debugMode = !debugMode;
    }

    @Override
    public void debug(final String text) {
        if (debugMode) {
            outText(DEBUG_PREFIX + text);
        }
    }

    @Override
    public void debug(final String text, final Object... args) {
        if (debugMode) {
            outText(DEBUG_PREFIX + text, args);
        }
    }

}
