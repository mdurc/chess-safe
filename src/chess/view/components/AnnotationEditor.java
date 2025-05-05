package chess.view.components;

import chess.controller.ChessController;

import java.awt.BorderLayout;

import javax.swing.*;

public class AnnotationEditor extends JPanel {
    private final JTextArea textArea = new JTextArea(10, 20);
    
    public AnnotationEditor(ChessController controller) {
        setLayout(new BorderLayout());
        JButton saveButton = new JButton("Save Annotation");
        saveButton.addActionListener(e -> 
            controller.saveAnnotation(textArea.getText()));
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        add(saveButton, BorderLayout.SOUTH);
    }
}
