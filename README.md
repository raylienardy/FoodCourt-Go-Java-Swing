# 🍔 FoodCourt Go - Java Swing (GUI)

Aplikasi desktop sederhana untuk manajemen food court: mengelola tenant, menu, dan pemesanan. Dibangun menggunakan **Java Swing** untuk antarmuka grafis dan **MySQL** sebagai basis data. Proyek ini merupakan versi GUI dari aplikasi Android FoodCourt Go yang sebelumnya dikembangkan.

---

## ✨ Fitur

- **Login** dengan peran admin/tenant.
- **Manajemen Tenant** – tambah, ubah, hapus, cari tenant.
- **Manajemen Menu** – tambah, ubah, hapus menu per tenant.
- **Pemesanan** – buat pesanan baru, pilih tenant & menu, tentukan jumlah, simpan ke database.
- **Tampilan tab** untuk navigasi mudah.

---

## 🧰 Teknologi

| Teknologi | Keterangan |
|-----------|------------|
| Java      | JDK 21+ (Amazon Corretto disarankan) |
| Swing     | GUI toolkit bawaan Java |
| MySQL     | Database server |
| JDBC      | Koneksi Java ke MySQL |
| NetBeans  | IDE yang digunakan |

---

## 📋 Prasyarat

1. **Java Development Kit (JDK)** versi 21 atau lebih baru.
2. **MySQL Server** (XAMPP, Laragon, atau stand‑alone).
3. **NetBeans IDE** (direkomendasikan) atau IDE lain yang mendukung Maven.
4. **MySQL Connector/J** (driver JDBC) – sudah termasuk jika menggunakan NetBeans dengan Maven.

---

## 🗄️ Persiapan Database

1. Jalankan MySQL dan buat database baru:

   ```sql
   CREATE DATABASE foodcourtgo;
   USE foodcourtgo;
   ```

2. Jalankan skrip berikut untuk membuat tabel:

   ```sql
   CREATE TABLE users (
       username VARCHAR(50) PRIMARY KEY,
       password VARCHAR(50),
       role VARCHAR(20)
   );

   CREATE TABLE tenant (
       id VARCHAR(10) PRIMARY KEY,
       nama VARCHAR(100),
       kategori VARCHAR(50),
       deskripsi TEXT
   );

   CREATE TABLE menu (
       id VARCHAR(10) PRIMARY KEY,
       tenant_id VARCHAR(10),
       nama VARCHAR(100),
       harga INT,
       deskripsi TEXT,
       FOREIGN KEY (tenant_id) REFERENCES tenant(id)
   );

   CREATE TABLE pesanan (
       id INT AUTO_INCREMENT PRIMARY KEY,
       tenant_id VARCHAR(10),
       waktu TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       status VARCHAR(20) DEFAULT 'pending'
   );

   CREATE TABLE pesanan_detail (
       id INT AUTO_INCREMENT PRIMARY KEY,
       pesanan_id INT,
       menu_id VARCHAR(10),
       jumlah INT,
       FOREIGN KEY (pesanan_id) REFERENCES pesanan(id),
       FOREIGN KEY (menu_id) REFERENCES menu(id)
   );
   ```

3. Masukkan data pengguna awal:

   ```sql
   INSERT INTO users VALUES ('admin', 'admin', 'admin');
   INSERT INTO users VALUES ('t001', 't001', 'tenant');
   ```

---

## 🔧 Konfigurasi Koneksi Database

Buka kelas `DatabaseConnection.java` dan sesuaikan konstanta berikut dengan konfigurasi MySQL Anda:

```java
private static final String URL = "jdbc:mysql://localhost:3306/foodcourtgo";
private static final String USER = "root";
private static final String PASS = "";   // isi jika ada password
```

---

## 🚀 Cara Menjalankan

1. Clone atau download repository ini.
2. Buka NetBeans → **File → Open Project** → pilih folder proyek.
3. Pastikan dependensi Maven terunduh (klik kanan proyek → **Build with Dependencies**).
4. Jalankan kelas `LoginFrame.java` sebagai main class (klik kanan → **Run File**).
5. Login dengan kredensial:
   - **Admin**: username `admin`, password `admin`, role `admin`.
   - **Tenant**: username `t001`, password `t001`, role `tenant`.

---

## 🧭 Panduan Penggunaan

### Login
- Pilih role **admin** untuk mengelola semua data.
- Role **tenant** saat ini masih membuka dashboard yang sama (dapat dikembangkan lebih lanjut).

### Tab Tenant
- **Tambah**: isi ID, Nama, Kategori, Deskripsi → klik **Tambah**.
- **Edit**: klik baris di tabel, ubah data di form → klik **Edit**.
- **Hapus**: klik baris, klik **Hapus** → konfirmasi.
- **Cari**: ketik kata kunci → klik **Cari**.

### Tab Menu
- Pilih tenant dari combo box di bagian atas.
- Operasi tambah/edit/hapus/cari sama seperti di Tenant.
- ID menu harus unik, dan tenant harus dipilih sebelum menambah.

### Tab Pesanan
- Klik **Tambah Pesanan** → dialog baru muncul.
- Pilih tenant, pilih menu, tentukan jumlah → klik **Tambahkan** (dapat dilakukan beberapa kali).
- Klik **Simpan Pesanan** untuk menyimpan ke database.
- Tabel pesanan akan terisi otomatis.

---

## 🧱 Struktur Kelas

| Kelas                   | Fungsi |
|-------------------------|--------|
| `LoginFrame`            | Frame login dengan pilihan role |
| `AdminDashboardFrame`   | Frame utama dengan tab Tenant, Menu, Pesanan |
| `DatabaseConnection`    | Mengelola koneksi JDBC ke MySQL |
| `TambahPesananFrame`    | Dialog untuk membuat pesanan baru |

Kelas inner dalam `AdminDashboardFrame`:
- `TenantPanel` – panel manajemen tenant
- `MenuPanel` – panel manajemen menu
- `OrderPanel` – panel daftar pesanan

---

## ❗ Troubleshooting

**Error: `ClassNotFoundException: com.mysql.cj.jdbc.Driver`**
- Pastikan MySQL Connector/J sudah ditambahkan di Libraries. Di NetBeans, klik kanan Libraries → Add Library → pilih MySQL JDBC Driver.

**Error: `Communications link failure`**
- MySQL belum berjalan. Nyalakan MySQL (XAMPP/Laragon) dan pastikan port 3306 terbuka.

**`NullPointerException` pada `cbTenant`**
- Urutan inisialisasi komponen sudah diperbaiki di kode. Jika masih terjadi, pastikan file sumber terbaru.

**Login gagal**
- Pastikan tabel `users` sudah diisi dan data sesuai.

**Data tidak muncul di tabel**
- Cek apakah database sudah berisi data. Gunakan tab Refresh jika perlu.

---

## 📝 Pengembangan Lebih Lanjut

- Dashboard terpisah untuk tenant.
- Ubah status pesanan (diproses/selesai).
- Cetak struk pemesanan.
- Ekspor laporan ke PDF/Excel.
- Integrasi dengan printer thermal.

---

## 📄 Lisensi

Proyek ini dibuat untuk keperluan tugas mata kuliah **Pemrograman Berorientasi Objek (PBO)**. Silakan digunakan dan dimodifikasi sesuai kebutuhan.

---

**© 2026 – FoodCourt Go Team**