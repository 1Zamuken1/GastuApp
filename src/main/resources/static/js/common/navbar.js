/**
 * Navbar Notifications Functionality
 * Fetches and displays real notifications from the API with tabs
 */

document.addEventListener("DOMContentLoaded", initNavbar);

function initNavbar() {
  console.log("Navbar JS initialized");
  // Variables globales
  let allNotifications = [];
  let currentTab = "no-leidas";

  // Elementos del DOM
  const notificationsBtn = document.getElementById("notificationsBtn");
  const notificationsModal = document.getElementById("notificationsModal");
  const closeNotificationsBtn = document.getElementById("closeNotifications");
  const notificationBadge = notificationsBtn.querySelector(".badge");
  const profileBtn = document.getElementById("profileBtn");
  const profileModal = document.getElementById("profileModal");

  // ============================================
  // FUNCIONES DE API
  // ============================================

  /**
   * Obtiene todas las notificaciones del usuario
   */
  async function fetchAllNotifications() {
    try {
      const response = await fetch("/api/notificaciones");
      if (response.ok) {
        allNotifications = await response.json();
        renderAllTabs();
        updateBadges();
      } else {
        showError("Error al cargar las notificaciones");
      }
    } catch (error) {
      console.error("Error fetching notifications:", error);
      showError("Error de conexión");
    }
  }

  /**
   * Obtiene el conteo de notificaciones no leídas
   */
  async function fetchUnreadCount() {
    try {
      const response = await fetch("/api/notificaciones/no-leidas/count");
      if (response.ok) {
        const data = await response.json();
        updateMainBadge(data.count);
      }
    } catch (error) {
      console.error("Error fetching unread count:", error);
    }
  }

  /**
   * Marca una notificación como leída
   */
  async function markAsRead(id) {
    try {
      const response = await fetch(`/api/notificaciones/${id}/marcar-leida`, {
        method: "PUT",
      });
      if (response.ok) {
        const notification = allNotifications.find((n) => n.id === id);
        if (notification) {
          notification.leida = true;
        }
        renderAllTabs();
        updateBadges();
        fetchUnreadCount();
      }
    } catch (error) {
      console.error("Error marking as read:", error);
    }
  }

  // ============================================
  // FUNCIONES DE RENDERIZADO
  // ============================================

  /**
   * Renderiza todas las pestañas
   */
  function renderAllTabs() {
    const unread = allNotifications.filter((n) => !n.leida);
    const read = allNotifications.filter((n) => n.leida);

    // Actualizar badges de pestañas
    updateTabBadges(unread.length, read.length, allNotifications.length);

    // Renderizar cada pestaña
    renderTab("no-leidas-content", unread);
    renderTab("leidas-content", read);
    renderTab("todas-content", allNotifications);
  }

  /**
   * Renderiza una pestaña específica
   */
  function renderTab(containerId, notifications) {
    const container = document.getElementById(containerId);

    if (!notifications || notifications.length === 0) {
      container.innerHTML = `
                <div class="p-5 text-center text-muted">
                    <i class="bi bi-bell-slash fs-1 mb-3 d-block"></i>
                    <p class="mb-0">No hay notificaciones</p>
                </div>
            `;
      return;
    }

    container.innerHTML = notifications
      .map((n) => renderNotificationCard(n))
      .join("");
  }

  /**
   * Renderiza una tarjeta de notificación individual
   */
  /**
   * Renderiza una tarjeta de notificación individual
   */
  function renderNotificationCard(notification) {
    const { tipo, titulo, descripcion, fechaCreacion, id, leida } =
      notification;

    const iconData = getNotificationIcon(tipo, titulo, descripcion);
    const timeAgo = formatTimeAgo(fechaCreacion);
    const unreadClass = leida
      ? ""
      : `border-start border-${iconData.color} border-4`;
    const bgClass = leida ? "bg-white" : "bg-light";

    // Truncate description for navbar (max 80 chars)
    const truncatedDesc =
      descripcion && descripcion.length > 80
        ? descripcion.substring(0, 80) + "..."
        : descripcion;

    // Only show Dismiss button if not read
    const dismissBtn = !leida
      ? `<button class="btn btn-sm btn-light text-muted flex-grow-1" 
                 onclick="window.dismissNotification(${id})">
           Descartar
         </button>`
      : "";

    return `
            <div class="notification-item p-3 ${unreadClass} ${bgClass} border-bottom" data-id="${id}">
                <div class="d-flex align-items-start">
                    <div class="notification-icon-box me-3 rounded-3 d-flex align-items-center justify-content-center ${
                      iconData.bgClass
                    } ${iconData.textClass}" 
                         style="width: 48px; height: 48px; min-width: 48px;">
                        <i class="${iconData.icon} fs-4"></i>
                    </div>
                    <div class="flex-grow-1">
                        <h6 class="mb-1 fw-bold text-dark">${
                          titulo || "Notificación"
                        }</h6>
                        <p class="mb-2 small text-muted text-break">${
                          truncatedDesc || ""
                        }</p>
                        <div class="d-flex align-items-center justify-content-between">
                            <span class="small text-muted">
                                <i class="bi bi-clock me-1"></i>${timeAgo}
                            </span>
                        </div>
                        <div class="mt-2 d-flex gap-2">
                            <button class="btn btn-sm btn-outline-${
                              iconData.color
                            } flex-grow-1" 
                                    onclick="window.viewNotificationDetails(${id})">
                                Ver detalles
                            </button>
                            ${dismissBtn}
                        </div>
                    </div>
                </div>
            </div>
        `;
  }

  // ... (getNotificationIcon, formatTimeAgo, updateMainBadge, updateTabBadges, updateBadges, showError, event listeners) ...

  // ============================================
  // FUNCIONES GLOBALES (para botones inline)
  // ============================================

  /**
   * Ver detalles de una notificación
   */
  window.viewNotificationDetails = function (id) {
    // Mark as read locally first for immediate feedback
    markAsRead(id);
    // Redirect to profile notifications tab with ID parameter
    window.location.href = `/perfil?tab=notificaciones&notificationId=${id}`;
  };

  /**
   * Obtiene el ícono y color según el tipo de notificación
   */
  function getNotificationIcon(tipo, titulo = "", descripcion = "") {
    // Combine text for broader search
    const textToSearch = `${tipo || ""} ${titulo || ""} ${
      descripcion || ""
    }`.toUpperCase();

    if (textToSearch.includes("INGRE") || textToSearch.includes("INCOME")) {
      return {
        color: "success",
        textClass: "text-success",
        bgClass: "bg-success-subtle",
        icon: "bi-graph-up-arrow",
      };
    } else if (
      textToSearch.includes("EGRE") ||
      textToSearch.includes("GASTO") ||
      textToSearch.includes("EXPENSE")
    ) {
      return {
        color: "danger",
        textClass: "text-danger",
        bgClass: "bg-danger-subtle",
        icon: "bi-graph-down-arrow",
      };
    } else if (
      textToSearch.includes("ALERT") ||
      textToSearch.includes("ADVERTENCIA") ||
      textToSearch.includes("WARNING")
    ) {
      return {
        color: "warning",
        textClass: "text-warning",
        bgClass: "bg-warning-subtle",
        icon: "bi-exclamation-triangle-fill",
      };
    } else {
      // Default nice light blue
      return {
        color: "info",
        textClass: "text-info",
        bgClass: "bg-info-subtle",
        icon: "bi-bell-fill",
      };
    }
  }
  /**
   * Formatea la fecha como "hace X tiempo"
   */
  function formatTimeAgo(dateString) {
    if (!dateString) return "Fecha desconocida";

    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);

    if (diffInSeconds < 60) return "hace unos segundos";
    if (diffInSeconds < 3600)
      return `hace ${Math.floor(diffInSeconds / 60)} minutos`;
    if (diffInSeconds < 86400)
      return `hace ${Math.floor(diffInSeconds / 3600)} horas`;
    if (diffInSeconds < 604800)
      return `hace ${Math.floor(diffInSeconds / 86400)} días`;
    return `hace ${Math.floor(diffInSeconds / 604800)} semanas`;
  }

  /**
   * Actualiza el badge principal de notificaciones
   */
  function updateMainBadge(count) {
    if (count > 0) {
      notificationBadge.textContent = count > 99 ? "99+" : count;
      notificationBadge.style.display = "inline-block";
    } else {
      notificationBadge.style.display = "none";
    }
  }

  /**
   * Actualiza los badges de las pestañas
   */
  function updateTabBadges(unreadCount, readCount, allCount) {
    const badgeNoLeidas = document.getElementById("badge-no-leidas");
    const badgeLeidas = document.getElementById("badge-leidas");
    const badgeTodas = document.getElementById("badge-todas");

    if (unreadCount > 0) {
      badgeNoLeidas.textContent = unreadCount;
      badgeNoLeidas.style.display = "inline-block";
    } else {
      badgeNoLeidas.style.display = "none";
    }

    if (readCount > 0) {
      badgeLeidas.textContent = readCount;
      badgeLeidas.style.display = "inline-block";
    } else {
      badgeLeidas.style.display = "none";
    }

    if (allCount > 0) {
      badgeTodas.textContent = allCount;
      badgeTodas.style.display = "inline-block";
    } else {
      badgeTodas.style.display = "none";
    }
  }

  /**
   * Actualiza todos los badges
   */
  function updateBadges() {
    const unread = allNotifications.filter((n) => !n.leida).length;
    const read = allNotifications.filter((n) => n.leida).length;
    const all = allNotifications.length;

    updateMainBadge(unread);
    updateTabBadges(unread, read, all);
  }

  /**
   * Muestra un mensaje de error
   */
  function showError(message) {
    const containers = ["no-leidas-content", "leidas-content", "todas-content"];
    containers.forEach((containerId) => {
      const container = document.getElementById(containerId);
      if (container) {
        container.innerHTML = `
                    <div class="p-5 text-center text-danger">
                        <i class="bi bi-exclamation-circle fs-1 mb-3 d-block"></i>
                        <p class="mb-0">${message}</p>
                    </div>
                `;
      }
    });
  }

  // ============================================
  // EVENT LISTENERS
  // ============================================

  /**
   * Toggle del modal de notificaciones
   */
  notificationsBtn.addEventListener("click", function (e) {
    e.preventDefault();
    e.stopPropagation();

    // Cerrar perfil si está abierto
    profileModal.style.display = "none";

    // Toggle notificaciones
    if (notificationsModal.style.display === "none") {
      fetchAllNotifications();
      notificationsModal.style.display = "block";
    } else {
      notificationsModal.style.display = "none";
    }
  });

  /**
   * Botón de cerrar notificaciones
   */
  if (closeNotificationsBtn) {
    closeNotificationsBtn.addEventListener("click", function (e) {
      e.preventDefault();
      e.stopPropagation();
      notificationsModal.style.display = "none";
    });
  }

  /**
   * Toggle del modal de perfil
   */
  profileBtn.addEventListener("click", function (e) {
    e.preventDefault();
    e.stopPropagation();

    // Cerrar notificaciones si está abierto
    notificationsModal.style.display = "none";

    // Toggle perfil
    profileModal.style.display =
      profileModal.style.display === "none" ? "block" : "none";
  });

  /**
   * Cerrar modales al hacer clic fuera
   */
  document.addEventListener("click", function (e) {
    if (
      !notificationsModal.contains(e.target) &&
      !notificationsBtn.contains(e.target)
    ) {
      notificationsModal.style.display = "none";
    }
    if (!profileModal.contains(e.target) && !profileBtn.contains(e.target)) {
      profileModal.style.display = "none";
    }
  });

  /**
   * Prevenir cierre al hacer clic dentro de los modales
   */
  notificationsModal.addEventListener("click", function (e) {
    e.stopPropagation();
  });

  profileModal.addEventListener("click", function (e) {
    e.stopPropagation();
  });

  // ============================================
  // FUNCIONES GLOBALES (para botones inline)
  // ============================================

  /**
   * Ver detalles de una notificación
   */
  window.viewNotificationDetails = function (id) {
    // Mark as read first for immediate feedback
    markAsRead(id);
    // Redirect to profile notifications tab with ID parameter for highlighting
    window.location.href = `/perfil?tab=notificaciones&notificationId=${id}`;
  };

  /**
   * Descartar una notificación
   */
  window.dismissNotification = function (id) {
    markAsRead(id);
  };

  // Expose fetchUnreadCount globally
  window.fetchUnreadCount = fetchUnreadCount;

  // ============================================
  // INICIALIZACIÓN
  // ============================================

  // Cargar conteo inicial
  fetchUnreadCount();

  // Actualizar cada 3 segundos para actualizaciones en tiempo real
  setInterval(fetchUnreadCount, 3000);
}
