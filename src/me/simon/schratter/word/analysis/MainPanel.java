package me.simon.schratter.word.analysis;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainPanel extends JPanel implements ActionListener,
        PropertyChangeListener {

    private final JButton chooseFileBt;
    private final JButton startParsingFileBt;
    private final DefaultTableModel defaultTableModel = new DefaultTableModel() {
        @Override
        public Class getColumnClass(int column) {
            return switch (column) {
                case 1 -> Integer.class;
                default -> String.class;
            };
        }
    };
    private ProgressMonitor progressMonitor;
    private Task task;
    private File textFile;
    private FileFormatParser fileFormatParser;

    public MainPanel() {
        super(new BorderLayout());

        fileFormatParser = new RegexFileFormatParser("\\s");
        final String info = """
                File Reader and Word occurrence Sorter by Simon Schratter\s
                \s
                Note:\s
                Select file to list word count by occurrence""";

        JTextArea infoTextArea = new JTextArea();
        infoTextArea.setWrapStyleWord(true);
        infoTextArea.setText(info);
        infoTextArea.setBackground(new Color(241, 241, 241));
        infoTextArea.setEditable(false);
        infoTextArea.setMargin(new Insets(5, 5, 5, 5));
        infoTextArea.setFocusable(false);

        JPanel infoPanel = new JPanel();
        infoPanel.add(infoTextArea);
        infoPanel.setFocusable(false);
        add(infoPanel, BorderLayout.PAGE_START);

        defaultTableModel.addColumn("WORDS");
        defaultTableModel.addColumn("COUNT");

        JTable table = new JTable(defaultTableModel);
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setAutoCreateRowSorter(true);

        ArrayList<RowSorter.SortKey> list = new ArrayList<>();
        var sorter = (DefaultRowSorter) table.getRowSorter();
        sorter.setSortsOnUpdates(true);
        list.add(new RowSorter.SortKey(1, SortOrder.DESCENDING));
        sorter.setSortKeys(list);
        sorter.sort();

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);


        chooseFileBt = new JButton("Select File");

        startParsingFileBt = new JButton("Start parsing file");
        startParsingFileBt.setActionCommand("start");
        startParsingFileBt.addActionListener(this);

        JPanel controlPanel = new JPanel();
        controlPanel.add(startParsingFileBt, BorderLayout.WEST);
        controlPanel.add(chooseFileBt, BorderLayout.EAST);
        controlPanel.setFocusable(false);
        add(controlPanel, BorderLayout.PAGE_END);

        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        chooseFileBt.addActionListener((e) -> {
            JFileChooser chooser = new JFileChooser();
            int returnValue = chooser.showOpenDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                textFile = chooser.getSelectedFile();
            }

        });

    }

    class Task extends SwingWorker<DefaultTableModel, Map<String, AnalysedWord>> {
        private final DefaultTableModel model;

        public Task(DefaultTableModel model) {
            this.model = model;
        }

        @Override
        public DefaultTableModel doInBackground() throws IOException {
            Path path = textFile.toPath();
            long size = Files.size(path);
            try (
                    CountingInputStream inputStream = new CountingInputStream(Files.newInputStream(path));
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))
            ) {
                Map<String, AnalysedWord> wordMap = new HashMap<>();
                String line;

                while ((line = bufferedReader.readLine()) != null && !isCancelled()) {
                    String[] words = fileFormatParser.parseLine(line);
                    parseWords(wordMap, words);
                    int progress = (int) ((inputStream.getBytesRead() / (double) size) * 100);
                    setProgress(progress);
                }
                setProgress(100);
                publish(wordMap);
                return model;
            }
        }

        private void parseWords(Map<String, AnalysedWord> wordMap, String[] words) {
            for (String word : words) {
                AnalysedWord analysedWord = wordMap.get(word);
                if (analysedWord == null) {
                    analysedWord = new AnalysedWord();
                    wordMap.put(word, analysedWord);
                }
                analysedWord.increment();
            }
        }

        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            startParsingFileBt.setEnabled(true);
            chooseFileBt.setEnabled(true);
        }

        @Override
        protected void process(List<Map<String, AnalysedWord>> wordsList) {
            model.setRowCount(0);

            if (!isCancelled()) {
                wordsList.forEach(wordMap -> {
                    wordMap.forEach((s, analysedWord) -> {
                        model.addRow(new Object[]{s, analysedWord.getCount()});
                    });
                });
                progressMonitor.close();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (textFile != null) {
            progressMonitor = new ProgressMonitor(MainPanel.this,
                    "Parse file", "", 0, 100);

            progressMonitor.setProgress(0);

            task = new Task(defaultTableModel);
            task.addPropertyChangeListener(this);
            task.execute();

            startParsingFileBt.setEnabled(false);
            chooseFileBt.setEnabled(false);
        } else {
            JOptionPane.showMessageDialog(new JFrame(), "Please choose a file!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            int progress = (Integer) evt.getNewValue();
            progressMonitor.setProgress(progress);

            String message =
                    String.format("Completed %d%%.\n", progress);
            progressMonitor.setNote(message);

            if (progressMonitor.isCanceled() || task.isDone()) {
                Toolkit.getDefaultToolkit().beep();
                if (progressMonitor.isCanceled()) {
                    task.cancel(true);
                } else {
                    textFile = null;
                }
                startParsingFileBt.setEnabled(true);
                chooseFileBt.setEnabled(true);

            }
        }
    }
}