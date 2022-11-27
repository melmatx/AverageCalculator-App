import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PresetSelector extends Main {
    Preset selected;
    private JPanel selectorPanel;
    private JList<String> presetList;
    private JButton createNewPresetButton;
    private JButton editPresetButton;
    private JButton deletePresetButton;
    private JButton continueButton;

    public PresetSelector() {
        if (selectorDialog != null) {
            selectorDialog.dispose();
        }
        selected = null;
        setupListModel();
        setupGUI();
    }

    public void setupListModel() {
        if (!presetLinkedList.isEmpty()) {
            DefaultListModel<String> presets = new DefaultListModel<>();

            for (Preset preset : presetLinkedList) {
                presets.addElement(preset.getName());
            }
            presetList.setModel(presets);
        }
    }

    public void setupGUI() {
        selectorDialog = new JDialog();
        selectorDialog.setTitle("Select a Preset");
        selectorDialog.add(selectorPanel);
        selectorDialog.pack();
        selectorDialog.setSize(350, 350);
        selectorDialog.setVisible(true);

        presetList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = presetList.getSelectedIndex();
                    if (index != -1) {
                        new PresetEditor(index, presetLinkedList.get(index));
                    }
                }
            }
        });
        presetList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                editPresetButton.setEnabled(true);
                deletePresetButton.setEnabled(true);
                continueButton.setEnabled(true);
            }
        });
        createNewPresetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new PresetEditor();
            }
        });
        editPresetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = presetList.getSelectedIndex();
                new PresetEditor(index, presetLinkedList.get(index));
            }
        });
        deletePresetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] indices = presetList.getSelectedIndices();
                for (int i = indices.length - 1; i > -1; i--) {
                    presetLinkedList.remove(indices[i]);
                }
                new PresetSelector();
            }
        });
        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selected = presetLinkedList.get(presetList.getSelectedIndex());
                createQuarterGUI();
            }
        });
    }

    public void createQuarterGUI() {
        JDialog quarterDialog = new JDialog();
        JPanel quarterPanel = new JPanel();
        quarterPanel.setLayout(new BoxLayout(quarterPanel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Enter Quarters");
        JTextField qField = new JTextField();
        JButton button = new JButton("Enter");

        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        qField.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        qField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                button.setEnabled(!qField.getText().equals("") && qField.getText() != null);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                button.setEnabled(!qField.getText().equals("") && qField.getText() != null);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                button.setEnabled(!qField.getText().equals("") && qField.getText() != null);
            }
        });
        button.setEnabled(false);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    quarters = Integer.parseInt(qField.getText());

                    if (quarters < 1) {
                        setTimedBorder(qField);
                    } else {
                        new GradesViewer(quarters, selected);
                        quarterDialog.dispose();
                        selectorDialog.dispose();
                    }
                } catch (NumberFormatException ev) {
                    setTimedBorder(qField);
                }
            }
        });

        if (quarters > 0) {
            qField.setText(String.valueOf(quarters));
            button.setEnabled(true);
        }

        quarterPanel.add(label);
        quarterPanel.add(qField);
        quarterPanel.add(new Panel());
        quarterPanel.add(button);
        quarterPanel.add(new Panel());
        quarterPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        quarterDialog.add(quarterPanel);
        quarterDialog.pack();
        quarterDialog.setVisible(true);
    }

    public void setTimedBorder(JTextField qField) {
        Border orig = new JTextField().getBorder();
        qField.setBorder(new LineBorder(Color.RED, 1));
        qField.setText("");

        Timer timer = new Timer(2000, event -> qField.setBorder(orig));
        timer.setRepeats(false);
        timer.start();
    }
}
