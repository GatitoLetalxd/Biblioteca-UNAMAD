import { prisma } from '../config/prisma';
import { RolUsuario } from '@prisma/client';
import { hashPassword, comparePassword } from '../utils/hash';
import { generateToken } from '../utils/jwt';
import { AppError } from '../middlewares/error.middleware';
import { esCarreraValida } from '../utils/carreras';
import { z } from 'zod';

// Esquema de validación para registro de estudiante
export const registroEstudianteSchema = z.object({
  codigo: z
    .string()
    .regex(/^\d{8}$/, 'El código de estudiante debe tener exactamente 8 dígitos numéricos'),
  password: z
    .string()
    .min(6, 'La contraseña debe tener al menos 6 caracteres'),
  nombre: z
    .string()
    .min(2, 'El primer nombre es obligatorio y debe tener al menos 2 caracteres'),
  apellido: z
    .string()
    .min(2, 'El primer apellido es obligatorio y debe tener al menos 2 caracteres'),
  carrera: z
    .string()
    .refine((val) => esCarreraValida(val), {
      message: 'La carrera seleccionada no es una carrera oficial válida de la UNAMAD'
    }),
  telefono: z
    .string()
    .min(6, 'El número de teléfono es obligatorio para notificaciones')
});

export type RegistroEstudianteDTO = z.infer<typeof registroEstudianteSchema>;

// Esquema de validación para inicio de sesión
export const loginSchema = z.object({
  codigo: z.string().min(1, 'El código universitario o de personal es obligatorio'),
  password: z.string().min(1, 'La contraseña es obligatoria')
});

export type LoginDTO = z.infer<typeof loginSchema>;

export class AuthService {
  /**
   * Registro público de alumnos (auto-asignación de rol ESTUDIANTE)
   */
  async registrarEstudiante(dto: RegistroEstudianteDTO) {
    const validated = registroEstudianteSchema.parse(dto);

    // Verificar si el código ya existe
    const usuarioExistente = await prisma.usuario.findUnique({
      where: { codigo: validated.codigo }
    });

    if (usuarioExistente) {
      throw new AppError('El código de estudiante ya se encuentra registrado en el sistema', 409);
    }

    const emailInstitucional = `${validated.codigo}@unamad.edu.pe`;

    // Hashear contraseña
    const hashedPassword = await hashPassword(validated.password);

    // Crear estudiante forzando rol ESTUDIANTE
    const nuevoEstudiante = await prisma.usuario.create({
      data: {
        codigo: validated.codigo,
        password: hashedPassword,
        nombre: validated.nombre.trim(),
        apellido: validated.apellido.trim(),
        carrera: validated.carrera,
        telefono: validated.telefono.trim(),
        email: emailInstitucional,
        rol: RolUsuario.ESTUDIANTE,
        activo: true
      },
      select: {
        id: true,
        codigo: true,
        nombre: true,
        apellido: true,
        carrera: true,
        telefono: true,
        email: true,
        rol: true,
        activo: true,
        createdAt: true
      }
    });

    const token = generateToken({
      id: nuevoEstudiante.id,
      codigo: nuevoEstudiante.codigo,
      email: nuevoEstudiante.email,
      rol: nuevoEstudiante.rol
    });

    return {
      usuario: nuevoEstudiante,
      token
    };
  }

  /**
   * Inicio de sesión institucional para cualquier rol (Estudiante, Bibliotecario, Admin)
   */
  async login(dto: LoginDTO) {
    const validated = loginSchema.parse(dto);

    const usuario = await prisma.usuario.findUnique({
      where: { codigo: validated.codigo.trim() }
    });

    if (!usuario) {
      throw new AppError('Credenciales incorrectas: Código universitario o contraseña no válidos', 401);
    }

    if (!usuario.activo) {
      throw new AppError('Cuenta institucional deshabilitada. Contacte con la Dirección de Biblioteca', 403);
    }

    const passwordValida = await comparePassword(validated.password, usuario.password);
    if (!passwordValida) {
      throw new AppError('Credenciales incorrectas: Código universitario o contraseña no válidos', 401);
    }

    const token = generateToken({
      id: usuario.id,
      codigo: usuario.codigo,
      email: usuario.email,
      rol: usuario.rol
    });

    return {
      usuario: {
        id: usuario.id,
        codigo: usuario.codigo,
        nombre: usuario.nombre,
        apellido: usuario.apellido,
        carrera: usuario.carrera,
        telefono: usuario.telefono,
        email: usuario.email,
        rol: usuario.rol,
        activo: usuario.activo
      },
      token
    };
  }

  /**
   * Consulta del perfil autenticado a partir del ID en el token
   */
  async obtenerPerfil(usuarioId: number) {
    const usuario = await prisma.usuario.findUnique({
      where: { id: usuarioId },
      select: {
        id: true,
        codigo: true,
        nombre: true,
        apellido: true,
        carrera: true,
        telefono: true,
        email: true,
        rol: true,
        activo: true,
        createdAt: true
      }
    });

    if (!usuario) {
      throw new AppError('Usuario no encontrado', 404);
    }

    return usuario;
  }
}
