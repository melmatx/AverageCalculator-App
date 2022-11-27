import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    static LinkedList<Preset> presetLinkedList = new LinkedList<>();
    static JDialog selectorDialog;
    static JDialog resultDialog;
    static int quarters;

    public static void main(String[] args) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel welcomeLabel = new JLabel("Welcome to Grades Calculator!");
        JButton selectAPresetButton = new JButton("Select a preset");
        JButton importPresetsButton = new JButton("Import presets");
        JButton exportPresetsButton = new JButton("Export presets");
        JButton exitButton = new JButton("Exit");

        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectAPresetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        importPresetsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exportPresetsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        selectAPresetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new PresetSelector();
            }
        });
        importPresetsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readPresetFile();
            }
        });
        exportPresetsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                savePresetFile(false);
            }
        });
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        panel.add(new Panel());
        panel.add(welcomeLabel);
        panel.add(new Panel());
        panel.add(selectAPresetButton);
        panel.add(importPresetsButton);
        panel.add(exportPresetsButton);
        panel.add(exitButton);
        panel.add(new Panel());

        JFrame frame = new JFrame("Welcome");
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setSize(250, 200);
        frame.setVisible(true);
    }

    public static void addPreset(Preset preset) {
        int id = 0;
        if (!presetLinkedList.isEmpty()) {
            int lastId = presetLinkedList.getLast().getId();
            if (lastId != 0) {
                id = lastId + 1;
            }
        }
        preset.setId(id);
        presetLinkedList.add(preset);
    }

    public static void createDialog(String labelText, boolean isSuccess) {
        if (resultDialog != null) {
            resultDialog.dispose();
        }
        resultDialog = new JDialog();
        String titleText = isSuccess ? "Success!" : "Error!";
        resultDialog.setTitle(titleText);

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(labelText);
        JButton okButton = new JButton("OK");

        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resultDialog.dispose();
            }
        });

        resultPanel.add(new Panel());
        resultPanel.add(label);
        resultPanel.add(new Panel());
        resultPanel.add(okButton);
        resultPanel.add(new Panel());

        resultDialog.add(resultPanel);
        resultDialog.pack();
        resultDialog.setSize(250, 120);
        resultDialog.setVisible(true);
    }

    public static boolean checkExisting(Preset preset) {
        try {
            File presetFile = new File("presets.txt");
            Scanner sc = new Scanner(presetFile);

            while (sc.hasNextLine()) {
                String[] line = sc.nextLine().split("\\s*" + ":" + "\\s*");
                String name = line[0];
                String[] subjects = line[1].split("\\s*" + "," + "\\s*");

                boolean isNameSame = Objects.equals(preset.getName(), name);
                boolean isSubjectsSame = Arrays.equals(preset.getSubjects(), subjects);

                if (isNameSame && isSubjectsSame) {
                    return true;
                }
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void savePresetFile(boolean silentMode) {
        try {
            if (presetLinkedList.isEmpty()) {
                createDialog("No Presets to Export", false);
                return;
            }

            FileWriter fw = new FileWriter("presets.txt", true);
            int count = 0;
            for (Preset preset : presetLinkedList) {
                if (checkExisting(preset)) {
                    continue;
                }

                fw.append(preset.getName()).append(": ");
                fw.append(String.join(", ", preset.getSubjects()));
                fw.append("\r\n");
                count++;
            }
            fw.close();

            if (!silentMode) {
                createDialog("Exported " + count + " Presets", true);
            }
        } catch (IOException e) {
            if (!silentMode) {
                createDialog("An error occurred", false);
            }
            e.printStackTrace();
        }
    }

    public static void readPresetFile() {
        try {
            File presetFile = new File("presets.txt");
            Scanner sc = new Scanner(presetFile);

            // Overwrite existing main presets
            presetLinkedList = new LinkedList<>();

            int count = 0;
            while (sc.hasNextLine()) {
                String[] line = sc.nextLine().split("\\s*" + ":" + "\\s*");
                String name = line[0];
                String[] subjects = line[1].split("\\s*" + "," + "\\s*");

                Preset newPreset = new Preset(name, subjects);
                addPreset(newPreset);
                count++;
            }
            sc.close();

            createDialog("Imported " + count + " Presets", true);
        } catch (FileNotFoundException e) {
            createDialog("No Preset File Detected", false);
            e.printStackTrace();
        }
    }
}
