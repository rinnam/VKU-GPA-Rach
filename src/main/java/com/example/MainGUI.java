package com.example;

import com.google.gson.Gson;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* =======================
   DATA CLASSES
======================= */
class Subject {
    int id;
    String name;
    int countTC;
    Object scoreT10;
}

class ScoreData {
    List<Subject> scoreAll;
}

/* =======================
   MAIN GUI
======================= */
public class MainGUI extends JFrame {

    private DefaultTableModel model;
    private JTable table;

    // File JSON hiện tại
    private File currentJsonFile = null;

    // Hàng đã chỉnh sửa
    private final Set<Integer> editedRows = new HashSet<>();

    // GPA
    private JLabel lblOldGPA, lblNewGPA, lblDiff;

    // Thống kê
    private JLabel lblA, lblB, lblC, lblD, lblF;

    private double oldGPA = -1;

    public MainGUI() {
        setTitle("Quản lý điểm & GPA");
        setSize(1200, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane())
                .setBorder(new EmptyBorder(10, 10, 10, 10));

        /* ===== TOP ===== */
        JPanel top = new JPanel(new BorderLayout());

        JLabel title = new JLabel("QUẢN LÝ ĐIỂM & GPA", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JButton btnBrowse = new JButton("BROWSE JSON");
        btnBrowse.addActionListener(e -> browseJsonFile());

        top.add(title, BorderLayout.CENTER);
        top.add(btnBrowse, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        /* ===== TABLE ===== */
        String[] cols = {
                "ID", "Tên môn", "Tín chỉ",
                "Điểm hệ 10", "Điểm hệ 4", "Điểm chữ"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 3;
            }
        };

        table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader()
                .setFont(new Font("Segoe UI", Font.BOLD, 14));

        RowHighlightRenderer rowRenderer = new RowHighlightRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i)
                    .setCellRenderer(rowRenderer);
        }

