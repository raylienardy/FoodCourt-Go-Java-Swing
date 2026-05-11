package com.belajar.foodcourtapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class CustomerFrame extends JFrame {
    private String customerId; // misal "A0001"
    private JTable tablePesanan;
    private DefaultTableModel tableModel;
    private JButton btnPesanBaru;

    public CustomerFrame(String customerId) {
        this.customerId = customerId;
        setTitle("Customer - " + customerId);
        setSize(700, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"ID Pesanan", "Waktu", "Status", "Total"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tablePesanan = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tablePesanan);

        btnPesanBaru = new JButton("Buat Pesanan Baru");
        btnPesanBaru.addActionListener(e -> {
            new CustomerOrderFrame(customerId, this).setVisible(true);
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnPesanBaru);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadMyOrders();
    }

    public void loadMyOrders() {
        tableModel.setRowCount(0);
        try {
            String jsonStr = FirebaseDB.get("pesanan.json");
            JSONObject pesananObj = new JSONObject(jsonStr);
            for (String key : pesananObj.keySet()) {
                JSONObject p = pesananObj.getJSONObject(key);
                if (p.optString("customerId", "").equals(customerId)) {
                    tableModel.addRow(new Object[]{
                        key,
                        p.optString("waktu", ""),
                        p.optString("status", ""),
                        p.optInt("totalHarga", 0)
                    });
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat pesanan: " + e.getMessage());
        }
    }

    // ========== Inner class: frame untuk membuat pesanan baru ==========
    class CustomerOrderFrame extends JFrame {
        private String custId;
        private CustomerFrame parent;
        private JComboBox<String> cbTenant, cbMenu;
        private JSpinner spJumlah;
        private JButton btnTambah, btnSimpan;
        private JTextArea taPesanan;
        private Vector<Object[]> pesananSementara = new Vector<>();
        private Map<String, JSONObject> menuDataMap = new HashMap<>();

        public CustomerOrderFrame(String custId, CustomerFrame parent) {
            this.custId = custId;
            this.parent = parent;
            setTitle("Pesanan Baru - " + custId);
            setSize(400, 400);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());

            JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
            inputPanel.add(new JLabel("Tenant:"));
            cbTenant = new JComboBox<>();
            inputPanel.add(cbTenant);
            inputPanel.add(new JLabel("Menu:"));
            cbMenu = new JComboBox<>();
            inputPanel.add(cbMenu);
            inputPanel.add(new JLabel("Jumlah:"));
            spJumlah = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
            inputPanel.add(spJumlah);
            btnTambah = new JButton("Tambahkan");
            inputPanel.add(btnTambah);

            taPesanan = new JTextArea(10, 30);
            taPesanan.setEditable(false);
            JScrollPane scroll = new JScrollPane(taPesanan);

            btnSimpan = new JButton("Simpan Pesanan");
            JPanel southPanel = new JPanel();
            southPanel.add(btnSimpan);

            add(inputPanel, BorderLayout.NORTH);
            add(scroll, BorderLayout.CENTER);
            add(southPanel, BorderLayout.SOUTH);

            try {
                loadTenantCombo();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }

            cbTenant.addActionListener(e -> loadMenuCombo());
            btnTambah.addActionListener(e -> tambahItem());
            btnSimpan.addActionListener(e -> simpanPesanan());

            loadMenuCombo();
        }

        private void loadTenantCombo() throws IOException {
            String jsonStr = FirebaseDB.get("tenant.json");
            JSONObject tenants = new JSONObject(jsonStr);
            cbTenant.removeAllItems();
            for (String id : tenants.keySet()) {
                JSONObject t = tenants.getJSONObject(id);
                String nama = t.optString("nama", "");
                cbTenant.addItem(nama + "|" + id);
            }
        }

        private void loadMenuCombo() {
            cbMenu.removeAllItems();
            menuDataMap.clear();
            String tenantId = getSelectedTenantId();
            if (tenantId.isEmpty()) return;
            try {
                String jsonStr = FirebaseDB.get("menu.json");
                JSONObject menus = new JSONObject(jsonStr);
                for (String id : menus.keySet()) {
                    JSONObject m = menus.getJSONObject(id);
                    if (m.optString("tenantId", "").equals(tenantId)) {
                        String nama = m.optString("nama", "");
                        cbMenu.addItem(nama + "|" + id);
                        menuDataMap.put(id, m);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String getSelectedTenantId() {
            String item = (String) cbTenant.getSelectedItem();
            if (item == null) return "";
            return item.split("\\|")[1];
        }

        private void tambahItem() {
            String menuItem = (String) cbMenu.getSelectedItem();
            if (menuItem == null) return;
            String[] parts = menuItem.split("\\|");
            String menuId = parts[1];
            JSONObject menu = menuDataMap.get(menuId);
            if (menu == null) return;
            String namaMenu = menu.optString("nama", "");
            int harga = menu.optInt("harga", 0);
            int jumlah = (int) spJumlah.getValue();
            pesananSementara.add(new Object[]{menuId, namaMenu, harga, jumlah});
            updatePesananText();
        }

        private void updatePesananText() {
            StringBuilder sb = new StringBuilder();
            for (Object[] item : pesananSementara) {
                sb.append(item[1]).append(" (").append(item[3]).append("x) Rp")
                  .append((int)item[2] * (int)item[3]).append("\n");
            }
            taPesanan.setText(sb.toString());
        }

        private void simpanPesanan() {
            if (pesananSementara.isEmpty()) return;
            String tenantId = getSelectedTenantId();
            try {
                String pesananId = "P" + System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
                String waktu = sdf.format(new Date());

                JSONObject pesanan = new JSONObject();
                pesanan.put("id", pesananId);
                pesanan.put("customerId", custId);
                pesanan.put("tenantId", tenantId);
                pesanan.put("meja", "A-01");  // bisa ditambah input meja
                pesanan.put("status", "pending");
                pesanan.put("waktu", waktu);
                pesanan.put("totalHarga", 0);

                JSONArray items = new JSONArray();
                int total = 0;
                for (Object[] item : pesananSementara) {
                    String menuId = (String) item[0];
                    String nama = (String) item[1];
                    int harga = (int) item[2];
                    int qty = (int) item[3];
                    total += harga * qty;
                    JSONObject it = new JSONObject();
                    it.put("menuId", menuId);
                    it.put("nama", nama);
                    it.put("qty", qty);
                    it.put("harga", harga);
                    it.put("opsi", "");
                    it.put("hargaTambahan", 0);
                    items.put(it);
                }
                pesanan.put("totalHarga", total);
                pesanan.put("items", items);

                FirebaseDB.put("pesanan/" + pesananId + ".json", pesanan.toString());
                JOptionPane.showMessageDialog(this, "Pesanan berhasil disimpan.");
                parent.loadMyOrders();
                dispose();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
}