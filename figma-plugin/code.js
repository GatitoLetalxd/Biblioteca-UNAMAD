// Plugin de Automatizacion Quirurgica para Figma - Biblioteca UNAMAD
// Precision de nivel MCP sin limites de llamadas ni pago de suscripcion

figma.showUI(__html__, { width: 380, height: 560, themeColors: true });

// Paleta Oficial Obsidian & Amber Gold (Identica al codigo Android Jetpack Compose)
const C = {
  // Fondos y Superficies
  obsidianBg: { r: 11 / 255, g: 15 / 255, b: 23 / 255 },       // #0B0F17
  cardSlate: { r: 18 / 255, g: 23 / 255, b: 34 / 255 },        // #121722
  surfaceElevated: { r: 26 / 255, g: 33 / 255, b: 48 / 255 },  // #1A2130
  borderSlate: { r: 43 / 255, g: 51 / 255, b: 70 / 255 },      // #2B3346
  
  // Acentos de Marca
  amberGold: { r: 251 / 255, g: 191 / 255, b: 36 / 255 },      // #FBBF24
  amberGoldMuted: { r: 217 / 255, g: 119 / 255, b: 6 / 255 },  // #D97706
  charcoalDark: { r: 28 / 255, g: 25 / 255, b: 23 / 255 },     // #1C1917
  
  // Badges y Estados
  emeraldBadgeBg: { r: 15 / 255, g: 41 / 255, b: 30 / 255 },   // #0F291E
  emeraldText: { r: 52 / 255, g: 211 / 255, b: 153 / 255 },    // #34D399
  indigoBadgeBg: { r: 30 / 255, g: 34 / 255, b: 56 / 255 },    // #1E2238
  indigoText: { r: 165 / 255, g: 180 / 255, b: 252 / 255 },   // #A5B4FC
  skyBadgeBg: { r: 30 / 255, g: 41 / 255, b: 59 / 255 },       // #1E293B
  skyText: { r: 56 / 255, g: 189 / 255, b: 248 / 255 },        // #38BDF8
  dangerBadgeBg: { r: 69 / 255, g: 10 / 255, b: 10 / 255 },    // #450A0A
  dangerBorder: { r: 127 / 255, g: 29 / 255, b: 29 / 255 },    // #7F1D1D
  dangerText: { r: 248 / 255, g: 113 / 255, b: 113 / 255 },    // #F87171
  
  // Tipografia
  textWhite: { r: 248 / 255, g: 250 / 255, b: 252 / 255 },     // #F8FAFC
  textSlate: { r: 148 / 255, g: 163 / 255, b: 184 / 255 },     // #94A3B8
  textMuted: { r: 100 / 255, g: 116 / 255, b: 139 / 255 }      // #64748B
};

// Funciones Auxiliares de Modificacion Segura
function safeStyleFrame(nodeId, options = {}) {
  const node = figma.getNodeById(nodeId);
  if (!node) return;
  if (options.fill) {
    node.fills = [{ type: 'SOLID', color: options.fill }];
  }
  if (options.stroke) {
    node.strokes = [{ type: 'SOLID', color: options.stroke }];
    node.strokeWeight = options.strokeWeight || 1;
  }
  if (options.cornerRadius !== undefined && 'cornerRadius' in node) {
    node.cornerRadius = options.cornerRadius;
  }
  if (options.x !== undefined) node.x = options.x;
  if (options.y !== undefined) node.y = options.y;
  if (options.width !== undefined && options.height !== undefined && typeof node.resize === 'function') {
    node.resize(options.width, options.height);
  }
}

async function safeSetText(nodeId, newText, newColor = null) {
  const node = figma.getNodeById(nodeId);
  if (!node || node.type !== 'TEXT') return;
  try {
    if (node.fontName === figma.mixed) {
      await figma.loadFontAsync({ family: "Inter", style: "Regular" });
      node.fontName = { family: "Inter", style: "Regular" };
    } else {
      await figma.loadFontAsync(node.fontName);
    }
    if (newText !== null && newText !== undefined) {
      node.characters = newText;
    }
    if (newColor) {
      node.fills = [{ type: 'SOLID', color: newColor }];
    }
  } catch (e) {
    console.warn("safeSetText fallo en nodo " + nodeId, e);
  }
}

async function safeStyleText(nodeId, newColor) {
  const node = figma.getNodeById(nodeId);
  if (!node || node.type !== 'TEXT') return;
  node.fills = [{ type: 'SOLID', color: newColor }];
}

