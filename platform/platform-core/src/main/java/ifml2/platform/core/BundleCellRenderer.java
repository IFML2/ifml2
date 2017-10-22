package ifml2.platform.core;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class BundleCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                row, column);

        JLabel l = (JLabel) comp;
        BundleEntry be = (BundleEntry) value;
        switch (column) {
        case 0:
            l.setText(be.getMainInfoString());
            break;
        case 1:
            l.setText(be.getState());
            break;
        }

        return comp;
    }

}
