import { Hono } from 'hono';
import { cors } from 'hono/cors';
import { WorkerMailer } from 'worker-mailer';

const app = new Hono();

// เปิดใช้งาน CORS สำหรับรองรับการเรียก API ทุกโดเมน
app.use('*', cors());

// ==================== HELPER FUNCTIONS ====================

function normalizePin(val) {
  if (val === null || val === undefined) return '';
  let s = String(val).trim();
  if (s.startsWith("'")) s = s.substring(1).trim();
  s = s.replace(/[^0-9]/g, '');
  if (!s) return '';
  if (s.length < 4) s = s.padStart(4, '0');
  return s;
}

function normalizePhone(val) {
  if (val === null || val === undefined) return '';
  let s = String(val).trim();
  if (s.startsWith("'")) s = s.substring(1).trim();
  s = s.replace(/[^0-9]/g, '');
  if (!s) return '';
  if (s.length === 9 && !s.startsWith('0')) s = '0' + s;
  return s;
}

function normalizeStudentId(val) {
  if (val === null || val === undefined) return '';
  let s = String(val).trim();
  if (s.startsWith("'")) s = s.substring(1).trim();
  return s.replace(/[^0-9]/g, '');
}

async function verifyAdminPin(db, pin) {
  const row = await db.prepare("SELECT value FROM settings WHERE key = 'AdminPIN'").first();
  const correctPin = normalizePin(row ? row.value : '1234');
  return normalizePin(pin) === correctPin;
}

function escapeHtml(text) {
  if (text === null || text === undefined) return '';
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function buildReadyPickupEmailHtml({
  name,
  studentId,
  orderId,
  itemsSummary,
  total,
  pickupLocation,
  clubSchedule
}) {
  const safeName = escapeHtml(name || 'นักศึกษา');
  const safeStudentId = escapeHtml(studentId || '-');
  const safeOrderId = escapeHtml(orderId);
  const safePickup = escapeHtml(pickupLocation || 'ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ');
  const safeSchedule = escapeHtml(clubSchedule || 'วันจันทร์ - ศุกร์ เวลา 09:00 - 16:30 น.');
  const safeItemsSummary = escapeHtml(itemsSummary).replace(/\n/g, '<br>');
  const safeTotal = Number(total || 0).toLocaleString();

  const badgeColor = '#059669';
  const badgeText = 'พร้อมรับเสื้อที่ห้องสโมสร';
  const headline = 'เสื้อสโมสร BBA UBRU ของคุณพร้อมให้มารับแล้ว!';
  const description = 'ข่าวดี! เสื้อสโมสรนักศึกษาที่คุณสั่งจองไว้พร้อมให้มารับแล้ว สามารถเดินทางมารับเสื้อได้ที่ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ ตามรายละเอียดด้านล่างนี้ได้เลยครับ/ค่ะ';
  const highlightBox = `
    <div style="background-color: #ecfdf5; border: 2px solid #a7f3d0; border-radius: 12px; padding: 16px; margin: 20px 0;">
      <h4 style="color: #065f46; margin-top: 0; margin-bottom: 8px; font-size: 15px;">📍 ข้อมูลจุดรับเสื้อ:</h4>
      <p style="margin: 4px 0; color: #047857; font-size: 13px;"><b>สถานที่:</b> ${safePickup}</p>
      <p style="margin: 4px 0; color: #047857; font-size: 13px;"><b>เวลาทำการ:</b> ${safeSchedule}</p>
      <p style="margin: 8px 0 0 0; color: #065f46; font-size: 12px; font-weight: bold;">
        * กรุณานำบัตรประจำตัวนักศึกษา หรือแสดงอีเมลนี้ต่อกรรมการสโมสรเพื่อรับเสื้อ
      </p>
    </div>
  `;

  return `<!DOCTYPE html>
<html>
<head><meta charset="utf-8"></head>
<body style="font-family: 'Segoe UI', Tahoma, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 24px; color: #1e293b;">
  <div style="max-width: 580px; margin: auto; background-color: #ffffff; border-radius: 16px; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);">
    
    <div style="background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 100%); padding: 24px 28px; text-align: center; color: #ffffff;">
      <h2 style="margin: 0; font-size: 19px; font-weight: 700; letter-spacing: -0.5px;">สโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ</h2>
      <p style="margin: 4px 0 0 0; font-size: 13px; color: #93c5fd; font-weight: 600;">มหาวิทยาลัยราชภัฏอุบลราชธานี (BBA UBRU)</p>
    </div>

    <div style="padding: 28px;">
      <div style="text-align: center; margin-bottom: 20px;">
        <span style="display: inline-block; background-color: ${badgeColor}; color: #ffffff; font-size: 13px; font-weight: bold; padding: 6px 18px; border-radius: 9999px;">
          สถานะ: ${escapeHtml(badgeText)}
        </span>
        <h3 style="margin: 16px 0 6px 0; font-size: 18px; color: #0f172a;">${headline}</h3>
        <p style="margin: 0; font-size: 13px; color: #64748b; line-height: 1.5;">${description}</p>
      </div>

      ${highlightBox}

      <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 18px; margin-top: 16px;">
        <table style="width: 100%; border-collapse: collapse; font-size: 13px;">
          <tr>
            <td style="padding: 6px 0; color: #64748b; width: 35%;">รหัสคำสั่งจอง:</td>
            <td style="padding: 6px 0; font-weight: bold; font-family: monospace; color: #0f172a;">${safeOrderId}</td>
          </tr>
          <tr>
            <td style="padding: 6px 0; color: #64748b;">รหัสนักศึกษา:</td>
            <td style="padding: 6px 0; font-weight: bold; color: #0f172a;">${safeStudentId}</td>
          </tr>
          <tr>
            <td style="padding: 6px 0; color: #64748b;">ชื่อ-นามสกุล:</td>
            <td style="padding: 6px 0; color: #0f172a;">${safeName}</td>
          </tr>
          <tr>
            <td style="padding: 6px 0; color: #64748b; vertical-align: top;">รายการเสื้อ:</td>
            <td style="padding: 6px 0; color: #0f172a;">${safeItemsSummary}</td>
          </tr>
          <tr style="border-top: 1px solid #cbd5e1;">
            <td style="padding: 10px 0 4px 0; font-weight: bold; color: #0f172a;">ยอดชำระ:</td>
            <td style="padding: 10px 0 4px 0; font-weight: bold; color: #1d4ed8; font-size: 15px;">฿${safeTotal} บาท</td>
          </tr>
        </table>
      </div>

      <div style="margin-top: 24px; text-align: center;">
        <p style="margin: 0; font-size: 12px; color: #94a3b8;">
          หากมีข้อสงสัย สามารถติดต่อได้ที่ <b>${safePickup}</b><br>
          เวลาทำการ: ${safeSchedule}
        </p>
      </div>
    </div>

    <div style="background-color: #f1f5f9; padding: 14px 20px; text-align: center; border-top: 1px solid #e2e8f0; font-size: 11px; color: #64748b;">
      อีเมลนี้ถูกส่งโดยระบบอัตโนมัติ สโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มหาวิทยาลัยราชภัฏอุบลราชธานี (BBA UBRU)
    </div>

  </div>
</body>
</html>`;
}

async function sendNotificationEmail(env, { to, subject, htmlBody }) {
  if (!to || !to.includes('@')) {
    return { success: false, reason: 'อีเมลผู้รับไม่ถูกต้อง' };
  }

  const db = env.DB;
  const { results: settingsRows } = await db.prepare(
    "SELECT key, value FROM settings WHERE key IN ('EmailService', 'EmailGasUrl', 'SmtpUser', 'SmtpPass', 'SmtpHost', 'SmtpPort', 'ResendApiKey', 'BrevoApiKey')"
  ).all();
  const config = Object.fromEntries(settingsRows.map(r => [r.key, r.value]));

  const gasUrl = config.EmailGasUrl || env.GAS_EMAIL_URL;
  const smtpUser = config.SmtpUser || env.SMTP_USER;
  const smtpPass = config.SmtpPass || env.SMTP_PASS;
  const smtpHost = config.SmtpHost || env.SMTP_HOST || 'smtp.gmail.com';
  const smtpPort = Number(config.SmtpPort || env.SMTP_PORT || 465);
  const resendKey = config.ResendApiKey || env.RESEND_API_KEY;
  const brevoKey = config.BrevoApiKey || env.BREVO_API_KEY;

  // 1. Google Apps Script Web App Relay
  if (gasUrl) {
    try {
      const res = await fetch(gasUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ to, subject, htmlBody })
      });
      if (res.ok) {
        return { success: true, method: 'Google Apps Script Webhook' };
      }
      const errText = await res.text();
      return { success: false, reason: `Google Apps Script error: ${errText.substring(0, 100)}` };
    } catch (err) {
      return { success: false, reason: `GAS fetch error: ${err.message}` };
    }
  }

  // 2. Gmail / Custom SMTP via worker-mailer
  if (smtpUser && smtpPass) {
    try {
      await WorkerMailer.send(
        {
          host: smtpHost,
          port: smtpPort,
          secure: smtpPort === 465,
          authType: ['plain', 'login'],
          credentials: {
            username: smtpUser,
            password: smtpPass
          }
        },
        {
          from: {
            name: 'สโมสรนักศึกษา BBA UBRU',
            email: smtpUser
          },
          to: to,
          subject: subject,
          html: htmlBody
        }
      );
      return { success: true, method: 'Gmail SMTP' };
    } catch (err) {
      return { success: false, reason: `SMTP error: ${err.message}` };
    }
  }

  // 3. Resend API
  if (resendKey) {
    try {
      const res = await fetch('https://api.resend.com/emails', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${resendKey}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          from: env.EMAIL_FROM || 'BBA UBRU <onboarding@resend.dev>',
          to: [to],
          subject: subject,
          html: htmlBody
        })
      });
      if (res.ok) {
        return { success: true, method: 'Resend API' };
      }
      const err = await res.json();
      return { success: false, reason: `Resend error: ${err.message || JSON.stringify(err)}` };
    } catch (err) {
      return { success: false, reason: `Resend fetch error: ${err.message}` };
    }
  }

  // 4. Brevo API
  if (brevoKey) {
    try {
      const res = await fetch('https://api.brevo.com/v3/smtp/email', {
        method: 'POST',
        headers: {
          'api-key': brevoKey,
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify({
          sender: { name: 'สโมสรนักศึกษา BBA UBRU', email: smtpUser || 'bba.ubru@gmail.com' },
          to: [{ email: to }],
          subject: subject,
          htmlContent: htmlBody
        })
      });
      if (res.ok) {
        return { success: true, method: 'Brevo API' };
      }
      const err = await res.json();
      return { success: false, reason: `Brevo error: ${err.message || JSON.stringify(err)}` };
    } catch (err) {
      return { success: false, reason: `Brevo fetch error: ${err.message}` };
    }
  }

  return {
    success: false,
    reason: 'ยังไม่ได้ตั้งค่าช่องทางส่งอีเมลในระบบ (เช่น Gmail SMTP หรือ Google Apps Script Relay)'
  };
}