function removeNode(nodeId) {
  const node = figma.getNodeById(nodeId);
  if (node) {
    node.remove();
  }
}

// -------------------------------------------------------------
// REDISEÑO PANTALLA 02 - CATÁLOGO
// -------------------------------------------------------------
async function redesignCatalogo() {
  // 1. Marco Principal
  safeStyleFrame("3:25", { fill: C.obsidianBg });

  // 2. Encabezado TopBar
  safeStyleFrame("3:26", { fill: C.cardSlate, stroke: C.borderSlate });
  await safeSetText("3:27", "Catálogo de Libros", C.textWhite);
  // Eliminar campana o vaciarla
  await safeSetText("3:28", "");

  // 3. Cuadro de Busqueda (Reduccion de espacio superior a y=84, estilo elevado)
  safeStyleFrame("3:29", {
    x: 20,
    y: 84,
    width: 350,
    height: 44,
    fill: C.surfaceElevated,
    stroke: C.borderSlate,
    cornerRadius: 12
  });
  // Remover icono de escaner o emojis
  await safeSetText("3:30", "");
  await safeSetText("3:31", "Buscar título, autor o ISBN...", C.textSlate);

  // 4. Pastillas de Categorias (Alineadas a y=136)
  safeStyleFrame("3:32", { x: 20, y: 136 });
  // Pill Todos (Activo)
  safeStyleFrame("3:33", { fill: C.amberGold, cornerRadius: 14 });
  await safeSetText("3:34", "Todos", C.charcoalDark);

  // Pills Inactivas
  safeStyleFrame("3:35", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 14 });
  await safeSetText("3:36", "Ingeniería", C.textSlate);

  safeStyleFrame("3:37", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 14 });
  await safeSetText("3:38", "Forestal", C.textSlate);

  safeStyleFrame("3:39", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 14 });
  await safeSetText("3:40", "Educación", C.textSlate);

  // 5. Tarjeta de Libro 1 (Cálculo)
  safeStyleFrame("3:41", { y: 178, fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 16 });
  safeStyleFrame("3:42", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 8 });
  await safeSetText("3:43", "LIBRO");
  await safeStyleText("3:43", C.textMuted);
  await safeSetText("3:44", "UNAMAD", C.amberGoldMuted);
  await safeSetText("3:46", "Cálculo Diferencial e Integral", C.textWhite);
  await safeSetText("3:47", "James Stewart • 8va Edición", C.textSlate);
  safeStyleFrame("3:49", { fill: C.emeraldBadgeBg, stroke: C.borderSlate, cornerRadius: 6 });
  await safeSetText("3:50", "Físico (4 disp.)", C.emeraldText);
  safeStyleFrame("3:51", { fill: C.indigoBadgeBg, stroke: C.borderSlate, cornerRadius: 6 });
  await safeSetText("3:52", "Digital PDF", C.indigoText);
  safeStyleFrame("3:53", { fill: C.amberGold, cornerRadius: 10 });
  await safeSetText("3:54", "Solicitar Préstamo", C.charcoalDark);

  // 6. Tarjeta de Libro 2 (Silvicultura)
  safeStyleFrame("3:55", { y: 338, fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 16 });
  safeStyleFrame("3:56", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 8 });
  await safeSetText("3:57", "LIBRO");
  await safeStyleText("3:57", C.textMuted);
  await safeSetText("3:58", "UNAMAD", C.amberGoldMuted);
  await safeSetText("3:60", "Silvicultura en Bosques Tropicales", C.textWhite);
  await safeSetText("3:61", "Editorial UNAMAD • 2023", C.textSlate);
  safeStyleFrame("3:63", { fill: C.emeraldBadgeBg, stroke: C.borderSlate, cornerRadius: 6 });
  await safeSetText("3:64", "Físico (2 disp.)", C.emeraldText);
  safeStyleFrame("3:65", { fill: C.amberGold, cornerRadius: 10 });
  await safeSetText("3:66", "Solicitar Préstamo", C.charcoalDark);

  // 7. Tarjeta de Libro 3 (Inteligencia Artificial)
  safeStyleFrame("3:67", { y: 498, fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 16 });
  safeStyleFrame("3:68", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 8 });
  await safeSetText("3:69", "LIBRO");
  await safeStyleText("3:69", C.textMuted);
  await safeSetText("3:70", "UNAMAD", C.amberGoldMuted);
  await safeSetText("3:72", "Inteligencia Artificial y Robótica", C.textWhite);
  await safeSetText("3:73", "Stuart Russell • 4ta Edición", C.textSlate);
  safeStyleFrame("3:75", { fill: C.indigoBadgeBg, stroke: C.borderSlate, cornerRadius: 6 });
  await safeSetText("3:76", "Solo Digital", C.indigoText);
  safeStyleFrame("3:77", { fill: C.surfaceElevated, stroke: C.amberGold, cornerRadius: 10 });
  await safeSetText("3:78", "Leer PDF Ahora", C.amberGold);

  // 8. Barra Inferior de Navegacion (Bottom Bar)
  safeStyleFrame("3:79", { fill: C.cardSlate, stroke: C.borderSlate });
  await safeSetText("3:81", "•");
  await safeStyleText("3:81", C.amberGold);
  await safeSetText("3:82", "Catálogo", C.amberGold);

  await safeSetText("3:84", "•");
  await safeStyleText("3:84", C.textMuted);
  await safeSetText("3:85", "Préstamos", C.textSlate);

  await safeSetText("3:87", "•");
  await safeStyleText("3:87", C.textMuted);
  await safeSetText("3:88", "Perfil", C.textSlate);
}

