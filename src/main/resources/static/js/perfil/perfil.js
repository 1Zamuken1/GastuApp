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

  // Preferencias Tab
  const formPreferencias = document.getElementById(
    "formPreferenciasFinancieras"
  );
  const btnReset = document.getElementById("btnResetPreferencias");
  const umbralInput = document.getElementById("umbralAdvertencia");
  const egresoGrandeInput = document.getElementById("egresoGrande");
  const alertaCheckbox = document.getElementById("alertaEgresoGrande");

  // Notificaciones Tab
  const formNotificaciones = document.getElementById("formNotificaciones");
  const egresoGrandeNotifInput = document.getElementById("egresoGrandeNotif");
  const alertaNotifCheckbox = document.getElementById(
    "alertaEgresoGrandeNotif"
  );

  const API_USUARIO = "/api/usuario";
  const API_PREFERENCIAS = "/api/movimientos/preferencias";

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
  // FINANCIAL PREFERENCES
  // ===================================

  async function cargarPreferencias() {
    try {
      const response = await fetch(API_PREFERENCIAS, {
        credentials: "include",
      });
      if (!response.ok) throw new Error("Error al cargar preferencias");

      const preferencias = await response.json();

      // Populate Preferencias tab
      if (umbralInput)
        umbralInput.value = preferencias.umbralAdvertenciaPorcentaje || 80;
      if (egresoGrandeInput)
        egresoGrandeInput.value = preferencias.egresoGrandePorcentaje || 20;
      if (alertaCheckbox)
        alertaCheckbox.checked =
          preferencias.alertaEgresoGrandeActiva !== false;

      // Populate Notificaciones tab
      if (egresoGrandeNotifInput)
        egresoGrandeNotifInput.value =
          preferencias.egresoGrandePorcentaje || 20;
      if (alertaNotifCheckbox)
        alertaNotifCheckbox.checked =
          preferencias.alertaEgresoGrandeActiva !== false;
    } catch (error) {
      console.error("Error cargando preferencias:", error);
      mostrarNotificacion("Error al cargar las preferencias", "danger");
    }
  }

  if (formPreferencias) {
    formPreferencias.addEventListener("submit", async function (e) {
      e.preventDefault();

      const data = {
        umbralAdvertenciaPorcentaje: parseInt(umbralInput.value),
        egresoGrandePorcentaje: parseInt(egresoGrandeInput.value),
        alertaEgresoGrandeActiva: alertaCheckbox.checked,
      };

      try {
        const response = await fetch(API_PREFERENCIAS, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          credentials: "include",
          body: JSON.stringify(data),
        });

        if (!response.ok) throw new Error("Error al guardar preferencias");

        await response.json();
        mostrarNotificacion("Preferencias guardadas exitosamente", "success");
      } catch (error) {
        console.error("Error guardando preferencias:", error);
        mostrarNotificacion("Error al guardar las preferencias", "danger");
      }
    });
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

        const preferencias = await response.json();

        if (umbralInput)
          umbralInput.value = preferencias.umbralAdvertenciaPorcentaje;
        if (egresoGrandeInput)
          egresoGrandeInput.value = preferencias.egresoGrandePorcentaje;
        if (alertaCheckbox)
          alertaCheckbox.checked = preferencias.alertaEgresoGrandeActiva;

        mostrarNotificacion(
          "Preferencias restauradas a valores predeterminados",
          "info"
        );
      } catch (error) {
        console.error("Error reseteando preferencias:", error);
        mostrarNotificacion("Error al restaurar las preferencias", "danger");
      }
    });
  }

  // ===================================
  // NOTIFICATIONS TAB
  // ===================================

  if (formNotificaciones) {
    formNotificaciones.addEventListener("submit", async function (e) {
      e.preventDefault();

      // Get current preferences first
      try {
        const getResponse = await fetch(API_PREFERENCIAS, {
          credentials: "include",
        });
        if (!getResponse.ok) throw new Error("Error al obtener preferencias");

        const currentPrefs = await getResponse.json();

        // Update with values from Notifications tab
        const data = {
          umbralAdvertenciaPorcentaje: currentPrefs.umbralAdvertenciaPorcentaje,
          egresoGrandePorcentaje: parseInt(egresoGrandeNotifInput.value),
          alertaEgresoGrandeActiva: alertaNotifCheckbox.checked,
        };

        const response = await fetch(API_PREFERENCIAS, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          credentials: "include",
          body: JSON.stringify(data),
        });

        if (!response.ok) throw new Error("Error al guardar notificaciones");

        await response.json();
        mostrarNotificacion("Configuración de alertas guardada", "success");
      } catch (error) {
        console.error("Error guardando notificaciones:", error);
        mostrarNotificacion("Error al guardar la configuración", "danger");
      }
    });
  }

  // ===================================
  // NOTIFICATIONS
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
});
