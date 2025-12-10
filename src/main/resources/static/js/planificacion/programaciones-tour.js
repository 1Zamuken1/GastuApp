/**
 * Programaciones Tour - Driver.js Configuration
 * Provides an interactive guided tour for the scheduled transactions view
 */

const PROGRAMACIONES_TOUR_STORAGE_KEY =
  "gastuapp_programaciones_tour_completed";

/**
 * Define the tour steps for the Programaciones view
 */
function getProgramacionesTourSteps() {
  return [
    {
      element: "h1.text-dark",
      popover: {
        title: "Mis Programaciones",
        description:
          "Gestiona aquí tus ingresos y egresos recurrentes. Automatiza tus finanzas y olvídate de registrar manualmente cada mes.",
        side: "bottom",
        align: "start",
      },
    },
    {
      element: "#btnVerPendientes",
      popover: {
        title: "Ver Pendientes",
        description:
          "Si tienes programaciones que requerían confirmación y ya pasó su fecha, aparecerán aquí para que las apruebes.",
        side: "bottom",
        align: "center",
      },
    },
    {
      element: ".panel-box .section-header",
      popover: {
        title: "Buscador y Filtros",
        description:
          "Utiliza estas herramientas para encontrar programaciones específicas por nombre, tipo (ingreso/egreso) o estado.",
        side: "bottom",
        align: "start",
      },
    },
    {
      element: "#btnNuevo",
      popover: {
        title: "Nueva Programación",
        description:
          "Haz clic aquí para crear una nueva transacción recurrente. Podrás definir la frecuencia (diaria, mensual, etc.) y la fecha de inicio.",
        side: "left",
        align: "center",
      },
    },
    {
      element: "#estadisticas",
      popover: {
        title: "Resumen General",
        description:
          "Este panel te muestra un resumen rápido del total de tus movimientos programados.",
        side: "top",
        align: "center",
      },
    },
    {
      element: "#contenedorProgramaciones",
      popover: {
        title: "Lista de Programaciones",
        description:
          "Aquí verás tus tarjetas de programaciones activas e inactivas. Puedes editarlas, pausarlas o ver cuándo es la próxima ejecución.",
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
 * Create and configure the Driver.js instance for Programaciones
 */
function createProgramacionesTourDriver() {
  const driverFn = window.driver?.js?.driver;

  if (!driverFn) {
    console.error("Driver.js not loaded properly");
    return null;
  }

  return driverFn({
    showProgress: true,
    allowClose: true,
    showButtons: ["next", "previous", "close"],
    steps: getProgramacionesTourSteps(),
    nextBtnText: "Siguiente",
    prevBtnText: "Anterior",
    doneBtnText: "¡Entendido!",
    progressText: "{{current}} de {{total}}",
    onDestroyed: () => {
      localStorage.setItem(PROGRAMACIONES_TOUR_STORAGE_KEY, "true");
    },
  });
}

/**
 * Start the programaciones tour
 */
function startProgramacionesTour() {
  const driverObj = createProgramacionesTourDriver();
  if (driverObj) {
    driverObj.drive();
  }
}

/**
 * Check if user has completed the programaciones tour before
 */
function hasCompletedProgramacionesTour() {
  return localStorage.getItem(PROGRAMACIONES_TOUR_STORAGE_KEY) === "true";
}

/**
 * Initialize programaciones tour functionality
 */
function initProgramacionesTour() {
  const helpBtn = document.getElementById("tour-help-btn");
  if (helpBtn) {
    helpBtn.addEventListener("click", function (e) {
      e.preventDefault();
      startProgramacionesTour();
    });
  }

  // Auto-start tour for first-time visitors
  if (!hasCompletedProgramacionesTour()) {
    setTimeout(function () {
      startProgramacionesTour();
    }, 1500);
  }
}

// Initialize when DOM is ready
document.addEventListener("DOMContentLoaded", initProgramacionesTour);
