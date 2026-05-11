package com.belajar.foodcourtapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import org.json.JSONObject;

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
        cbRole = new JComboBox<>(new String[]{"Super Admin", "Tenant", "Customer"});
        add(cbRole);

        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(this::login);
        add(btnLogin);
    }

    private void login(ActionEvent e) {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword());
        String selectedRole = (String) cbRole.getSelectedItem();

        String firebaseRole;
        switch (selectedRole) {
            case "Super Admin": firebaseRole = "super_admin"; break;
            case "Tenant":      firebaseRole = "tenant";      break;
            case "Customer":    firebaseRole = "customer";    break;
            default:            firebaseRole = "";
        }

        try {
            String jsonStr = FirebaseDB.get("akun.json");
            JSONObject akunObj = new JSONObject(jsonStr);

            for (String key : akunObj.keySet()) {
                JSONObject akun = akunObj.getJSONObject(key);
                String uname = akun.optString("username", "");
                String pass = akun.optString("pass", "");
                String r = akun.optString("role", "");

                if (uname.equals(username) && pass.equals(password) && r.equals(firebaseRole)) {
                    if (firebaseRole.equals("super_admin")) {
                        new AdminDashboardFrame("super_admin", null).setVisible(true);
                    } else if (firebaseRole.equals("tenant")) {
                        String tenantId = akun.optString("tenantId", "");
                        new AdminDashboardFrame("tenant", tenantId).setVisible(true);
                    } else if (firebaseRole.equals("customer")) {
                        new CustomerFrame(key).setVisible(true);  // key = A0001, dsb
                    }
                    dispose();
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Login gagal.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Koneksi gagal: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}