// -------------------------------------------------------------
// REDISEÑO PANTALLA 01 - LOGIN
// -------------------------------------------------------------
async function redesignLogin() {
  safeStyleFrame("3:2", { fill: C.obsidianBg });
  safeStyleFrame("3:3", { fill: C.cardSlate });
  await safeSetText("3:5", "UNAMAD", C.amberGold);
  await safeSetText("3:6", "Biblioteca Universitaria Central", C.textWhite);
  await safeSetText("3:7", "Sistema Integrado de Préstamos", C.textSlate);

  safeStyleFrame("3:8", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 16 });
  safeStyleFrame("3:9", { fill: C.surfaceElevated, cornerRadius: 10 });
  
  // Tab Estudiante
  safeStyleFrame("3:10", { fill: C.amberGold, cornerRadius: 8 });
  await safeSetText("3:11", "Estudiante", C.charcoalDark);
  
  // Tab Bibliotecario
  await safeSetText("3:13", "Bibliotecario", C.textSlate);

  // Inputs
  await safeSetText("3:15", "Código Institucional", C.textSlate);
  safeStyleFrame("3:16", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 10 });
  await safeSetText("3:17", "Ej: 20230451 o BIBLIO-01", C.textMuted);

  await safeSetText("3:19", "Contraseña", C.textSlate);
  safeStyleFrame("3:20", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 10 });
  await safeSetText("3:21", "••••••••••••", C.textWhite);

  // Boton Principal
  safeStyleFrame("3:22", { fill: C.amberGold, cornerRadius: 12 });
  await safeSetText("3:23", "Iniciar Sesión", C.charcoalDark);

  await safeSetText("3:24", "Acceso oficial de la Universidad Nacional Amazónica de Madre de Dios", C.textMuted);
  await safeSetText("22:2", "¿Eres nuevo estudiante? Regístrate aquí", C.amberGold);
}

