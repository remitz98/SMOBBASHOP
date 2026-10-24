# 🚀 คู่มือการติดตั้งและ Deploy ระบบร้านค้าเสื้อสโมสร BBA UBRU
## สถาปัตยกรรม: Cloudflare Workers + Cloudflare D1 + Cloudflare R2 + Hono

ระบบนี้ถูกพัฒนาเพื่อย้ายจาก **Google Apps Script + Google Sheets** มาสู่ **Cloudflare Workers (Edge API) + Cloudflare D1 (SQL Database) + Cloudflare R2 (Object Storage สำหรับเก็บรูปภาพ)** อย่างสมบูรณ์แบบ รองรับผู้ใช้งานพร้อมกันได้ระดับหมื่นคน ไม่ติดปัญหา Timeout, โควต้า Google Drive หรือชีตล็อก

---

### 📂 โครงสร้างโฟลเดอร์ของโปรเจกต์
```
c:\Users\ReMiTz\Downloads\bba-cloudflare\
├── public\
│   ├── Index.html      # เว็บหน้าร้านค้าสำหรับนักศึกษา (จองเสื้อ, ลงทะเบียน, เช็กประวัติ, พิมพ์ใบเสร็จ)
│   └── Admin.html      # เว็บระบบหลังบ้านสำหรับแอดมิน (จัดการออเดอร์, คืนสต็อก, สรุปยอดตัดเย็บ, จัดการหมวดหมู่)
├── src\
│   └── index.js        # Backend API ทั้งหมด พัฒนาด้วย Hono Framework บน Cloudflare Workers
├── node_modules\       # Dependency Packages (Hono, Wrangler)
├── package.json        # กำหนดเวอร์ชันและคำสั่งรันระบบ
├── schema.sql          # โครงสร้างฐานข้อมูล D1 (ตารางสินค้า, ออเดอร์, ลูกค้า, การตั้งค่า, หมวดหมู่)
├── wrangler.toml       # ไฟล์คอนฟิกหลักสำหรับเชื่อมต่อ Cloudflare (D1 + R2 + Static Assets)
└── README.md           # คู่มือฉบับนี้
```

---

## 🛠️ ขั้นตอนการ Deploy ขึ้น Cloudflare (ทำตาม 5 ขั้นตอนนี้ได้ทันที)

### ขั้นตอนที่ 1: เปิด Terminal ในโฟลเดอร์โปรเจกต์
เปิด PowerShell หรือ Command Prompt แล้วเข้าไปที่โฟลเดอร์โปรเจกต์:
```powershell
cd c:\Users\ReMiTz\Downloads\bba-cloudflare
```

---

### ขั้นตอนที่ 2: ล็อกอินเข้าบัญชี Cloudflare ของคุณ
รันคำสั่ง:
```powershell
npx wrangler login
```
*ระบบจะเปิดหน้าเบราว์เซอร์ขึ้นมา ให้กดปุ่ม **"Allow"** เพื่ออนุญาตให้คอมพิวเตอร์เข้าถึงบัญชี Cloudflare ของคุณ*

---

### ขั้นตอนที่ 3: สร้างฐานข้อมูล Cloudflare D1
รันคำสั่งสร้าง Database ชื่อ `bba-store-db`:
```powershell
npx wrangler d1 create bba-store-db
```

เมื่อรันเสร็จ Wrangler จะแสดงข้อความประมาณนี้:
```toml
[[d1_databases]]
binding = "DB"
database_name = "bba-store-db"
database_id = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
```

👉 **นำค่า `database_id` ที่ได้** ไปเปิดไฟล์ `wrangler.toml` แล้วแก้ไขบรรทัดที่ 13:
```toml
database_id = "ใส่-database_id-จริงของคุณที่นี่"
```

---

