# Bot OverDrive 

Dibuat untuk memenuhi Tugas Besar 1 IF2211 Strategi Algoritma.

## Deskripsi

Program ini merupakan Bot untuk game [OverDrive](https://github.com/EntelectChallenge/2020-Overdrive). Bot berjalan menggunakan *Algoritma Greedy* yang fokus ke menghancurkan mobil lawan. 

Detail Algoritma Bot adalah berikut:

 - Bot mengecek potensial damage yang akan diterima ketika berada di lane kiri / tengah / kanan. Bot akan memilih jalan dengan potensisal damage terkecil.
 - Jika semua jalan memiliki potensial damage yang sama, Bot akan memilih lane dengan potensial mendapatkan power up terbanyak.
 - Jika semua lane memiliki potensial damage dan power ups yang sama, bot akan memilih lurus. Bot akan menggunakan power up Lizard apabila ada obstacle di depan mobil.
 - Saat lurus, mobil akan menggunakan power up dengan syarat sebagai berikut:
   - OIL    : Mobil lawan berada dibelakang 
   - EMP    : Mobil musuh di jangkauan EMP
   - TWEET  : Kondisi tertentu (detail ada di laporan)
 - Jika tidak ada power up lagi, mobil akan menggunakan power up Boost.

## Build

### Prerequsite
- [IntelIiJ IDEA](https://www.jetbrains.com/idea/)
### Linux

Build memerlukan [Maven](https://maven.apache.org/).

```bash
make build
```

Bot ada di `bin/pick_up.jar`.

### Windows

- Buka direktori bot dengan menggunakan IntelIiJ IDEA
- Pilih Maven Package -> Lifecycle -> package

## Run

### Prerequsite

 - [Game OverDrive](https://github.com/EntelectChallenge/2020-Overdrive/releases/tag/2020.3.4)
 - [Java](https://www.java.com/)

 - Pastikan Bot sudah di-build. Baca [Build](#build) untuk melihat cara build Bot.
 - Ubah `player-a` dan `player-b` di `game-runner-config` dengan path ke Bot.
### Linux

 - Run game dengan perintah `make run`.

### Windows
 - Run game dengan mengeklik file `run.bat` yang berada pada Game Overdrive
## Anggota

| Nama | NIM |
| ---- | -------- |
| Fitrah Ramadhani Nugroho | 13520030 |
| Tri Sulton Adila | 13520033 |
| Muhammad Alif Putra Yasa | 13520135 |