// -------------------------------------------------------------
// REDISEÑO PANTALLA 06 - CARNET DIGITAL & PERFIL
// -------------------------------------------------------------
async function redesignCarnet() {
  safeStyleFrame("24:30", { fill: C.obsidianBg });
  safeStyleFrame("24:31", { fill: C.cardSlate, stroke: C.borderSlate });
  await safeSetText("24:32", "Perfil y Carnet Digital", C.textWhite);

  // Carnet Card
  safeStyleFrame("24:33", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 16 });
  await safeSetText("24:35", "CARNET DE BIBLIOTECA — UNAMAD", C.amberGold);
  await safeSetText("24:37", "● Lector Activo", C.emeraldText);

  // Avatar RM
  safeStyleFrame("24:39", { fill: C.surfaceElevated, stroke: C.amberGold, cornerRadius: 28 });
  await safeSetText("24:40", "RM", C.amberGold);

  await safeSetText("24:42", "Rogeero Daniel Montufar", C.textWhite);
  await safeSetText("24:43", "Cód: 21200100 • Pregrado", C.textSlate);
  await safeSetText("24:44", "Ing. de Sistemas e Informática", C.textMuted);

  // Solvencia Card
  safeStyleFrame("24:45", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 12 });
  await safeSetText("24:46", "Estado de Solvencia: Solvente", C.emeraldText);
  await safeSetText("24:47", "S/. 0.00 deudas", C.textSlate);

  // Crear o actualizar boton de Cerrar Sesion en el cuerpo del perfil
  const carnetScreen = figma.getNodeById("24:30");
  if (carnetScreen && carnetScreen.type === 'FRAME') {
    let logoutBtn = figma.getNodeById("btn-logout-carnet");
    if (!logoutBtn) {
      const btn = figma.createFrame();
      btn.id = "btn-logout-carnet";
      btn.name = "Boton Cerrar Sesion";
      btn.resize(342, 48);
      btn.x = 24;
      btn.y = 420;
      btn.fills = [{ type: 'SOLID', color: C.cardSlate }];
      btn.strokes = [{ type: 'SOLID', color: C.dangerBorder }];
      btn.strokeWeight = 1;
      btn.cornerRadius = 12;

      await figma.loadFontAsync({ family: "Inter", style: "Bold" });
      const txt = figma.createText();
      txt.fontName = { family: "Inter", style: "Bold" };
      txt.characters = "Cerrar Sesión Institucional";
      txt.fills = [{ type: 'SOLID', color: C.dangerText }];
      txt.fontSize = 14;
      txt.x = 80;
      txt.y = 15;
      btn.appendChild(txt);
      carnetScreen.appendChild(btn);
    }
  }
}

// -------------------------------------------------------------
// REDISEÑO PANTALLA 03 - DASHBOARD ESTUDIANTE
// -------------------------------------------------------------
async function redesignDashboardEstudiante() {
  safeStyleFrame("3:89", { fill: C.obsidianBg });
  safeStyleFrame("3:90", { fill: C.cardSlate, stroke: C.borderSlate });
  await safeSetText("3:91", "¡Hola, Rogeero!", C.textWhite);
  await safeSetText("3:92", "Ing. de Sistemas e Informática • Cód: 21200100", C.textSlate);

  // Metricas
  safeStyleFrame("3:94", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 12 });
  await safeSetText("3:95", "Préstamos Activos", C.textSlate);
  await safeSetText("3:96", "2 libros", C.amberGold);
  await safeSetText("3:97", "En plazo vigente", C.emeraldText);

  safeStyleFrame("3:98", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 12 });
  await safeSetText("3:99", "Multas Pendientes", C.textSlate);
  await safeSetText("3:100", "S/. 0.00", C.emeraldText);
  await safeSetText("3:101", "Sin deudas", C.textMuted);

  await safeSetText("3:102", "Mis Préstamos en Curso", C.textWhite);

  // Prestamo 1
  safeStyleFrame("3:103", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 16 });
  await safeSetText("3:105", "Estructuras de Datos y Algoritmos", C.textWhite);
  safeStyleFrame("3:106", { fill: C.emeraldBadgeBg, cornerRadius: 6 });
  await safeSetText("3:107", "Activo", C.emeraldText);
  await safeSetText("3:109", "Devolución: 28 Oct 2026", C.textSlate);
  await safeSetText("3:110", "Quedan 7 días", C.amberGold);
  safeStyleFrame("3:111", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 8 });
  await safeSetText("3:112", "Renovar Plazo (+3 días)", C.textWhite);

  // Prestamo 2
  safeStyleFrame("3:113", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 16 });
  await safeSetText("3:115", "Ecología Amazónica", C.textWhite);
  safeStyleFrame("3:116", { fill: C.emeraldBadgeBg, cornerRadius: 6 });
  await safeSetText("3:117", "Activo", C.emeraldText);
  await safeSetText("3:119", "Devolución: 24 Oct 2026", C.textSlate);
  await safeSetText("3:120", "Quedan 3 días", C.dangerText);
  safeStyleFrame("3:121", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 8 });
  await safeSetText("3:122", "Renovar Plazo (+3 días)", C.textWhite);

  // Continuar lectura
  safeStyleFrame("3:123", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 12 });
  await safeSetText("3:124", "•");
  await safeStyleText("3:124", C.amberGold);
  await safeSetText("3:126", "Continuar lectura digital", C.amberGold);
  await safeSetText("3:127", "Metodología de la Investigación (Pág. 45)", C.textSlate);

  // Bottom bar
  safeStyleFrame("3:128", { fill: C.cardSlate, stroke: C.borderSlate });
  await safeSetText("3:130", "•");
  await safeSetText("3:131", "Catálogo", C.textSlate);
  await safeSetText("3:133", "•");
  await safeStyleText("3:133", C.amberGold);
  await safeSetText("3:134", "Préstamos", C.amberGold);
  await safeSetText("3:136", "•");
  await safeSetText("3:137", "Perfil", C.textSlate);
}

