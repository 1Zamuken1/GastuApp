/**
 * Profile Tour - Driver.js Configuration
 * Provides an interactive guided tour for the profile view across multiple tabs
 */

const PERFIL_TOUR_STORAGE_KEY = "gastuapp_perfil_tour_state";

/**
 * Helper: click element programmatically
 */
function clickElement(selector) {
  const el = document.querySelector(selector);
  if (el) el.click();
}

/**
 * Helper: check if element exists
 */
function elementExists(selector) {
  return document.querySelector(selector) !== null;
}

/**
 * STEPS DEFINITIONS
 */

// 1. GENERAL TAB STEPS
function getGeneralTourSteps() {
  return [
    {
      element: "h1.h2",
      popover: {
        title: "Bienvenido a tu Perfil",
        description:
          "Aquí puedes gestionar tu cuenta, cambiar tu contraseña y configurar tus preferencias financieras.",
        side: "bottom",
        align: "start",
      },
    },
    {
      element: ".card-header-tabs",
      popover: {
        title: "Pestañas de Navegación",
        description:
          "El perfil se divide en tres secciones: General, Configuración de Notificaciones (alertas) y Mis Notificaciones.",
        side: "bottom",
        align: "start",
      },
    },
    {
      element: "#formPerfil",
      popover: {
        title: "Información Personal",
        description:
          "En esta sección puedes actualizar tu nombre de usuario y cambiar tu contraseña de acceso.",
        side: "top",
        align: "center",
      },
    },
    {
      element: "a[href*='tab=alertas']",
      popover: {
        title: "Vamos a Configuración",
        description:
          "Ahora configuraremos las alertas financieras para que GastuApp te avise de lo importante. Haz clic en 'Siguiente' para ir allí.",
        side: "bottom",
        align: "center",
        onNextClick: () => {
          // Manually navigate and persist state
          updateTourState("alertas");
          window.location.href = "/perfil?tab=alertas";
        },
      },
    },
  ];
}

// 2. CONFIGURACION TAB STEPS
function getAlertasTourSteps() {
  return [
    {
      element: "#configTabs",
      popover: {
        title: "Módulos de Alertas",
        description:
          "Puedes configurar alertas específicas para Ingresos, Egresos, Ahorros y Presupuestos.",
        side: "bottom",
        align: "start",
      },
    },
    // --- INGRESOS SUB-TAB ---
    {
      element: "#pills-ingresos-tab",
      popover: {
        title: "Alertas de Ingresos",
        description: "Revisemos las alertas relacionadas con tus ingresos.",
        side: "bottom",
        align: "center",
      },
      onHighlightStarted: () => clickElement("#pills-ingresos-tab"),
    },
    {
      element: "#alertReduccionIngresosEnabled",
      popover: {
        title: "Reducción de Ingresos",
        description:
          "Te avisa si tus ingresos del mes actual son inferiores a los del mes anterior en el porcentaje que definas.",
        side: "right",
        align: "center",
      },
      onHighlightStarted: (element) => {
        // Fix: Highlight Parent Card
        const el = document.querySelector("#alertReduccionIngresosEnabled");
        if (el) {
          const card = el.closest(".config-card-styled");
          if (card && window.driver) {
            // Re-target the driver to the card if possible or add a style?
            // Driver.js caches elements.
            // Simpler: Just make the element in the step definition dynamic or selecting the card parent?
            // But 'element' can be a string selector.
            // Let's use a function based element definition or just complex selection if supported?
            // Driver.js supports DOM element.
          }
        }
      },
    },
    // REWRITE STEPS TO USE DOM ELEMENT RESOLUTION AT RUNTIME
    // Since we cannot easily pass a selector for "parent of X", we will modify the step definition logic
    // But `getAlertasTourSteps` returns the array.
    // Let's change the strings to functions or use a helper.
    // Driver.js V1: `element` must be a selector or HTMLElement.
    // We will use a helper function to resolve the card.
  ];
}

// Helper to find card for input
function getCardFor(selector) {
  const el = document.querySelector(selector);
  return el ? el.closest(".config-card-styled") : selector;
}

