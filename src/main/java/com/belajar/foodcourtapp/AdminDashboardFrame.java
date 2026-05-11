package com.belajar.foodcourtapp;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class AdminDashboardFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private TenantPanel tenantPanel;
    private MenuPanel menuPanel;
    private OrderPanel orderPanel;

    public static final String UPLOAD_TENANT_DIR = "uploads/tenant/";
    public static final String UPLOAD_MENU_DIR = "uploads/menu/";

    public AdminDashboardFrame() {
        setTitle("FoodCourt Go - Admin Dashboard");
        setSize(950, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        new File(UPLOAD_TENANT_DIR).mkdirs();
        new File(UPLOAD_MENU_DIR).mkdirs();

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
        private JLabel imagePreview;
        private JButton btnPilihGambar, btnHapusGambar;
        private File selectedImageFile;
        private boolean deletePhoto = false;
        private Map<String, JSONObject> tenantCache = new HashMap<>(); // id -> data

        public TenantPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel topPanel = new JPanel(new BorderLayout(10, 10));
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createTitledBorder("Form Tenant"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("ID:"), gbc);
            gbc.gridx = 1;
            tfId = new JTextField(10);
            formPanel.add(tfId, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("Cari:"), gbc);
            gbc.gridx = 3;
            tfSearch = new JTextField(12);
            formPanel.add(tfSearch, gbc);
            gbc.gridx = 4;
            JButton btnSearch = new JButton("Cari");
            formPanel.add(btnSearch, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Nama:"), gbc);
            gbc.gridx = 1;
            tfNama = new JTextField(15);
            formPanel.add(tfNama, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Kategori:"), gbc);
            gbc.gridx = 1;
            tfKategori = new JTextField(15);
            formPanel.add(tfKategori, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Deskripsi:"), gbc);
            gbc.gridx = 1;
            gbc.gridwidth = 3;
            tfDeskripsi = new JTextField(20);
            formPanel.add(tfDeskripsi, gbc);

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

            topPanel.add(formPanel, BorderLayout.CENTER);

            JPanel imagePanel = new JPanel(new BorderLayout());
            imagePanel.setBorder(BorderFactory.createTitledBorder("Foto Tenant"));
            imagePreview = new JLabel("", SwingConstants.CENTER);
            imagePreview.setPreferredSize(new Dimension(200, 200));
            imagePreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            imagePanel.add(imagePreview, BorderLayout.CENTER);

            JPanel btnImagePanel = new JPanel(new FlowLayout());
            btnPilihGambar = new JButton("Pilih Gambar");
            btnHapusGambar = new JButton("Hapus Gambar");
            btnImagePanel.add(btnPilihGambar);
            btnImagePanel.add(btnHapusGambar);
            imagePanel.add(btnImagePanel, BorderLayout.SOUTH);

            topPanel.add(imagePanel, BorderLayout.EAST);
            add(topPanel, BorderLayout.NORTH);

            tableModel = new DefaultTableModel(new String[]{"ID", "Nama", "Kategori", "Deskripsi"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            table = new JTable(tableModel);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.CENTER);

            loadData("");

            btnSearch.addActionListener(e -> loadData(tfSearch.getText()));
            btnRefresh.addActionListener(e -> loadData(""));
            btnClear.addActionListener(e -> clearForm());

            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        String id = (String) tableModel.getValueAt(row, 0);
                        JSONObject tenant = tenantCache.get(id);
                        if (tenant != null) {
                            tfId.setText(id);
                            tfNama.setText(tenant.optString("nama", ""));
                            tfKategori.setText(tenant.optString("kategori", ""));
                            tfDeskripsi.setText(tenant.optString("deskripsi", ""));
                            String gambar = tenant.optString("gambar", "");
                            loadPreviewImage(gambar, "restaurant");
                            selectedImageFile = null;
                            deletePhoto = false;
                        }
                    }
                }
            });

            btnPilihGambar.addActionListener(e -> pilihGambar());
            btnHapusGambar.addActionListener(e -> {
                selectedImageFile = null;
                deletePhoto = true;
                imagePreview.setIcon(null);
                imagePreview.setText("Foto akan dihapus");
            });

            btnTambah.addActionListener(e -> tambahTenant());
            btnEdit.addActionListener(e -> editTenant());
            btnHapus.addActionListener(e -> hapusTenant());
        }

        private void pilihGambar() {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Gambar (JPG, PNG)", "jpg", "jpeg", "png");
            chooser.setFileFilter(filter);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedImageFile = chooser.getSelectedFile();
                ImageIcon icon = new ImageIcon(new ImageIcon(selectedImageFile.getAbsolutePath())
                        .getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH));
                imagePreview.setIcon(icon);
                imagePreview.setText("");
                deletePhoto = false;
            }
        }

        private void loadPreviewImage(String urlStr, String unsplashKeyword) {
            imagePreview.setIcon(null);
            if (urlStr != null && !urlStr.isEmpty()) {
                // Cek apakah URL lengkap atau nama file lokal
                if (urlStr.startsWith("http")) {
                    try {
                        ImageIcon icon = new ImageIcon(new ImageIcon(new URL(urlStr))
                                .getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH));
                        imagePreview.setIcon(icon);
                    } catch (IOException ex) {}
                } else {
                    File f = new File(UPLOAD_TENANT_DIR + urlStr);
                    if (f.exists()) {
                        ImageIcon icon = new ImageIcon(new ImageIcon(f.getAbsolutePath())
                                .getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH));
                        imagePreview.setIcon(icon);
                    }
                }
            } else {
                // Fallback Unsplash
                try {
                    URL url = new URL("https://source.unsplash.com/300x300/?" + unsplashKeyword);
                    ImageIcon icon = new ImageIcon(new ImageIcon(url)
                            .getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH));
                    imagePreview.setIcon(icon);
                } catch (Exception e) {
                    imagePreview.setText("Tidak ada gambar");
                }
            }
        }

        private void loadData(String keyword) {
            tableModel.setRowCount(0);
            tenantCache.clear();
            try {
                String jsonStr = FirebaseDB.get("tenant.json");
                JSONObject tenants = new JSONObject(jsonStr);
                for (String id : tenants.keySet()) {
                    JSONObject t = tenants.getJSONObject(id);
                    if (!keyword.isEmpty()) {
                        String nama = t.optString("nama", "").toLowerCase();
                        String kat = t.optString("kategori", "").toLowerCase();
                        String kw = keyword.toLowerCase();
                        if (!nama.contains(kw) && !kat.contains(kw)) continue;
                    }
                    tableModel.addRow(new Object[]{
                        id,
                        t.optString("nama", ""),
                        t.optString("kategori", ""),
                        t.optString("deskripsi", "")
                    });
                    tenantCache.put(id, t);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Gagal memuat data: " + ex.getMessage());
            }
        }

        private void clearForm() {
            tfId.setText("");
            tfNama.setText("");
            tfKategori.setText("");
            tfDeskripsi.setText("");
            table.clearSelection();
            tfId.requestFocus();
            selectedImageFile = null;
            deletePhoto = false;
            imagePreview.setIcon(null);
            imagePreview.setText("");
        }

        private void tambahTenant() {
            String id = tfId.getText().trim();
            String nama = tfNama.getText().trim();
            String kategori = tfKategori.getText().trim();
            String deskripsi = tfDeskripsi.getText().trim();
            if (id.isEmpty() || nama.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID dan Nama wajib diisi.");
                return;
            }
            try {
                JSONObject tenant = new JSONObject();
                tenant.put("nama", nama);
                tenant.put("kategori", kategori);
                tenant.put("deskripsi", deskripsi);
                // Tangani gambar
                if (selectedImageFile != null) {
                    String namaFile = saveImage(selectedImageFile, UPLOAD_TENANT_DIR);
                    tenant.put("gambar", namaFile);
                } else {
                    tenant.put("gambar", "");
                }
                // Tambahan field default
                tenant.put("status", "active");
                tenant.put("email", "");
                tenant.put("telepon", "");
                tenant.put("lokasi", "");
                tenant.put("namaPemilik", "");

                FirebaseDB.put("tenant/" + id + ".json", tenant.toString());
                loadData("");
                clearForm();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        private void editTenant() {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih tenant yang akan diedit.");
                return;
            }
            String id = (String) tableModel.getValueAt(row, 0);
            String nama = tfNama.getText().trim();
            if (nama.isEmpty()) return;

            try {
                JSONObject update = new JSONObject();
                update.put("nama", nama);
                update.put("kategori", tfKategori.getText().trim());
                update.put("deskripsi", tfDeskripsi.getText().trim());

                // Gambar
                JSONObject oldTenant = tenantCache.get(id);
                String oldGambar = oldTenant != null ? oldTenant.optString("gambar", "") : "";
                if (selectedImageFile != null) {
                    // Hapus file lama jika bukan URL
                    if (!oldGambar.startsWith("http") && !oldGambar.isEmpty()) {
                        new File(UPLOAD_TENANT_DIR + oldGambar).delete();
                    }
                    String newFile = saveImage(selectedImageFile, UPLOAD_TENANT_DIR);
                    update.put("gambar", newFile);
                } else if (deletePhoto) {
                    if (!oldGambar.startsWith("http") && !oldGambar.isEmpty()) {
                        new File(UPLOAD_TENANT_DIR + oldGambar).delete();
                    }
                    update.put("gambar", "");
                } else {
                    update.put("gambar", oldGambar);
                }

                FirebaseDB.patch("tenant/" + id + ".json", update.toString());
                loadData("");
                clearForm();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        private void hapusTenant() {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih tenant yang akan dihapus.");
                return;
            }
            String id = (String) tableModel.getValueAt(row, 0);
            JSONObject t = tenantCache.get(id);
            if (JOptionPane.showConfirmDialog(this, "Yakin hapus tenant " + id + "?") == JOptionPane.YES_OPTION) {
                try {
                    if (t != null) {
                        String gambar = t.optString("gambar", "");
                        if (!gambar.startsWith("http") && !gambar.isEmpty()) {
                            new File(UPLOAD_TENANT_DIR + gambar).delete();
                        }
                    }
                    FirebaseDB.delete("tenant/" + id + ".json");
                    loadData("");
                    clearForm();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        }
    }

    // ==================== Menu Panel ====================
    class MenuPanel extends JPanel {
        private JTable table;
        private DefaultTableModel tableModel;
        private JComboBox<String> cbTenant;
        private JTextField tfSearch, tfId, tfNama, tfHarga, tfDeskripsi;
        private JButton btnTambah, btnEdit, btnHapus, btnRefresh, btnClear;
        private JLabel imagePreview;
        private JButton btnPilihGambar, btnHapusGambar;
        private File selectedImageFile;
        private boolean deletePhoto = false;
        private Map<String, JSONObject> menuCache = new HashMap<>();
        private Map<String, String> tenantNameMap = new HashMap<>(); // id -> nama

        public MenuPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel topPanel = new JPanel(new BorderLayout(10, 10));
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createTitledBorder("Form Menu"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

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

            gbc.gridy = 1; gbc.gridx = 0;
            formPanel.add(new JLabel("ID Menu:"), gbc);
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

            gbc.gridy = 2; gbc.gridx = 0;
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

            gbc.gridy = 3; gbc.gridx = 0;
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

            topPanel.add(formPanel, BorderLayout.CENTER);

            JPanel imagePanel = new JPanel(new BorderLayout());
            imagePanel.setBorder(BorderFactory.createTitledBorder("Foto Menu"));
            imagePreview = new JLabel("", SwingConstants.CENTER);
            imagePreview.setPreferredSize(new Dimension(200, 200));
            imagePreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            imagePanel.add(imagePreview, BorderLayout.CENTER);

            JPanel btnImagePanel = new JPanel(new FlowLayout());
            btnPilihGambar = new JButton("Pilih Gambar");
            btnHapusGambar = new JButton("Hapus Gambar");
            btnImagePanel.add(btnPilihGambar);
            btnImagePanel.add(btnHapusGambar);
            imagePanel.add(btnImagePanel, BorderLayout.SOUTH);

            topPanel.add(imagePanel, BorderLayout.EAST);
            add(topPanel, BorderLayout.NORTH);

            tableModel = new DefaultTableModel(new String[]{"ID", "Tenant", "Nama", "Harga", "Deskripsi"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            table = new JTable(tableModel);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.CENTER);

            loadTenantCombo();
            loadData("");

            btnSearch.addActionListener(e -> loadData(tfSearch.getText()));
            btnRefresh.addActionListener(e -> loadData(""));
            btnClear.addActionListener(e -> clearForm());
            cbTenant.addActionListener(e -> loadData(""));

            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        String id = (String) tableModel.getValueAt(row, 0);
                        JSONObject menu = menuCache.get(id);
                        if (menu != null) {
                            tfId.setText(id);
                            tfNama.setText(menu.optString("nama", ""));
                            tfHarga.setText(String.valueOf(menu.optInt("harga", 0)));
                            tfDeskripsi.setText(menu.optString("deskripsi", ""));
                            String gambar = menu.optString("gambar", "");
                            loadPreviewImage(gambar, "food");
                            selectedImageFile = null;
                            deletePhoto = false;

                            // Set tenant combo
                            String tenantId = menu.optString("tenantId", "");
                            for (int i = 0; i < cbTenant.getItemCount(); i++) {
                                if (cbTenant.getItemAt(i).endsWith("|" + tenantId)) {
                                    cbTenant.setSelectedIndex(i);
                                    break;
                                }
                            }
                        }
                    }
                }
            });

            btnPilihGambar.addActionListener(e -> pilihGambar());
            btnHapusGambar.addActionListener(e -> {
                selectedImageFile = null;
                deletePhoto = true;
                imagePreview.setIcon(null);
                imagePreview.setText("Foto akan dihapus");
            });

            btnTambah.addActionListener(e -> tambahMenu());
            btnEdit.addActionListener(e -> editMenu());
            btnHapus.addActionListener(e -> hapusMenu());
        }

        private void pilihGambar() {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Gambar (JPG, PNG)", "jpg", "jpeg", "png");
            chooser.setFileFilter(filter);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedImageFile = chooser.getSelectedFile();
                ImageIcon icon = new ImageIcon(new ImageIcon(selectedImageFile.getAbsolutePath())
                        .getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH));
                imagePreview.setIcon(icon);
                imagePreview.setText("");
                deletePhoto = false;
            }
        }

        private void loadPreviewImage(String urlStr, String unsplashKeyword) {
            imagePreview.setIcon(null);
            if (urlStr != null && !urlStr.isEmpty()) {
                if (urlStr.startsWith("http")) {
                    try {
                        ImageIcon icon = new ImageIcon(new ImageIcon(new URL(urlStr))
                                .getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH));
                        imagePreview.setIcon(icon);
                    } catch (IOException ex) {}
                } else {
                    File f = new File(UPLOAD_MENU_DIR + urlStr);
                    if (f.exists()) {
                        ImageIcon icon = new ImageIcon(new ImageIcon(f.getAbsolutePath())
                                .getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH));
                        imagePreview.setIcon(icon);
                    }
                }
            } else {
                try {
                    URL url = new URL("https://source.unsplash.com/300x300/?" + unsplashKeyword);
                    ImageIcon icon = new ImageIcon(new ImageIcon(url)
                            .getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH));
                    imagePreview.setIcon(icon);
                } catch (Exception e) {
                    imagePreview.setText("Tidak ada gambar");
                }
            }
        }

        private void loadTenantCombo() {
            tenantNameMap.clear();
            cbTenant.removeAllItems();
            try {
                String jsonStr = FirebaseDB.get("tenant.json");
                JSONObject tenants = new JSONObject(jsonStr);
                for (String id : tenants.keySet()) {
                    JSONObject t = tenants.getJSONObject(id);
                    String nama = t.optString("nama", "");
                    cbTenant.addItem(nama + "|" + id);
                    tenantNameMap.put(id, nama);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Gagal memuat tenant: " + ex.getMessage());
            }
        }

        private String getSelectedTenantId() {
            String item = (String) cbTenant.getSelectedItem();
            if (item == null) return "";
            return item.split("\\|")[1];
        }

        private void loadData(String keyword) {
            tableModel.setRowCount(0);
            menuCache.clear();
            String filterTenantId = getSelectedTenantId();
            try {
                String jsonStr = FirebaseDB.get("menu.json");
                JSONObject menus = new JSONObject(jsonStr);
                for (String id : menus.keySet()) {
                    JSONObject m = menus.getJSONObject(id);
                    String tenantId = m.optString("tenantId", "");
                    if (!filterTenantId.isEmpty() && !tenantId.equals(filterTenantId)) continue;
                    String namaMenu = m.optString("nama", "");
                    if (!keyword.isEmpty() && !namaMenu.toLowerCase().contains(keyword.toLowerCase())) continue;

                    String namaTenant = tenantNameMap.getOrDefault(tenantId, "?");
                    tableModel.addRow(new Object[]{
                        id,
                        namaTenant,
                        namaMenu,
                        m.optInt("harga", 0),
                        m.optString("deskripsi", "")
                    });
                    menuCache.put(id, m);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Gagal memuat menu: " + ex.getMessage());
            }
        }

        private void clearForm() {
            tfId.setText("");
            tfNama.setText("");
            tfHarga.setText("");
            tfDeskripsi.setText("");
            table.clearSelection();
            tfId.requestFocus();
            selectedImageFile = null;
            deletePhoto = false;
            imagePreview.setIcon(null);
            imagePreview.setText("");
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
            try {
                JSONObject menu = new JSONObject();
                menu.put("nama", nama);
                menu.put("deskripsi", deskripsi);
                menu.put("harga", harga);
                menu.put("tenantId", tenantId);
                menu.put("gambar", selectedImageFile != null ? saveImage(selectedImageFile, UPLOAD_MENU_DIR) : "");
                menu.put("tambahan", new JSONArray()); // kosong

                FirebaseDB.put("menu/" + id + ".json", menu.toString());
                loadData("");
                clearForm();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        private void editMenu() {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih menu yang akan diedit.");
                return;
            }
            String id = (String) tableModel.getValueAt(row, 0);
            JSONObject oldMenu = menuCache.get(id);
            if (oldMenu == null) return;
            try {
                JSONObject update = new JSONObject();
                update.put("nama", tfNama.getText().trim());
                update.put("harga", Integer.parseInt(tfHarga.getText().trim()));
                update.put("deskripsi", tfDeskripsi.getText().trim());

                String oldGambar = oldMenu.optString("gambar", "");
                if (selectedImageFile != null) {
                    if (!oldGambar.startsWith("http") && !oldGambar.isEmpty()) {
                        new File(UPLOAD_MENU_DIR + oldGambar).delete();
                    }
                    update.put("gambar", saveImage(selectedImageFile, UPLOAD_MENU_DIR));
                } else if (deletePhoto) {
                    if (!oldGambar.startsWith("http") && !oldGambar.isEmpty()) {
                        new File(UPLOAD_MENU_DIR + oldGambar).delete();
                    }
                    update.put("gambar", "");
                } else {
                    update.put("gambar", oldGambar);
                }

                FirebaseDB.patch("menu/" + id + ".json", update.toString());
                loadData("");
                clearForm();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        private void hapusMenu() {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih menu yang akan dihapus.");
                return;
            }
            String id = (String) tableModel.getValueAt(row, 0);
            JSONObject menu = menuCache.get(id);
            if (JOptionPane.showConfirmDialog(this, "Yakin hapus menu " + id + "?") == JOptionPane.YES_OPTION) {
                try {
                    if (menu != null) {
                        String gambar = menu.optString("gambar", "");
                        if (!gambar.startsWith("http") && !gambar.isEmpty()) {
                            new File(UPLOAD_MENU_DIR + gambar).delete();
                        }
                    }
                    FirebaseDB.delete("menu/" + id + ".json");
                    loadData("");
                    clearForm();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        }
    }

    // ==================== Order Panel ====================
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
            try {
                String jsonStr = FirebaseDB.get("pesanan.json");
                JSONObject pesanan = new JSONObject(jsonStr);
                for (String id : pesanan.keySet()) {
                    JSONObject p = pesanan.getJSONObject(id);
                    String tenantId = p.optString("tenantId", "");
                    String namaTenant = "";
                    // Ambil nama tenant dari cache? Bisa load ulang tenant.json
                    try {
                        JSONObject tObj = new JSONObject(FirebaseDB.get("tenant/" + tenantId + ".json"));
                        namaTenant = tObj.optString("nama", "");
                    } catch (IOException e) { namaTenant = tenantId; }
                    tableModel.addRow(new Object[]{
                        id,
                        namaTenant,
                        p.optString("waktu", ""),
                        p.optString("status", "")
                    });
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Gagal memuat pesanan: " + ex.getMessage());
            }
        }
    }

    // ------------------ Helper save image ------------------
    private String saveImage(File source, String targetDir) {
        try {
            String ext = source.getName().substring(source.getName().lastIndexOf('.'));
            String newName = System.currentTimeMillis() + "_" + (int)(Math.random() * 1000) + ext;
            File dest = new File(targetDir + newName);
            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return newName;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan gambar: " + e.getMessage());
            return null;
        }
    }
}