// -------------------------------------------------------------
// REDISEÑO PANTALLAS 04, 05, 07, 08, 09, 10
// -------------------------------------------------------------
async function redesignRemainingScreens() {
  // Screen 04 - Bibliotecario
  safeStyleFrame("3:138", { fill: C.obsidianBg });
  safeStyleFrame("3:139", { fill: C.cardSlate, stroke: C.borderSlate });
  await safeSetText("3:140", "Panel Bibliotecario", C.textWhite);
  await safeSetText("3:141", "Gestión de Préstamos y Solicitudes", C.textSlate);
  safeStyleFrame("3:143", { fill: C.amberGold, cornerRadius: 8 });
  await safeSetText("3:144", "Pendientes (3)", C.charcoalDark);
  safeStyleFrame("3:145", { fill: C.surfaceElevated, cornerRadius: 8 });
  await safeSetText("3:146", "Activos (12)", C.textSlate);
  safeStyleFrame("3:147", { fill: C.surfaceElevated, cornerRadius: 8 });
  await safeSetText("3:148", "Historial (45)", C.textSlate);

  // Solicitud Cards
  safeStyleFrame("3:149", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 16 });
  await safeSetText("3:151", "Carlos Mendoza (221045)", C.textWhite);
  safeStyleFrame("3:152", { fill: C.indigoBadgeBg, cornerRadius: 6 });
  await safeSetText("3:153", "Pendiente", C.indigoText);
  await safeSetText("3:154", "Libro: Ecología y Recursos Amazónicos", C.amberGold);
  safeStyleFrame("3:157", { fill: C.surfaceElevated, stroke: C.dangerBorder, cornerRadius: 8 });
  await safeSetText("3:158", "Rechazar", C.dangerText);
  safeStyleFrame("3:159", { fill: C.amberGold, cornerRadius: 8 });
  await safeSetText("3:160", "Aprobar y Entregar", C.charcoalDark);

  safeStyleFrame("3:161", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 16 });
  await safeSetText("3:163", "María Fernández (211089)", C.textWhite);
  safeStyleFrame("3:164", { fill: C.indigoBadgeBg, cornerRadius: 6 });
  await safeSetText("3:165", "Pendiente", C.indigoText);
  await safeSetText("3:166", "Libro: Derecho Ambiental Peruano", C.amberGold);
  safeStyleFrame("3:169", { fill: C.surfaceElevated, stroke: C.dangerBorder, cornerRadius: 8 });
  await safeSetText("3:170", "Rechazar", C.dangerText);
  safeStyleFrame("3:171", { fill: C.amberGold, cornerRadius: 8 });
  await safeSetText("3:172", "Aprobar y Entregar", C.charcoalDark);

  safeStyleFrame("3:185", { fill: C.cardSlate, stroke: C.borderSlate });

  // Screen 05 - Registro
  safeStyleFrame("24:2", { fill: C.obsidianBg });
  safeStyleFrame("24:3", { fill: C.cardSlate, stroke: C.borderSlate });
  await safeSetText("24:4", "REGISTRO DE ESTUDIANTE", C.amberGold);
  await safeSetText("24:5", "Universidad Nacional Amazónica de Madre de Dios", C.textSlate);
  safeStyleFrame("24:6", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 16 });
  safeStyleFrame("24:9", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 8 });
  safeStyleFrame("24:13", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 8 });
  safeStyleFrame("24:17", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 8 });
  safeStyleFrame("24:21", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 8 });
  safeStyleFrame("24:25", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 8 });
  safeStyleFrame("24:27", { fill: C.amberGold, cornerRadius: 12 });
  await safeSetText("24:28", "REGISTRARME COMO LECTOR", C.charcoalDark);
  await safeSetText("24:29", "¿Ya tienes cuenta? Inicia sesión aquí", C.amberGold);

  // Screen 07 - Inventario
  safeStyleFrame("37:2", { fill: C.obsidianBg });
  safeStyleFrame("37:3", { fill: C.cardSlate, stroke: C.borderSlate });
  await safeSetText("37:4", "Inventario y Base de Datos", C.textWhite);
  await safeSetText("37:5", "Vista técnica optimizada · Solo texto con Ficha", C.textSlate);
  safeStyleFrame("37:6", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 10 });
  await safeSetText("37:7", "Buscar por título, autor, código ISBN...", C.textMuted);
  safeStyleFrame("37:8", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 14 });
  safeStyleFrame("37:20", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 14 });
  safeStyleFrame("37:32", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 14 });
  safeStyleFrame("37:44", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 14 });
  safeStyleFrame("37:18", { fill: C.surfaceElevated, stroke: C.amberGold, cornerRadius: 8 });
  await safeSetText("37:19", "Ver Ficha Completa", C.amberGold);
  safeStyleFrame("37:30", { fill: C.surfaceElevated, stroke: C.amberGold, cornerRadius: 8 });
  await safeSetText("37:31", "Ver Ficha Completa", C.amberGold);
  safeStyleFrame("37:56", { fill: C.cardSlate, stroke: C.borderSlate });

  // Screen 08 - Modal Ficha Tecnica
  safeStyleFrame("37:60", { fill: C.obsidianBg });
  safeStyleFrame("37:61", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 20 });
  await safeSetText("37:63", "FICHA TÉCNICA DEL LIBRO", C.textWhite);
  safeStyleFrame("37:65", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 12 });
  await safeSetText("37:66", "LIBRO", C.textMuted);
  await safeSetText("37:67", "BIBLIOTECA UNIVERSITARIA UNAMAD", C.amberGoldMuted);
  await safeSetText("37:69", "Sistemas Distribuidos y Cloud", C.textWhite);
  await safeSetText("37:70", "Andrew S. Tanenbaum & Maarten van Steen", C.textSlate);
  safeStyleFrame("37:78", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 10 });
  safeStyleFrame("37:94", { fill: C.amberGold, cornerRadius: 10 });
  await safeSetText("37:95", "Solicitar Préstamo Físico", C.charcoalDark);
  safeStyleFrame("37:96", { fill: C.surfaceElevated, stroke: C.amberGold, cornerRadius: 10 });
  await safeSetText("37:97", "Descargar Versión PDF", C.amberGold);

  // Screen 09 - Multas
  safeStyleFrame("37:98", { fill: C.obsidianBg });
  safeStyleFrame("37:99", { fill: C.cardSlate, stroke: C.borderSlate });
  await safeSetText("37:100", "Control de Multas y Ventanilla", C.textWhite);
  await safeSetText("37:101", "Módulo de Cobranza, Mora y Condonación UNAMAD", C.textSlate);
  safeStyleFrame("37:103", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 12 });
  await safeSetText("37:105", "S/ 142.50", C.emeraldText);
  safeStyleFrame("37:106", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 12 });
  await safeSetText("37:108", "4 (S/ 26.00)", C.dangerText);
  safeStyleFrame("37:109", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 16 });
  safeStyleFrame("37:119", { fill: C.amberGold, cornerRadius: 8 });
  await safeSetText("37:120", "Registrar Pago", C.charcoalDark);
  safeStyleFrame("37:121", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 8 });
  await safeSetText("37:122", "Condonar Multa", C.textSlate);

  // Screen 10 - Admin Dashboard
  safeStyleFrame("36:55", { fill: C.obsidianBg });
  safeStyleFrame("36:56", { fill: C.cardSlate, stroke: C.borderSlate });
  await safeSetText("36:57", "Dashboard Central UNAMAD", C.textWhite);
  await safeSetText("36:58", "Monitoreo de Circulación, Stock y Solicitudes en Vivo", C.textSlate);
  safeStyleFrame("36:59", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 14 });
  await safeSetText("36:61", "154", C.amberGold);
  safeStyleFrame("36:63", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 14 });
  await safeSetText("36:65", "428", C.skyText);
  safeStyleFrame("36:67", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 14 });
  await safeSetText("36:69", "32", C.amberGold);
  safeStyleFrame("36:71", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 14 });
  await safeSetText("36:73", "4", C.dangerText);
  safeStyleFrame("36:75", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 16 });
  safeStyleFrame("36:77", { fill: C.amberGold, cornerRadius: 10 });
  await safeSetText("36:78", "+ Registrar Nuevo Libro en Inventario", C.charcoalDark);
  safeStyleFrame("36:79", { fill: C.surfaceElevated, stroke: C.borderSlate, cornerRadius: 10 });
  await safeSetText("36:80", "Exportar Reporte de Circulación", C.textWhite);
  safeStyleFrame("36:81", { fill: C.cardSlate, stroke: C.borderSlate, cornerRadius: 16 });
}

