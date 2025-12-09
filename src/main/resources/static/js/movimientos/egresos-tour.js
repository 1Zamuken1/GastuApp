/**
 * Egresos Tour - Driver.js Configuration
 * Provides an interactive guided tour for the expenses view
 */

const EGRESOS_TOUR_STORAGE_KEY = "gastuapp_egresos_tour_completed";

/**
 * Define the tour steps for the Egresos view
 */
function getEgresosTourSteps() {
  return [
    {
      element: "#totalCard",
      popover: {
        title: "Total del Mes",
        description:
          "Aquí puedes ver el total de todos tus gastos registrados en el mes actual.",
        side: "bottom",
        align: "center",
      },
    },
    {
      element: "#statsCard",
      popover: {
        title: "Estadísticas Rápidas",
        description:
          "Este panel muestra: cantidad de registros, número de conceptos diferentes, y la fecha de tu último gasto.",
        side: "bottom",
        align: "center",
      },
    },
    {
      element: ".search-container",
      popover: {
        title: "Buscador",
        description:
          "Usa el buscador para filtrar tus gastos por concepto. Escribe cualquier término y los resultados se filtrarán automáticamente.",
        side: "bottom",
        align: "start",
      },
    },
    {
      element: "#exportButtons",
      popover: {
        title: "Exportar Reportes",
        description:
          "Puedes exportar tus egresos a PDF, Excel o CSV para llevar un control fuera de la aplicación o compartirlos.",
        side: "bottom",
        align: "center",
      },
    },
    {
      element: ".btn-danger.btn-pulse",
      popover: {
        title: "Nuevo Egreso",
        description:
          "Haz clic aquí para registrar un nuevo gasto. Deberás seleccionar un concepto, ingresar el monto, fecha y opcionalmente una descripción.",
        side: "left",
        align: "center",
      },
    },
    {
      element: "#conceptosGrid",
      popover: {
        title: "Desglose por Concepto",
        description:
          "Aquí verás tarjetas agrupando tus gastos por concepto. Cada tarjeta muestra el total y la última fecha de gasto. Haz clic en cualquiera para ver el detalle.",
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
 * Create and configure the Driver.js instance for Egresos
 */
function createEgresosTourDriver() {
  const driverFn = window.driver?.js?.driver;

  if (!driverFn) {
    console.error("Driver.js not loaded properly");
    return null;
  }

  return driverFn({
    showProgress: true,
    allowClose: true,
    showButtons: ["next", "previous", "close"],
    steps: getEgresosTourSteps(),
    nextBtnText: "Siguiente",
    prevBtnText: "Anterior",
    doneBtnText: "¡Entendido!",
    progressText: "{{current}} de {{total}}",
    onDestroyed: () => {
      localStorage.setItem(EGRESOS_TOUR_STORAGE_KEY, "true");
    },
  });
}

/**
 * Start the egresos tour
 */
function startEgresosTour() {
  const driverObj = createEgresosTourDriver();
  if (driverObj) {
    driverObj.drive();
  }
}

/**
 * Check if user has completed the egresos tour before
 */
function hasCompletedEgresosTour() {
  return localStorage.getItem(EGRESOS_TOUR_STORAGE_KEY) === "true";
}

/**
 * Initialize egresos tour functionality
 */
function initEgresosTour() {
  const helpBtn = document.getElementById("tour-help-btn");
  if (helpBtn) {
    helpBtn.addEventListener("click", function (e) {
      e.preventDefault();
      startEgresosTour();
    });
  }

  // Auto-start tour for first-time visitors
  if (!hasCompletedEgresosTour()) {
    setTimeout(function () {
      startEgresosTour();
    }, 1500);
  }
}

// Initialize when DOM is ready
document.addEventListener("DOMContentLoaded", initEgresosTour);