function parseDataUrl(dataUrl) {
  if (!dataUrl || typeof dataUrl !== 'string') return null;
  const match = dataUrl.match(/^data:([^;]+);base64,(.+)$/);
  if (!match) return null;
  const contentType = match[1];
  const base64Data = match[2];
  const buffer = Buffer.from(base64Data, 'base64');
  return { contentType, buffer };
}

// ==================== 1. PUBLIC API (หน้าบ้านร้านค้า) ====================

// 1.1 ดึงข้อมูลร้านค้า สินค้า และหมวดหมู่
app.get('/api/store', async (c) => {
  try {
    const db = c.env.DB;

    // ดึงการตั้งค่าทั้งหมด
    const { results: settingsRows } = await db.prepare("SELECT key, value FROM settings").all();
    const settings = Object.fromEntries(settingsRows.map(r => [r.key, r.value]));

    // ดึงหมวดหมู่
    const { results: categoryRows } = await db.prepare("SELECT name FROM categories ORDER BY id ASC").all();
    const categories = categoryRows.map(r => r.name);

    // ดึงสินค้า
    const { results: productRows } = await db.prepare("SELECT * FROM products ORDER BY created_at ASC").all();
    const products = productRows.map(p => {
      let sizeStock = {};
      try { sizeStock = JSON.parse(p.size_stock || '{}'); } catch (e) {}

      return {
        productId: p.product_id,
        name: p.name,
        category: p.category,
        price: Number(p.price) || 0,
        sizes: p.sizes ? p.sizes.split(',').map(s => s.trim()).filter(Boolean) : [],
        colors: p.colors ? p.colors.split(',').map(s => s.trim()).filter(Boolean) : [],
        stock: Number(p.stock) || 0,
        sizeStock: sizeStock,
        imageUrl: p.image_url || 'https://placehold.co/400x500?text=BBA+Shirt',
        description: p.description || '',
        status: p.status || 'พร้อมจำหน่าย',
        sizeGuide: p.size_guide || ''
      };
    });

    return c.json({
      success: true,
      storeName: settings.StoreName || 'SMO BBA SHOP',
      clubLocation: settings.ClubLocation || 'ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ',
      clubSchedule: settings.ClubSchedule || 'วันจันทร์ - ศุกร์ เวลา 09:00 - 16:30 น.',
      promptPay: normalizePhone(settings.PromptPay || '004999245434427'),
      bankDetails: settings.BankDetails || '',
      allowedDomain: settings.AllowedDomain || '@ubru.ac.th',
      categories: categories.length > 0 ? categories : ['เสื้อโปโล', 'เสื้อยืด Oversize', 'เสื้อกิจกรรม', 'เสื้อกันหนาว/แจ็คเก็ต'],
      products: products
    });
  } catch (error) {
    return c.json({ success: false, message: error.toString() }, 500);
  }
});

