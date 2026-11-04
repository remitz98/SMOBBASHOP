import assert from 'node:assert';

// 1. Logic under test: normalizePin, hashPin, verifyPin
function normalizePin(val) {
  if (val === null || val === undefined) return '';
  let s = String(val).trim();
  if (s.startsWith("'")) s = s.substring(1).trim();
  s = s.replace(/[^0-9]/g, '');
  if (!s) return '';
  if (s.length < 4) s = s.padStart(4, '0');
  return s;
}

async function hashPin(pin, salt = 'bba_ubru_pin_salt_') {
  const norm = normalizePin(pin);
  if (!norm) return '';
  const buf = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(salt + norm));
  return Array.from(new Uint8Array(buf)).map(b => b.toString(16).padStart(2, '0')).join('');
}

async function verifyPin(storedPin, inputPin) {
  const normInput = normalizePin(inputPin);
  if (!normInput || !storedPin) return false;
  const storedStr = String(storedPin).trim();
  if (storedStr.length === 64) {
    const inputHash = await hashPin(normInput);
    return storedStr === inputHash;
  }
  return normalizePin(storedStr) === normInput;
}

// 2. Logic under test: Rate limiter
const rateLimitMap = new Map();
function checkRateLimit(key, maxAttempts = 5, windowMs = 60000) {
  const now = Date.now();
  const record = rateLimitMap.get(key);
  if (!record || now > record.resetAt) {
    rateLimitMap.set(key, { count: 1, resetAt: now + windowMs });
    return { allowed: true };
  }
  if (record.count >= maxAttempts) {
    const waitSec = Math.ceil((record.resetAt - now) / 1000);
    return { allowed: false, waitSec };
  }
  record.count++;
  return { allowed: true };
}
function clearRateLimit(key) {
  rateLimitMap.delete(key);
}

// 3. Logic under test: R2 Slip Key Extraction
function extractSlipR2Key(slipUrl) {
  if (!slipUrl) return null;
  if (slipUrl.includes('/api/images/')) {
    return slipUrl.split('/api/images/')[1];
  } else if (slipUrl.includes('/api/slips/')) {
    return slipUrl.split('/api/slips/')[1];
  }
  return null;
}

