import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PresetEditor extends Main {
    DefaultListModel<String> subjectModel;
    Preset edit;
    int index;
    private JDialog editorDialog;
    private JDialog addSubjDialog;
    private JDialog editSubjDialog;
    private JPanel editorPanel;
    private JTextField nameField;
    private JList<String> subjectList;
    private JButton addSubjectButton;
    private JButton deleteSubjectButton;
    private JButton saveButton;
    private JButton cancelButton;

    public PresetEditor() {
        subjectModel = new DefaultListModel<>();
        createGui();
    }

    public PresetEditor(int index, Preset edit) {
        this.index = index;
        this.edit = edit;

        subjectModel = new DefaultListModel<>();
        nameField.setText(edit.getName());
        for (String subject : edit.getSubjects()) {
            subjectModel.addElement(subject);
        }
        subjectList.setModel(subjectModel);
        createGui();
    }

    public void createGui() {
        editorDialog = new JDialog();
        editorDialog.setTitle("Preset Editor");
        editorDialog.add(editorPanel);
        editorDialog.pack();
        editorDialog.setSize(350, 350);
        editorDialog.setVisible(true);

        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                saveButton.setEnabled(checkIfValid(nameField, subjectList));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saveButton.setEnabled(checkIfValid(nameField, subjectList));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                saveButton.setEnabled(checkIfValid(nameField, subjectList));
            }
        });
        subjectList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedIndex = subjectList.getSelectedIndex();

                    if (selectedIndex != -1) {
                        if (editSubjDialog != null) {
                            editSubjDialog.dispose();
                        }
                        createEditGUI(selectedIndex);
                    }
                }
            }
        });
        subjectList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                saveButton.setEnabled(checkIfValid(nameField, subjectList));
                deleteSubjectButton.setEnabled(checkIfValid(subjectList));
            }
        });
        addSubjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (addSubjDialog != null) {
                    addSubjDialog.dispose();
                }
                createAddGUI();
            }
        });
        deleteSubjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] indices = subjectList.getSelectedIndices();
                for (int i = indices.length - 1; i > -1; i--) {
                    subjectModel.remove(indices[i]);
                }
                subjectList.setModel(subjectModel);
                deleteSubjectButton.setEnabled(false);
            }
        });
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                name = name.replaceAll(",", "");
                name = name.replaceAll("#", "");

                int subjSize = subjectModel.getSize();
                String[] subjects = new String[subjSize];
                for (int i = 0; i < subjSize; i++) {
                    String subject = subjectModel.get(i);
                    subject = subject.replaceAll(",", "");
                    subject = subject.replaceAll("#", "");
                    subjects[i] = subject;
                }
                Preset newPreset = new Preset(name, subjects);

                if (edit != null) {
                    newPreset.setId(index);
                    presetLinkedList.set(index, newPreset);
                } else {
                    Main.addPreset(newPreset);
                }

                new PresetSelector();
                editorDialog.dispose();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editorDialog.dispose();
            }
        });
    }

    public void createAddGUI() {
        addSubjDialog = new JDialog();
        JPanel addSubjPanel = new JPanel();
        addSubjPanel.setLayout(new BoxLayout(addSubjPanel, BoxLayout.Y_AXIS));

        JLabel subjName = new JLabel("Subject Name");
        JTextField subjField = new JTextField();
        JButton addSubjButton = new JButton("Add Subject");

        subjName.setAlignmentX(Component.CENTER_ALIGNMENT);
        subjField.setAlignmentX(Component.CENTER_ALIGNMENT);
        addSubjButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        subjField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                addSubjButton.setEnabled(checkIfValid(subjField));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                addSubjButton.setEnabled(checkIfValid(subjField));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                addSubjButton.setEnabled(checkIfValid(subjField));
            }
        });
        addSubjButton.setEnabled(false);
        addSubjButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                subjectModel.addElement(subjField.getText());
                subjectList.setModel(subjectModel);
                saveButton.setEnabled(checkIfValid(nameField, subjectList));
                deleteSubjectButton.setEnabled(subjectList.getSelectedIndex() != -1);
                addSubjDialog.dispose();
            }
        });

        addSubjPanel.add(subjName);
        addSubjPanel.add(subjField);
        addSubjPanel.add(new Panel());
        addSubjPanel.add(addSubjButton);
        addSubjPanel.add(new Panel());
        addSubjPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        addSubjDialog.add(addSubjPanel);
        addSubjDialog.pack();
        addSubjDialog.setVisible(true);
    }

    public void createEditGUI(int selectedIndex) {
        editSubjDialog = new JDialog();
        JPanel editSubjPanel = new JPanel();
        editSubjPanel.setLayout(new BoxLayout(editSubjPanel, BoxLayout.Y_AXIS));

        JLabel subjName = new JLabel("New Name");
        JTextField subjField = new JTextField(subjectModel.get(selectedIndex));
        JButton editSubjButton = new JButton("Save Name");

        subjName.setAlignmentX(Component.CENTER_ALIGNMENT);
        subjField.setAlignmentX(Component.CENTER_ALIGNMENT);
        editSubjButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        subjField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                editSubjButton.setEnabled(checkIfValid(subjField));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                editSubjButton.setEnabled(checkIfValid(subjField));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                editSubjButton.setEnabled(checkIfValid(subjField));
            }
        });
        editSubjButton.setEnabled(false);
        editSubjButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                subjectModel.set(selectedIndex, subjField.getText());
                subjectList.setModel(subjectModel);
                saveButton.setEnabled(checkIfValid(nameField, subjectList));
                deleteSubjectButton.setEnabled(subjectList.getSelectedIndex() != -1);
                editSubjDialog.dispose();
            }
        });

        editSubjPanel.add(subjName);
        editSubjPanel.add(subjField);
        editSubjPanel.add(new Panel());
        editSubjPanel.add(editSubjButton);
        editSubjPanel.add(new Panel());
        editSubjPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        editSubjDialog.add(editSubjPanel);
        editSubjDialog.pack();
        editSubjDialog.setVisible(true);
    }

    boolean checkIfValid(JTextField nameField, JList<String> subjectList) {
        boolean isNameNotEmpty = !nameField.getText().equals("") && nameField.getText() != null;
        boolean hasSubjects = subjectList.getModel() != null && subjectList.getModel().getSize() >= 1;
        return isNameNotEmpty && hasSubjects;
    }

    boolean checkIfValid(JTextField nameField) {
        return !nameField.getText().equals("") && nameField.getText() != null;
    }

    boolean checkIfValid(JList<String> subjectList) {
        return subjectList.getModel() != null && subjectList.getModel().getSize() >= 1;
    }
}
