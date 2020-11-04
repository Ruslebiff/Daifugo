package client;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ButtonEditor extends DefaultCellEditor {
    protected JButton button;

    private String label;

    private boolean isPushed;
    private boolean isEnabled;

    public ButtonEditor(JButton button1, JCheckBox checkBox) {
        super(checkBox);
        button = button1;
        button.setOpaque(true);
        button.addActionListener(e -> fireEditingStopped()); // needed to be able to click button multiple times
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        if (isSelected) {
            button.setForeground(table.getSelectionForeground());
            button.setBackground(table.getSelectionBackground());
        } else {
            button.setForeground(table.getForeground());
            button.setBackground(table.getBackground());
        }
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        isPushed = true;
        return button;
    }

    public Object getCellEditorValue() {
        if (isPushed) {
            button.getAction(); // run the button's registered action, if it has one
        }
        isPushed = false;
        return label;
    }

    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing(); // Calls fireEditingStopped and returns true
    }

    protected void fireEditingStopped() { // Notifies all listeners that have registered interest for notification on this event type
        super.fireEditingStopped();
    }
}