function getAlertasTourSteps() {
  return [
    {
      element: "#configTabs",
      popover: {
        title: "Módulos de Alertas",
        description:
          "Puedes configurar alertas específicas para Ingresos, Egresos, Ahorros y Presupuestos.",
        side: "bottom",
        align: "start",
      },
    },
    // --- INGRESOS SUB-TAB ---
    {
      element: "#pills-ingresos-tab",
      popover: {
        title: "Alertas de Ingresos",
        description: "Revisemos las alertas relacionadas con tus ingresos.",
        side: "bottom",
        align: "center",
      },
      onHighlightStarted: () => clickElement("#pills-ingresos-tab"),
    },
    {
      // Dynamic resolution: we need the element to be present when tour starts?
      // Or we can rely on Driver.js refreshing.
      // But sub-tabs are hidden initially. Driver.js might fail to find the element if we pass a DOM reference that is null at init time?
      // `getAlertasTourSteps` is called inside `startTourPhase`.
      // At that point invalid tabs are hidden.
      // We should use a getter.
      // Driver.js doesn't support getter for element.
      // BUT, we are in `startTourPhase`. We trigger the click on the tab.
      // The elements ARE in the DOM, just hidden.
      // So `document.querySelector` works? Yes.
      // So we can pass the DOM element directly.
      element:
        document
          .querySelector("#alertReduccionIngresosEnabled")
          ?.closest(".config-card-styled") || "#alertReduccionIngresosEnabled",
      popover: {
        title: "Reducción de Ingresos",
        description:
          "Te avisa si tus ingresos del mes actual son inferiores a los del mes anterior en el porcentaje que definas.",
        side: "right",
        align: "center",
      },
    },
    {
      element:
        document
          .querySelector("#alertIngresoInusualEnabled")
          ?.closest(".config-card-styled") || "#alertIngresoInusualEnabled",
      popover: {
        title: "Ingreso Inusual",
        description:
          "Detecta si recibes un ingreso mucho mayor a tu promedio habitual.",
        side: "right",
        align: "center",
      },
    },
    {
      element:
        document
          .querySelector("#alertInactividadIngresosEnabled")
          ?.closest(".config-card-styled") ||
        "#alertInactividadIngresosEnabled",
      popover: {
        title: "Inactividad",
        description:
          "Recibe una alerta si pasan X días sin que registres ningún ingreso.",
        side: "right",
        align: "center",
      },
    },

    // --- EGRESOS SUB-TAB ---
    {
      element: "#pills-egresos-tab",
      popover: {
        title: "Alertas de Egresos",
        description: "Ahora veamos cómo controlar tus gastos.",
        side: "bottom",
        align: "center",
      },
      onHighlightStarted: () => clickElement("#pills-egresos-tab"),
    },
    {
      element:
        document
          .querySelector("#egresoGrande")
          ?.closest(".config-card-styled") || "#egresoGrande",
      popover: {
        title: "Definir Egreso Grande",
        description:
          "Define qué porcentaje de tus ingresos consideras un 'Gasto Grande'.",
        side: "right",
        align: "center",
      },
    },
    {
      element:
        document
          .querySelector("#alertaEgresoGrande")
          ?.closest(".config-card-styled") || "#alertaEgresoGrande",
      popover: {
        title: "Alerta de Egreso Grande",
        description:
          "Activa esta opción para recibir una notificación inmediata cuando registres un gasto que supere el umbral definido anteriormente.",
        side: "right",
        align: "center",
      },
    },
    {
      element:
        document
          .querySelector("#alertConcentracionGastosEnabled")
          ?.closest(".config-card-styled") ||
        "#alertConcentracionGastosEnabled",
      popover: {
        title: "Concentración de Gastos",
        description:
          "Te avisa si una sola categoría (ej. Comida) está consumiendo más del X% de tus ingresos totales.",
        side: "right",
        align: "center",
      },
    },
    {
      element:
        document
          .querySelector("#alertMicroGastosEnabled")
          ?.closest(".config-card-styled") || "#alertMicroGastosEnabled",
      popover: {
        title: "Micro-gastos",
        description:
          "Detecta si estás realizando demasiados gastos pequeños (hormiga) que sumados pueden afectar tu presupuesto.",
        side: "right",
        align: "center",
      },
    },

    // --- AHORROS SUB-TAB ---
    {
      element: "#pills-ahorros-tab",
      popover: {
        title: "Alertas de Ahorros",
        description: "Estas notificaciones son automáticas.",
        side: "bottom",
        align: "center",
      },
      onHighlightStarted: () => clickElement("#pills-ahorros-tab"),
    },
    {
      element: ".list-group-item:nth-child(1)",
      popover: {
        title: "Aporte Disponible",
        description:
          "Te recordaremos cuando sea momento de hacer tu aporte (diario, semanal o mensual).",
        side: "top",
        align: "start",
      },
    },
    {
      element: ".list-group-item:nth-child(2)",
      popover: {
        title: "Aporte Perdido",
        description:
          "Si se pasa la fecha límite sin registrar el aporte, se marcará como perdido.",
        side: "top",
        align: "start",
      },
    },
    {
      element: ".list-group-item:nth-child(3)",
      popover: {
        title: "Ahorro Abandonado",
        description:
          "Si acumulas 3 cuotas vencidas seguidas, el ahorro se considerará abandonado y dejarás de recibir alertas.",
        side: "top",
        align: "start",
      },
    },

    // --- TRANSITION TO NEXT TAB ---
    {
      element: "a[href*='tab=notificaciones']",
      popover: {
        title: "Ver Historial",
        description:
          "Finalmente, vamos a ver dónde puedes revisar todas tus notificaciones pasadas.",
        side: "bottom",
        align: "center",
        onNextClick: () => {
          updateTourState("notificaciones");
          window.location.href = "/perfil?tab=notificaciones";
        },
      },
    },
  ];
}

