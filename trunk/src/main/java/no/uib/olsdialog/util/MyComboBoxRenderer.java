package no.uib.olsdialog.util;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * A combo box renderer that allows tooltip for each element in the combo
 * box list.
 *
 * @author Harald Barsnes
 *         <p/>
 *         Created March 2008
 */
public class MyComboBoxRenderer extends BasicComboBoxRenderer {

    private Vector tooltips;
    private int align;

    /**
     * Creates a new instance of the MyComboBoxRenderer.
     *
     * @param tooltips vector containg the tooltips
     * @param align    the horizontal alignment of the text
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
                        String temp = "", temp2 = "";

                        while (tok.hasMoreTokens()) {
                            temp += tok.nextToken() + " ";

                            if (temp.length() > 40) {
                                temp2 += temp + "<br>";
                                temp = "";
                            }
                        }

                        if (temp.length() > 0) {
                            temp2 += temp;
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
            lbl.setBackground(list.getBackground());
            lbl.setForeground(list.getForeground());
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