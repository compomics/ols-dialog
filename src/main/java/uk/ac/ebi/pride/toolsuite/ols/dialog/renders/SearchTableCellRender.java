package uk.ac.ebi.pride.toolsuite.ols.dialog.renders;

import uk.ac.ebi.pride.toolsuite.ols.dialog.util.Util;
import uk.ac.ebi.pride.utilities.ols.web.service.model.ITerm;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 06/03/2016
 */
public class SearchTableCellRender implements TableCellRenderer {

    public static final DefaultTableCellRenderer DEFAULT_RENDERER =
            new DefaultTableCellRenderer();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        JLabel label = (JLabel)(new DefaultTableCellRenderer()).getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        Color bg = label.getBackground();

        label.setBackground(new Color(bg.getRed(), bg.getGreen(), bg.getBlue()));

        if(column == 0 && value instanceof ITerm){
            label.setText(Util.getOlsAccessionLink((ITerm) value));

        }else if(column == 1 && value instanceof ITerm){
            ITerm term = (ITerm) value;
            String description = (term != null && term.getName() != null)? term.getName(): term.getGlobalId().getIdentifier();
            label.setText(description);
        }else if(value instanceof String){
            String term = (String) value;
            label.setText(term);
        }
        return label;
    }
}