// 1.2 ลงทะเบียนนักศึกษาใหม่
app.post('/api/register', async (c) => {
  try {
    const db = c.env.DB;
    const body = await c.req.json();

    const studentId = normalizeStudentId(body.studentId);
    const name = String(body.name || '').trim();
    const email = String(body.email || '').trim().toLowerCase();
    const phone = normalizePhone(body.phone);
    const pin = normalizePin(body.pin);

    if (!studentId || !name || !email || !phone || !pin) {
      return c.json({ success: false, message: 'กรุณากรอกข้อมูลให้ครบถ้วนทุกช่อง' }, 400);
    }

    if (pin.length !== 4) {
      return c.json({ success: false, message: 'รหัส PIN ต้องเป็นตัวเลข 4 หลักเท่านั้น' }, 400);
    }

    // ตรวจสอบว่าเคยลงทะเบียนไว้แล้วหรือไม่
    const existing = await db.prepare("SELECT student_id FROM customers WHERE student_id = ?").bind(studentId).first();
    if (existing) {
      return c.json({ success: false, message: 'รหัสนักศึกษานี้ลงทะเบียนไว้ในระบบแล้ว กรุณาเข้าสู่ระบบ' }, 400);
    }

    await db.prepare(`
      INSERT INTO customers (student_id, name, email, phone, pin)
      VALUES (?, ?, ?, ?, ?)
    `).bind(studentId, name, email, phone, pin).run();

    return c.json({
      success: true,
      message: 'ลงทะเบียนสำเร็จ! เข้าสู่ระบบเพื่อเริ่มสั่งจองเสื้อได้ทันที',
      student: { studentId, name, email, phone }
    });
  } catch (error) {
    return c.json({ success: false, message: error.toString() }, 500);
  }
});

// 1.3 เข้าสู่ระบบนักศึกษา
app.post('/api/login', async (c) => {
  try {
    const db = c.env.DB;
    const body = await c.req.json();

    const studentId = normalizeStudentId(body.studentId);
    const pin = normalizePin(body.pin);

    if (!studentId || !pin) {
      return c.json({ success: false, message: 'กรุณากรอกรหัสนักศึกษาและรหัส PIN' }, 400);
    }

    const customer = await db.prepare("SELECT * FROM customers WHERE student_id = ?").bind(studentId).first();
    if (!customer) {
      return c.json({ success: false, message: 'ไม่พบรหัสนักศึกษานี้ในระบบ กรุณาลงทะเบียนก่อนเข้าสู่ระบบ' }, 404);
    }

    if (normalizePin(customer.pin) !== pin) {
      return c.json({ success: false, message: 'รหัส PIN 4 หลักไม่ถูกต้อง กรุณาลองใหม่อีกครั้ง' }, 401);
    }

    return c.json({
      success: true,
      message: 'เข้าสู่ระบบสำเร็จ',
      student: {
        studentId: customer.student_id,
        name: customer.name,
        email: customer.email,
        phone: customer.phone
      }
    });
  } catch (error) {
    return c.json({ success: false, message: error.toString() }, 500);
  }
});

// 1.4 ดึงประวัติการสั่งซื้อของนักศึกษา
app.post('/api/my-orders', async (c) => {
  try {
    const db = c.env.DB;
    const body = await c.req.json();

    const studentId = normalizeStudentId(body.studentId);
    if (!studentId) {
      return c.json({ success: false, message: 'กรุณาระบุรหัสนักศึกษา' }, 400);
    }

    const pin = normalizePin(body.pin);

    // ยืนยันตัวตนก่อนดูประวัติ: หากในคำขอระบุ PIN มาและในระบบมี PIN ให้ตรวจสอบความถูกต้อง
    const customer = await db.prepare("SELECT pin FROM customers WHERE student_id = ?").bind(studentId).first();
    if (customer && customer.pin && pin) {
      if (normalizePin(customer.pin) !== pin) {
        return c.json({ success: false, message: 'รหัส PIN ไม่ถูกต้อง' }, 401);
      }
    }

    const { results: orders } = await db.prepare(`
      SELECT * FROM orders WHERE student_id = ? ORDER BY order_date DESC
    `).bind(studentId).all();

    return c.json({
      success: true,
      orders: orders.map(o => ({
        orderId: o.order_id,
        orderDate: o.order_date,
        studentId: o.student_id,
        customerName: o.customer_name,
        studentEmail: o.student_email,
        phone: o.phone,
        itemsSummary: o.items_summary,
        itemsJSON: o.items_json,
        totalAmount: o.total_amount,
        slipUrl: o.slip_url,
        status: o.status,
        pickupLocation: o.pickup_location,
        adminNotes: o.admin_notes
      }))
    });
  } catch (error) {
    return c.json({ success: false, message: error.toString() }, 500);
  }
});

