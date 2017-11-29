package ifml2.editor.gui;

import ifml2.GUIUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Helper for JButton & AbstractAction: takes button text & icon,
 * assigns it to ButtonAction and assigns it to button.
 * Can be created with provided isEnabled state.
 * Also you can override init() method and initialize state, properties,
 * add listeners for controlling action state and so on.
 */
public abstract class ButtonAction extends AbstractAction
{
    /**
     * Creates ButtonAction and assigns it to provided button. Button will be enabled.
     *
     * @param button JButton that will be assigned by action
     */
    public ButtonAction(@NotNull JButton button)
    {
        this(button, true);
    }

    /**
     * Creates ButtonAction and assigns it to provided button. Button enable depends on param button.
     *
     * @param button    JButton that will be assigned by action
     * @param isEnabled Button enable
     */
    public ButtonAction(@NotNull JButton button, boolean isEnabled)
    {
        super(button.getText(), button.getIcon()); // create abstract action with button text and icon
        button.setAction(this); // set action to button
        setEnabled(isEnabled); // set isEnabled
        init(); // register listeners
    }

    /**
     * Creates ButtonAction and assigns it to provided button. Button enable depends on provided JList.
     *
     * @param button JButton that will be assigned by action
     * @param list   JList which selection controls action enabled property
     */
    public ButtonAction(@NotNull JButton button, @NotNull JList list)
    {
        super(button.getText(), button.getIcon()); // create abstract action with button text and icon
        button.setAction(this); // set action to button
        GUIUtils.makeActionDependentFromJList(this, list); // make action dependent from JList selection
    }

    /**
     * Initialize action. You can register isEnabled listener. Overwrite it for registering listeners for executing at creation.
     */
    public void init()
    {
        // optional
    }
}