        table.getColumnModel().getColumn(5)
                .setCellRenderer(new GradeRenderer());

        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);

        /* ===== LISTEN EDIT ===== */
        model.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 3) {
                editedRows.add(e.getFirstRow());
                table.repaint();
            }
        });

        /* ===== BOTTOM ===== */
        JPanel bottom = new JPanel(new BorderLayout(10, 10));

        // GPA
        JPanel gpaPanel = new JPanel(new GridLayout(3, 1));
        gpaPanel.setBorder(BorderFactory.createTitledBorder("GPA"));

        lblOldGPA = new JLabel("GPA ban đầu: --");
        lblNewGPA = new JLabel("GPA hiện tại: --");
        lblDiff   = new JLabel("Chênh lệch: --");

        gpaPanel.add(lblOldGPA);
        gpaPanel.add(lblNewGPA);
        gpaPanel.add(lblDiff);

        // Stats
        JPanel statPanel = new JPanel(new GridLayout(1, 5));
        statPanel.setBorder(BorderFactory.createTitledBorder("Thống kê"));

        lblA = new JLabel("A: 0", JLabel.CENTER);
        lblB = new JLabel("B: 0", JLabel.CENTER);
        lblC = new JLabel("C: 0", JLabel.CENTER);
        lblD = new JLabel("D: 0", JLabel.CENTER);
        lblF = new JLabel("F: 0", JLabel.CENTER);

        statPanel.add(lblA);
        statPanel.add(lblB);
        statPanel.add(lblC);
        statPanel.add(lblD);
        statPanel.add(lblF);

        // Buttons
        JPanel btnPanel = new JPanel();

        JButton btnAdd   = new JButton("THÊM MÔN");
        JButton btnCalc  = new JButton("TÍNH GPA");
        JButton btnReset = new JButton("RESET");

        btnAdd.addActionListener(e -> showAddSubjectDialog());
        btnCalc.addActionListener(e -> calculateGPA());
        btnReset.addActionListener(e -> resetAndLoadJson());

        btnPanel.add(btnAdd);
        btnPanel.add(btnCalc);
        btnPanel.add(btnReset);

        bottom.add(gpaPanel, BorderLayout.WEST);
        bottom.add(statPanel, BorderLayout.CENTER);
        bottom.add(btnPanel, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);
    }

    /* =======================
       BROWSE JSON FILE
    ======================= */
    private void browseJsonFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter(
                        "JSON files", "json"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentJsonFile = chooser.getSelectedFile();
            resetAndLoadJson();
        }
    }

    /* =======================
       LOAD JSON FROM FILE
    ======================= */
    private void loadJson(File file) {
        try {
            Gson gson = new Gson();
            FileReader reader = new FileReader(file);
            ScoreData data = gson.fromJson(reader, ScoreData.class);

            for (Subject s : data.scoreAll) {
                model.addRow(new Object[]{
                        s.id,
                        s.name,
                        s.countTC,
                        s.scoreT10,
                        "",
                        ""
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "File JSON không hợp lệ!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /* =======================
       RESET = LOAD LẠI FILE ĐÃ CHỌN
    ======================= */
    private void resetAndLoadJson() {
        model.setRowCount(0);
        editedRows.clear();
        oldGPA = -1;

        lblOldGPA.setText("GPA ban đầu: --");
        lblNewGPA.setText("GPA hiện tại: --");
        lblDiff.setText("Chênh lệch: --");

        lblA.setText("A: 0");
        lblB.setText("B: 0");
        lblC.setText("C: 0");
        lblD.setText("D: 0");
        lblF.setText("F: 0");

        if (currentJsonFile != null) {
            loadJson(currentJsonFile);
        }

        table.repaint();
    }

    /* =======================
       ADD SUBJECT (TEMP)
    ======================= */
    private void showAddSubjectDialog() {
        JDialog dialog = new JDialog(this, "Thêm môn mới", true);
        dialog.setSize(350, 230);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField txtName = new JTextField();
        JTextField txtTC = new JTextField();
        JTextField txtScore = new JTextField();

        form.add(new JLabel("Tên môn:"));
        form.add(txtName);
        form.add(new JLabel("Tín chỉ:"));
        form.add(txtTC);
        form.add(new JLabel("Điểm hệ 10:"));
        form.add(txtScore);

        JPanel btns = new JPanel();
        JButton ok = new JButton("Thêm");
        JButton cancel = new JButton("Hủy");

        btns.add(ok);
        btns.add(cancel);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btns, BorderLayout.SOUTH);

        ok.addActionListener(e -> {
            try {
                String name = txtName.getText().trim();
                int tc = Integer.parseInt(txtTC.getText().trim());
                double score10 = Double.parseDouble(txtScore.getText().trim());

                if (name.isEmpty() || tc <= 0 || score10 < 0 || score10 > 10)
                    throw new Exception();

                int newId = -model.getRowCount() - 1;

                model.addRow(new Object[]{
                        newId,
                        name,
                        tc,
                        score10,
                        "",
                        ""
                });

                dialog.dispose();
                table.repaint();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Dữ liệu không hợp lệ!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    /* =======================
       CALCULATE GPA
    ======================= */
    private void calculateGPA() {
        if (currentJsonFile == null && model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn file JSON trước!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        double totalPoints = 0;
        int totalCredits = 0;
        int a=0,b=0,c=0,d=0,f=0;

        for (int i = 0; i < model.getRowCount(); i++) {
            Object s10 = model.getValueAt(i, 3);
            if (s10 == null || s10.toString().isEmpty()) continue;

            double score10 = Double.parseDouble(s10.toString());
            int tc = Integer.parseInt(model.getValueAt(i, 2).toString());

            double score4 = score10 / 10 * 4;
            String grade = convert10ToLetter(score10);

            model.setValueAt(String.format("%.2f", score4), i, 4);
            model.setValueAt(grade, i, 5);

            switch (grade) {
                case "A": a++; break;
                case "B": b++; break;
                case "C": c++; break;
                case "D": d++; break;
                case "F": f++; break;
            }

            totalPoints += score4 * tc;
            totalCredits += tc;
        }

        if (totalCredits > 0) {
            double newGPA = totalPoints / totalCredits;
            if (oldGPA < 0) oldGPA = newGPA;

            lblOldGPA.setText(String.format("GPA ban đầu: %.2f", oldGPA));
            lblNewGPA.setText(String.format("GPA hiện tại: %.2f", newGPA));
            lblDiff.setText(String.format("Chênh lệch: %.2f", newGPA - oldGPA));
        }

        lblA.setText("A: " + a);
        lblB.setText("B: " + b);
        lblC.setText("C: " + c);
        lblD.setText("D: " + d);
        lblF.setText("F: " + f);

        table.repaint();
    }

    /* =======================
       CONVERT 10 -> LETTER
    ======================= */
    private String convert10ToLetter(double s) {
        if (s >= 8.5) return "A";
        if (s >= 7.0) return "B";
        if (s >= 5.5) return "C";
        if (s >= 4.0) return "D";
        return "F";
    }

    /* =======================
       RENDERERS
    ======================= */
    class RowHighlightRenderer extends DefaultTableCellRenderer {
        private final Color editedColor = new Color(255,245,200);

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (!isSelected)
                c.setBackground(editedRows.contains(row) ? editedColor : Color.WHITE);
            c.setForeground(Color.BLACK);
            return c;
        }
    }

    class GradeRenderer extends RowHighlightRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            setHorizontalAlignment(CENTER);
            setFont(c.getFont().deriveFont(Font.BOLD));

            if (value == null) return c;

            switch (value.toString()) {
                case "A": c.setForeground(Color.GREEN.darker()); break;
                case "B": c.setForeground(Color.BLUE); break;
                case "C": c.setForeground(Color.GRAY); break;
                case "D": c.setForeground(Color.ORANGE); break;
                case "F": c.setForeground(Color.RED); break;
            }
            return c;
        }
    }

    /* =======================
       MAIN
    ======================= */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new MainGUI().setVisible(true));
    }
}