// 1.5 บันทึกคำสั่งซื้อ + ตัดสต็อกแบบ Real-time Atomic Transaction
app.post('/api/orders', async (c) => {
  try {
    const db = c.env.DB;
    const body = await c.req.json();
    const orderData = body.orderData || body;

    const studentId = normalizeStudentId(orderData.studentId);
    const customerName = String(orderData.customerName || '').trim();
    const studentEmail = String(orderData.studentEmail || '').trim().toLowerCase();
    const phone = normalizePhone(orderData.phone);
    const items = Array.isArray(orderData.items) ? orderData.items : [];
    const slipBase64 = orderData.slipBase64 || '';
    const pickupLocation = orderData.pickupLocation || 'ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ';

    if (!studentId || !customerName || items.length === 0) {
      return c.json({ success: false, message: 'ข้อมูลคำสั่งซื้อไม่ครบถ้วน' }, 400);
    }

    // 1. ตรวจสอบสต็อกสินค้าปัจจุบัน
    const productIds = items.map(it => it.productId);
    const placeholders = productIds.map(() => '?').join(',');
    const { results: prods } = await db.prepare(`SELECT * FROM products WHERE product_id IN (${placeholders})`).bind(...productIds).all();
    const prodMap = new Map(prods.map(p => [p.product_id, p]));

    for (const item of items) {
      const prod = prodMap.get(item.productId);
      if (!prod) {
        return c.json({ success: false, message: `ไม่พบสินค้ารหัส ${item.productId}` }, 400);
      }
      if (prod.status === 'ปิดรับการสั่งจอง') {
        return c.json({ success: false, message: `สินค้า "${prod.name}" ปิดรับการสั่งจองแล้ว` }, 400);
      }

      let sizeStock = {};
      try { sizeStock = JSON.parse(prod.size_stock || '{}'); } catch (e) {}

      if (item.size && sizeStock[item.size] !== undefined) {
        const currentSizeStock = Number(sizeStock[item.size]) || 0;
        if (currentSizeStock < item.qty) {
          return c.json({ success: false, message: `สินค้า "${prod.name}" ไซส์ ${item.size} คงเหลือเพียง ${currentSizeStock} ตัว (ต้องการสั่ง ${item.qty} ตัว)` }, 400);
        }
      } else {
        if (Number(prod.stock) < item.qty) {
          return c.json({ success: false, message: `สินค้า "${prod.name}" คงเหลือไม่เพียงพอ` }, 400);
        }
      }
    }

    // 2. สร้างรหัสคำสั่งซื้อที่ไม่ซ้ำกัน 100% (High-Entropy UUID Collision-free)
    // ใช้ UUID สุ่ม 8 หลัก ไม่ต้อง SELECT COUNT(*) ป้องกัน Order ID ชนกันเมื่อมีคำขอสั่งซื้อพร้อมกัน
    const now = new Date();
    const y = String(now.getFullYear()).slice(-2);
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const d = String(now.getDate()).padStart(2, '0');
    const datePrefix = `BBA-${y}${m}${d}`;
    const uniqueSuffix = crypto.randomUUID().replace(/-/g, '').slice(0, 8).toUpperCase();
    const orderId = `${datePrefix}-${uniqueSuffix}`;

    // คำนวณยอดเงินรวม
    let calculatedTotal = 0;
    const itemsSummaryArr = [];

    items.forEach(it => {
      const basePrice = Number(it.price) || 0;
      let extraPrice = 0;
      const sz = String(it.size || '').toUpperCase();
      if (sz === '2XL' || sz === 'XXL') extraPrice = 20;
      else if (sz === '3XL' || sz === 'XXXL') extraPrice = 40;
      else if (sz === '4XL' || sz === 'XXXXL') extraPrice = 60;

      const unitPrice = basePrice + extraPrice;
      const subtotal = unitPrice * it.qty;
      calculatedTotal += subtotal;

      itemsSummaryArr.push(`${it.name} [ไซส์ ${it.size}, สี ${it.color}] x ${it.qty}`);
    });

    const itemsSummary = itemsSummaryArr.join(', ');
    const paymentId = `PAY-${orderId}`;

    // อัปโหลดสลิปเข้า Cloudflare R2 (ถ้าเปิดใช้งาน)
    let slipUrl = '';
    if (slipBase64 && c.env.BUCKET) {
      try {
        const parsed = parseDataUrl(slipBase64);
        if (parsed) {
          const ext = parsed.contentType.includes('png') ? 'png' : 'jpg';
          const r2Key = `slips/${orderId}-${Date.now()}.${ext}`;
          await c.env.BUCKET.put(r2Key, parsed.buffer, {
            httpMetadata: { contentType: parsed.contentType }
          });
          slipUrl = `/api/images/${r2Key}`;
        } else {
          slipUrl = slipBase64;
        }
      } catch (err) {
        console.error('R2 slip upload error:', err);
        slipUrl = slipBase64;
      }
    } else {
      slipUrl = slipBase64 || '';
    }

    // 3. เตรียมคำสั่ง Batch Transaction: บันทึกคำสั่งซื้อ + ตัดสต็อกแบบ Conditional Atomic SQL
    // มี Trigger ป้องกันสต็อกติดลบ check_product_stock_non_negative คอย Rollback ทันทีหากเกิด Race Condition
    const batchOps = [
      // บันทึกลงตาราง orders
      db.prepare(`
        INSERT INTO orders (order_id, student_id, customer_name, student_email, phone, items_summary, items_json, total_amount, slip_url, status, pickup_location)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `).bind(orderId, studentId, customerName, studentEmail, phone, itemsSummary, JSON.stringify(items), calculatedTotal, slipUrl, 'รอตรวจสอบการชำระเงิน', pickupLocation),

      // บันทึกลงตาราง payments
      db.prepare(`
        INSERT INTO payments (payment_id, order_id, student_id, amount, slip_url, verify_status)
        VALUES (?, ?, ?, ?, ?, ?)
      `).bind(paymentId, orderId, studentId, calculatedTotal, slipUrl, 'รอตรวจสอบ')
    ];

    // ตัดสต็อกสินค้าทีละรายการด้วยคำสั่ง SQL ที่คำนวณและตรวจสอบเงื่อนไขในระดับฐานข้อมูลโดยตรง
    for (const item of items) {
      if (item.size) {
        // มีระบุไซส์: ตัดสต็อกระดับไซส์ใน JSON และตัดสต็อกรวม พร้อมตรวจเงื่อนไขสต็อกคงเหลือ
        // รองรับชื่อไซส์ทุกรูปแบบ (เช่น M, 2XL, Free Size) ด้วย JSON path แบบ $."key"
        const jsonPath = '$."' + String(item.size).replace(/"/g, '\\"') + '"';
        batchOps.push(
          db.prepare(`
            UPDATE products
            SET stock = stock - ?,
                size_stock = CASE
                  WHEN json_extract(size_stock, ?) IS NOT NULL
                  THEN json_set(size_stock, ?, CAST(json_extract(size_stock, ?) AS INT) - ?)
                  ELSE size_stock
                END,
                status = CASE WHEN (stock - ?) <= 0 THEN 'สินค้าหมด' ELSE status END,
                updated_at = CURRENT_TIMESTAMP
            WHERE product_id = ?
              AND stock >= ?
              AND (
                json_extract(size_stock, ?) IS NULL
                OR CAST(json_extract(size_stock, ?) AS INT) >= ?
              )
          `).bind(
            item.qty,
            jsonPath,
            jsonPath,
            jsonPath,
            item.qty,
            item.qty,
            item.productId,
            item.qty,
            jsonPath,
            jsonPath,
            item.qty
          )
        );
      } else {
        // สินค้าไม่มีไซส์: ตัดสต็อกรวมโดยตรง
        batchOps.push(
          db.prepare(`
            UPDATE products
            SET stock = stock - ?,
                status = CASE WHEN (stock - ?) <= 0 THEN 'สินค้าหมด' ELSE status END,
                updated_at = CURRENT_TIMESTAMP
            WHERE product_id = ?
              AND stock >= ?
          `).bind(
            item.qty,
            item.qty,
            item.productId,
            item.qty
          )
        );
      }
    }

    // ประมวลผล Batch Transaction ใน Cloudflare D1
    // หากมีคำขอสั่งซื้ออื่นตัดสต็อกไปก่อนจนสต็อกไม่พอ คำสั่ง UPDATE จะไม่ตรงเงื่อนไข หรือ Trigger จะสั่ง ABORT ทันที
    const batchResults = await db.batch(batchOps);

    // ตรวจสอบผลลัพธ์: ตรวจสอบว่าคำสั่งตัดสต็อกทุกรายการมีแถวที่ถูกอัปเดตจริง (changes > 0)
    // batchResults[0] คือ INSERT orders, batchResults[1] คือ INSERT payments, รายการที่เหลือคือ UPDATE products
    for (let i = 2; i < batchResults.length; i++) {
      const updateRes = batchResults[i];
      if (!updateRes || updateRes.meta?.changes === 0) {
        // หากรายการใดตัดสต็อกไม่สำเร็จ (ถูกสั่งซื้อไปก่อนในเสี้ยววินาที) ทำการ Rollback คำสั่งซื้อทันที
        await db.prepare("DELETE FROM orders WHERE order_id = ?").bind(orderId).run();
        await db.prepare("DELETE FROM payments WHERE order_id = ?").bind(orderId).run();
        return c.json({
          success: false,
          code: 'CONCURRENT_STOCK_EXHAUSTED',
          message: 'ขออภัย สินค้าบางรายการหรือไซส์ที่คุณเลือกเพิ่งถูกสั่งซื้อหมดไปเมื่อสักครู่ กรุณาตรวจสอบจำนวนคงเหลือในหน้าร้านอีกครั้ง'
        }, 409);
      }
    }

    return c.json({
      success: true,
      message: 'บันทึกคำสั่งซื้อสำเร็จ',
      orderId: orderId,
      totalAmount: calculatedTotal
    });
  } catch (error) {
    const errMsg = String(error.message || error);
    console.error('[Order Error]', errMsg);
    if (
      errMsg.includes('INSUFFICIENT_STOCK') ||
      errMsg.includes('UNIQUE constraint failed') ||
      errMsg.includes('CHECK constraint failed')
    ) {
      return c.json({
        success: false,
        code: 'CONCURRENT_STOCK_EXHAUSTED',
        message: 'ขออภัย สินค้าบางรายการหรือไซส์ที่คุณเลือกเพิ่งถูกสั่งซื้อหมดไปเมื่อสักครู่ กรุณาตรวจสอบจำนวนคงเหลือในหน้าร้านอีกครั้ง'
      }, 409);
    }
    return c.json({ success: false, message: error.toString() }, 500);
  }
});

// ==================== 2. ADMIN API (หลังบ้านสโมสร) ====================

// 2.1 เข้าสู่ระบบแอดมิน (Verify PIN)
app.post('/api/admin/login', async (c) => {
  try {
    const db = c.env.DB;
    const body = await c.req.json();
    const isValid = await verifyAdminPin(db, body.pin);

    if (!isValid) {
      return c.json({ success: false, message: 'รหัส PIN ไม่ถูกต้อง' }, 401);
    }
    return c.json({ success: true, message: 'ยืนยันสิทธิ์สำเร็จ' });
  } catch (error) {
    return c.json({ success: false, message: error.toString() }, 500);
  }
});

// 2.2 ดึงข้อมูลหลังบ้านทั้งหมด (Stats, Orders, Products, Categories)
app.post('/api/admin/data', async (c) => {
  try {
    const db = c.env.DB;
    const body = await c.req.json();
    const isValid = await verifyAdminPin(db, body.pin);

    if (!isValid) {
      return c.json({ success: false, message: 'รหัส PIN ไม่ถูกต้อง' }, 401);
    }

    // สถิติยอดขายและสถานะคำสั่งซื้อ
    const salesRow = await db.prepare("SELECT SUM(total_amount) as total FROM orders WHERE status != 'ยกเลิกคำสั่งซื้อ'").first();
    const totalSales = Number(salesRow ? salesRow.total : 0) || 0;

    const countRows = await db.prepare("SELECT status, COUNT(*) as count FROM orders GROUP BY status").all();
    const statusMap = Object.fromEntries(countRows.results.map(r => [r.status, r.count]));

    // คำสั่งซื้อทั้งหมด
    const { results: orders } = await db.prepare("SELECT * FROM orders ORDER BY order_date DESC").all();

    // สินค้าทั้งหมด
    const { results: products } = await db.prepare("SELECT * FROM products ORDER BY created_at ASC").all();

    // หมวดหมู่
    const { results: categories } = await db.prepare("SELECT name FROM categories ORDER BY id ASC").all();

    return c.json({
      success: true,
      stats: {
        totalSales: totalSales,
        pendingCount: statusMap['รอตรวจสอบการชำระเงิน'] || 0,
        paidCount: statusMap['ชำระเงินเรียบร้อย'] || 0,
        readyCount: statusMap['พร้อมรับเสื้อที่ห้องสโมสร'] || 0,
        completedCount: statusMap['รับเสื้อแล้ว'] || 0
      },
      orders: orders.map(o => ({
        orderId: o.order_id,
        orderDate: o.order_date,
        studentId: o.student_id,
        customerName: o.customer_name,
        studentEmail: o.student_email,
        phone: o.phone,
        itemsSummary: o.items_summary,
        itemsJSON: o.items_json,
        totalAmount: o.total_amount,
        slipUrl: o.slip_url,
        status: o.status,
        pickupLocation: o.pickup_location,
        adminNotes: o.admin_notes
      })),
      products: products.map(p => {
        let sizeStock = {};
        try { sizeStock = JSON.parse(p.size_stock || '{}'); } catch (e) {}
        return {
          productId: p.product_id,
          name: p.name,
          category: p.category,
          price: Number(p.price) || 0,
          sizes: p.sizes ? p.sizes.split(',').map(s => s.trim()).filter(Boolean) : [],
          colors: p.colors ? p.colors.split(',').map(s => s.trim()).filter(Boolean) : [],
          stock: Number(p.stock) || 0,
          sizeStock: sizeStock,
          imageUrl: p.image_url,
          description: p.description,
          status: p.status,
          sizeGuide: p.size_guide
        };
      }),
      categories: categories.map(c => c.name)
    });
  } catch (error) {
    return c.json({ success: false, message: error.toString() }, 500);
  }
});

// 2.3 อัปเดตสถานะคำสั่งซื้อ (+ คืนสต็อกกรณีสั่งยกเลิก)
app.post('/api/admin/orders/status', async (c) => {
  try {
    const db = c.env.DB;
    const body = await c.req.json();
    const isValid = await verifyAdminPin(db, body.pin);

    if (!isValid) return c.json({ success: false, message: 'รหัส PIN ไม่ถูกต้อง' }, 401);

    const { orderId, newStatus, adminNotes, notifyEmail } = body;
    const order = await db.prepare("SELECT * FROM orders WHERE order_id = ?").bind(orderId).first();
    if (!order) return c.json({ success: false, message: 'ไม่พบคำสั่งซื้อนี้' }, 404);

    const oldStatus = order.status;
    const finalAdminNotes = (adminNotes !== undefined) ? adminNotes : (order.admin_notes || '');
    const batchOps = [
      db.prepare("UPDATE orders SET status = ?, admin_notes = ? WHERE order_id = ?").bind(newStatus, finalAdminNotes, orderId),
      db.prepare("UPDATE payments SET verify_status = ?, admin_notes = ? WHERE order_id = ?").bind(newStatus, finalAdminNotes, orderId)
    ];

    // คืนสต็อกสินค้าอัตโนมัติหากเปลี่ยนเป็น 'ยกเลิกคำสั่งซื้อ'
    if (newStatus === 'ยกเลิกคำสั่งซื้อ' && oldStatus !== 'ยกเลิกคำสั่งซื้อ') {
      let items = [];
      try { items = JSON.parse(order.items_json || '[]'); } catch (e) {}

      for (const item of items) {
        const prod = await db.prepare("SELECT * FROM products WHERE product_id = ?").bind(item.productId).first();
        if (prod) {
          let sizeStock = {};
          try { sizeStock = JSON.parse(prod.size_stock || '{}'); } catch (e) {}

          let newTotal = Number(prod.stock) + item.qty;
          if (item.size && sizeStock[item.size] !== undefined) {
            sizeStock[item.size] = Number(sizeStock[item.size]) + item.qty;
            newTotal = Object.values(sizeStock).reduce((acc, v) => acc + (Number(v) || 0), 0);
          }

          let newProdStatus = prod.status;
          if (newProdStatus !== 'ปิดรับการสั่งจอง' && newTotal > 0) {
            newProdStatus = 'พร้อมจำหน่าย';
          }

          batchOps.push(
            db.prepare("UPDATE products SET stock = ?, size_stock = ?, status = ? WHERE product_id = ?")
              .bind(newTotal, JSON.stringify(sizeStock), newProdStatus, item.productId)
          );
        }
      }
    }

    // กรณีเปลี่ยนจาก 'ยกเลิกคำสั่งซื้อ' กลับมาเป็นสถานะปกติ (ตัดสต็อกกลับ)
    if (oldStatus === 'ยกเลิกคำสั่งซื้อ' && newStatus !== 'ยกเลิกคำสั่งซื้อ') {
      let items = [];
      try { items = JSON.parse(order.items_json || '[]'); } catch (e) {}

      for (const item of items) {
        const prod = await db.prepare("SELECT * FROM products WHERE product_id = ?").bind(item.productId).first();
        if (prod) {
          let sizeStock = {};
          try { sizeStock = JSON.parse(prod.size_stock || '{}'); } catch (e) {}

          let newTotal = Math.max(0, Number(prod.stock) - item.qty);
          if (item.size && sizeStock[item.size] !== undefined) {
            sizeStock[item.size] = Math.max(0, Number(sizeStock[item.size]) - item.qty);
            newTotal = Object.values(sizeStock).reduce((acc, v) => acc + (Number(v) || 0), 0);
          }

          let newProdStatus = prod.status;
          if (newTotal <= 0 && newProdStatus === 'พร้อมจำหน่าย') {
            newProdStatus = 'สินค้าหมด';
          }

          batchOps.push(
            db.prepare("UPDATE products SET stock = ?, size_stock = ?, status = ? WHERE product_id = ?")
              .bind(newTotal, JSON.stringify(sizeStock), newProdStatus, item.productId)
          );
        }
      }
    }

    await db.batch(batchOps);

    // ส่งอีเมลแจ้งเตือนนักศึกษาเมื่อเปลี่ยนสถานะเป็น 'พร้อมรับเสื้อที่ห้องสโมสร'
    let emailNotice = '';
    if (newStatus === 'พร้อมรับเสื้อที่ห้องสโมสร' && notifyEmail !== false) {
      const studentEmail = String(order.student_email || '').trim();
      if (studentEmail && studentEmail.includes('@')) {
        try {
          const { results: setRows } = await db.prepare(
            "SELECT key, value FROM settings WHERE key IN ('ClubLocation', 'ClubSchedule')"
          ).all();
          const settingsMap = Object.fromEntries(setRows.map(r => [r.key, r.value]));

          const pickupLocation = order.pickup_location || settingsMap.ClubLocation || 'ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ';
          const clubSchedule = settingsMap.ClubSchedule || 'วันจันทร์ - ศุกร์ เวลา 09:00 - 16:30 น.';

          const subject = `🎉 [BBA UBRU] เสื้อสโมสรของคุณพร้อมให้มารับแล้ว! - ${order.order_id}`;
          const htmlBody = buildReadyPickupEmailHtml({
            name: order.customer_name,
            studentId: order.student_id,
            orderId: order.order_id,
            itemsSummary: order.items_summary,
            total: order.total_amount,
            pickupLocation: pickupLocation,
            clubSchedule: clubSchedule
          });

          const mailRes = await sendNotificationEmail(c.env, {
            to: studentEmail,
            subject: subject,
            htmlBody: htmlBody
          });

          if (mailRes.success) {
            emailNotice = `\n(ส่งอีเมลแจ้งพร้อมรับเสื้อไปยัง ${studentEmail} เรียบร้อยแล้ว)`;
          } else {
            emailNotice = `\n(บันทึกสถานะแล้ว แต่ยังส่งอีเมลไม่สำเร็จ: ${mailRes.reason})`;
          }
        } catch (mailErr) {
          emailNotice = `\n(บันทึกสถานะแล้ว แต่เกิดข้อผิดพลาดในการส่งอีเมล: ${mailErr.message})`;
        }
      } else {
        emailNotice = '\n(ไม่พบอีเมลนักศึกษาสำหรับคำสั่งซื้อนี้)';
      }
    }

    return c.json({ success: true, message: `อัปเดตสถานะเป็น "${newStatus}" สำเร็จเรียบร้อย${emailNotice}` });
  } catch (error) {
    return c.json({ success: false, message: error.toString() }, 500);
  }
});

// 2.4 เพิ่มหรือแก้ไขข้อมูลสินค้า
app.post('/api/admin/products', async (c) => {
  try {
    const db = c.env.DB;
    const body = await c.req.json();
    const isValid = await verifyAdminPin(db, body.pin);

    if (!isValid) return c.json({ success: false, message: 'รหัส PIN ไม่ถูกต้อง' }, 401);

    const mode = body.mode || 'add';
    let productId = String(body.productId || '').trim();
    const name = String(body.name || '').trim();
    const category = String(body.category || 'เสื้อทั่วไป').trim();
    const price = Number(body.price) || 0;
    const sizes = String(body.sizes || '').trim();
    const colors = String(body.colors || '').trim();
    const sizeStock = typeof body.sizeStock === 'object' ? JSON.stringify(body.sizeStock) : '{}';
    const stock = Number(body.stock) || 0;
    const description = String(body.description || '').trim();
    const status = String(body.status || 'พร้อมจำหน่าย').trim();
    const sizeGuide = String(body.sizeGuide || '').trim();

    if (mode === 'add' && !productId) {
      const countRow = await db.prepare("SELECT COUNT(*) as count FROM products").first();
      const nextId = (countRow ? countRow.count : 0) + 1;
      productId = `BBA-${String(nextId).padStart(2, '0')}`;
    }

    // จัดการรูปภาพ (ถ้ามีภาพใหม่ที่เป็น Base64 ให้อัปโหลดเข้า Cloudflare R2 ทันที)
    let imageUrlArr = [];
    if (Array.isArray(body.images)) {
      for (let i = 0; i < body.images.length; i++) {
        const img = body.images[i];
        const raw = typeof img === 'string' ? img : (img.url || img.base64 || '');
        if (!raw) continue;

        if (c.env.BUCKET && raw.startsWith('data:image/')) {
          try {
            const parsed = parseDataUrl(raw);
            if (parsed) {
              const ext = parsed.contentType.includes('png') ? 'png' : 'jpg';
              const r2Key = `products/${productId}-${Date.now()}-${i + 1}.${ext}`;
              await c.env.BUCKET.put(r2Key, parsed.buffer, {
                httpMetadata: { contentType: parsed.contentType }
              });
              imageUrlArr.push(`/api/images/${r2Key}`);
              continue;
            }
          } catch (e) {
            console.error('R2 product image upload error:', e);
          }
        }
        imageUrlArr.push(raw);
      }
    } else if (body.imageUrl) {
      const raw = String(body.imageUrl).trim();
      if (c.env.BUCKET && raw.startsWith('data:image/')) {
        try {
          const parsed = parseDataUrl(raw);
          if (parsed) {
            const ext = parsed.contentType.includes('png') ? 'png' : 'jpg';
            const r2Key = `products/${productId}-${Date.now()}-1.${ext}`;
            await c.env.BUCKET.put(r2Key, parsed.buffer, {
              httpMetadata: { contentType: parsed.contentType }
            });
            imageUrlArr.push(`/api/images/${r2Key}`);
          } else {
            imageUrlArr.push(raw);
          }
        } catch (e) {
          imageUrlArr.push(raw);
        }
      } else {
        imageUrlArr.push(raw);
      }
    }

    let imageUrl = imageUrlArr.filter(Boolean).join(' ||| ');
    if (!imageUrl) imageUrl = 'https://placehold.co/400x500?text=BBA+Shirt';

    if (mode === 'add') {
      await db.prepare(`
        INSERT INTO products (product_id, name, category, price, sizes, colors, stock, size_stock, image_url, description, status, size_guide)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `).bind(productId, name, category, price, sizes, colors, stock, sizeStock, imageUrl, description, status, sizeGuide).run();

      return c.json({ success: true, message: `เพิ่มสินค้า ${name} (${productId}) สำเร็จ` });
    } else {
      await db.prepare(`
        UPDATE products
        SET name = ?, category = ?, price = ?, sizes = ?, colors = ?, stock = ?, size_stock = ?, image_url = ?, description = ?, status = ?, size_guide = ?, updated_at = CURRENT_TIMESTAMP
        WHERE product_id = ?
      `).bind(name, category, price, sizes, colors, stock, sizeStock, imageUrl, description, status, sizeGuide, productId).run();

      return c.json({ success: true, message: `บันทึกข้อมูลสินค้า ${name} สำเร็จ` });
    }
  } catch (error) {
    return c.json({ success: false, message: error.toString() }, 500);
  }
});

// 2.5 ปิด/เปิดรับจองรายสินค้า
app.post('/api/admin/products/toggle-preorder', async (c) => {
  try {
    const db = c.env.DB;
    const body = await c.req.json();
    const isValid = await verifyAdminPin(db, body.pin);

    if (!isValid) return c.json({ success: false, message: 'รหัส PIN ไม่ถูกต้อง' }, 401);

    const { productId, status } = body;
    await db.prepare("UPDATE products SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?").bind(status, productId).run();

    return c.json({ success: true, message: `ปรับสถานะเป็น "${status}" เรียบร้อย` });
  } catch (error) {
    return c.json({ success: false, message: error.toString() }, 500);
  }
});

// 2.5.1 ลบรายการสินค้าออกจากระบบสต็อก
app.post('/api/admin/products/delete', async (c) => {
  try {
    const db = c.env.DB;
    const body = await c.req.json();
    const isValid = await verifyAdminPin(db, body.pin);

    if (!isValid) return c.json({ success: false, message: 'รหัส PIN ไม่ถูกต้อง' }, 401);

    const productId = String(body.productId || '').trim();
    if (!productId) return c.json({ success: false, message: 'กรุณาระบุรหัสสินค้าที่ต้องการลบ' }, 400);

    await db.prepare("DELETE FROM products WHERE product_id = ?").bind(productId).run();

    return c.json({ success: true, message: `ลบรายการสินค้า ${productId} ออกจากระบบเรียบร้อยแล้ว` });
  } catch (error) {
    return c.json({ success: false, message: error.toString() }, 500);
  }
});

// 2.6 เพิ่มหมวดหมู่เสื้อ
app.post('/api/admin/categories/add', async (c) => {
  try {
    const db = c.env.DB;
    const body = await c.req.json();
    const isValid = await verifyAdminPin(db, body.pin);

    if (!isValid) return c.json({ success: false, message: 'รหัส PIN ไม่ถูกต้อง' }, 401);

    const category = String(body.category || '').trim();
    if (!category) return c.json({ success: false, message: 'กรุณาระบุชื่อหมวดหมู่' }, 400);

    await db.prepare("INSERT OR IGNORE INTO categories (name) VALUES (?)").bind(category).run();

    const { results } = await db.prepare("SELECT name FROM categories ORDER BY id ASC").all();
    return c.json({ success: true, message: `เพิ่มหมวดหมู่ "${category}" สำเร็จ`, categories: results.map(r => r.name) });
  } catch (error) {
    return c.json({ success: false, message: error.toString() }, 500);
  }
});

// 2.7 ลบหมวดหมู่เสื้อ
app.post('/api/admin/categories/delete', async (c) => {
  try {
    const db = c.env.DB;
    const body = await c.req.json();
    const isValid = await verifyAdminPin(db, body.pin);

    if (!isValid) return c.json({ success: false, message: 'รหัส PIN ไม่ถูกต้อง' }, 401);

    const category = String(body.category || '').trim();
    await db.prepare("DELETE FROM categories WHERE name = ?").bind(category).run();

    const { results } = await db.prepare("SELECT name FROM categories ORDER BY id ASC").all();
    return c.json({ success: true, message: `ลบหมวดหมู่ "${category}" สำเร็จ`, categories: results.map(r => r.name) });
  } catch (error) {
    return c.json({ success: false, message: error.toString() }, 500);
  }
});

// ==================== 2.8 SERVE IMAGES FROM CLOUDFLARE R2 ====================
app.get('/api/images/*', async (c) => {
  try {
    const key = c.req.path.replace(/^\/api\/images\//, '');
    if (!key) return c.text('Not found', 404);

    const bucket = c.env.BUCKET;
    if (!bucket) return c.text('R2 Storage not configured', 500);

    const object = await bucket.get(key);
    if (!object) return c.text('Image not found', 404);

    const headers = new Headers();
    object.writeHttpMetadata(headers);
    headers.set('etag', object.httpEtag);
    // แคชรูปภาพ 7 วัน ทั้งที่เบราว์เซอร์และ Cloudflare Edge CDN
    headers.set('Cache-Control', 'public, max-age=604800, immutable');

    return new Response(object.body, { headers });
  } catch (err) {
    return c.text(err.toString(), 500);
  }
});

// ==================== 2.9 ADMIN SETTINGS MANAGEMENT ====================
app.post('/api/admin/settings', async (c) => {
  try {
    const db = c.env.DB;
    const body = await c.req.json();
    const isValid = await verifyAdminPin(db, body.pin);

    if (!isValid) return c.json({ success: false, message: 'รหัส PIN ไม่ถูกต้อง' }, 401);

    const { settings } = body;
    if (settings && typeof settings === 'object') {
      const ops = [];
      for (const [key, value] of Object.entries(settings)) {
        ops.push(
          db.prepare(
            "INSERT INTO settings (key, value, description) VALUES (?, ?, '') ON CONFLICT(key) DO UPDATE SET value = excluded.value"
          ).bind(key, String(value ?? ''))
        );
      }
      if (ops.length > 0) {
        await db.batch(ops);
      }
    }

    const { results } = await db.prepare("SELECT key, value, description FROM settings").all();
    return c.json({ success: true, message: 'บันทึกการตั้งค่าเรียบร้อยแล้ว', settings: results });
  } catch (error) {
    return c.json({ success: false, message: error.toString() }, 500);
  }
});

// ==================== 2.10 TEST EMAIL NOTIFICATION ====================
app.post('/api/admin/email/test', async (c) => {
  try {
    const db = c.env.DB;
    const body = await c.req.json();
    const isValid = await verifyAdminPin(db, body.pin);

    if (!isValid) return c.json({ success: false, message: 'รหัส PIN ไม่ถูกต้อง' }, 401);

    const to = String(body.to || '').trim();
    if (!to || !to.includes('@')) {
      return c.json({ success: false, message: 'กรุณาระบุอีเมลปลายทางที่ถูกต้อง' }, 400);
    }

    const testSubject = '🧪 [BBA UBRU] ทดสอบระบบส่งอีเมลแจ้งเตือนสโมสรนักศึกษา';
    const testHtml = buildReadyPickupEmailHtml({
      name: 'นักศึกษาทดสอบระบบ',
      studentId: '67123000000',
      orderId: 'TEST-BBA-001',
      itemsSummary: 'เสื้อทดสอบระบบ Size L x 1',
      total: 350,
      pickupLocation: 'ห้องสโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ',
      clubSchedule: 'วันจันทร์ - ศุกร์ เวลา 09:00 - 16:30 น.'
    });

    const res = await sendNotificationEmail(c.env, {
      to: to,
      subject: testSubject,
      htmlBody: testHtml
    });

    if (res.success) {
      return c.json({ success: true, message: `ส่งอีเมลทดสอบไปยัง ${to} สำเร็จผ่านระบบ ${res.method}` });
    } else {
      return c.json({ success: false, message: `ส่งอีเมลทดสอบไม่สำเร็จ: ${res.reason}` });
    }
  } catch (error) {
    return c.json({ success: false, message: error.toString() }, 500);
  }
});

// ==================== 3. FRONTEND STATIC ASSET ROUTING ====================

// เสิร์ฟหน้าหลัก (Shop)
app.get('/', async (c) => {
  if (c.env.ASSETS) {
    return c.env.ASSETS.fetch(new Request(new URL('/index.html', c.req.url)));
  }
  return c.text('BBA Store Frontend');
});

// เสิร์ฟหน้าแอดมิน (Admin)
app.get('/admin', async (c) => {
  if (c.env.ASSETS) {
    return c.env.ASSETS.fetch(new Request(new URL('/admin.html', c.req.url)));
  }
  return c.text('BBA Store Admin Dashboard');
});

// Fallback สำหรับ static assets อื่นๆ ใน ./public
app.get('*', async (c) => {
  if (c.env.ASSETS) {
    return c.env.ASSETS.fetch(c.req.raw);
  }
  return c.notFound();
});

export default app;
