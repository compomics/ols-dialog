package uk.ac.ebi.pride.toolsuite.ols.dialog.util;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * A combo box renderer that allows tooltip for each element in the combo box
 * list.
 *
 * @author Harald Barsnes
 */
public class MyComboBoxRenderer extends BasicComboBoxRenderer {

    /**
     * The list of tooltips.
     */
    private Vector tooltips;
    /**
     * The horizontal alignment type.
     */
    private int align;

    /**
     * Creates a new instance of the MyComboBoxRenderer.
     *
     * @param tooltips vector containing the tooltips
     * @param align the horizontal alignment of the text
     */
    public MyComboBoxRenderer(Vector tooltips, int align) {
        this.tooltips = tooltips;
        this.align = align;
    }

    /**
     * Set the tooltips.
     *
     * @param tooltips vector containg the tooltips
     */
    public void setToolTips(Vector tooltips) {
        this.tooltips = tooltips;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {

        // DefaultListCellRenderer uses a JLabel as the rendering component:
        JLabel lbl = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);

        if (isSelected) {

            lbl.setBackground(list.getSelectionBackground());
            lbl.setForeground(list.getSelectionForeground());

            if (tooltips != null) {
                if (-1 < index && index < tooltips.size()) {

                    if (tooltips.get(index) != null) {

                        String toolTip = (String) tooltips.get(index);
                        StringTokenizer tok = new StringTokenizer(toolTip);
                        StringBuilder temp = new StringBuilder();StringBuilder temp2 = new StringBuilder();

                        while (tok.hasMoreTokens()) {
                            temp.append(tok.nextToken()).append(" ");

                            if (temp.length() > 40) {
                                temp2.append(temp).append("<br>");
                                temp = new StringBuilder();
                            }
                        }

                        if (temp.length() > 0) {
                            temp2.append(temp);
                        }

                        list.setToolTipText("<html>" + temp2 + "</html>");
                    } else {
                        list.setToolTipText(null);
                    }
                }
            } else {
                list.setToolTipText(null);
            }
        } else {
            lbl.setBackground(Color.WHITE);
        }

        lbl.setFont(list.getFont());
        lbl.setText((value == null) ? "" : value.toString());

        if (("" + value).length() < 80) {
            lbl.setHorizontalAlignment(align);
        } else {
            lbl.setHorizontalAlignment(SwingConstants.LEADING);
        }

        return lbl;
    }
}