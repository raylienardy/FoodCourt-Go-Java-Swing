package com.belajar.foodcourtapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private TenantPanel tenantPanel;
    private MenuPanel menuPanel;
    private OrderPanel orderPanel;

    public AdminDashboardFrame() {
        setTitle("FoodCourt Go - Admin Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        tenantPanel = new TenantPanel();
        menuPanel = new MenuPanel();
        orderPanel = new OrderPanel();

        tabbedPane.addTab("Tenant", tenantPanel);
        tabbedPane.addTab("Menu", menuPanel);
        tabbedPane.addTab("Pesanan", orderPanel);

        add(tabbedPane);
    }

    // ==================== Tenant Panel ====================
    class TenantPanel extends JPanel {
        private JTable table;
        private DefaultTableModel tableModel;
        private JTextField tfSearch, tfId, tfNama, tfKategori, tfDeskripsi;
        private JButton btnTambah, btnEdit, btnHapus, btnRefresh, btnClear;

        public TenantPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // -- Form Panel --
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createTitledBorder("Form Tenant"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Baris 0: ID
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("ID:"), gbc);
            gbc.gridx = 1;
            tfId = new JTextField(10);
            formPanel.add(tfId, gbc);
            // Baris 0: Search
            gbc.gridx = 2;
            formPanel.add(new JLabel("Cari:"), gbc);
            gbc.gridx = 3;
            tfSearch = new JTextField(12);
            formPanel.add(tfSearch, gbc);
            gbc.gridx = 4;
            JButton btnSearch = new JButton("Cari");
            formPanel.add(btnSearch, gbc);

            // Baris 1: Nama
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Nama:"), gbc);
            gbc.gridx = 1;
            tfNama = new JTextField(15);
            formPanel.add(tfNama, gbc);

            // Baris 2: Kategori
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Kategori:"), gbc);
            gbc.gridx = 1;
            tfKategori = new JTextField(15);
            formPanel.add(tfKategori, gbc);

            // Baris 3: Deskripsi
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Deskripsi:"), gbc);
            gbc.gridx = 1;
            gbc.gridwidth = 3; // Lebar 3 kolom
            tfDeskripsi = new JTextField(20);
            formPanel.add(tfDeskripsi, gbc);

            // Baris 4: Tombol aksi
            gbc.gridy = 4;
            gbc.gridx = 0;
            gbc.gridwidth = 5;
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            btnTambah = new JButton("Tambah");
            btnEdit = new JButton("Edit");
            btnHapus = new JButton("Hapus");
            btnRefresh = new JButton("Refresh");
            btnClear = new JButton("Bersihkan");
            btnPanel.add(btnTambah);
            btnPanel.add(btnEdit);
            btnPanel.add(btnHapus);
            btnPanel.add(btnRefresh);
            btnPanel.add(btnClear);
            formPanel.add(btnPanel, gbc);

            add(formPanel, BorderLayout.NORTH);

            // -- Table --
            tableModel = new DefaultTableModel(new String[]{"ID", "Nama", "Kategori", "Deskripsi"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            table = new JTable(tableModel);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            add(scrollPane, BorderLayout.CENTER);

            loadData("");

            // Event listeners
            btnSearch.addActionListener(e -> loadData(tfSearch.getText()));
            btnRefresh.addActionListener(e -> loadData(""));
            btnClear.addActionListener(e -> clearForm());

            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        tfId.setText((String) tableModel.getValueAt(row, 0));
                        tfNama.setText((String) tableModel.getValueAt(row, 1));
                        tfKategori.setText((String) tableModel.getValueAt(row, 2));
                        tfDeskripsi.setText((String) tableModel.getValueAt(row, 3));
                    }
                }
            });

            btnTambah.addActionListener(e -> {
                String id = tfId.getText().trim();
                String nama = tfNama.getText().trim();
                String kategori = tfKategori.getText().trim();
                String deskripsi = tfDeskripsi.getText().trim();
                if (id.isEmpty() || nama.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "ID dan Nama wajib diisi.");
                    return;
                }
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement("INSERT INTO tenant (id, nama, kategori, deskripsi) VALUES (?,?,?,?)")) {
                    ps.setString(1, id);
                    ps.setString(2, nama);
                    ps.setString(3, kategori);
                    ps.setString(4, deskripsi);
                    ps.executeUpdate();
                    loadData("");
                    clearForm();
                } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
            });

            btnEdit.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(this, "Pilih tenant yang akan diedit.");
                    return;
                }
                String id = (String) tableModel.getValueAt(row, 0);
                String nama = tfNama.getText().trim();
                if (nama.isEmpty()) return;
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement("UPDATE tenant SET nama=?, kategori=?, deskripsi=? WHERE id=?")) {
                    ps.setString(1, nama);
                    ps.setString(2, tfKategori.getText().trim());
                    ps.setString(3, tfDeskripsi.getText().trim());
                    ps.setString(4, id);
                    ps.executeUpdate();
                    loadData("");
                    clearForm();
                } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
            });

            btnHapus.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(this, "Pilih tenant yang akan dihapus.");
                    return;
                }
                String id = (String) tableModel.getValueAt(row, 0);
                if (JOptionPane.showConfirmDialog(this, "Yakin hapus tenant " + id + "?") == JOptionPane.YES_OPTION) {
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement ps = conn.prepareStatement("DELETE FROM tenant WHERE id=?")) {
                        ps.setString(1, id);
                        ps.executeUpdate();
                        loadData("");
                        clearForm();
                    } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
                }
            });
        }

        private void loadData(String keyword) {
            tableModel.setRowCount(0);
            String sql = "SELECT id, nama, kategori, deskripsi FROM tenant";
            if (!keyword.isEmpty()) sql += " WHERE nama LIKE ? OR kategori LIKE ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                if (!keyword.isEmpty()) {
                    String like = "%" + keyword + "%";
                    ps.setString(1, like);
                    ps.setString(2, like);
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    tableModel.addRow(new Object[]{rs.getString("id"), rs.getString("nama"), rs.getString("kategori"), rs.getString("deskripsi")});
                }
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }

        private void clearForm() {
            tfId.setText("");
            tfNama.setText("");
            tfKategori.setText("");
            tfDeskripsi.setText("");
            table.clearSelection();
            tfId.requestFocus();
        }
    }

    // ==================== Menu Panel ====================
    class MenuPanel extends JPanel {
        private JTable table;
        private DefaultTableModel tableModel;
        private JComboBox<String> cbTenant;
        private JTextField tfSearch, tfId, tfNama, tfHarga, tfDeskripsi;
        private JButton btnTambah, btnEdit, btnHapus, btnRefresh, btnClear;
        private List<String> tenantIds = new ArrayList<>();

        public MenuPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Form panel
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createTitledBorder("Form Menu"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Baris 0: Tenant, Search
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Tenant:"), gbc);
            gbc.gridx = 1;
            cbTenant = new JComboBox<>();
            formPanel.add(cbTenant, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("Cari:"), gbc);
            gbc.gridx = 3;
            tfSearch = new JTextField(10);
            formPanel.add(tfSearch, gbc);
            gbc.gridx = 4;
            JButton btnSearch = new JButton("Cari");
            formPanel.add(btnSearch, gbc);

            // Baris 1: ID, Nama
            gbc.gridy = 1;
            gbc.gridx = 0;
            formPanel.add(new JLabel("ID:"), gbc);
            gbc.gridx = 1;
            tfId = new JTextField(8);
            formPanel.add(tfId, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("Nama:"), gbc);
            gbc.gridx = 3;
            gbc.gridwidth = 2;
            tfNama = new JTextField(15);
            formPanel.add(tfNama, gbc);
            gbc.gridwidth = 1;

            // Baris 2: Harga, Deskripsi
            gbc.gridy = 2;
            gbc.gridx = 0;
            formPanel.add(new JLabel("Harga:"), gbc);
            gbc.gridx = 1;
            tfHarga = new JTextField(8);
            formPanel.add(tfHarga, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("Deskripsi:"), gbc);
            gbc.gridx = 3;
            gbc.gridwidth = 2;
            tfDeskripsi = new JTextField(15);
            formPanel.add(tfDeskripsi, gbc);
            gbc.gridwidth = 1;

            // Baris 3: Tombol
            gbc.gridy = 3;
            gbc.gridx = 0;
            gbc.gridwidth = 5;
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            btnTambah = new JButton("Tambah");
            btnEdit = new JButton("Edit");
            btnHapus = new JButton("Hapus");
            btnRefresh = new JButton("Refresh");
            btnClear = new JButton("Bersihkan");
            btnPanel.add(btnTambah);
            btnPanel.add(btnEdit);
            btnPanel.add(btnHapus);
            btnPanel.add(btnRefresh);
            btnPanel.add(btnClear);
            formPanel.add(btnPanel, gbc);

            add(formPanel, BorderLayout.NORTH);

            // Table
            tableModel = new DefaultTableModel(new String[]{"ID", "Tenant", "Nama", "Harga", "Deskripsi"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            table = new JTable(tableModel);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.CENTER);

            // Load data combo + table
            loadTenantCombo();
            if (cbTenant.getItemCount() > 0) cbTenant.setSelectedIndex(0);
            loadData("");

            // Listeners
            btnSearch.addActionListener(e -> loadData(tfSearch.getText()));
            btnRefresh.addActionListener(e -> loadData(""));
            btnClear.addActionListener(e -> clearForm());
            cbTenant.addActionListener(e -> loadData(""));

            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        tfId.setText((String) tableModel.getValueAt(row, 0));
                        tfNama.setText((String) tableModel.getValueAt(row, 2));
                        tfHarga.setText(String.valueOf(tableModel.getValueAt(row, 3)));
                        tfDeskripsi.setText((String) tableModel.getValueAt(row, 4));
                        // set combo tenant sesuai
                        String tenantName = (String) tableModel.getValueAt(row, 1);
                        for (int i = 0; i < cbTenant.getItemCount(); i++) {
                            if (cbTenant.getItemAt(i).startsWith(tenantName)) {
                                cbTenant.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                }
            });

            btnTambah.addActionListener(e -> tambahMenu());
            btnEdit.addActionListener(e -> editMenu());
            btnHapus.addActionListener(e -> hapusMenu());
        }

        private void loadTenantCombo() {
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, nama FROM tenant")) {
                cbTenant.removeAllItems();
                tenantIds.clear();
                while (rs.next()) {
                    String item = rs.getString("nama") + " | " + rs.getString("id");
                    cbTenant.addItem(item);
                    tenantIds.add(rs.getString("id"));
                }
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }

        private String getSelectedTenantId() {
            int idx = cbTenant.getSelectedIndex();
            return (idx >= 0 && idx < tenantIds.size()) ? tenantIds.get(idx) : "";
        }

        private void loadData(String keyword) {
            tableModel.setRowCount(0);
            String selectedTenantId = getSelectedTenantId();
            String sql = "SELECT m.id, t.nama AS tenant, m.nama, m.harga, m.deskripsi " +
                         "FROM menu m JOIN tenant t ON m.tenant_id = t.id";
            boolean hasWhere = false;
            if (!selectedTenantId.isEmpty()) {
                sql += " WHERE m.tenant_id = ?";
                hasWhere = true;
            }
            if (!keyword.isEmpty()) {
                sql += (hasWhere ? " AND" : " WHERE") + " m.nama LIKE ?";
            }
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = 1;
                if (!selectedTenantId.isEmpty()) ps.setString(idx++, selectedTenantId);
                if (!keyword.isEmpty()) ps.setString(idx++, "%" + keyword + "%");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    tableModel.addRow(new Object[]{rs.getString("id"), rs.getString("tenant"), rs.getString("nama"), rs.getInt("harga"), rs.getString("deskripsi")});
                }
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }

        private void clearForm() {
            tfId.setText("");
            tfNama.setText("");
            tfHarga.setText("");
            tfDeskripsi.setText("");
            table.clearSelection();
            tfId.requestFocus();
        }

        private void tambahMenu() {
            String id = tfId.getText().trim();
            String nama = tfNama.getText().trim();
            String hargaStr = tfHarga.getText().trim();
            String deskripsi = tfDeskripsi.getText().trim();
            String tenantId = getSelectedTenantId();
            if (tenantId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Pilih tenant dulu.");
                return;
            }
            if (id.isEmpty() || nama.isEmpty() || hargaStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID, Nama, dan Harga wajib diisi.");
                return;
            }
            int harga;
            try { harga = Integer.parseInt(hargaStr); }
            catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Harga harus angka.");
                return;
            }
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO menu (id, tenant_id, nama, harga, deskripsi) VALUES (?,?,?,?,?)")) {
                ps.setString(1, id);
                ps.setString(2, tenantId);
                ps.setString(3, nama);
                ps.setInt(4, harga);
                ps.setString(5, deskripsi);
                ps.executeUpdate();
                loadData("");
                clearForm();
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }

        private void editMenu() {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih menu yang akan diedit.");
                return;
            }
            String id = (String) tableModel.getValueAt(row, 0);
            String nama = tfNama.getText().trim();
            String hargaStr = tfHarga.getText().trim();
            if (nama.isEmpty() || hargaStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama dan Harga wajib diisi.");
                return;
            }
            int harga;
            try { harga = Integer.parseInt(hargaStr); }
            catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Harga harus angka.");
                return;
            }
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE menu SET nama=?, harga=?, deskripsi=? WHERE id=?")) {
                ps.setString(1, nama);
                ps.setInt(2, harga);
                ps.setString(3, tfDeskripsi.getText().trim());
                ps.setString(4, id);
                ps.executeUpdate();
                loadData("");
                clearForm();
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }

        private void hapusMenu() {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih menu yang akan dihapus.");
                return;
            }
            String id = (String) tableModel.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Yakin hapus menu " + id + "?") == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM menu WHERE id=?")) {
                    ps.setString(1, id);
                    ps.executeUpdate();
                    loadData("");
                    clearForm();
                } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
            }
        }
    }

    // ==================== Order Panel (tetap sederhana) ====================
    class OrderPanel extends JPanel {
        private JTable table;
        private DefaultTableModel tableModel;

        public OrderPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            tableModel = new DefaultTableModel(new String[]{"ID", "Tenant", "Waktu", "Status"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.CENTER);

            JButton btnTambah = new JButton("Tambah Pesanan");
            btnTambah.addActionListener(e -> new TambahPesananFrame(this).setVisible(true));
            JPanel btnPanel = new JPanel();
            btnPanel.add(btnTambah);
            add(btnPanel, BorderLayout.SOUTH);

            loadData();
        }

        void loadData() {
            tableModel.setRowCount(0);
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT p.id, t.nama AS tenant, p.waktu, p.status FROM pesanan p JOIN tenant t ON p.tenant_id = t.id ORDER BY p.waktu DESC")) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("tenant"), rs.getTimestamp("waktu"), rs.getString("status")});
                }
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }
}