package ifml2.editor.gui;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Helper for JButton & AbstractAction: takes button text & icon,
 * assigns it to ButtonAction and assigns it to button.
 * Can be created with provided isEnabled state.
 * Also you can override registerListeners() method and add listeners for
 * controlling action state.
 */
public abstract class ButtonAction extends AbstractAction
{
    /**
     * Creates ButtonAction and assigns it to provided button. Button will be enabled.
     * @param button JButton that will be assigned by action
     */
    public ButtonAction(@NotNull JButton button)
    {
        this(button, true);
    }

    /**
     * Creates ButtonAction and assigns it to provided button. Button enable depends on param button.
     * @param button JButton that will be assigned by action
     * @param isEnabled Button enable
     */
    public ButtonAction(@NotNull JButton button, boolean isEnabled)
    {
        super(button.getText(), button.getIcon()); // create abstract action with button text and icon
        button.setAction(this); // set action to button
        setEnabled(isEnabled); // set isEnabled
        registerListeners(); // register listeners
    }

    /**
     * Register isEnabled listener. Overwrite it for registering listeners for executing at creation.
     */
    public void registerListeners()
    {
        // optional
    }
}