// 3. NOTIFICACIONES TAB STEPS
function getNotificacionesTourSteps() {
  return [
    {
      element: "#notificaciones-list",
      popover: {
        title: "Historial de Notificaciones",
        description:
          "Aquí encontrarás todas las alertas que te ha enviado el sistema. Puedes filtrarlas o marcarlas como leídas.",
        side: "top",
        align: "center",
      },
    },
    {
      element: "#tour-help-btn",
      popover: {
        title: "Tour Completado",
        description:
          "¡Has configurado tu perfil! Puedes repetir este tour cuando quieras con este botón.",
        side: "left",
        align: "center",
      },
    },
  ];
}

/**
 * State Management using LocalStorage
 */
function getTourState() {
  const stored = localStorage.getItem(PERFIL_TOUR_STORAGE_KEY);
  return stored ? JSON.parse(stored) : { status: "idle" }; // status: idle, active, completed
}

function updateTourState(phase) {
  localStorage.setItem(
    PERFIL_TOUR_STORAGE_KEY,
    JSON.stringify({ status: "active", phase: phase })
  );
}

function completeTour() {
  localStorage.setItem(
    PERFIL_TOUR_STORAGE_KEY,
    JSON.stringify({ status: "completed", phase: null })
  );
}

function resetTour() {
  updateTourState("general");
}

/**
 * Driver Initialization
 */
function createPerfilTourDriver(steps) {
  const driverFn = window.driver?.js?.driver;
  if (!driverFn) return null;

  return driverFn({
    showProgress: true,
    allowClose: true,
    steps: steps,
    nextBtnText: "Siguiente",
    prevBtnText: "Anterior",
    doneBtnText: "Finalizar",
    progressText: "{{current}} de {{total}}",
    onDestroyed: () => {
      // If destroyed manually, we might want to keep state to 'active' or reset?
      // For now, assume if closed mid-tour, it stays 'active' but won't auto-popup unless we logic that.
      // But typically user closes = stop.
      // We will only mark complete at the very end.
    },
  });
}

/**
 * Main Logic
 */
function initPerfilTour() {
  const state = getTourState();
  const urlParams = new URLSearchParams(window.location.search);
  const currentTab = urlParams.get("tab") || "general";
  const helpBtn = document.getElementById("tour-help-btn");

  // Help Button Logic
  if (helpBtn) {
    helpBtn.addEventListener("click", () => {
      // If we are not on general, go there to start
      if (currentTab !== "general") {
        updateTourState("general");
        window.location.href = "/perfil?tab=general";
      } else {
        resetTour();
        startTourPhase("general");
      }
    });
  }

  // Auto-Start Logic
  if (state.status === "active") {
    // Check if we are in the correct phase
    if (state.phase === currentTab) {
      setTimeout(() => startTourPhase(currentTab), 500);
    } else if (state.phase === "alertas" && currentTab !== "alertas") {
      // Should redirect if somehow user navigated away?
      // window.location.href = "/perfil?tab=alertas";
    }
  } else if (state.status === "idle") {
    // First time visit? Or explicit start required.
    // Let's auto-start if never completed?
    // User requested explicit tour start usually via button, but for consistency with others:
    setTimeout(() => {
      resetTour();
      startTourPhase("general");
    }, 1000);
  }
}

function startTourPhase(phase) {
  let steps = [];
  let driverObj = null;

  if (phase === "general") {
    steps = getGeneralTourSteps();
  } else if (phase === "alertas") {
    steps = getAlertasTourSteps();
  } else if (phase === "notificaciones") {
    steps = getNotificacionesTourSteps();
  }

  if (steps.length > 0) {
    driverObj = createPerfilTourDriver(steps);
    if (driverObj) {
      // Special cleanup for last step completion
      if (phase === "notificaciones") {
        driverObj.setConfig({
          onDestroyed: () => completeTour(),
        });
      }
      driverObj.drive();
    }
  }
}

document.addEventListener("DOMContentLoaded", initPerfilTour);
