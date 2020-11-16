package client;

import java.awt.Color;
import java.awt.Component;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class TableRowColorRenderer extends DefaultTableCellRenderer {

    public static final DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
    ) {
        Component c = DEFAULT_RENDERER.getTableCellRendererComponent(
                table,
                value,
                isSelected,
                hasFocus,
                row,
                column
        );

        // Alternate colors per row
        if (row % 2 == 0) {
            c.setBackground(Color.lightGray);
        } else {
            c.setBackground(Color.white);
        }

        // Center text in columns
        if (column == 3 || column == 4) {          // These will be centered
            DEFAULT_RENDERER.setHorizontalAlignment(JLabel.CENTER);
        } else {
            DEFAULT_RENDERER.setHorizontalAlignment(JLabel.LEFT);
        }




        return c;
    }

}