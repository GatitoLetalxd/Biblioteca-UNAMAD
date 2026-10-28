import * as dotenv from 'dotenv';
import { z } from 'zod';

dotenv.config();

const envSchema = z.object({
  PORT: z.string().default('3000').transform((val) => parseInt(val, 10)),
  NODE_ENV: z.enum(['development', 'production', 'test']).default('development'),
  DATABASE_URL: z.string().min(1, 'DATABASE_URL es obligatoria'),
  JWT_SECRET: z.string().min(10, 'JWT_SECRET debe tener al menos 10 caracteres'),
  JWT_EXPIRES_IN: z.string().default('7d'),
  TARIFA_MORA_POR_DIA: z.string().default('1.50').transform((val) => parseFloat(val)),
  STORAGE_DIR: z.string().default('./storage/libros'),
  CORS_ORIGIN: z.string().default('*')
});

const _env = envSchema.safeParse(process.env);

if (!_env.success) {
  console.error('❌ Error de validación en variables de entorno:', _env.error.format());
  process.exit(1);
}

export const env = _env.data;
