/**
 * Dashboard Tour - Driver.js Configuration
 * Provides an interactive guided tour for new users
 */

const TOUR_STORAGE_KEY = "gastuapp_dashboard_tour_completed";

/**
 * Define the tour steps
 * Note: Driver.js escapes HTML in titles for security, so we use plain text titles
 */
function getTourSteps() {
  return [
    {
      element: ".month-nav",
      popover: {
        title: "Navegación de Meses",
        description:
          "Usa estos botones para ver el resumen de meses anteriores o futuros. El mes actual se muestra en el centro.",
        side: "bottom",
        align: "center",
      },
    },
    {
      element: ".dashboard-balance-card",
      popover: {
        title: "Balance del Mes",
        description:
          "Este es tu balance mensual: la diferencia entre tus ingresos y egresos. Verde significa que tienes superávit, rojo indica déficit.",
        side: "bottom",
        align: "center",
      },
    },
    {
      element: ".dashboard-stats-card",
      popover: {
        title: "Resumen Financiero",
        description:
          "Aquí puedes ver el total de tus ingresos, egresos y ahorros del mes actual de un vistazo.",
        side: "bottom",
        align: "center",
      },
    },
    {
      element: "#mainTrendChart",
      popover: {
        title: "Tendencia del Mes",
        description:
          "Este gráfico muestra la evolución diaria de tus ingresos, egresos y ahorros durante el mes.",
        side: "top",
        align: "center",
      },
    },
    {
      element: "#expenseDonutChart",
      popover: {
        title: "Gastos por Categoría",
        description:
          "Visualiza cómo se distribuyen tus egresos entre las diferentes categorías que has definido.",
        side: "left",
        align: "center",
      },
    },
    {
      element: "#balanceTrendChart",
      popover: {
        title: "Balance Disponible",
        description:
          "Muestra tu balance real disponible: Ingresos menos Egresos menos Ahorros. Es el dinero que realmente tienes para gastar.",
        side: "top",
        align: "center",
      },
    },
    {
      element: "#savingsChart",
      popover: {
        title: "Metas de Ahorro",
        description:
          "Aquí puedes ver el progreso hacia tus metas de ahorro. Las barras muestran cuánto has acumulado vs tu objetivo.",
        side: "top",
        align: "center",
      },
    },
    {
      element: "#budgetChart",
      popover: {
        title: "Presupuestos",
        description:
          "Controla tus límites de gasto por categoría. Las barras te indican cuánto has gastado vs tu presupuesto asignado.",
        side: "top",
        align: "center",
      },
    },
    {
      element: "#projectionsCard",
      popover: {
        title: "Proyecciones",
        description:
          "Próximamente: aquí podrás ver proyecciones financieras basadas en tus patrones de ingresos y gastos.",
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
 * Create and configure the Driver.js instance
 */
function createTourDriver() {
  // Driver.js CDN IIFE structure: window.driver = { js: { driver: function } }
  const driverFn = window.driver?.js?.driver;

  if (!driverFn) {
    console.error("Driver.js not loaded properly");
    return null;
  }

  return driverFn({
    showProgress: true,
    allowClose: true,
    showButtons: ["next", "previous", "close"],
    steps: getTourSteps(),
    nextBtnText: "Siguiente",
    prevBtnText: "Anterior",
    doneBtnText: "¡Entendido!",
    progressText: "{{current}} de {{total}}",
    onDestroyed: () => {
      // Mark tour as completed when user finishes or closes
      localStorage.setItem(TOUR_STORAGE_KEY, "true");
    },
  });
}

/**
 * Start the dashboard tour
 */
function startDashboardTour() {
  const driverObj = createTourDriver();
  if (driverObj) {
    driverObj.drive();
  }
}

/**
 * Check if user has completed the tour before
 */
function hasCompletedTour() {
  return localStorage.getItem(TOUR_STORAGE_KEY) === "true";
}

/**
 * Initialize tour functionality
 * - Auto-start for first-time visitors
 * - Setup help button click handler
 */
function initDashboardTour() {
  // Setup help button click handler
  const helpBtn = document.getElementById("tour-help-btn");
  if (helpBtn) {
    helpBtn.addEventListener("click", function (e) {
      e.preventDefault();
      startDashboardTour();
    });
  }

  // Auto-start tour for first-time visitors (with a small delay for charts to render)
  if (!hasCompletedTour()) {
    setTimeout(function () {
      startDashboardTour();
    }, 1000);
  }
}

// Initialize when DOM is ready
document.addEventListener("DOMContentLoaded", initDashboardTour);
