package ifml2.editor.gui.instructions;

import ifml2.CommonUtils;
import ifml2.GUIUtils;
import ifml2.editor.IFML2EditorException;
import ifml2.vm.instructions.Instruction;
import ifml2.vm.instructions.ShowPictureInstruction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import java.awt.*;
import java.io.File;

public class ShowPictureInstrEditor extends AbstractInstrEditor {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField picturePathText;
    private JButton selectPictureButton;
    private JSpinner heightSpinner;
    private JSpinner widthSpinner;

    public ShowPictureInstrEditor(Window owner, @NotNull ShowPictureInstruction instruction) {
        super(owner);
        initializeEditor(Instruction.getTitleFor(ShowPictureInstruction.class), contentPane, buttonOK, buttonCancel);

        // listeners
        selectPictureButton.addActionListener(e -> {
            // choose story file:
            JFileChooser storyFileChooser = new JFileChooser(CommonUtils.getGamesDirectory());
            storyFileChooser.removeChoosableFileFilter(storyFileChooser.getAcceptAllFileFilter()); // remove All files filter
            storyFileChooser.setFileFilter(new FileFilter() {
                @Override
                public String getDescription() {
                    return "Файлы изображений (*.jpg, *.jpeg, *.png, *.gif)";
                }

                @Override
                public boolean accept(File file) {
                    String loweredName = file.getName().toLowerCase();
                    return file.isDirectory()
                            || loweredName.endsWith(".jpg")
                            || loweredName.endsWith(".jpeg")
                            || loweredName.endsWith(".png")
                            || loweredName.endsWith(".gif");
                }
            });

            storyFileChooser.setFileView(new FileView() {
                @Override
                public Icon getIcon(File file) {
                    return file.isDirectory() ? GUIUtils.DIRECTORY_ICON : GUIUtils.STORY_FILE_ICON; // TODO: 07.02.2016 change to image file icon
                }
            });

            if (storyFileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            String pictureFilePath = storyFileChooser.getSelectedFile().getName();
            picturePathText.setText(pictureFilePath);
        });

        // load data
        picturePathText.setText(instruction.getFilePath());
        heightSpinner.setModel(new SpinnerNumberModel(instruction.getMaxHeight(), 0, 1000, 10));
        widthSpinner.setModel(new SpinnerNumberModel(instruction.getMaxWidth(), 0, 1000, 10));
    }

    @Override
    protected Class<? extends Instruction> getInstrClass() {
        return ShowPictureInstruction.class;
    }

    @Override
    public void getInstruction(@NotNull Instruction instruction) throws IFML2EditorException {
        ShowPictureInstruction showPictureInstruction = (ShowPictureInstruction) instruction;
        showPictureInstruction.setFilePath(picturePathText.getText());
        showPictureInstruction.setMaxHeight((Integer) heightSpinner.getValue());
        showPictureInstruction.setMaxWidth((Integer) widthSpinner.getValue());
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

}
