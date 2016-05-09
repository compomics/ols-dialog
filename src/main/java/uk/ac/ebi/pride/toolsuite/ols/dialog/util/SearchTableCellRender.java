package uk.ac.ebi.pride.toolsuite.ols.dialog.util;

import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

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
        if(column == 0){
            label.setText(Util.getOlsAccessionLink((Term) value));
        }else if(column == 1){
            Term term = (Term) value;
            String description = (term != null && term.getLabel() != null)? term.getLabel(): term.getGlobalId().getIdentifier();
            label.setText(description);
        }
        return label;
    }

//    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//
//        String link = (String)value;
//        if(link != null) {
//            if(isSelected) {
//                link = link.replace(this.notSelectedRowFontColor, this.selectedRowFontColor);
//            } else {
//                link = link.replace(this.selectedRowFontColor, this.notSelectedRowFontColor);
//            }
//
//            label.setText(link);
//        }
//
//        return label;
//    }
}