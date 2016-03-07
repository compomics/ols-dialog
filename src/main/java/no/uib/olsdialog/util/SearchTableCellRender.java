package no.uib.olsdialog.util;

import uk.ac.pride.ols.web.service.model.Term;

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
        Component component;
        if(column == 0){
            component = new JLabel(Util.getOlsAccessionLink((Term) value));
        }else if(column == 1){
            Term term = (Term) value;
            String description = (term != null && term.getLabel() != null)? term.getLabel(): term.getGlobalId().getIdentifier();
            component = new JLabel(description);
        } else{
            component =
                    DEFAULT_RENDERER.getTableCellRendererComponent(table, value,
                            isSelected, hasFocus, row, column);
        }
        return component;
    }
}
