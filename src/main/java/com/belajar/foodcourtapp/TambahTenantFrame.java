/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.belajar.foodcourtapp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class TambahTenantFrame extends JFrame {
    private JTextField tfId, tfNama, tfKategori, tfDeskripsi;
    private DashboardFrame parent;

    public TambahTenantFrame(DashboardFrame parent) {
        this.parent = parent;
        setTitle("Tambah Tenant");
        setSize(350, 250);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(5, 2));

        add(new JLabel("ID:"));
        tfId = new JTextField();
        add(tfId);

        add(new JLabel("Nama:"));
        tfNama = new JTextField();
        add(tfNama);

        add(new JLabel("Kategori:"));
        tfKategori = new JTextField();
        add(tfKategori);

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
        String kategori = tfKategori.getText().trim();
        String deskripsi = tfDeskripsi.getText().trim();

        if (id.isEmpty() || nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID dan nama harus diisi.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO tenant (id, nama, kategori, deskripsi) VALUES (?,?,?,?)")) {
            ps.setString(1, id);
            ps.setString(2, nama);
            ps.setString(3, kategori);
            ps.setString(4, deskripsi);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Tenant berhasil ditambahkan.");
            parent.loadTenantData();
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}