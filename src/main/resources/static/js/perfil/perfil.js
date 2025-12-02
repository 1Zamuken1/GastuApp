/**
 * Perfil Page - Frontend Logic
 * Handles user profile, financial preferences, and notifications
 */

document.addEventListener("DOMContentLoaded", function () {
  // General Tab - User Profile
  const formPerfil = document.getElementById("formPerfil");
  const usernameInput = document.getElementById("username");
  const emailInput = document.getElementById("email");

  // Password Change Modal
  const formPassword = document.getElementById("formCambiarPassword");
  const modalPassword = document.getElementById("modalCambiarPassword");

  // Preferencias Tab (General)
  const formPreferencias = document.getElementById(
    "formPreferenciasFinancieras"
  );
  const btnReset = document.getElementById("btnResetPreferencias");
  const umbralInput = document.getElementById("umbralAdvertencia");
  const egresoGrandeInput = document.getElementById("egresoGrande");
  const alertaCheckbox = document.getElementById("alertaEgresoGrande");

  // Notificaciones Tab (Alertas Avanzadas)
  const formNotificaciones = document.getElementById("formNotificaciones");

  const API_USUARIO = "/api/usuario";
  const API_PREFERENCIAS = "/api/movimientos/preferencias";

  // Mapping of alert fields to their types for easy handling
  const alertFields = [
    { id: "alertGastoIncrementalEnabled", type: "checkbox" },
    { id: "alertGastoIncrementalPorcentaje", type: "number" },
    { id: "alertGastoIncrementalMeses", type: "number" },
    { id: "alertReduccionIngresosEnabled", type: "checkbox" },
    { id: "alertReduccionIngresosPorcentaje", type: "number" },
    { id: "alertPatronInusualEnabled", type: "checkbox" },
    { id: "alertConcentracionGastosEnabled", type: "checkbox" },
    { id: "alertConcentracionGastosPorcentaje", type: "number" },
    { id: "alertConceptoSinUsoEnabled", type: "checkbox" },
    { id: "alertConceptoSinUsoDias", type: "number" },
    { id: "alertVelocidadGastoEnabled", type: "checkbox" },
    { id: "alertInactividadIngresosEnabled", type: "checkbox" },
    { id: "alertInactividadDias", type: "number" },
    { id: "alertEgresosAgrupadosEnabled", type: "checkbox" },
    { id: "alertEgresosAgrupadosCantidad", type: "number" },
    { id: "alertEgresosAgrupadosHoras", type: "number" },
    { id: "metaAhorroMensual", type: "number" },
    { id: "alertMetaAhorroEnabled", type: "checkbox" },
    { id: "alertBalanceCriticoEnabled", type: "checkbox" },
    { id: "alertMicroGastosEnabled", type: "checkbox" },
    { id: "alertMicroGastosCantidad", type: "number" },
    { id: "alertMicroGastosMontoMax", type: "number" },
    { id: "alertGastosHormigaEnabled", type: "checkbox" },
    { id: "alertGastosHormigaMontoMax", type: "number" },
    { id: "alertProyeccionSobregastoEnabled", type: "checkbox" },
    { id: "alertComparacionPeriodoEnabled", type: "checkbox" },
    { id: "alertDiaMesCriticoEnabled", type: "checkbox" },
    { id: "alertDiaMesCriticoPorcentaje", type: "number" },
    { id: "alertEgresoSinConceptoEnabled", type: "checkbox" },
    { id: "alertIngresoInusualEnabled", type: "checkbox" },
  ];

  // ===================================
  // INITIALIZATION
  // ===================================

  if (formPerfil) {
    cargarDatosUsuario();
  }

  if (formPreferencias || formNotificaciones) {
    cargarPreferencias();
  }

  // ===================================
  // USER PROFILE
  // ===================================

  async function cargarDatosUsuario() {
    try {
      const response = await fetch(API_USUARIO, { credentials: "include" });
      if (!response.ok) throw new Error("Error al cargar usuario");

      const usuario = await response.json();
      if (usernameInput) usernameInput.value = usuario.username || "";
      if (emailInput) emailInput.value = usuario.email || "";
    } catch (error) {
      console.error("Error cargando usuario:", error);
      mostrarNotificacion("Error al cargar datos del usuario", "danger");
    }
  }

  if (formPerfil) {
    formPerfil.addEventListener("submit", async function (e) {
      e.preventDefault();

      const data = {
        username: usernameInput.value,
      };

      try {
        const response = await fetch(API_USUARIO, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          credentials: "include",
          body: JSON.stringify(data),
        });

        if (!response.ok) {
          const error = await response.json();
          throw new Error(error.error || "Error al actualizar perfil");
        }

        await response.json();
        mostrarNotificacion("Perfil actualizado exitosamente", "success");
      } catch (error) {
        console.error("Error actualizando perfil:", error);
        mostrarNotificacion(error.message, "danger");
      }
    });
  }

  // ===================================
  // PASSWORD CHANGE
  // ===================================

  if (formPassword) {
    formPassword.addEventListener("submit", async function (e) {
      e.preventDefault();

      const passwordActual = document.getElementById("passwordActual").value;
      const passwordNueva = document.getElementById("passwordNueva").value;
      const passwordConfirmar =
        document.getElementById("passwordConfirmar").value;

      if (passwordNueva !== passwordConfirmar) {
        mostrarNotificacion("Las contraseñas no coinciden", "warning");
        return;
      }

      const data = {
        passwordActual: passwordActual,
        passwordNueva: passwordNueva,
      };

      try {
        const response = await fetch(`${API_USUARIO}/cambiar-password`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          credentials: "include",
          body: JSON.stringify(data),
        });

        if (!response.ok) {
          const error = await response.json();
          throw new Error(error.error || "Error al cambiar contraseña");
        }

        await response.json();
        mostrarNotificacion("Contraseña cambiada exitosamente", "success");

        // Close modal and reset form
        const modal = bootstrap.Modal.getInstance(modalPassword);
        if (modal) modal.hide();
        formPassword.reset();
      } catch (error) {
        console.error("Error cambiando contraseña:", error);
        mostrarNotificacion(error.message, "danger");
      }
    });
  }

  // ===================================
  // FINANCIAL PREFERENCES & NOTIFICATIONS
  // ===================================

  async function cargarPreferencias() {
    try {
      const response = await fetch(API_PREFERENCIAS, {
        credentials: "include",
      });
      if (!response.ok) throw new Error("Error al cargar preferencias");

      const preferencias = await response.json();

      // Populate Preferencias tab (General)
      if (umbralInput)
        umbralInput.value = preferencias.umbralAdvertenciaPorcentaje || 80;
      if (egresoGrandeInput)
        egresoGrandeInput.value = preferencias.egresoGrandePorcentaje || 20;
      if (alertaCheckbox)
        alertaCheckbox.checked =
          preferencias.alertaEgresoGrandeActiva !== false;

      // Populate Notificaciones tab (Advanced Alerts)
      alertFields.forEach((field) => {
        const element = document.getElementById(field.id);
        if (element) {
          if (field.type === "checkbox") {
            element.checked = preferencias[field.id] === true;
          } else {
            element.value =
              preferencias[field.id] !== null ? preferencias[field.id] : "";
          }
        }
      });
    } catch (error) {
      console.error("Error cargando preferencias:", error);
      mostrarNotificacion("Error al cargar las preferencias", "danger");
    }
  }

  if (formPreferencias) {
    formPreferencias.addEventListener("submit", async function (e) {
      e.preventDefault();
      await guardarPreferenciasGenerales();
    });
  }

  async function guardarPreferenciasGenerales() {
    // Get current full preferences first to avoid overwriting other fields with null
    let currentPrefs = {};
    try {
      const getResponse = await fetch(API_PREFERENCIAS, {
        credentials: "include",
      });
      if (getResponse.ok) {
        currentPrefs = await getResponse.json();
      }
    } catch (e) {
      console.warn(
        "Could not fetch current prefs, proceeding with partial update"
      );
    }

    const data = {
      ...currentPrefs, // Keep existing values
      umbralAdvertenciaPorcentaje: parseInt(umbralInput.value),
      egresoGrandePorcentaje: parseInt(egresoGrandeInput.value),
      alertaEgresoGrandeActiva: alertaCheckbox.checked,
    };

    await enviarPreferencias(data, "Preferencias guardadas exitosamente");
  }

  if (formNotificaciones) {
    formNotificaciones.addEventListener("submit", async function (e) {
      e.preventDefault();
      await guardarConfiguracionAlertas();
    });
  }

  async function guardarConfiguracionAlertas() {
    // Get current full preferences first
    let currentPrefs = {};
    try {
      const getResponse = await fetch(API_PREFERENCIAS, {
        credentials: "include",
      });
      if (getResponse.ok) {
        currentPrefs = await getResponse.json();
      }
    } catch (e) {
      console.warn(
        "Could not fetch current prefs, proceeding with partial update"
      );
    }

    // Build update object
    const updates = {};
    alertFields.forEach((field) => {
      const element = document.getElementById(field.id);
      if (element) {
        if (field.type === "checkbox") {
          updates[field.id] = element.checked;
        } else {
          updates[field.id] = element.value ? parseFloat(element.value) : null;
        }
      }
    });

    const data = {
      ...currentPrefs,
      ...updates,
    };

    await enviarPreferencias(data, "Configuración de alertas guardada");
  }

  async function enviarPreferencias(data, successMessage) {
    try {
      const response = await fetch(API_PREFERENCIAS, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(data),
      });

      if (!response.ok) throw new Error("Error al guardar configuración");

      await response.json();
      mostrarNotificacion(successMessage, "success");

      // Reload to ensure UI is in sync with server state
      cargarPreferencias();
    } catch (error) {
      console.error("Error guardando:", error);
      mostrarNotificacion("Error al guardar la configuración", "danger");
    }
  }

  if (btnReset) {
    btnReset.addEventListener("click", async function () {
      if (!confirm("¿Estás seguro de restaurar los valores predeterminados?"))
        return;

      try {
        const response = await fetch(`${API_PREFERENCIAS}/reset`, {
          method: "POST",
          credentials: "include",
        });

        if (!response.ok) throw new Error("Error al resetear preferencias");

        await response.json();
        mostrarNotificacion(
          "Preferencias restauradas a valores predeterminados",
          "info"
        );
        cargarPreferencias(); // Reload UI
      } catch (error) {
        console.error("Error reseteando preferencias:", error);
        mostrarNotificacion("Error al restaurar las preferencias", "danger");
      }
    });
  }

  // ===================================
  // NOTIFICATIONS UI HELPERS
  // ===================================

  function mostrarNotificacion(mensaje, tipo = "info") {
    let toastContainer = document.querySelector(".toast-container");
    if (!toastContainer) {
      toastContainer = document.createElement("div");
      toastContainer.className =
        "toast-container position-fixed top-0 end-0 p-3";
      document.body.appendChild(toastContainer);
    }

    const toastId = `toast-${Date.now()}`;
    const toastHTML = `
      <div id="${toastId}" class="toast align-items-center text-bg-${tipo} border-0" role="alert">
        <div class="d-flex">
          <div class="toast-body">${mensaje}</div>
          <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
      </div>
    `;

    toastContainer.insertAdjacentHTML("beforeend", toastHTML);

    const toastElement = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastElement, {
      autohide: true,
      delay: 3000,
    });
    toast.show();

    toastElement.addEventListener("hidden.bs.toast", () =>
      toastElement.remove()
    );
  }
  // ==========================================
  // NOTIFICATIONS HISTORY LOGIC
  // ==========================================

  const notificacionesList = document.getElementById("notificaciones-list");
  const btnMarcarTodasLeidas = document.getElementById("btnMarcarTodasLeidas");
  const filterButtons = document.querySelectorAll("[data-filter]");

  // Load notifications if we are on the notifications tab
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.get("tab") === "notificaciones") {
    cargarNotificaciones();
  }

  // Event Listeners
  if (btnMarcarTodasLeidas) {
    btnMarcarTodasLeidas.addEventListener("click", marcarTodasLeidas);
  }

  filterButtons.forEach((btn) => {
    btn.addEventListener("click", (e) => {
      // Update active state
      filterButtons.forEach((b) => b.classList.remove("active"));
      e.target.closest("button").classList.add("active");

      // Filter
      const filtro = e.target.closest("button").dataset.filter;
      cargarNotificaciones(filtro);
    });
  });

  async function cargarNotificaciones(filtro = "all") {
    if (!notificacionesList) return;

    notificacionesList.innerHTML = `
      <div class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Cargando...</span>
        </div>
        <p class="text-muted mt-2">Cargando notificaciones...</p>
      </div>
    `;

    try {
      const response = await fetch("/api/notificaciones");
      if (!response.ok) throw new Error("Error al cargar notificaciones");

      const notificaciones = await response.json();

      // Filter logic
      let filtered = notificaciones;
      if (filtro === "unread") {
        filtered = notificaciones.filter((n) => !n.leida);
      } else if (filtro !== "all") {
        filtered = notificaciones.filter((n) => n.tipo === filtro);
      }

      renderNotificationCards(filtered);
    } catch (error) {
      console.error(error);
      notificacionesList.innerHTML = `
        <div class="alert alert-danger">
          <i class="bi bi-exclamation-triangle"></i> Error al cargar las notificaciones.
        </div>
      `;
    }
  }

  function renderNotificationCards(notificaciones) {
    if (notificaciones.length === 0) {
      notificacionesList.innerHTML = `
        <div class="text-center py-5 text-muted">
          <i class="bi bi-bell-slash display-4"></i>
          <p class="mt-3">No hay notificaciones para mostrar.</p>
        </div>
      `;
      return;
    }

    notificacionesList.innerHTML = notificaciones
      .map(
        (n) => `
      <div class="card mb-3 notification-card ${
        !n.leida ? "border-primary shadow-sm" : ""
      }" data-id="${n.id}">
        <div class="card-body">
          <div class="d-flex justify-content-between align-items-start">
            <h5 class="card-title mb-1">
              <i class="bi bi-${getIconForType(n.tipo)} me-2"></i>
              ${n.titulo}
              ${
                !n.leida
                  ? '<span class="badge bg-primary ms-2">Nueva</span>'
                  : ""
              }
            </h5>
            <small class="text-muted">${new Date(
              n.fechaCreacion
            ).toLocaleString()}</small>
          </div>
          <p class="card-text mt-2">${n.descripcion}</p>
          <div class="d-flex justify-content-between align-items-center mt-3">
            <span class="badge bg-light text-dark border">${n.tipo}</span>
            ${
              !n.leida
                ? `
              <button class="btn btn-sm btn-outline-primary btn-marcar-leida" onclick="window.marcarLeida(${n.id})">
                <i class="bi bi-check2"></i> Marcar como leída
              </button>
            `
                : '<small class="text-success"><i class="bi bi-check-circle"></i> Leída</small>'
            }
          </div>
        </div>
      </div>
    `
      )
      .join("");
  }

  function getIconForType(tipo) {
    const iconMap = {
      AHORRO: "piggy-bank",
      MOVIMIENTO: "arrow-left-right",
      SISTEMA: "gear",
      PROGRAMACION: "calendar-event",
      TENDENCIA: "graph-up",
      PRESUPUESTO: "wallet2",
    };
    return iconMap[tipo] || "bell";
  }

  // Expose to global scope for onclick handlers
  window.marcarLeida = async function (id) {
    try {
      const response = await fetch(`/api/notificaciones/${id}/leida`, {
        method: "PUT",
      });

      if (response.ok) {
        mostrarNotificacion("Notificación marcada como leída", "success");
        // Refresh current view
        const activeFilter =
          document.querySelector("[data-filter].active")?.dataset.filter ||
          "all";
        cargarNotificaciones(activeFilter);
      }
    } catch (error) {
      console.error(error);
      mostrarNotificacion("Error al actualizar notificación", "danger");
    }
  };

  async function marcarTodasLeidas() {
    try {
      // Fetch all unread first
      const response = await fetch("/api/notificaciones");
      const notificaciones = await response.json();
      const unread = notificaciones.filter((n) => !n.leida);

      if (unread.length === 0) {
        mostrarNotificacion("No hay notificaciones pendientes", "info");
        return;
      }

      // Mark each as read (since we don't have a bulk endpoint yet)
      let successCount = 0;
      for (const n of unread) {
        await fetch(`/api/notificaciones/${n.id}/leida`, { method: "PUT" });
        successCount++;
      }

      mostrarNotificacion(
        `${successCount} notificaciones marcadas como leídas`,
        "success"
      );
      cargarNotificaciones("all");
    } catch (error) {
      console.error(error);
      mostrarNotificacion("Error al marcar notificaciones", "danger");
    }
  }
});
