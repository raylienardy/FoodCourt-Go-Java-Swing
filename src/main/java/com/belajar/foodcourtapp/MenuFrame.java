/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.belajar.foodcourtapp;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MenuFrame extends JFrame {
    public String tenantId;      // diubah jadi public
    public String tenantNama;    // diubah jadi public
    private JTable tableMenu;
    private DefaultTableModel tableModel;

    public MenuFrame(String tenantId, String tenantNama) {
        this.tenantId = tenantId;
        this.tenantNama = tenantNama;
        setTitle("Menu " + tenantNama);
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"ID", "Nama", "Harga"}, 0);
        tableMenu = new JTable(tableModel);
        loadMenuData();

        JButton btnTambahMenu = new JButton("Tambah Menu");
        JButton btnPesan = new JButton("Pesan");

        JPanel panelBtn = new JPanel();
        panelBtn.add(btnTambahMenu);
        panelBtn.add(btnPesan);

        add(new JScrollPane(tableMenu), BorderLayout.CENTER);
        add(panelBtn, BorderLayout.SOUTH);

        btnTambahMenu.addActionListener(e -> {
            new TambahMenuFrame(this).setVisible(true);
        });

        btnPesan.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Fitur pesan akan datang.");
        });
    }

    public void loadMenuData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, nama, harga FROM menu WHERE tenant_id = ?")) {
            ps.setString(1, tenantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getString("id"), rs.getString("nama"), rs.getInt("harga")});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
