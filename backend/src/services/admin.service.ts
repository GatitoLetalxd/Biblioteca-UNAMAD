import { prisma } from '../config/prisma';
import { RolUsuario } from '@prisma/client';
import { hashPassword } from '../utils/hash';
import { AppError } from '../middlewares/error.middleware';
import { z } from 'zod';

export const crearPersonalSchema = z.object({
  codigo: z.string().min(3, 'El código de personal debe tener al menos 3 caracteres'),
  password: z.string().min(6, 'La contraseña debe tener al menos 6 caracteres'),
  nombre: z.string().min(2, 'El primer nombre es obligatorio'),
  apellido: z.string().min(2, 'El primer apellido es obligatorio'),
  carreraOArea: z.string().min(2, 'El área o cargo institucional es obligatorio'),
  telefono: z.string().optional(),
  email: z.string().email('Debe ser un correo electrónico válido'),
  rol: z.enum([RolUsuario.BIBLIOTECARIO, RolUsuario.ADMIN], {
    errorMap: () => ({ message: 'El administrador solo puede crear usuarios con rol BIBLIOTECARIO o ADMIN' })
  })
});

export type CrearPersonalDTO = z.infer<typeof crearPersonalSchema>;

export class AdminService {
  /**
   * Creación de personal administrativo (Solo Admin puede invocarlo)
   * Restringido estrictamente a roles BIBLIOTECARIO o ADMIN
   */
  async crearPersonal(dto: CrearPersonalDTO) {
    const validated = crearPersonalSchema.parse(dto);

    // Validar que no se intente crear estudiantes por esta vía
    if ((validated.rol as any) === RolUsuario.ESTUDIANTE) {
      throw new AppError('No se pueden crear estudiantes por el panel administrativo. Los estudiantes deben autoregistrarse.', 400);
    }

    const codigoExiste = await prisma.usuario.findUnique({
      where: { codigo: validated.codigo.trim() }
    });
    if (codigoExiste) {
      throw new AppError('El código de personal ya se encuentra asignado a otro usuario', 409);
    }

    const emailExiste = await prisma.usuario.findUnique({
      where: { email: validated.email.trim().toLowerCase() }
    });
    if (emailExiste) {
      throw new AppError('El correo electrónico ya se encuentra registrado', 409);
    }

    const hashedPassword = await hashPassword(validated.password);

    const nuevoPersonal = await prisma.usuario.create({
      data: {
        codigo: validated.codigo.trim(),
        password: hashedPassword,
        nombre: validated.nombre.trim(),
        apellido: validated.apellido.trim(),
        carrera: validated.carreraOArea.trim(),
        telefono: validated.telefono?.trim() || null,
        email: validated.email.trim().toLowerCase(),
        rol: validated.rol,
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

    return nuevoPersonal;
  }

  /**
   * Listar usuarios administrativos y de ventanilla
   */
  async listarPersonal() {
    return prisma.usuario.findMany({
      where: {
        rol: { in: [RolUsuario.BIBLIOTECARIO, RolUsuario.ADMIN] }
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
      },
      orderBy: { nombre: 'asc' }
    });
  }
}
