/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.belajar.foodcourtapp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class TambahPesananFrame extends JFrame {
    private JComboBox<String> cbTenant, cbMenu;
    private JSpinner spJumlah;
    private JButton btnTambah, btnSimpan;
    private JTextArea taPesanan;
    private Vector<Object[]> pesananSementara = new Vector<>(); // menu_id, nama, harga, jumlah
    private Connection conn;

    private AdminDashboardFrame.OrderPanel parent;

    public TambahPesananFrame(AdminDashboardFrame.OrderPanel parent) {
        this.parent = parent;
        setTitle("Pesanan Baru");
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

        // Load tenant
        try {
            conn = DatabaseConnection.getConnection();
            loadTenantCombo();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error koneksi: " + e.getMessage());
        }

        cbTenant.addActionListener(e -> loadMenuCombo());
        btnTambah.addActionListener(e -> tambahItem());
        btnSimpan.addActionListener(e -> simpanPesanan());

        // Pastikan load pertama
        loadMenuCombo();
    }

    private void loadTenantCombo() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id, nama FROM tenant");
        cbTenant.removeAllItems();
        while (rs.next()) {
            cbTenant.addItem(rs.getString("nama") + "|" + rs.getString("id"));
        }
    }

    private void loadMenuCombo() {
        try {
            String tenantId = getSelectedTenantId();
            if (tenantId.isEmpty()) return;
            PreparedStatement ps = conn.prepareStatement("SELECT id, nama FROM menu WHERE tenant_id=?");
            ps.setString(1, tenantId);
            ResultSet rs = ps.executeQuery();
            cbMenu.removeAllItems();
            while (rs.next()) {
                cbMenu.addItem(rs.getString("nama") + "|" + rs.getString("id"));
            }
        } catch (SQLException e) {
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
        String menuNama = parts[0];
        int jumlah = (int) spJumlah.getValue();
        // cari harga
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT harga FROM menu WHERE id=?");
            ps.setString(1, menuId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int harga = rs.getInt("harga");
                pesananSementara.add(new Object[]{menuId, menuNama, harga, jumlah});
                updatePesananText();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updatePesananText() {
        StringBuilder sb = new StringBuilder();
        for (Object[] item : pesananSementara) {
            sb.append(item[1]).append(" (").append(item[3]).append("x) Rp").append((int)item[2] * (int)item[3]).append("\n");
        }
        taPesanan.setText(sb.toString());
    }

    private void simpanPesanan() {
        if (pesananSementara.isEmpty()) return;
        String tenantId = getSelectedTenantId();
        try {
            conn.setAutoCommit(false);
            PreparedStatement psPesanan = conn.prepareStatement("INSERT INTO pesanan (tenant_id, status) VALUES (?, 'pending')", Statement.RETURN_GENERATED_KEYS);
            psPesanan.setString(1, tenantId);
            psPesanan.executeUpdate();
            ResultSet keys = psPesanan.getGeneratedKeys();
            int pesananId = 0;
            if (keys.next()) pesananId = keys.getInt(1);

            PreparedStatement psDetail = conn.prepareStatement("INSERT INTO pesanan_detail (pesanan_id, menu_id, jumlah) VALUES (?,?,?)");
            for (Object[] item : pesananSementara) {
                psDetail.setInt(1, pesananId);
                psDetail.setString(2, (String) item[0]);
                psDetail.setInt(3, (int) item[3]);
                psDetail.executeUpdate();
            }
            conn.commit();
            JOptionPane.showMessageDialog(this, "Pesanan berhasil disimpan.");
            parent.loadData();
            dispose();
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }
}