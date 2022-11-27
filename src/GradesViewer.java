import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class GradesViewer extends Main {
    int quarters;
    Preset preset;
    String[] subjects;
    ArrayList<ArrayList<JTextField>> mainGradeFieldList;
    private CardLayout cl;
    private JPanel cards;
    private JPanel card1;
    private JPanel card2;
    private JComboBox<String> cb;

    public GradesViewer(int quarters, Preset preset) {
        this.quarters = quarters;
        this.preset = preset;
        this.subjects = preset.getSubjects();
        mainGradeFieldList = new ArrayList<>();
        createGUI();
    }

    public void createGUI() {
        JFrame frame = new JFrame("Grades Viewer");

        JPanel viewerPanel = new JPanel(new BorderLayout());
        cl = new CardLayout();
        cards = new JPanel(cl);
        card1 = new JPanel();
        card2 = new JPanel();

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GradesViewer gv = new GradesViewer(quarters, preset);
                gv.resetFields();
                frame.dispose();
            }
        });
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveGradeFile();
            }
        });

        card1.setLayout(new BoxLayout(card1, BoxLayout.Y_AXIS));
        card2.setLayout(new BoxLayout(card2, BoxLayout.Y_AXIS));

        String[] cardNames = {"Enter Grades", "Grades Overview"};

        cards.add(new JScrollPane(card1), cardNames[0]);
        cards.add(new JScrollPane(card2), cardNames[1]);

        cb = new JComboBox<>(cardNames);
        cb.setEditable(false);
        cb.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setupCard2();
                cl.show(cards, e.getItem().toString());
            }
        });
        JPanel comboBoxPane = new JPanel();
        comboBoxPane.add(cb);
        comboBoxPane.add(resetButton);
        comboBoxPane.add(exportButton);

        viewerPanel.add(comboBoxPane, BorderLayout.PAGE_START);
        viewerPanel.add(cards, BorderLayout.CENTER);

        setupCard1();
        setupCard2();

        frame.setContentPane(viewerPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void setupCard1() {
        card1.add(new Panel());

        JButton continueButton = new JButton("Continue");
        continueButton.setEnabled(false);
        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cb.setSelectedIndex(1);
            }
        });
        JPanel continuePanel = new JPanel();
        continuePanel.setLayout(new FlowLayout());
        continuePanel.add(continueButton);

        for (String subject : subjects) {
            JPanel subjPanel = new JPanel();
            JLabel subjLabel = new JLabel(subject);

            // Set JLabel text to bold
            String defaultFont = subjLabel.getFont().getName();
            int defaultSize = subjLabel.getFont().getSize();
            Font newLabelFont = new Font(defaultFont, Font.BOLD, defaultSize);
            subjLabel.setFont(newLabelFont);

            subjPanel.add(subjLabel);
            card1.add(subjPanel);

            JPanel gradesPanel = new JPanel();
            gradesPanel.setLayout(new FlowLayout());

            ArrayList<JTextField> gradeFieldList = new ArrayList<>();
            for (int i = 0; i < quarters; i++) {
                JLabel quarterLabel = new JLabel("Q" + (i + 1));
                JTextField gradeField = new JTextField(3);

                gradeField.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        verifyField(gradeField);
                        enableContinueBtn(continueButton);
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        verifyField(gradeField);
                        enableContinueBtn(continueButton);
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        verifyField(gradeField);
                        enableContinueBtn(continueButton);
                    }
                });
                gradeFieldList.add(gradeField);

                JPanel padding = new JPanel();
                padding.add(quarterLabel);
                padding.add(gradeField);
                gradesPanel.add(padding);
            }
            mainGradeFieldList.add(gradeFieldList);

            card1.add(gradesPanel);
            card1.add(new Panel());
        }
        card1.add(new Panel());
        card1.add(continuePanel);
        card1.add(new Panel());

        readGradeFile();
    }

    public void setupCard2() {
        card2.removeAll();

        DefaultTableModel gradesModel = new DefaultTableModel() {

            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0 && column != (quarters + 1) && column != (quarters + 2);
            }
        };

        JTable gradesTable = new JTable(gradesModel);

        // Insert headers
        gradesModel.addColumn("Subject");
        for (int i = 1, column_size = quarters + 1; i < column_size; i++) {
            gradesModel.addColumn("Q" + i);
        }
        gradesModel.addColumn("Average");

        // Insert data
        double genAverage = 0;
        for (int i = 0, row_size = subjects.length; i < row_size; i++) {
            int k = 0;
            gradesModel.addRow(new Object[0]);

            // Insert current subject on row
            gradesModel.setValueAt(subjects[i], i, k);

            // Insert grades of current subject
            double average = 0;
            for (int j = 0; j < quarters; j++) {
                k++;
                try {
                    double grade = Double.parseDouble(mainGradeFieldList.get(i).get(j).getText());
                    average += grade;
                    gradesModel.setValueAt(grade, i, k);
                } catch (NumberFormatException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
            average /= quarters;
            genAverage += average;

            // Round up Subject Average to 2 decimal places
            average = Math.round(average * 100.0) / 100.0;

            gradesModel.setValueAt(average, i, k + 1);
        }
        genAverage /= subjects.length;

        // Round up General Average to 2 decimal places
        genAverage = Math.round(genAverage * 100.0) / 100.0;

        // Add General Average Column
        gradesModel.addColumn("General Average: " + genAverage);

        // Set width of every column except General Average Column
        gradesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0, column_size = quarters + 1; i < column_size; i++) {
            TableColumn col = gradesTable.getColumnModel().getColumn(i);
            if (i > 0) {
                col.setPreferredWidth(50);
            } else {
                col.setPreferredWidth(120);
            }
        }

        // Set General Average Column Width
        TableColumnModel gradesCM = gradesTable.getColumnModel();
        int last = gradesCM.getColumnCount() - 1;
        gradesCM.getColumn(last).setPreferredWidth(200);

        gradesModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                TableModel model = (TableModel) e.getSource();
                String data = model.getValueAt(row, column).toString();

                JTextField changeField = mainGradeFieldList.get(row).get(column - 1);
                changeField.setText(data);

                // Compute New Average for Changed Row
                double newAverage = 0;
                for (int i = 1; i <= quarters; i++) {
                    try {
                        double grade = Double.parseDouble(gradesModel.getValueAt(row, i).toString());
                        newAverage += grade;
                    } catch (NumberFormatException | NullPointerException ev) {
                        ev.printStackTrace();
                    }
                }
                newAverage /= quarters;

                // Set New Average for Changed Row
                gradesModel.removeTableModelListener(this);
                gradesModel.setValueAt(newAverage, row, quarters + 1);
                gradesModel.addTableModelListener(this);

                // Compute New General Average
                double newGenAverage = 0;
                for (int i = 0, length = subjects.length; i < length; i++) {
                    try {
                        String value = gradesModel.getValueAt(i, quarters + 1).toString();
                        double grade = Double.parseDouble(value);
                        newGenAverage += grade;
                    } catch (NumberFormatException | NullPointerException ev) {
                        ev.printStackTrace();
                    }
                }
                newGenAverage /= subjects.length;

                // Set New General Average
                TableColumnModel gradesCM = gradesTable.getColumnModel();
                int last = gradesCM.getColumnCount() - 1;
                gradesCM.getColumn(last).setHeaderValue("General Average: " + newGenAverage);
            }
        });

        card2.add(new JScrollPane(gradesTable));
    }

    public void saveGradeFile() {
        try {
            replaceInvalidFields();
            removeExistingLine();

            FileWriter fw = new FileWriter("grades.txt", true);
            fw.append(preset.getName()).append(" #");
            fw.append(String.valueOf(subjects.length)).append(" #");
            fw.append(String.valueOf(quarters)).append(": ");

            int i = 0, size = mainGradeFieldList.size() * quarters;
            String[] gradeField = new String[size];
            for (ArrayList<JTextField> gradeFieldList : mainGradeFieldList) {
                for (JTextField jTextField : gradeFieldList) {
                    gradeField[i] = jTextField.getText();
                    i++;
                }
            }

            fw.append(String.join(", ", gradeField));
            fw.append("\r\n");
            fw.close();

            savePresetFile(true);
            createDialog("Exported grades + preset!", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readGradeFile() {
        try {
            File gradeFile = new File("grades.txt");
            if (!gradeFile.exists()) {
                return;
            }

            Scanner sc = new Scanner(gradeFile);
            while (sc.hasNextLine()) {
                String[] line = sc.nextLine().split("\\s*" + "#" + "\\s*");

                String saved_presetName = line[0];
                String saved_subjNum = line[1];

                line = line[2].split("\\s*" + ":" + "\\s*");
                String saved_quarters = line[0];

                boolean isCurrentPreset = Objects.equals(saved_presetName, preset.getName());
                boolean isCurrentNum = Objects.equals(saved_subjNum, String.valueOf(subjects.length));
                boolean isCurrentQuarters = Objects.equals(saved_quarters, String.valueOf(quarters));
                if (!isCurrentPreset || !isCurrentNum || !isCurrentQuarters) {
                    continue;
                }

                int count = 0;
                String[] grades = line[1].split("\\s*" + "," + "\\s*");
                for (ArrayList<JTextField> gradeFieldList : mainGradeFieldList) {
                    for (JTextField gradeField : gradeFieldList) {
                        gradeField.setText(grades[count]);
                        count++;
                    }
                }
                break;
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void removeExistingLine() {
        try {
            File gradeFile = new File("grades.txt");
            if (!gradeFile.exists()) {
                return;
            }

            Scanner sc = new Scanner(gradeFile);
            StringBuilder sb = new StringBuilder();
            boolean alreadyExists = false;

            while (sc.hasNextLine()) {
                String origLine = sc.nextLine();
                String[] line = origLine.split("\\s*" + "#" + "\\s*");

                String saved_presetName = line[0];
                String saved_subjNum = line[1];

                line = line[2].split("\\s*" + ":" + "\\s*");
                String saved_quarters = line[0];

                boolean isCurrentPreset = Objects.equals(saved_presetName, preset.getName());
                boolean isCurrentNum = Objects.equals(saved_subjNum, String.valueOf(subjects.length));
                boolean isCurrentQuarters = Objects.equals(saved_quarters, String.valueOf(quarters));
                if (!isCurrentPreset || !isCurrentNum || !isCurrentQuarters) {
                    sb.append(origLine).append("\r\n");
                } else {
                    alreadyExists = true;
                }
            }
            sc.close();

            if (alreadyExists) {
                FileWriter fw = new FileWriter(gradeFile);
                fw.write(sb.toString());
                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void replaceInvalidFields() {
        for (ArrayList<JTextField> gradeFieldList : mainGradeFieldList) {
            for (JTextField gradeField : gradeFieldList) {
                String extracted = gradeField.getText().replaceAll("[^0-9?!.]", "");
                gradeField.setText(extracted);

                if (Objects.equals(extracted, "")) {
                    gradeField.setText(String.valueOf(0));
                }
            }
        }
    }

    public void resetFields() {
        for (ArrayList<JTextField> gradeFieldList : mainGradeFieldList) {
            for (JTextField gradeField : gradeFieldList) {
                gradeField.setText("");
            }
        }
    }

    public void verifyField(JTextField gradeField) {
        Border orig = new JTextField(3).getBorder();
        try {
            Double.parseDouble(gradeField.getText());
            gradeField.setBorder(orig);
        } catch (NumberFormatException | NullPointerException e) {
            gradeField.setBorder(new LineBorder(Color.RED, 5));
        }
        cards.revalidate();
    }

    public void enableContinueBtn(JButton continueButton) {
        for (ArrayList<JTextField> gradeFieldList : mainGradeFieldList) {
            for (JTextField gradeField : gradeFieldList) {
                if (Objects.equals(gradeField.getText(), "")) {
                    continueButton.setEnabled(false);
                    return;
                }
            }
        }
        continueButton.setEnabled(true);
    }
}
