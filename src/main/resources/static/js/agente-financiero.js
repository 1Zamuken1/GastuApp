// Variables globales definidas desde el HTML
let USUARIO_ID = window.USUARIO_ID;
const API_URL = window.API_URL || "/api/agente-financiero";

// ===== LOGS DE DEBUG =====
console.log("=== INICIALIZANDO AGENTE FINANCIERO ===");
console.log("USUARIO_ID:", USUARIO_ID);
console.log("Tipo de USUARIO_ID:", typeof USUARIO_ID);
console.log("API_URL:", API_URL);
console.log("=======================================");

// Inicializar al cargar la página
document.addEventListener("DOMContentLoaded", function () {
  console.log("DOM cargado, validando usuario...");
  
  // Verificar que tengamos el usuario
  if (!USUARIO_ID || USUARIO_ID === null || USUARIO_ID === "null") {
    console.error("ERROR: No se pudo obtener el ID del usuario");
    console.error("window.USUARIO_ID:", window.USUARIO_ID);
    
    Swal.fire({
      icon: "error",
      title: "Error",
      text: "No se pudo identificar al usuario. Por favor, inicia sesión nuevamente.",
      confirmButtonColor: "#0d6efd",
    }).then(() => {
      window.location.href = "/login";
    });
    return;
  }

  console.log("Usuario validado correctamente, inicializando...");
  inicializar();
});

function inicializar() {
  cargarEstadisticas();
  cargarHistorial();
  configurarEventos();
  mostrarHoraInicial();
}

function configurarEventos() {
  // Botón enviar
  const enviarBtn = document.getElementById("enviarBtn");
  if (enviarBtn) {
    enviarBtn.addEventListener("click", enviarConsulta);
  }

  // Enter en el input
  const preguntaInput = document.getElementById("preguntaInput");
  if (preguntaInput) {
    preguntaInput.addEventListener("keypress", function (e) {
      if (e.key === "Enter") {
        enviarConsulta();
      }
    });
  }

  // Botones de sugerencias
  const suggestionBtns = document.querySelectorAll(".suggestion-btn");
  suggestionBtns.forEach((btn) => {
    btn.addEventListener("click", function () {
      const pregunta = this.getAttribute("data-pregunta");
      sugerirPregunta(pregunta);
    });
  });
}

function mostrarHoraInicial() {
  const tiempoElement = document.getElementById("tiempoInicial");
  if (tiempoElement) {
    tiempoElement.textContent = obtenerHoraActual();
  }
}

function obtenerHoraActual() {
  return new Date().toLocaleTimeString("es-ES", {
    hour: "2-digit",
    minute: "2-digit",
  });
}

// ============================================
// CARGAR DATOS
// ============================================

function cargarEstadisticas() {
  // Consultas del día
  fetch(`${API_URL}/consultas-del-dia/${USUARIO_ID}`)
    .then((response) => {
      if (!response.ok) {
        throw new Error("Error al cargar estadísticas del día");
      }
      return response.json();
    })
    .then((data) => {
      document.getElementById("consultasHoy").textContent =
        data.consultasHoy || 0;
    })
    .catch((error) => {
      console.error("Error cargando estadísticas del día:", error);
      document.getElementById("consultasHoy").textContent = "0";
    });

  // Total de consultas
  fetch(`${API_URL}/historial/${USUARIO_ID}`)
    .then((response) => {
      if (!response.ok) {
        throw new Error("Error al cargar historial");
      }
      return response.json();
    })
    .then((data) => {
      document.getElementById("totalConsultas").textContent = data.length || 0;
    })
    .catch((error) => {
      console.error("Error cargando total de consultas:", error);
      document.getElementById("totalConsultas").textContent = "0";
    });
}

function cargarHistorial() {
  fetch(`${API_URL}/historial/${USUARIO_ID}`)
    .then((response) => {
      if (!response.ok) {
        throw new Error("Error al cargar historial");
      }
      return response.json();
    })
    .then((data) => {
      const chatContainer = document.getElementById("chatContainer");

      // Guardar el mensaje de bienvenida
      const mensajeBienvenida = chatContainer.innerHTML;

      // Limpiar chat
      chatContainer.innerHTML = mensajeBienvenida;

      // Mostrar historial (últimos 10, en orden inverso)
      if (data && data.length > 0) {
        data
          .slice(0, 10)
          .reverse()
          .forEach((consulta) => {
            agregarMensaje(consulta.pregunta, "user", false);
            agregarMensaje(consulta.respuesta, "assistant", false);
          });
      }
    })
    .catch((error) => {
      console.error("Error cargando historial:", error);
    });
}

