-- =========================================================================
-- Cloudflare D1 Database Schema for BBA UBRU Student Union Store
-- =========================================================================

-- 1. ตารางสินค้า (Products)
CREATE TABLE IF NOT EXISTS products (
  product_id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  category TEXT NOT NULL,
  price REAL NOT NULL,
  sizes TEXT NOT NULL,
  colors TEXT NOT NULL,
  stock INTEGER NOT NULL DEFAULT 0,
  size_stock TEXT NOT NULL DEFAULT '{}',
  image_url TEXT NOT NULL DEFAULT '',
  description TEXT NOT NULL DEFAULT '',
  status TEXT NOT NULL DEFAULT 'พร้อมจำหน่าย',
  size_guide TEXT NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. ตารางข้อมูลนักศึกษา / ลูกค้า (Customers)
CREATE TABLE IF NOT EXISTS customers (
  student_id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  email TEXT NOT NULL,
  phone TEXT NOT NULL,
  pin TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 3. ตารางคำสั่งจองเสื้อ (Orders)
CREATE TABLE IF NOT EXISTS orders (
  order_id TEXT PRIMARY KEY,
  order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  student_id TEXT NOT NULL,
  customer_name TEXT NOT NULL,
  student_email TEXT NOT NULL,
  phone TEXT NOT NULL,
  items_summary TEXT NOT NULL,
  items_json TEXT NOT NULL,
  total_amount REAL NOT NULL,
  slip_url TEXT DEFAULT '',
  status TEXT NOT NULL DEFAULT 'รอตรวจสอบการชำระเงิน',
  pickup_location TEXT DEFAULT 'ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ',
  admin_notes TEXT DEFAULT '',
  FOREIGN KEY (student_id) REFERENCES customers(student_id)
);

-- 4. ตารางประวัติการชำระเงิน (Payments)
CREATE TABLE IF NOT EXISTS payments (
  payment_id TEXT PRIMARY KEY,
  order_id TEXT NOT NULL,
  student_id TEXT NOT NULL,
  amount REAL NOT NULL,
  slip_url TEXT DEFAULT '',
  verify_status TEXT NOT NULL DEFAULT 'รอตรวจสอบ',
  admin_notes TEXT DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

-- 5. ตารางการตั้งค่าระบบ (Settings)
CREATE TABLE IF NOT EXISTS settings (
  key TEXT PRIMARY KEY,
  value TEXT NOT NULL,
  description TEXT DEFAULT ''
);

-- 6. ตารางหมวดหมู่สินค้า (Categories)
CREATE TABLE IF NOT EXISTS categories (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 7. Trigger ป้องกันสต็อกติดลบและการแย่งซื้อพร้อมกัน (Concurrency / Race Condition Guard)
CREATE TRIGGER IF NOT EXISTS check_product_stock_non_negative
BEFORE UPDATE ON products
FOR EACH ROW
WHEN (
  NEW.stock < 0 OR
  (NEW.size_stock IS NOT NULL AND NEW.size_stock != '' AND EXISTS (
    SELECT 1 FROM json_each(NEW.size_stock) WHERE CAST(value AS NUMERIC) < 0
  ))
)
BEGIN
  SELECT RAISE(ABORT, 'INSUFFICIENT_STOCK');
END;

-- ==================== ข้อมูลเริ่มต้น (Seed Initial Data) ====================

-- ข้อมูลการตั้งค่าระบบ
INSERT OR IGNORE INTO settings (key, value, description) VALUES
('StoreName', 'SMO BBA SHOP', 'ชื่อระบบร้านค้าสโมสร'),
('ClubLocation', 'ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ', 'สถานที่รับเสื้อ'),
('ClubSchedule', 'วันจันทร์ - ศุกร์ เวลา 09:00 - 16:30 น.', 'วันเวลาทำการรับเสื้อ'),
('AdminPIN', '1234', 'รหัส PIN เข้าหน้าจัดการแอดมินสโมสร'),
('PromptPay', '004999245434427', 'เบอร์พร้อมเพย์ หรือเลขบัญชีสโมสร'),
('BankDetails', 'ธนาคารกสิกรไทย เลขบัญชี 2343158184 ชื่อบัญชี นายธรรณรงณ์ จันดาชัย และ นายอภิสิทธิ์ สมตน และ นาย นิรวิทย์ ดวงจันทร์', 'ข้อมูลบัญชีธนาคารสำหรับโอนเงิน'),
('AllowedDomain', '@ubru.ac.th', 'โดเมนอีเมลมหาวิทยาลัยที่อนุญาต');

-- หมวดหมู่เริ่มต้น
INSERT OR IGNORE INTO categories (name) VALUES
('เสื้อโปโล'),
('เสื้อยืด Oversize'),
('เสื้อกิจกรรม'),
('เสื้อกันหนาว/แจ็คเก็ต');

-- สินค้าตัวอย่างเริ่มต้น
INSERT OR IGNORE INTO products (product_id, name, category, price, sizes, colors, stock, size_stock, image_url, description, status, size_guide) VALUES
('BBA-01', 'เสื้อโปโลสโมสรนักศึกษา BBA UBRU รุ่นเกียรติยศ 2026', 'เสื้อโปโล', 350, 'S, M, L, XL, 2XL, 3XL', 'ฟ้าคราม-ทอง, กรมท่า-ขาว', 110, '{"S":20,"M":25,"L":30,"XL":20,"2XL":10,"3XL":5}', 'https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=600&q=80', 'เสื้อโปโลผ้าทอ Dry-Tech ระบายอากาศดีเยี่ยม ปักตราสโมสรคณะบริหารธุรกิจและการจัดการ สวมใส่สบาย เหมาะสำหรับใส่เรียนและงานพิธีการ', 'พร้อมจำหน่าย', 'S อก 36\" ยาว 26\" / M อก 38\" ยาว 27\" / L อก 40\" ยาว 28\" / XL อก 42\" ยาว 29\" / 2XL อก 44\" ยาว 30\" / 3XL อก 46\" ยาว 31\"'),
('BBA-02', 'เสื้อยืดสโมสร BBA UBRU Oversize Street Style', 'เสื้อยืด Oversize', 290, 'M, L, XL, 2XL', 'ดำพรีเมียม, ขาวมินิมอล', 80, '{"M":20,"L":30,"XL":20,"2XL":10}', 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=600&q=80', 'เสื้อยืด Cotton Comb 100% ทรงหลวม Oversize สกรีนลายกราฟิกสโมสร สไตล์สตรีทแฟชั่น ทันสมัย ใส่เที่ยวหรือทำกิจกรรมได้คล่องตัว', 'พร้อมจำหน่าย', 'M อก 40\" ยาว 28\" / L อก 44\" ยาว 29\" / XL อก 48\" ยาว 30\" / 2XL อก 52\" ยาว 31\"'),
('BBA-03', 'เสื้อกิจกรรม Freshy & Sport Day BBA 2026', 'เสื้อกิจกรรม', 250, 'S, M, L, XL, 2XL', 'ส้มสดใส, กรมท่าสปอร์ต', 90, '{"S":15,"M":25,"L":25,"XL":15,"2XL":10}', 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=600&q=80', 'เสื้อยืดกีฬาเนื้อผ้านาโนระบายเหงื่อ แห้งไว สำหรับกิจกรรมรับน้องและกีฬาสี สดใส แข็งแรงทนทาน', 'พร้อมจำหน่าย', 'S อก 36\" ยาว 26\" / M อก 38\" ยาว 27\" / L อก 40\" ยาว 28\" / XL อก 42\" ยาว 29\" / 2XL อก 44\" ยาว 30\"');
