package ifml2.editor.gui;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class ButtonAction extends AbstractAction
{
    public ButtonAction(@NotNull JButton button)
    {
        this(button, true);
    }

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
