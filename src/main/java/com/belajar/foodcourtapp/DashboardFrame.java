package com.belajar.foodcourtapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class DashboardFrame extends JFrame {
    private JTable tableTenant;
    private DefaultTableModel tableModel;

    public DashboardFrame() {
        setTitle("Dashboard Tenant");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"ID", "Nama", "Kategori"}, 0);
        tableTenant = new JTable(tableModel);
        loadTenantData();

        JButton btnTambahTenant = new JButton("Tambah Tenant");
        JButton btnPilihTenant = new JButton("Pilih Tenant");
        JButton btnRefresh = new JButton("Refresh");

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(tableTenant), BorderLayout.CENTER);

        JPanel panelBtn = new JPanel();
        panelBtn.add(btnTambahTenant);
        panelBtn.add(btnPilihTenant);
        panelBtn.add(btnRefresh);
        panel.add(panelBtn, BorderLayout.SOUTH);

        btnTambahTenant.addActionListener(e -> {
            new TambahTenantFrame(this).setVisible(true);
        });

        btnPilihTenant.addActionListener(e -> {
            int row = tableTenant.getSelectedRow();
            if (row != -1) {
                String tenantId = (String) tableModel.getValueAt(row, 0);
                String tenantNama = (String) tableModel.getValueAt(row, 1);
                new MenuFrame(tenantId, tenantNama).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Pilih tenant dulu.");
            }
        });

        btnRefresh.addActionListener(e -> loadTenantData());

        add(panel);
    }

    public void loadTenantData() {  // akses public agar bisa diakses dari TambahTenantFrame
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nama, kategori FROM tenant")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getString("id"), rs.getString("nama"), rs.getString("kategori")});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}