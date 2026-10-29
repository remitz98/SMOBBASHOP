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
  if (storedStr.length === 4) {
    return normalizePin(storedStr) === normInput;
  }
  const inputHash = await hashPin(normInput);
  return storedStr === inputHash;
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

  console.log('🎉 ALL TESTS PASSED SUCCESSFULLY!');
}

runTests().catch(err => {
  console.error('❌ Test failed:', err);
  process.exit(1);
});
