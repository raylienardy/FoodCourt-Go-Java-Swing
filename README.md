# 🍔 FoodCourt Go – Java Swing + Firebase Edition

Aplikasi desktop **FoodCourt Go** kini telah bermigrasi dari MySQL ke **Firebase Realtime Database** dan dilengkapi fitur unggahan gambar untuk tenant dan menu. Antarmuka dibangun dengan **Java Swing**, mendukung tiga peran pengguna: **Super Admin**, **Tenant**, dan **Customer**. Setiap peran memiliki hak akses berbeda untuk mengelola tenant, menu, dan pemesanan.

---

## ✨ Fitur Unggulan

- **Login Multi‑Role** – Super Admin, Tenant, dan Customer menggunakan data akun dari Firebase.
- **Manajemen Tenant** (Super Admin) – tambah, edit, hapus, cari tenant lengkap dengan foto/thumbnail.
- **Manajemen Menu** (Super Admin & Tenant) – kelola menu per tenant, tambah/edit/hapus, cari, dan foto menu.
- **Pemesanan** (Customer) – pelanggan dapat membuat pesanan baru, memilih tenant dan menu, serta melihat riwayat pesanan.
- **Upload Gambar Lokal** – Tenant dan menu bisa memiliki foto tersimpan di folder `uploads/`, dengan fallback ke gambar dari **Unsplash** jika foto belum diatur.
- **Pembatasan Akses** – Setiap peran hanya dapat mengakses fitur sesuai kewenangannya.
- **UI Tab & Form Interaktif** – Pratinjau gambar langsung, pencarian real‑time, dan tombol aksi yang intuitif.

---

## 🧰 Teknologi

| Teknologi                | Keterangan                                                                 |
|--------------------------|----------------------------------------------------------------------------|
| Java                     | JDK 8+ (disarankan JDK 11/21 untuk fitur HTTP yang lebih baik)             |
| Java Swing               | Toolkit GUI bawaan Java                                                    |
| Firebase Realtime Database | Penyimpanan data berbasis cloud, REST API                                  |
| JSON (org.json)          | Parsing dan pembuatan data JSON                                            |
| Unsplash (source.unsplash.com) | Sumber gambar fallback jika foto belum diunggah                       |
| NetBeans IDE             | IDE yang digunakan untuk pengembangan (opsional, bisa diganti)             |

---

## 📋 Prasyarat

1. **Java Development Kit (JDK)** versi 8 atau lebih baru.  
   ‣ Cek dengan `java -version` dan `javac -version`.  
   ‣ Rekomendasi: Amazon Corretto 11/21.

2. **NetBeans IDE** atau IDE Java lain (IntelliJ, Eclipse) – opsional.