### ขั้นตอนที่ 4: สร้างตารางและใส่ข้อมูลเริ่มต้น (Seed Data)
รันคำสั่งเพื่อสร้างตารางทั้งหมดใน `schema.sql` ขึ้นไปยังฐานข้อมูล D1 บนคลาวด์:
```powershell
npx wrangler d1 execute bba-store-db --remote --file=./schema.sql
```
*(เมื่อสำเร็จ จะมีข้อความแสดงว่าตาราง products, customers, orders, payments, settings, categories ถูกสร้างเรียบร้อยแล้ว)*

---

### ขั้นตอนที่ 5: Deploy ขึ้นสู่ระบบจริง
รันคำสั่ง Deploy ได้ทันที:
```powershell
npm run deploy
```
*(หรือใช้ `npx wrangler deploy`)*

🎉 เมื่อ Deploy เสร็จ Wrangler จะให้ URL เว็บไซต์ของคุณทันที เช่น:
- **หน้าร้านค้าสำหรับนักศึกษา:** `https://bba-store.<ชื่อบัญชีของคุณ>.workers.dev/`
- **หน้าระบบแอดมินหลังบ้าน:** `https://bba-store.<ชื่อบัญชีของคุณ>.workers.dev/admin`

---

## 💻 การทดสอบและรันในเครื่องก่อนขึ้นจริง (Local Development)
หากต้องการทดสอบระบบในเครื่องคอมพิวเตอร์ก่อน deploy ขึ้นคลาวด์ สามารถทำได้ง่ายๆ:

1. นำเข้าฐานข้อมูลจำลองในเครื่อง:
```powershell
npx wrangler d1 execute bba-store-db --local --file=./schema.sql
```
2. รันเซิร์ฟเวอร์จำลอง:
```powershell
npm run dev
```
3. เปิดเบราว์เซอร์ไปที่: `http://localhost:8787` เพื่อดูหน้าร้าน และ `http://localhost:8787/admin` เพื่อดูหลังบ้าน

---

## ⚙️ ข้อมูลการเข้าใช้งานเริ่มต้น (Default Credentials)
- **รหัส PIN แอดมินเริ่มต้น:** `1234`
- **สถานที่รับเสื้อเริ่มต้น:** `ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ`
- **วันเวลาทำการเริ่มต้น:** `วันจันทร์ - ศุกร์ เวลา 09:00 - 16:30 น.`
- **พร้อมเพย์เริ่มต้น:** `004999245434427`

*(คุณสามารถแก้ไขข้อมูลเหล่านี้ได้ตลอดเวลาผ่านหน้าแอดมิน หรืออัปเดตค่าในตาราง `settings` ของ D1)*

---

## 🌟 จุดเด่นที่ได้รับการปรับปรุงจากระบบเดิม
1. **แยกหน้าเว็บเป็นสัดส่วนสมบูรณ์**: หน้าร้านอยู่ที่ `/` และหลังบ้านอยู่ที่ `/admin` กดสลับหน้าได้โดยตรง ไม่ต้องผ่าน URL iframe แปลกๆ ของ Apps Script
2. **ระบบตัดสต็อกระดับไซส์แบบ Atomic Transaction**: เมื่อมีนักศึกษาสั่งซื้อ ระบบจะตัดสต็อกต่อไซส์ใน Database ทันทีแบบ Transaction ป้องกันปัญหาสต็อกติดลบจากการกดพร้อมกัน
3. **คืนสต็อกอัตโนมัติเมื่อยกเลิกออเดอร์**: เมื่อแอดมินกดยกเลิกคำสั่งจอง ระบบจะบวกสต็อกเสื้อและไซส์กลับเข้าคลังอัตโนมัติ
4. **รักษาเลข 0 ด้านหน้ารหัส PIN 4 หลัก**: ไม่ว่า PIN จะเป็น `0123`, `0078` หรือ `0000` ระบบจะแปลงเป็น String 4 หลักเสมอ ป้องกันปัญหาเลข 0 หายเหมือนบน Google Sheets
5. **โหลดเร็วระดับ Millisecond**: ทำงานบนเครือข่าย Global Edge 300+ เมืองทั่วโลก โหลดหน้าเว็บและบันทึกออเดอร์ได้เร็วกว่า Google Apps Script หลายเท่าตัว