async function runTests() {
  console.log('Running security & logic test suite...');

  // Test 1: PIN Normalization
  assert.strictEqual(normalizePin('1234'), '1234');
  assert.strictEqual(normalizePin(' 012 '), '0012');
  assert.strictEqual(normalizePin("'1234"), '1234');
  assert.strictEqual(normalizePin('abcd'), '');
  console.log('✔ Test 1: normalizePin passed');

  // Test 2: PIN Hashing & Verification
  const legacyPlaintext = '1234';
  assert.strictEqual(await verifyPin(legacyPlaintext, '1234'), true);
  assert.strictEqual(await verifyPin(legacyPlaintext, '5678'), false);

  // Test 2.1: 6-digit Admin PIN (505050)
  const admin6Digit = '505050';
  assert.strictEqual(await verifyPin(admin6Digit, '505050'), true);
  assert.strictEqual(await verifyPin(admin6Digit, '123456'), false);

  const hashed = await hashPin('1234');
  assert.strictEqual(hashed.length, 64);
  assert.strictEqual(await verifyPin(hashed, '1234'), true);
  assert.strictEqual(await verifyPin(hashed, '9999'), false);
  console.log('✔ Test 2: hashPin & verifyPin (backward-compatible) passed');

  // Test 3: Rate Limiter
  const testIp = 'ip_test_123';
  for (let i = 1; i <= 5; i++) {
    const res = checkRateLimit(testIp, 5, 10000);
    assert.strictEqual(res.allowed, true, `Attempt ${i} should be allowed`);
  }
  const blockedRes = checkRateLimit(testIp, 5, 10000);
  assert.strictEqual(blockedRes.allowed, false, '6th attempt should be blocked');
  assert(blockedRes.waitSec > 0, 'Wait time should be positive');

  clearRateLimit(testIp);
  const resetRes = checkRateLimit(testIp, 5, 10000);
  assert.strictEqual(resetRes.allowed, true, 'After clearing, attempt should be allowed');
  console.log('✔ Test 3: In-memory Rate Limiter passed');

  // Test 4: R2 Slip Key Extraction (Both formats)
  assert.strictEqual(extractSlipR2Key('/api/images/slips/ord-123.jpg'), 'slips/ord-123.jpg');
  assert.strictEqual(extractSlipR2Key('/api/slips/ord-456.png'), 'ord-456.png');
  assert.strictEqual(extractSlipR2Key(''), null);
  console.log('✔ Test 4: R2 Slip Key extraction passed');

  // Test 5: Safe DataURL / Slip parsing & MIME validation
  function parseDataUrlTest(dataUrl, maxBytes = 5 * 1024 * 1024) {
    if (!dataUrl || typeof dataUrl !== 'string') return null;
    const match = dataUrl.match(/^data:([^;]+);base64,(.+)$/);
    if (!match) return null;
    const contentType = match[1].toLowerCase().trim();
    const ALLOWED_MIME = ['image/jpeg', 'image/png', 'image/webp'];
    if (!ALLOWED_MIME.includes(contentType)) return null;
    const buffer = Buffer.from(match[2], 'base64');
    if (buffer.length > maxBytes) return null;
    return { contentType, buffer };
  }

  assert.strictEqual(parseDataUrlTest('data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg=='), null, 'HTML/Script must be rejected');
  assert.strictEqual(parseDataUrlTest('data:image/svg+xml;base64,PHN2Zz48L3N2Zz4='), null, 'SVG must be rejected');
  assert.notStrictEqual(parseDataUrlTest('data:image/jpeg;base64,/9j/4AAQSkZJRg=='), null, 'Valid JPEG should pass');
  assert.notStrictEqual(parseDataUrlTest('data:image/png;base64,iVBORw0KGgo='), null, 'Valid PNG should pass');
  assert.strictEqual(parseDataUrlTest('data:image/jpeg;base64,' + 'A'.repeat(7000000), 5 * 1024 * 1024), null, 'Oversized file (>5MB) must be rejected');
  console.log('✔ Test 5: parseDataUrl Safe MIME & Size Limit passed');

  // Test 6: Order Item & Server-Authoritative Price Calculation
  function calculateOrderTotalTest(items, dbProducts) {
    const prodMap = new Map(dbProducts.map(p => [p.product_id, p]));
    let total = 0;
    for (const it of items) {
      const qty = parseInt(it.qty, 10);
      if (isNaN(qty) || qty <= 0 || qty > 100) throw new Error('Invalid qty');
      const prod = prodMap.get(it.productId);
      if (!prod) throw new Error('Product not found');
      
      const basePrice = Number(prod.price) || 0; // DB price ONLY
      let extra = 0;
      const sz = String(it.size || '').toUpperCase().trim();
      if (sz === '2XL' || sz === 'XXL') extra = 20;
      else if (sz === '3XL' || sz === 'XXXL') extra = 40;
      else if (sz === '4XL' || sz === 'XXXXL') extra = 60;
      else if (sz === '5XL' || sz === 'XXXXXL') extra = 80;
      else if (sz === '6XL' || sz === 'XXXXXXL') extra = 100;
      else if (sz === '7XL' || sz === 'XXXXXXXL') extra = 120;
      
      total += (basePrice + extra) * qty;
    }
    return total;
  }

  const sampleDbProds = [{ product_id: 'BBA-01', price: 290 }];
  // Client attempts to spoof price as 1 baht or send negative quantity:
  const spoofedItems = [{ productId: 'BBA-01', price: 1, size: '2XL', qty: 2 }];
  const verifiedTotal = calculateOrderTotalTest(spoofedItems, sampleDbProds);
  assert.strictEqual(verifiedTotal, (290 + 20) * 2, 'Total must equal (290 + 20) * 2 = 620, ignoring client price 1');

  assert.throws(() => calculateOrderTotalTest([{ productId: 'BBA-01', price: 290, qty: -5 }], sampleDbProds), /Invalid qty/);
  assert.throws(() => calculateOrderTotalTest([{ productId: 'BBA-01', price: 290, qty: 0 }], sampleDbProds), /Invalid qty/);
  console.log('✔ Test 6: Server-Authoritative Price & Qty Validation passed');

  console.log('🎉 ALL TESTS PASSED SUCCESSFULLY!');
}

runTests().catch(err => {
  console.error('❌ Test failed:', err);
  process.exit(1);
});