// ============================================
// ENVIAR CONSULTA
// ============================================

function enviarConsulta() {
  const preguntaInput = document.getElementById("preguntaInput");
  const pregunta = preguntaInput.value.trim();

  if (!pregunta) {
    Swal.fire({
      icon: "warning",
      title: "Campo vacío",
      text: "Por favor escribe una pregunta",
      confirmButtonColor: "#0d6efd",
    });
    return;
  }

  const incluirContexto = document.getElementById("incluirContexto").checked;
  const enviarBtn = document.getElementById("enviarBtn");
  const loading = document.getElementById("loading");

  // Mostrar mensaje del usuario
  agregarMensaje(pregunta, "user");

  // Deshabilitar controles
  preguntaInput.disabled = true;
  enviarBtn.disabled = true;
  loading.classList.remove("d-none");

  // Limpiar input
  preguntaInput.value = "";

  // Preparar datos
  const requestData = {
    pregunta: pregunta,
    usuarioId: USUARIO_ID,
    incluirContextoFinanciero: incluirContexto,
  };

  console.log("Enviando consulta:", requestData);

  // Enviar a la API
  fetch(`${API_URL}/consultar`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(requestData),
  })
    .then((response) => {
      if (!response.ok) {
        return response.json().then((errorData) => {
          throw new Error(errorData.mensaje || "Error en la respuesta del servidor");
        });
      }
      return response.json();
    })
    .then((data) => {
      console.log("Respuesta recibida:", data);
      agregarMensaje(data.respuesta, "assistant");
      cargarEstadisticas();
    })
    .catch((error) => {
      console.error("Error:", error);
      agregarError(
        "Lo siento, ocurrió un error al procesar tu consulta. Por favor intenta de nuevo."
      );
    })
    .finally(() => {
      preguntaInput.disabled = false;
      enviarBtn.disabled = false;
      loading.classList.add("d-none");
      preguntaInput.focus();
    });
}

// ============================================
// AGREGAR MENSAJES AL CHAT
// ============================================

function agregarMensaje(texto, tipo, scroll = true) {
  const chatContainer = document.getElementById("chatContainer");

  // Crear contenedor del mensaje
  const messageDiv = document.createElement("div");
  messageDiv.className = `d-flex align-items-start mb-3 ${
    tipo === "user" ? "justify-content-end" : ""
  }`;

  // Avatar
  const avatarDiv = document.createElement("div");
  avatarDiv.className = `rounded-circle bg-primary text-white d-flex align-items-center justify-content-center ${
    tipo === "user" ? "ms-2 order-2" : "me-2"
  }`;
  avatarDiv.style.cssText = "width: 40px; height: 40px; min-width: 40px;";
  avatarDiv.innerHTML =
    tipo === "user"
      ? '<i class="bi bi-person-fill"></i>'
      : '<i class="bi bi-robot"></i>';

  // Contenido del mensaje
  const contentDiv = document.createElement("div");
  contentDiv.className = `${
    tipo === "user" ? "bg-primary text-white" : "bg-white"
  } rounded p-3 shadow-sm`;
  contentDiv.style.maxWidth = "70%";

  // Texto del mensaje
  const textP = document.createElement("p");
  textP.className = "mb-1";
  textP.style.whiteSpace = "pre-wrap"; // Para respetar saltos de línea
  textP.textContent = texto;

  // Timestamp
  const timestamp = document.createElement("small");
  timestamp.className = tipo === "user" ? "text-white-50" : "text-muted";
  timestamp.textContent = obtenerHoraActual();

  contentDiv.appendChild(textP);
  contentDiv.appendChild(timestamp);

  // Ensamblar mensaje
  messageDiv.appendChild(avatarDiv);
  messageDiv.appendChild(contentDiv);

  // Agregar al chat
  chatContainer.appendChild(messageDiv);

  // Scroll automático
  if (scroll) {
    chatContainer.scrollTop = chatContainer.scrollHeight;
  }
}

function agregarError(mensaje) {
  const chatContainer = document.getElementById("chatContainer");

  const errorDiv = document.createElement("div");
  errorDiv.className = "alert alert-danger d-flex align-items-center mb-3";
  errorDiv.innerHTML = `
    <i class="bi bi-exclamation-triangle-fill me-2"></i>
    <div>${mensaje}</div>
  `;

  chatContainer.appendChild(errorDiv);
  chatContainer.scrollTop = chatContainer.scrollHeight;
}

// ============================================
// FUNCIONES AUXILIARES
// ============================================

function sugerirPregunta(pregunta) {
  const input = document.getElementById("preguntaInput");
  if (input) {
    input.value = pregunta;
    input.focus();
  }
}
