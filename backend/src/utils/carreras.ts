// =====================================================================
// LISTA OFICIAL DE CARRERAS PROFESIONALES DE LA UNAMAD
// =====================================================================

export const CARRERAS_UNAMAD = [
  'Ingeniería de Sistemas e Informática',
  'Ingeniería Forestal y Medio Ambiente',
  'Ingeniería Agroindustrial',
  'Medicina Veterinaria y Zootecnia',
  'Ecoturismo',
  'Enfermería',
  'Educación: Especialidad Matemática y Computación',
  'Educación: Especialidad Inicial y Especial',
  'Educación: Especialidad Primaria e Informática',
  'Administración y Negocios Internacionales',
  'Contabilidad y Finanzas',
  'Derecho y Ciencias Políticas'
] as const;

export type CarreraUNAMAD = typeof CARRERAS_UNAMAD[number];

export function esCarreraValida(carrera: string): boolean {
  return CARRERAS_UNAMAD.includes(carrera as CarreraUNAMAD);
}
