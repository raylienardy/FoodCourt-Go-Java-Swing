/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.belajar.foodcourtapp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class TambahMenuFrame extends JFrame {
    private JTextField tfId, tfNama, tfHarga, tfDeskripsi;
    private MenuFrame parent;

    public TambahMenuFrame(MenuFrame parent) {
        this.parent = parent;
        setTitle("Tambah Menu");
        setSize(350, 250);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(5, 2));

        add(new JLabel("ID:"));
        tfId = new JTextField();
        add(tfId);

        add(new JLabel("Nama:"));
        tfNama = new JTextField();
        add(tfNama);

        add(new JLabel("Harga:"));
        tfHarga = new JTextField();
        add(tfHarga);

        add(new JLabel("Deskripsi:"));
        tfDeskripsi = new JTextField();
        add(tfDeskripsi);

        JButton btnSimpan = new JButton("Simpan");
        btnSimpan.addActionListener(e -> simpan());
        add(btnSimpan);
    }

    private void simpan() {
        String id = tfId.getText().trim();
        String nama = tfNama.getText().trim();
        String hargaStr = tfHarga.getText().trim();
        String deskripsi = tfDeskripsi.getText().trim();

        if (id.isEmpty() || nama.isEmpty() || hargaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID, nama, dan harga wajib diisi.");
            return;
        }
        int harga = Integer.parseInt(hargaStr);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO menu (id, tenant_id, nama, harga, deskripsi) VALUES (?,?,?,?,?)")) {
            ps.setString(1, id);
            ps.setString(2, parent.tenantId);
            ps.setString(3, nama);
            ps.setInt(4, harga);
            ps.setString(5, deskripsi);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Menu berhasil ditambahkan.");
            parent.loadMenuData();
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
