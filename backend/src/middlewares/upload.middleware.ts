import multer from 'multer';
import * as path from 'path';
import * as fs from 'fs';
import * as crypto from 'crypto';
import { env } from '../config/env';
import { AppError } from './error.middleware';

const storageDir = path.resolve(env.STORAGE_DIR);
if (!fs.existsSync(storageDir)) {
  fs.mkdirSync(storageDir, { recursive: true });
}

const storage = multer.diskStorage({
  destination: (_req, _file, cb) => {
    cb(null, storageDir);
  },
  filename: (_req, file, cb) => {
    // Sanitizar nombre de archivo
    const cleanOriginal = file.originalname.replace(/[^a-zA-Z0-9_\-\.]/g, '_');
    const timestamp = Date.now();
    cb(null, `${timestamp}_${cleanOriginal}`);
  }
});

const fileFilter = (
  _req: any,
  file: Express.Multer.File,
  cb: multer.FileFilterCallback
) => {
  if (file.mimetype === 'application/pdf' || file.originalname.toLowerCase().endsWith('.pdf')) {
    cb(null, true);
  } else {
    cb(new AppError('Solo se permiten archivos en formato PDF (.pdf)', 400));
  }
};

export const uploadPdf = multer({
  storage,
  fileFilter,
  limits: {
    fileSize: 50 * 1024 * 1024 // 50 MB límite por libro
  }
});

export function calcularSha256YTamano(filePath: string): { sha256: string; tamanoMb: number } {
  const fileBuffer = fs.readFileSync(filePath);
  const hashSum = crypto.createHash('sha256');
  hashSum.update(fileBuffer);
  const sha256 = hashSum.digest('hex');
  const tamanoMb = parseFloat((fileBuffer.length / (1024 * 1024)).toFixed(2));
  return { sha256, tamanoMb };
}