3. **Firebase Project** dengan **Realtime Database** yang sudah aktif.  
   ‣ Daftar di [firebase.google.com](https://firebase.google.com) jika belum punya.  
   ‣ Buat project baru, aktifkan Realtime Database, pilih mode **test** (public) untuk pengembangan.

4. **Koneksi Internet** – Aplikasi memerlukan internet untuk mengakses Firebase dan sumber gambar Unsplash.

5. **Library JSON (`org.json`)** – sudah ditambahkan di project (Maven dependency atau file JAR manual).

---

## 🗄️ Persiapan Firebase

### 1. Struktur Data di Firebase
Data Anda sudah tersedia di Firebase dengan struktur berikut:

```
akun/
  ├─ ADMIN001: { username, pass, role: "super_admin", ... }
  ├─ TENANT001: { username, pass, role: "tenant", tenantId: "T0004", ... }
  ├─ A0001, A0002, ... : { ..., role: "customer" }
tenant/
  ├─ T0001: { nama, kategori, deskripsi, gambar, status, ... }
  ├─ T0002, ... , T0006
menu/
  ├─ T0001_M01: { nama, harga, deskripsi, gambar, tenantId, tambahan: [...] }
  ├─ T0001_M02, ...
pesanan/
  ├─ P002: { id, customerId, tenantId, items, totalHarga, status, meja, waktu }
  ├─ P1778..., ...
notifications/ (opsional, belum terintegrasi penuh)
```

Anda bisa menggunakan data yang sudah ada, atau mengimpor JSON tersebut langsung dari Firebase Console → Realtime Database → Import JSON.

### 2. Aturan Keamanan Firebase (Development)
Saat pengembangan, ubah **Rules** di Realtime Database menjadi publik agar aplikasi dapat membaca/menulis tanpa autentikasi:

```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```
⚠ **Jangan gunakan aturan ini di production.** Untuk lingkungan production, gunakan autentikasi Firebase dan aturan yang lebih ketat.

### 3. Salin URL Database
Di Firebase Console → Realtime Database, Anda akan melihat URL seperti:  
`https://foodcourtgo-xxxx-default-rtdb.firebaseio.com/`  
Catat URL ini, akan dipakai di kode.

---

## 🔧 Konfigurasi Aplikasi

Buka kelas **`FirebaseDB.java`** dan ganti nilai `BASE_URL` dengan URL Firebase Anda:

```java
private static final String BASE_URL = "https://foodcourtgo-xxxx-default-rtdb.firebaseio.com/";
```

Pastikan URL diakhiri dengan garis miring `/`.

---

## 🚀 Cara Menjalankan Aplikasi

1. **Clone atau download** project ini ke komputer Anda.
2. **Buka NetBeans**, lalu **File → Open Project**, arahkan ke folder project.
3. **Tambahkan library `json-20231013.jar`** jika belum ada:
   - Klik kanan project → **Properties** → **Libraries** → **Add JAR/Folder**.
   - Cari file JAR `json-20231013.jar` (unduh dari [https://repo1.maven.org/maven2/org/json/json/20231013/](https://repo1.maven.org/maven2/org/json/json/20231013/)).
4. **Buat folder `uploads/`** (opsional, akan otomatis dibuat oleh aplikasi).
5. Jalankan kelas utama `LoginFrame.java` (klik kanan → **Run File**).
6. Aplikasi akan terbuka dan menampilkan form login.

---

## 🔑 Kredensial Login

Data akun diambil langsung dari Firebase (node `akun`). Berikut akun yang tersedia (sesuai data yang Anda berikan):

| Username | Password  | Role (Pilih di ComboBox) | Keterangan                     |
|----------|-----------|---------------------------|--------------------------------|
| admin    | admin123  | Super Admin               | Bisa kelola tenant & menu     |
| buwarsi  | tenant123 | Tenant                    | Kelola menu Stand Bu Warsi     |
| Budi     | 123       | Customer                  | Membuat pesanan & lihat riwayat|
| Tono     | 12345     | Customer                  | Sama seperti di atas           |
| fana     | 12345     | Customer                  |                                |
| Yono     | 12345     | Customer                  |                                |

**Catatan:**  
- Role di ComboBox: **Super Admin**, **Tenant**, **Customer**.  
- Password disimpan dalam bentuk plaintext – untuk keamanan production sebaiknya di-hash.

---

## 🧭 Panduan Penggunaan

### 1. Login
- Pilih **Role** sesuai dengan peran Anda.
- Masukkan **Username** dan **Password** yang sesuai.
- Klik **Login**. Jika berhasil, frame sesuai role akan terbuka.

### 2. Dashboard Super Admin
Super Admin memiliki 2 tab: **Tenant** dan **Menu**.

#### 🏢 Tab Tenant
- **Tambah**: Isi ID, Nama, Kategori, Deskripsi → (opsional) Pilih gambar dengan tombol **Pilih Gambar** → klik **Tambah**.  
  Tenant baru akan tersimpan di Firebase dan muncul di tabel.
- **Edit**: Klik salah satu baris di tabel → data akan muncul di form. Ubah data, lalu klik **Edit**. Gambar bisa diganti dengan memilih gambar baru, atau dihapus dengan tombol **Hapus Gambar**.
- **Hapus**: Pilih tenant, klik **Hapus**, konfirmasi. Tenant dan file foto terkait akan dihapus.
- **Cari**: Ketik kata kunci di kotak **Cari**, klik **Cari** atau tekan Enter.
- **Refresh** memuat ulang semua data dari Firebase.
- **Bersihkan** mengosongkan form.

#### 🍽️ Tab Menu
- **Pilih Tenant** dari combo box di bagian atas untuk menampilkan menu milik tenant tersebut.
- Operasi **Tambah, Edit, Hapus, Cari** serupa dengan Tenant.
- Saat menambah atau mengedit, Anda dapat menyertakan gambar menu (lokal) atau mengosongkannya.
- Jika gambar tidak diunggah, pratinjau akan menampilkan gambar dari Unsplash dengan kata kunci `food`.

> ⚠ Super Admin **tidak dapat membuat pesanan**.

### 3. Dashboard Tenant (contoh: Stand Bu Warsi)
Tenant hanya memiliki 1 tab: **Menu** untuk tenant miliknya.
- Combo Tenant terkunci ke tenant yang sesuai (tidak bisa diganti).
- Tenant dapat **menambah, mengedit, dan menghapus menu** miliknya sendiri.
- **Tidak dapat mengelola tenant lain dan tidak dapat membuat pesanan**.

### 4. Frame Customer
Setelah login sebagai Customer, Anda akan melihat jendela yang menampilkan **riwayat pesanan** pribadi.

#### 🛒 Membuat Pesanan Baru
1. Klik tombol **Buat Pesanan Baru**.
2. Dialog baru muncul:
   - Pilih **Tenant** (dari daftar tenant yang aktif).
   - Pilih **Menu** yang tersedia (menu tenant tersebut akan dimuat otomatis).
   - Tentukan **Jumlah**.
   - Klik **Tambahkan** untuk memasukkan item ke keranjang sementara.
   - Ulangi untuk menambahkan menu lain dari tenant yang sama.
   - Keranjang akan ditampilkan di area teks.
3. Klik **Simpan Pesanan** untuk menyimpan ke Firebase.
4. Pesanan baru akan muncul di tabel daftar pesanan customer.

> 📌 Saat ini meja default adalah "A-01" – fitur pemilihan meja dapat ditambahkan nanti.

---

## 🧱 Struktur Proyek & Kelas

| Kelas                     | Fungsi |
|---------------------------|--------|
| `LoginFrame`              | Frame login dengan pilihan role dan koneksi ke Firebase |
| `AdminDashboardFrame`     | Frame utama untuk Super Admin dan Tenant, berisi tab Tenant dan Menu |
| `CustomerFrame`           | Frame untuk Customer, menampilkan riwayat pesanan dan tombol buat pesanan baru |
| `FirebaseDB`              | Kelas utilitas untuk mengakses Firebase Realtime Database via REST API (GET, PUT, PATCH, DELETE) |
| (Inner class: `TenantPanel`) | Panel dalam `AdminDashboardFrame` untuk manajemen tenant |
| (Inner class: `MenuPanel`)   | Panel untuk manajemen menu (dengan filter per tenant) |
| (Inner class: `CustomerOrderFrame`) | Dialog di `CustomerFrame` untuk membuat pesanan baru |

**File pendukung:**
- `uploads/tenant/` – folder penyimpanan gambar tenant.
- `uploads/menu/` – folder penyimpanan gambar menu.

---

## 🖼️ Fitur Gambar & Thumbnail

- Setiap tenant dan menu dapat memiliki **gambar sendiri**.
- Gambar bisa dipilih dari komputer melalui **JFileChooser**, lalu disalin ke folder `uploads/` dengan nama unik.
- Jika gambar belum diunggah, aplikasi akan menampilkan gambar dari **Unsplash** sebagai tempat sementara (placeholder).
- Tombol **Hapus Gambar** akan menghapus file lokal dan mengosongkan field `gambar` di Firebase, sehingga fallback Unsplash aktif.
- Saat menghapus tenant atau menu, file gambar terkait juga akan dihapus dari server.

---

## ❗ Troubleshooting

**1. Data tenant/menu tidak muncul (tab kosong)**  
- Pastikan URL Firebase di `FirebaseDB` sudah benar dan diakhiri `/`.  
- Periksa koneksi internet.  
- Buka browser, masukkan `https://URL-ANDA/tenant.json` – jika muncul data, URL sudah benar.  
- Pastikan Firebase Rules sedang `public` (development).  
- Cek apakah ada exception di console NetBeans (bisa jadi library JSON tidak ada).

**2. Login selalu gagal**  
- Pastikan username, password, dan role yang dipilih sesuai dengan data di Firebase (node `akun`).  
- Perhatikan role di ComboBox: pilih **Super Admin** untuk username `admin`.  
- Password bersifat case‑sensitive.

**3. Gambar tidak muncul / error**  
- Jika gambar dari Unsplash tidak muncul, mungkin koneksi internet lambat atau URL Unsplash bermasalah.  
- Gambar lokal: pastikan folder `uploads/tenant/` atau `uploads/menu/` memiliki izin tulis dan file ada di sana.  
- Error `FileNotFoundException` saat menyimpan gambar – buat folder upload secara manual atau jalankan aplikasi sebagai administrator.

**4. NetBeans error kompilasi `cannot find symbol`**  
- Beberapa file lama (MySQL) mungkin masih tersisa. Hapus file‑file berikut:  
  `DatabaseConnection.java`, `DashboardFrame.java`, `MenuFrame.java`, `TambahTenantFrame.java`, `TambahMenuFrame.java`, `TambahPesananFrame.java`.  
- Lakukan **Clean and Build** setelah penghapusan.

**5. Aplikasi crash saat pertama dijalankan**  
- Pastikan ada koneksi internet.  
- Jika menggunakan JDK 8, beberapa fitur mungkin tidak tersedia. Disarankan JDK 11+.

---

## 📝 Pengembangan Lebih Lanjut

- **Autentikasi Firebase** menggantikan penyimpanan password plaintext.
- **Firebase Storage** untuk menyimpan gambar, bukan di server lokal.
- **Notifikasi real‑time** untuk tenant saat ada pesanan baru (menggunakan Firebase listener/event – perlu integrasi web socket atau pooling).
- **Dashboard pemilik** dengan laporan penjualan dan statistik.
- **Pemilihan meja/kasir** saat membuat pesanan.
- **Cetak struk** atau ekspor laporan ke PDF.
- **Desain UI yang lebih modern** (menggunakan Look and Feel seperti FlatLaf).

---

## 📄 Lisensi

Proyek ini dibuat untuk keperluan tugas mata kuliah **Pemrograman Berorientasi Objek (PBO)** tahun 2026. Silakan dimodifikasi dan digunakan untuk pembelajaran.

---

**© 2026 – FoodCourt Go Team**  
Versi Firebase ⚡