// -------------------------------------------------------------
// SANEAMIENTO GLOBAL DE EMOJIS
// -------------------------------------------------------------
async function sanitizeAllEmojis() {
  const emojiRegex = /[\u{1F300}-\u{1FAFF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}]/gu;
  const allTexts = figma.currentPage.findAll(n => n.type === 'TEXT');
  let sanitizedCount = 0;

  for (const t of allTexts) {
    if (emojiRegex.test(t.characters)) {
      try {
        if (t.fontName === figma.mixed) {
          await figma.loadFontAsync({ family: "Inter", style: "Regular" });
          t.fontName = { family: "Inter", style: "Regular" };
        } else {
          await figma.loadFontAsync(t.fontName);
        }
        const cleaned = t.characters.replace(emojiRegex, '').trim();
        t.characters = cleaned.length > 0 ? cleaned : '•';
        sanitizedCount++;
      } catch (err) {
        console.warn("No se pudo sanear emoji en texto:", t.id, err);
      }
    }
  }
  return sanitizedCount;
}

// -------------------------------------------------------------
// LIMPIEZA DE RESIDUALES (ESCANER Y LOGOUT SUPERIOR)
// -------------------------------------------------------------
function cleanResiduals() {
  let count = 0;
  const nodes = figma.currentPage.findAll(n => {
    const name = n.name.toLowerCase();
    const isScanner = name.includes('scanner') || name.includes('barcode') || name.includes('escan') || name.includes('qr');
    const isTopLogout = (name.includes('exit') || name.includes('logout') || name.includes('cerrar')) &&
                        n.parent && (n.parent.name.toLowerCase().includes('top') || n.parent.name.toLowerCase().includes('bar') || n.parent.name.toLowerCase().includes('header'));
    return isScanner || isTopLogout;
  });

  for (const n of nodes) {
    n.remove();
    count++;
  }
  return count;
}

