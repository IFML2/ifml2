package ifml2.editor.gui.editors;

import ifml2.GUIUtils;
import ifml2.IFML2Exception;
import ifml2.editor.DataNotValidException;
import ifml2.editor.IFML2EditorException;
import ifml2.editor.gui.AbstractEditor;
import ifml2.editor.gui.ButtonAction;
import ifml2.editor.gui.forms.ListEditForm;
import ifml2.om.Hook;
import ifml2.om.Location;
import ifml2.om.Story;
import ifml2.om.Word;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static ifml2.om.Location.ExitDirection;
import static ifml2.om.Location.ExitDirection.*;

public class LocationEditor extends AbstractEditor<Location> {
    private static final String LOCATION_EDITOR_TITLE = "Локация";
    private final Location locationClone;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField locationIDText;
    private JTextField locationNameText;
    private JTextArea descriptionText;
    private JComboBox<Location> northCombo;
    private JComboBox<Location> eastCombo;
    private JComboBox<Location> southCombo;
    private JComboBox<Location> westCombo;
    private JComboBox<Location> upCombo;
    private JComboBox<Location> downCombo;
    private JComboBox<Location> northEastCombo;
    private JComboBox<Location> southEastCombo;
    private JComboBox<Location> southWestCombo;
    private JComboBox<Location> northWestCombo;
    private ListEditForm<Hook> hooksListEditForm;
    private JButton enableIdButton;
    private Map<Location.ExitDirection, JComboBox<Location>> exitCombosMap = new HashMap<Location.ExitDirection, JComboBox<Location>>() {
        {
            put(NORTH, northCombo);
            put(NORTH_EAST, northEastCombo);
            put(EAST, eastCombo);
            put(SOUTH_EAST, southEastCombo);
            put(SOUTH, southCombo);
            put(SOUTH_WEST, southWestCombo);
            put(WEST, westCombo);
            put(NORTH_WEST, northWestCombo);
            put(UP, upCombo);
            put(DOWN, downCombo);
        }
    };
    private boolean toGenerateId = false;
    private Story.DataHelper storyDataHelper;
    private Location originalLocation;

    public LocationEditor(Window owner, Location location, final Story.DataHelper storyDataHelper) throws IFML2EditorException {
        super(owner);
        this.originalLocation = location;
        this.storyDataHelper = storyDataHelper;
        initializeEditor(LOCATION_EDITOR_TITLE, contentPane, buttonOK, buttonCancel);

        // clone
        try {
            locationClone = location.clone();
        } catch (CloneNotSupportedException e) {
            throw new IFML2EditorException("Ошибка при клонировании локации: {0}", e.getMessage());
        }

        // bind
        bindData();

        locationNameText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateId();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateId();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                //do nothing
            }
        });

        enableIdButton.setAction(new ButtonAction(enableIdButton) {
            @Override
            public void actionPerformed(ActionEvent e) {
                locationIDText.setEditable(true); // enable edit
                toGenerateId = false;
                setEnabled(false); // disable button
                locationIDText.requestFocusInWindow();
            }
        });

        for (ExitDirection exitDirection : ExitDirection.values()) {
            JComboBox<Location> comboBox = exitCombosMap.get(exitDirection);
            comboBox.setModel(new GUIUtils.EventComboBoxModelWithNullElement<>(storyDataHelper.getLocations(),
                    locationClone.getExit(exitDirection)));
        }

        locationNameText.setText(locationClone.getName());
        locationIDText.setText(locationClone.getId());
        descriptionText.setText(locationClone.getDescription());

        String id = locationClone.getId();
        toGenerateId = id == null || "".equals(id);

        locationNameText.requestFocusInWindow();
    }

    private void bindData() {
        hooksListEditForm.bindData(locationClone.getHooks());
    }

    private boolean editHook(Hook hook) throws IFML2EditorException {
        HookEditor hookEditor = new HookEditor(LocationEditor.this, hook, false, storyDataHelper);
        if (hookEditor.showDialog()) {
            hookEditor.updateData(hook);
            return true;
        }
        return false;
    }

    private void updateId() {
        if (toGenerateId) {
            locationIDText.setText(storyDataHelper.generateIdByName(locationNameText.getText(), Location.class));
        }
    }

    @Override
    protected void validateData() throws DataNotValidException {
        // check name
        if (locationNameText.getText().trim().length() == 0) {
            throw new DataNotValidException("У локации должно быть задано имя.", locationNameText);
        }

        // check id
        String id = locationIDText.getText().trim();
        if (id.length() == 0) {
            throw new DataNotValidException("У локации должен быть задан идентификатор.", locationIDText);
        }

        Object object = storyDataHelper.findObjectById(id);
        if (object != null && !object.equals(originalLocation)) {
            String className = null;
            try {
                className = storyDataHelper.getObjectClassName(object);
            } catch (IFML2Exception e) {
                GUIUtils.showErrorMessage(this, e);
            }
            throw new DataNotValidException(
                    "У локации должен быть уникальный идентификатор - не пересекаться с другими локациями, предметами и словарём.\n" +
                            MessageFormat.format("Найден другой объект с тем же идентификатором: \"{0}\" типа \"{1}\".", object, className),
                    locationIDText);
        }
    }

    @Override
    public void updateData(@NotNull Location location) throws IFML2EditorException {
        locationClone.setName(locationNameText.getText().trim());
        locationClone.setId(locationIDText.getText().trim());
        locationClone.setDescription(descriptionText.getText());
        for (ExitDirection exitDirection : ExitDirection.values()) {
            JComboBox<Location> combo = exitCombosMap.get(exitDirection);
            locationClone.setExit(exitDirection, (Location) combo.getSelectedItem());
        }
        try {
            locationClone.copyTo(location);
        } catch (CloneNotSupportedException e) {
            throw new IFML2EditorException("Ошибка при копировании локации: {0}", e.toString());
        }
    }

    private void createUIComponents() {
        hooksListEditForm = new ListEditForm<Hook>(LocationEditor.this, "перехват", "перехвата", Word.Gender.MASCULINE) {
            @Override
            protected Hook createElement() throws Exception {
                Hook hook = new Hook();
                if (editHook(hook)) {
                    return hook;
                }
                return null;
            }

            @Override
            protected boolean editElement(Hook selectedElement, Consumer<Hook> elementUpdater) throws Exception {
                return editHook(selectedElement);
            }
        };
    }
}