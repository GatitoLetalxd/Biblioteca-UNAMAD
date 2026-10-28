import * as jwt from 'jsonwebtoken';
import { env } from '../config/env';
import { RolUsuario } from '@prisma/client';

export interface TokenPayload {
  id: number;
  codigo: string;
  email: string;
  rol: RolUsuario;
}

export function generateToken(payload: TokenPayload): string {
  return jwt.sign(payload, env.JWT_SECRET, {
    expiresIn: env.JWT_EXPIRES_IN as any
  });
}

export function verifyToken(token: string): TokenPayload {
  return jwt.verify(token, env.JWT_SECRET) as TokenPayload;
}
