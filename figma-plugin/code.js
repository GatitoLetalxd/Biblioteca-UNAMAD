// Plugin de Automatización Local para Figma - Biblioteca UNAMAD
// Funciona 100% en local sin consumir llamadas de API de Figma MCP

figma.showUI(__html__, { width: 340, height: 420, themeColors: true });

figma.ui.onmessage = async (msg) => {
  // Paleta Obsidian Minimalista UNAMAD
  const OBSIDIAN_BG = { r: 11 / 255, g: 15 / 255, b: 23 / 255 };      // #0B0F17
  const SLATE_SURFACE = { r: 18 / 255, g: 23 / 255, b: 34 / 255 };    // #121722
  const CARD_SLATE = { r: 26 / 255, g: 33 / 255, b: 48 / 255 };       // #1A2130
  const AMBER_GOLD = { r: 251 / 255, g: 191 / 255, b: 36 / 255 };     // #FBBF24
  const TEXT_WHITE = { r: 248 / 255, g: 250 / 255, b: 252 / 255 };    // #F8FAFC
  const TEXT_SLATE = { r: 148 / 255, g: 163 / 255, b: 184 / 255 };    // #94A3B8

  if (msg.type === 'aplicar-tema-obsidian') {
    let count = 0;
    const targetNodes = figma.currentPage.selection.length > 0 
      ? figma.currentPage.selection 
      : figma.currentPage.children;

    for (const node of targetNodes) {
      if (node.type === 'FRAME') {
        node.fills = [{ type: 'SOLID', color: OBSIDIAN_BG }];
        count++;
      }
    }
    figma.notify(`Se aplico el fondo Obsidian a ${count} marcos.`);
  }

  if (msg.type === 'limpiar-residuales') {
    let deletedCount = 0;
    const allNodes = figma.currentPage.findAll(n => {
      const name = n.name.toLowerCase();
      const isScanner = name.includes('scanner') || name.includes('barcode') || name.includes('escan') || name.includes('qr');
      const isTopBarLogout = (name.includes('exit') || name.includes('logout') || name.includes('cerrar')) && 
                             n.parent && (n.parent.name.toLowerCase().includes('top') || n.parent.name.toLowerCase().includes('bar') || n.parent.name.toLowerCase().includes('header'));
      return isScanner || isTopBarLogout;
    });

    for (const node of allNodes) {
      node.remove();
      deletedCount++;
    }
    figma.notify(`Se eliminaron ${deletedCount} nodos residuales (escaner/logout superior).`);
  }

  if (msg.type === 'sincronizar-todo') {
    let frameCount = 0;
    let deletedCount = 0;

    // 1. Fondos Obsidian
    for (const node of figma.currentPage.children) {
      if (node.type === 'FRAME') {
        node.fills = [{ type: 'SOLID', color: OBSIDIAN_BG }];
        frameCount++;
      }
    }

    // 2. Limpiar iconos viejos
    const allNodes = figma.currentPage.findAll(n => {
      const name = n.name.toLowerCase();
      const isScanner = name.includes('scanner') || name.includes('barcode') || name.includes('escan') || name.includes('qr');
      const isTopBarLogout = (name.includes('exit') || name.includes('logout') || name.includes('cerrar')) && 
                             n.parent && (n.parent.name.toLowerCase().includes('top') || n.parent.name.toLowerCase().includes('bar') || n.parent.name.toLowerCase().includes('header'));
      return isScanner || isTopBarLogout;
    });

    for (const node of allNodes) {
      node.remove();
      deletedCount++;
    }

    figma.notify(`Completado: ${frameCount} marcos actualizados y ${deletedCount} nodos limpiados.`);
  }

  if (msg.type === 'cerrar') {
    figma.closePlugin();
  }
};
