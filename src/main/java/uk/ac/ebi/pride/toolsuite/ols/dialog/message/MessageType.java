package uk.ac.ebi.pride.toolsuite.ols.dialog.message;


import javax.swing.*;


public enum MessageType {
    INFO(new ImageIcon(MessageType.class.getResource("icons/info_message.png"))),
    WARNING(new ImageIcon(MessageType.class.getResource("icons/warning_message.png"))),
    ERROR(new ImageIcon(MessageType.class.getResource("icons/error_message.png")));

    private Icon icon;

    MessageType(Icon icon) {
        this.icon = icon;
    }

    public Icon getIcon() {
        return icon;
    }
}
