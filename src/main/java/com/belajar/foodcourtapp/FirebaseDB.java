package com.belajar.foodcourtapp;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class FirebaseDB {
    // Ganti dengan URL Firebase Anda
    private static final String BASE_URL = "https://foodcourtgo-default-rtdb.firebaseio.com/";

    /**
     * GET data dari path (misal: "tenant.json")
     */
    public static String get(String path) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        int code = conn.getResponseCode();
        if (code == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                return sb.toString();
            }
        } else {
            throw new IOException("GET gagal, kode: " + code);
        }
    }

    /**
     * PUT (overwrite) data ke path. Gunakan .json di akhir.
     */
    public static void put(String path, String jsonData) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonData.getBytes(StandardCharsets.UTF_8));
        }
        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("PUT gagal, kode: " + code);
        }
    }

    /**
     * PATCH (partial update)
     */
    public static void patch(String path, String jsonData) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PATCH");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonData.getBytes(StandardCharsets.UTF_8));
        }
        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("PATCH gagal, kode: " + code);
        }
    }

    /**
     * DELETE
     */
    public static void delete(String path) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("DELETE gagal, kode: " + code);
        }
    }
}