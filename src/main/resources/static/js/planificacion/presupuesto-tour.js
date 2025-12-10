/**
 * Presupuestos Tour - Driver.js Configuration
 * Provides an interactive guided tour for the budget view
 */

const PRESUPUESTOS_TOUR_STORAGE_KEY = "gastuapp_presupuestos_tour_completed";

/**
 * Define the tour steps for the Presupuestos view
 */
function getPresupuestosTourSteps() {
  return [
    {
      element: ".text-dark.mb-4", // "Mis presupuestos" header
      popover: {
        title: "Mis Presupuestos",
        description:
          "Aquí puedes administrar todos tus presupuestos mensuales para mantener el control de tus finanzas.",
        side: "bottom",
        align: "start",
      },
    },
    {
      element: ".search-bar",
      popover: {
        title: "Buscador de Presupuestos",
        description:
          "Encuentra rápidamente un presupuesto específico buscando por nombre.",
        side: "bottom",
        align: "start",
      },
    },
    {
      element: "#btnCrearPresupuesto",
      popover: {
        title: "Crear Nuevo Presupuesto",
        description:
          "Haz clic aquí para crear un nuevo presupuesto. Podrás definir un monto límite para tus categorías de gastos.",
        side: "left",
        align: "center",
      },
    },
    {
      element: "#cardsPresupuestos",
      popover: {
        title: "Lista de Presupuestos",
        description:
          "Aquí aparecerán todas tus tarjetas de presupuesto. Podrás ver el progreso, cuánto te queda y editar o eliminarlos.",
        side: "top",
        align: "center",
      },
    },
    {
      element: "#tour-help-btn",
      popover: {
        title: "¿Necesitas Ayuda?",
        description:
          "¡Puedes repetir este tour en cualquier momento haciendo clic en este botón!",
        side: "left",
        align: "center",
      },
    },
  ];
}

/**
 * Create and configure the Driver.js instance for Presupuestos
 */
function createPresupuestosTourDriver() {
  const driverFn = window.driver?.js?.driver;

  if (!driverFn) {
    console.error("Driver.js not loaded properly");
    return null;
  }

  return driverFn({
    showProgress: true,
    allowClose: true,
    showButtons: ["next", "previous", "close"],
    steps: getPresupuestosTourSteps(),
    nextBtnText: "Siguiente",
    prevBtnText: "Anterior",
    doneBtnText: "¡Entendido!",
    progressText: "{{current}} de {{total}}",
    onDestroyed: () => {
      localStorage.setItem(PRESUPUESTOS_TOUR_STORAGE_KEY, "true");
    },
  });
}

/**
 * Start the presupuestos tour
 */
function startPresupuestosTour() {
  const driverObj = createPresupuestosTourDriver();
  if (driverObj) {
    driverObj.drive();
  }
}

/**
 * Check if user has completed the presupuestos tour before
 */
function hasCompletedPresupuestosTour() {
  return localStorage.getItem(PRESUPUESTOS_TOUR_STORAGE_KEY) === "true";
}

/**
 * Initialize presupuestos tour functionality
 */
function initPresupuestosTour() {
  const helpBtn = document.getElementById("tour-help-btn");
  if (helpBtn) {
    helpBtn.addEventListener("click", function (e) {
      e.preventDefault();
      startPresupuestosTour();
    });
  }

  // Auto-start tour for first-time visitors
  if (!hasCompletedPresupuestosTour()) {
    setTimeout(function () {
      startPresupuestosTour();
    }, 1500);
  }
}

// Initialize when DOM is ready
document.addEventListener("DOMContentLoaded", initPresupuestosTour);