// -------------------------------------------------------------
// MANEJADOR DE MENSAJES DE LA UI
// -------------------------------------------------------------
figma.ui.onmessage = async (msg) => {
  if (msg.type === 'transformacion-completa') {
    figma.notify("Iniciando transformación quirúrgica de 10 pantallas...");
    
    // 1. Fondos Dark a todas las pantallas principales
    const rootFrames = figma.currentPage.children.filter(n => n.type === 'FRAME');
    for (const f of rootFrames) {
      f.fills = [{ type: 'SOLID', color: C.obsidianBg }];
    }

    // 2. Rediseños especificos por pantalla
    await redesignLogin();
    await redesignCatalogo();
    await redesignDashboardEstudiante();
    await redesignCarnet();
    await redesignRemainingScreens();

    // 3. Limpieza de residuales y emojis
    const deletedResiduals = cleanResiduals();
    const sanitizedEmojis = await sanitizeAllEmojis();

    figma.notify(`Transformación completada con éxito. ${sanitizedEmojis} emojis saneados, ${deletedResiduals} residuales removidos.`);
  }

  if (msg.type === 'redisenar-catalogo') {
    await redesignCatalogo();
    figma.notify("Pantalla Catálogo rediseñada: cuadro compacto y botones Amber Gold.");
  }

  if (msg.type === 'redisenar-login-perfil') {
    await redesignLogin();
    await redesignCarnet();
    figma.notify("Login y Perfil Estudiante rediseñados con éxito.");
  }

  if (msg.type === 'redisenar-dashboards') {
    await redesignDashboardEstudiante();
    await redesignRemainingScreens();
    figma.notify("Dashboards, Inventario y Solicitudes rediseñados.");
  }

  if (msg.type === 'sanear-emojis-residuales') {
    const deleted = cleanResiduals();
    const emojis = await sanitizeAllEmojis();
    figma.notify(`Limpieza: ${emojis} emojis reemplazados, ${deleted} residuales eliminados.`);
  }

  if (msg.type === 'cerrar') {
    figma.closePlugin();
  }
};
