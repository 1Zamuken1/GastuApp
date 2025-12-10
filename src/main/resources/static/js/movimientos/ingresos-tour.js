/**
 * Ingresos Tour - Driver.js Configuration
 * Provides an interactive guided tour for the income view
 */

const INGRESOS_TOUR_STORAGE_KEY = "gastuapp_ingresos_tour_completed";

/**
 * Define the tour steps for the Ingresos view
 */
function getIngresosTourSteps() {
  return [
    {
      element: "#totalCard",
      popover: {
        title: "Total del Mes",
        description:
          "Aquí puedes ver el total de todos tus ingresos registrados en el mes actual.",
        side: "bottom",
        align: "center",
      },
    },
    {
      element: "#statsCard",
      popover: {
        title: "Estadísticas Rápidas",
        description:
          "Este panel muestra: cantidad de registros, número de conceptos diferentes, y la fecha de tu último ingreso.",
        side: "bottom",
        align: "center",
      },
    },
    {
      element: ".search-container",
      popover: {
        title: "Buscador",
        description:
          "Usa el buscador para filtrar tus ingresos por concepto. Escribe cualquier término y los resultados se filtrarán automáticamente.",
        side: "bottom",
        align: "start",
      },
    },
    {
      element: "#exportButtons",
      popover: {
        title: "Exportar Reportes",
        description:
          "Puedes exportar tus ingresos a PDF, Excel o CSV para llevar un control fuera de la aplicación o compartirlos.",
        side: "bottom",
        align: "center",
      },
    },
    {
      element: ".btn-success.btn-pulse",
      popover: {
        title: "Nuevo Ingreso",
        description:
          "Haz clic aquí para registrar un nuevo ingreso. Deberás seleccionar un concepto, ingresar el monto y opcionalmente una descripción.",
        side: "left",
        align: "center",
      },
    },
    {
      element: "#conceptosGrid",
      popover: {
        title: "Desglose por Concepto",
        description:
          "Aquí verás tarjetas agrupando tus ingresos por concepto. Haz clic en cualquier tarjeta para ver el detalle de los movimientos de ese concepto.",
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
 * Create and configure the Driver.js instance for Ingresos
 */
function createIngresosTourDriver() {
  const driverFn = window.driver?.js?.driver;

  if (!driverFn) {
    console.error("Driver.js not loaded properly");
    return null;
  }

  return driverFn({
    showProgress: true,
    allowClose: true,
    showButtons: ["next", "previous", "close"],
    steps: getIngresosTourSteps(),
    nextBtnText: "Siguiente",
    prevBtnText: "Anterior",
    doneBtnText: "¡Entendido!",
    progressText: "{{current}} de {{total}}",
    onDestroyed: () => {
      localStorage.setItem(INGRESOS_TOUR_STORAGE_KEY, "true");
    },
  });
}

/**
 * Start the ingresos tour
 */
function startIngresosTour() {
  const driverObj = createIngresosTourDriver();
  if (driverObj) {
    driverObj.drive();
  }
}

/**
 * Check if user has completed the ingresos tour before
 */
function hasCompletedIngresosTour() {
  return localStorage.getItem(INGRESOS_TOUR_STORAGE_KEY) === "true";
}

/**
 * Initialize ingresos tour functionality
 */
function initIngresosTour() {
  const helpBtn = document.getElementById("tour-help-btn");
  if (helpBtn) {
    helpBtn.addEventListener("click", function (e) {
      e.preventDefault();
      startIngresosTour();
    });
  }

  // Auto-start tour for first-time visitors
  if (!hasCompletedIngresosTour()) {
    setTimeout(function () {
      startIngresosTour();
    }, 1500); // Slightly longer delay for data to load
  }
}

// Initialize when DOM is ready
document.addEventListener("DOMContentLoaded", initIngresosTour);
