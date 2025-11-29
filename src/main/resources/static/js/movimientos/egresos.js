document.addEventListener("DOMContentLoaded", function () {
  const API_BASE = "/api/movimientos/egresos";
  const API_CONCEPTOS = "/api/conceptos";
  let currentDeleteId = null;

  // Elementos del DOM
  const conceptosGrid = document.getElementById("conceptosGrid");
  const loadingSpinner = document.getElementById("loadingSpinner");
  const emptyState = document.getElementById("emptyState");
  const modalDetalle = new bootstrap.Modal(
    document.getElementById("modalDetalle")
  );
  const modalForm = new bootstrap.Modal(
    document.getElementById("modalFormEgreso")
  );
  const modalEliminar = new bootstrap.Modal(
    document.getElementById("modalEliminar")
  );
  const modalSeleccionConcepto = new bootstrap.Modal(
    document.getElementById("modalSeleccionConcepto")
  );

  const formEgreso = document.getElementById("formEgreso");
  const conceptoInput = document.getElementById("conceptoInput");
  const conceptoIdHidden = document.getElementById("conceptoId");
  const listaConceptosSeleccion = document.getElementById(
    "listaConceptosSeleccion"
  );

  const buscadorPrincipal = document.getElementById("buscadorPrincipal");
  const buscadorModal = document.getElementById("buscadorModal");

  // Cargar resumen inicial
  cargarResumen();

  // Event Listeners
  formEgreso.addEventListener("submit", handleFormSubmit);
  document
    .getElementById("btnConfirmarEliminar")
    .addEventListener("click", confirmarEliminar);
  document
    .getElementById("btnAgregarDesdeDetalle")
    .addEventListener("click", () => {
      modalDetalle.hide();
      abrirModalCrear();
    });

  // Buscadores
  buscadorPrincipal.addEventListener("input", (e) =>
    filtrarTarjetas(e.target.value, conceptosGrid)
  );
  buscadorModal.addEventListener("input", (e) =>
    filtrarTarjetas(e.target.value, listaConceptosSeleccion)
  );

  function filtrarTarjetas(texto, contenedor) {
    const termino = texto.toLowerCase();
    const columnas = contenedor.children;

    Array.from(columnas).forEach((col) => {
      // Ignorar si es spinner o mensaje
      if (!col.classList.contains("col") && !col.classList.contains("col-md-4"))
        return;

      const card = col.querySelector(".card");
      if (!card) return;

      const titulo =
        card.querySelector(".card-title")?.textContent.toLowerCase() || "";
      const desc =
        card.querySelector(".card-text")?.textContent.toLowerCase() || "";

      if (titulo.includes(termino) || desc.includes(termino)) {
        col.classList.remove("d-none");
      } else {
        col.classList.add("d-none");
      }
    });
  }

  // --- Funciones Principales ---

  function cargarResumen() {
    mostrarLoading(true);
    fetch(`${API_BASE}/resumen-conceptos`, {
      credentials: "include",
    })
      .then((response) => {
        if (!response.ok) throw new Error("Error cargando resumen");
        return response.json();
      })
      .then((data) => {
        renderizarConceptos(data);
      })
      .catch((error) => {
        console.error("Error:", error);
        mostrarLoading(false);
        if (conceptosGrid.innerHTML === "") {
          emptyState.classList.remove("d-none");
        }
      })
      .finally(() => mostrarLoading(false));
  }

  function renderizarConceptos(conceptos) {
    conceptosGrid.innerHTML = "";

    if (!conceptos || conceptos.length === 0) {
      conceptosGrid.classList.add("d-none");
      emptyState.classList.remove("d-none");
      return;
    }

    emptyState.classList.add("d-none");
    conceptosGrid.classList.remove("d-none");

    conceptos.forEach((c) => {
      const card = document.createElement("div");
      card.className = "col-md-4 col-lg-3";
      card.innerHTML = `
                <div class="card h-100 border-0 shadow-sm hover-card" style="cursor: pointer; transition: transform 0.2s;">
                    <div class="card-body text-center">
                        <div class="rounded-circle bg-danger-subtle text-danger d-inline-flex align-items-center justify-content-center mb-3" style="width: 60px; height: 60px;">
                            <i class="bi bi-wallet2 fs-3"></i>
                        </div>
                        <h5 class="card-title fw-bold text-dark">${
                          c.nombre
                        }</h5>
                        <p class="card-text text-muted small">${
                          c.descripcion || "Sin descripción"
                        }</p>
                        <hr class="my-2">
                        <div class="d-flex justify-content-between text-danger fw-bold">
                            <span>${c.cantidadRegistros} Reg.</span>
                            <span>$${c.totalAcumulado.toFixed(2)}</span>
                        </div>
                    </div>
                </div>
            `;

      const cardInner = card.querySelector(".card");
      cardInner.addEventListener(
        "mouseenter",
        () => (cardInner.style.transform = "translateY(-5px)")
      );
      cardInner.addEventListener(
        "mouseleave",
        () => (cardInner.style.transform = "translateY(0)")
      );

      card.addEventListener("click", () => abrirDetalleConcepto(c));
      conceptosGrid.appendChild(card);
    });
  }

  function abrirDetalleConcepto(concepto) {
    document.getElementById("modalDetalleTitle").textContent = concepto.nombre;
    document.getElementById(
      "modalDetalleTotal"
    ).textContent = `Total: $${concepto.totalAcumulado.toFixed(2)}`;
    const tbody = document.getElementById("tablaDetalleBody");
    tbody.innerHTML =
      '<tr><td colspan="4" class="text-center"><div class="spinner-border text-danger spinner-border-sm"></div></td></tr>';

    modalDetalle.show();

    fetch(`${API_BASE}/concepto/${concepto.conceptoId}`, {
      credentials: "include",
    })
      .then((res) => res.json())
      .then((egresos) => {
        tbody.innerHTML = "";
        egresos.forEach((egreso) => {
          const tr = document.createElement("tr");
          const fecha = new Date(egreso.fechaRegistro).toLocaleDateString();
          tr.innerHTML = `
                    <td>${fecha}</td>
                    <td>${egreso.descripcion || "-"}</td>
                    <td class="text-end fw-bold text-danger">$${egreso.monto.toFixed(
                      2
                    )}</td>
                    <td class="text-end">
                        <button class="btn btn-sm btn-outline-primary me-1 btn-editar" data-id="${
                          egreso.id
                        }"><i class="bi bi-pencil"></i></button>
                        <button class="btn btn-sm btn-outline-danger btn-eliminar" data-id="${
                          egreso.id
                        }"><i class="bi bi-trash"></i></button>
                    </td>
                `;

          tr.querySelector(".btn-editar").addEventListener("click", (e) => {
            e.stopPropagation();
            abrirModalEditar(egreso);
          });
          tr.querySelector(".btn-eliminar").addEventListener("click", (e) => {
            e.stopPropagation();
            currentDeleteId = egreso.id;
            modalEliminar.show();
          });

          tbody.appendChild(tr);
        });
      });
  }

  // --- Funciones de Formulario (Crear/Editar) ---

  window.abrirModalCrear = function () {
    document.getElementById("modalFormTitle").textContent = "Nuevo Egreso";
    formEgreso.reset();
    document.getElementById("egresoId").value = "";
    document.getElementById("conceptoId").value = "";
    document.getElementById("conceptoInput").value = "";

    const conceptoInputField = document.getElementById("conceptoInput");
    // Restore concept field to clickable state
    conceptoInputField.style.cursor = "pointer";
    conceptoInputField.style.backgroundColor = "white";
    conceptoInputField.onclick = () => abrirModalSeleccionConcepto();

    modalForm.show();
  };

  window.abrirModalSeleccionConcepto = function () {
    listaConceptosSeleccion.innerHTML =
      '<div class="text-center w-100"><div class="spinner-border text-danger" role="status"></div></div>';
    buscadorModal.value = "";
    modalSeleccionConcepto.show();

    fetch(`${API_CONCEPTOS}/tipo/EGRESO`, { credentials: "include" })
      .then((res) => res.json())
      .then((conceptos) => {
        listaConceptosSeleccion.innerHTML = "";
        conceptos.forEach((c) => {
          const col = document.createElement("div");
          col.className = "col";
          col.innerHTML = `
                    <div class="card h-100 border-0 shadow-sm hover-card concept-card" style="cursor: pointer;">
                        <div class="card-body text-center">
                            <i class="bi bi-wallet2 fs-1 text-danger mb-2"></i>
                            <h6 class="card-title fw-bold">${c.nombre}</h6>
                            <p class="card-text small text-muted">${
                              c.descripcion || ""
                            }</p>
                        </div>
                    </div>
                `;
          col.querySelector(".card").addEventListener("click", () => {
            seleccionarConcepto(c);
          });
          listaConceptosSeleccion.appendChild(col);
        });
      });
  };

  function seleccionarConcepto(concepto) {
    document.getElementById("conceptoId").value = concepto.id;
    document.getElementById("conceptoInput").value = concepto.nombre;
    modalSeleccionConcepto.hide();
    modalForm.show();
  }

  function abrirModalEditar(egreso) {
    document.getElementById("modalFormTitle").textContent = "Editar Egreso";
    document.getElementById("egresoId").value = egreso.id;
    document.getElementById("montoInput").value = egreso.monto;
    document.getElementById("descripcionInput").value = egreso.descripcion;

    const conceptoInputField = document.getElementById("conceptoInput");
    // Make concept field readonly in edit mode
    conceptoInputField.style.cursor = "not-allowed";
    conceptoInputField.style.backgroundColor = "#e9ecef";
    conceptoInputField.onclick = null;

    fetch(`${API_CONCEPTOS}/${egreso.conceptoId}`, { credentials: "include" })
      .then((res) => res.json())
      .then((concepto) => {
        document.getElementById("conceptoId").value = concepto.id;
        document.getElementById("conceptoInput").value = concepto.nombre;
      });

    modalDetalle.hide();
    modalForm.show();
  }

  function handleFormSubmit(e) {
    e.preventDefault();

    const id = document.getElementById("egresoId").value;
    const isEdit = !!id;
    const url = isEdit ? `${API_BASE}/${id}` : API_BASE;
    const method = isEdit ? "PUT" : "POST";

    const data = {
      conceptoId: parseInt(document.getElementById("conceptoId").value),
      monto: parseFloat(document.getElementById("montoInput").value),
      descripcion: document.getElementById("descripcionInput").value,
    };

    fetch(url, {
      method: method,
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    })
      .then((response) => {
        if (!response.ok) throw new Error("Error al guardar");
        return response.json();
      })
      .then(() => {
        modalForm.hide();
        cargarResumen();
      })
      .catch((error) => alert("Error: " + error.message));
  }

  // --- Funciones de Eliminación ---

  function confirmarEliminar() {
    if (!currentDeleteId) return;

    fetch(`${API_BASE}/${currentDeleteId}`, {
      method: "DELETE",
      credentials: "include",
    })
      .then((response) => {
        if (!response.ok) throw new Error("Error al eliminar");
        modalEliminar.hide();
        modalDetalle.hide();
        cargarResumen();
      })
      .catch((error) => alert("Error: " + error.message));
  }

  function mostrarLoading(show) {
    if (show) {
      loadingSpinner.classList.remove("d-none");
      conceptosGrid.classList.add("d-none");
      emptyState.classList.add("d-none");
    } else {
      loadingSpinner.classList.add("d-none");
    }
  }
});
