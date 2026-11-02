import sharp from 'sharp';
import fs from 'node:fs';
import path from 'node:path';

const SIZES = [
  { name: 'mipmap-mdpi', fg: 108, legacy: 48 },
  { name: 'mipmap-hdpi', fg: 162, legacy: 72 },
  { name: 'mipmap-xhdpi', fg: 216, legacy: 96 },
  { name: 'mipmap-xxhdpi', fg: 324, legacy: 144 },
  { name: 'mipmap-xxxhdpi', fg: 432, legacy: 192 },
];

const TARGET_DIRS = [
  'kmp-app/src/androidMain/res',
  'src-tauri/icons/android'
];

async function generate() {
  const source = 'public/logo.png';
  if (!fs.existsSync(source)) {
    throw new Error(`Source not found: ${source}`);
  }

  // 1. Trim transparency from source logo
  const trimmedBuf = await sharp(source).trim().toBuffer();
  console.log('Trimmed source logo successfully.');

  for (const { name, fg: fgSize, legacy: legSize } of SIZES) {
    console.log(`Generating ${name} (fg: ${fgSize}x${fgSize}, legacy: ${legSize}x${legSize})...`);

    // A. Adaptive Foreground:
    // Scale logo to 58% of canvas size to guarantee it sits safely within the 66dp Android circle mask
    const targetFgLogoSize = Math.round(fgSize * 0.58);
    const fgLogo = await sharp(trimmedBuf)
      .resize(targetFgLogoSize, targetFgLogoSize, { fit: 'inside' })
      .toBuffer();
    const fgLogoMeta = await sharp(fgLogo).metadata();
    const fgLeft = Math.round((fgSize - fgLogoMeta.width) / 2);
    const fgTop = Math.round((fgSize - fgLogoMeta.height) / 2);

    const fgBuffer = await sharp({
      create: {
        width: fgSize,
        height: fgSize,
        channels: 4,
        background: { r: 0, g: 0, b: 0, alpha: 0 }
      }
    })
      .composite([{ input: fgLogo, left: fgLeft, top: fgTop }])
      .png()
      .toBuffer();

    // B. Legacy Square Icon (with rounded white background):
    const cornerRadius = Math.round(legSize * 0.18);
    const roundRectSvg = Buffer.from(`
      <svg width="${legSize}" height="${legSize}">
        <rect x="0" y="0" width="${legSize}" height="${legSize}" rx="${cornerRadius}" ry="${cornerRadius}" fill="#ffffff"/>
      </svg>
    `);
    const legLogoSize = Math.round(legSize * 0.74);
    const legLogo = await sharp(trimmedBuf)
      .resize(legLogoSize, legLogoSize, { fit: 'inside' })
      .toBuffer();
    const legLogoMeta = await sharp(legLogo).metadata();
    const legLeft = Math.round((legSize - legLogoMeta.width) / 2);
    const legTop = Math.round((legSize - legLogoMeta.height) / 2);

    const legacyBuffer = await sharp({
      create: {
        width: legSize,
        height: legSize,
        channels: 4,
        background: { r: 0, g: 0, b: 0, alpha: 0 }
      }
    })
      .composite([
        { input: roundRectSvg, left: 0, top: 0 },
        { input: legLogo, left: legLeft, top: legTop }
      ])
      .png()
      .toBuffer();

    // C. Legacy Round Icon (with circular white background):
    const circleRadius = Math.round(legSize * 0.48);
    const circleSvg = Buffer.from(`
      <svg width="${legSize}" height="${legSize}">
        <circle cx="${legSize / 2}" cy="${legSize / 2}" r="${circleRadius}" fill="#ffffff"/>
      </svg>
    `);
    const roundLogoSize = Math.round(legSize * 0.68);
    const roundLogo = await sharp(trimmedBuf)
      .resize(roundLogoSize, roundLogoSize, { fit: 'inside' })
      .toBuffer();
    const roundLogoMeta = await sharp(roundLogo).metadata();
    const roundLeft = Math.round((legSize - roundLogoMeta.width) / 2);
    const roundTop = Math.round((legSize - roundLogoMeta.height) / 2);

    const roundBuffer = await sharp({
      create: {
        width: legSize,
        height: legSize,
        channels: 4,
        background: { r: 0, g: 0, b: 0, alpha: 0 }
      }
    })
      .composite([
        { input: circleSvg, left: 0, top: 0 },
        { input: roundLogo, left: roundLeft, top: roundTop }
      ])
      .png()
      .toBuffer();

    // Write to each target directory
    for (const baseDir of TARGET_DIRS) {
      const dir = path.join(baseDir, name);
      if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
      }

      fs.writeFileSync(path.join(dir, 'ic_launcher_foreground.png'), fgBuffer);
      fs.writeFileSync(path.join(dir, 'ic_launcher.png'), legacyBuffer);
      fs.writeFileSync(path.join(dir, 'ic_launcher_round.png'), roundBuffer);
    }
  }

  console.log('✅ All Android adaptive and legacy icons generated successfully!');
}

generate().catch(err => {
  console.error('❌ Failed to generate icons:', err);
  process.exit(1);
});
