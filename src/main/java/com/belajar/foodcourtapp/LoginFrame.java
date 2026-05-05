package com.belajar.foodcourtapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JComboBox<String> cbRole;

    public LoginFrame() {
        setTitle("FoodCourt Go - Login");
        setSize(350, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("Username:"));
        tfUsername = new JTextField();
        add(tfUsername);

        add(new JLabel("Password:"));
        pfPassword = new JPasswordField();
        add(pfPassword);

        add(new JLabel("Role:"));
        cbRole = new JComboBox<>(new String[]{"admin", "tenant"});
        add(cbRole);

        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(this::login);
        add(btnLogin);
    }

    private void login(ActionEvent e) {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword());
        String role = (String) cbRole.getSelectedItem();
        System.out.println("Login attempt: " + username + " / " + role);


        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=? AND role=?")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if ("admin".equals(role)) {
                    new AdminDashboardFrame().setVisible(true);
                } else {
                    // tenant login (asumsikan tenant punya tenant_id sesuai username)
                    new AdminDashboardFrame().setVisible(true); // sementara kita buka dashboard umum
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Login gagal.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}