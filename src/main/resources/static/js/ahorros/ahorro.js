document.addEventListener("DOMContentLoaded", function () {
  const API_BASE = "/api/ahorros";
  const API_CONCEPTOS = "/api/conceptos";
  let currentDeleteId = null;
  let currentAhorroId = null;

  // Normaliza la representación de un ahorro (soporta distintos nombres JSON)
  function normalizeAhorro(a) {
    if (!a) return a;
    return {
      ahorroId: a.ahorroId ?? a.id ?? a.ahorro_id ?? null,
      id: a.id ?? a.ahorroId ?? null,
      conceptoId: a.conceptoId ?? a.concepto_id ?? null,
      nombreConcepto: a.nombreConcepto ?? a.nombre_concepto ?? a.nombre ?? null,
      descripcion: a.descripcion ?? "",
      montoMeta: a.montoMeta ?? a.monto ?? a.monto_meta ?? 0,
      totalAcumulado: a.totalAcumulado ?? a.acumulado ?? a.total_acumulado ?? 0,
      frecuencia: a.frecuencia ?? null,
      fechaMeta: a.fechaMeta ?? a.meta ?? null,
      estado: a.estado ?? null,
      cantidadCuotas: a.cantidadCuotas ?? a.cantCuotas ?? a.cantidad_cuotas ?? null,
      raw: a,
    };
  }

  // Elementos del DOM
  const ahorrosGrid = document.getElementById("ahorrosGrid");
  const loadingSpinner = document.getElementById("loadingSpinner");
  const emptyState = document.getElementById("emptyState");
  const formAhorro = document.getElementById("formAhorro");
  const modalDetalle = new bootstrap.Modal(document.getElementById("modalDetalle"));
  const modalForm = new bootstrap.Modal(document.getElementById("modalFormAhorro"));
  const modalEliminar = new bootstrap.Modal(document.getElementById("modalEliminar"));
  const modalSeleccionConcepto = new bootstrap.Modal(
    document.getElementById("modalSeleccionConcepto")
  );

  const listaConceptosSeleccion = document.getElementById("listaConceptosSeleccion");
  const buscadorPrincipal = document.getElementById("buscador");
  const estadoFilter = document.getElementById("estadoFilter");

  // Cargar datos iniciales
  cargarAhorros();

  // Event Listeners
  formAhorro.addEventListener("submit", handleFormSubmit);
  document
    .getElementById("btnConfirmarEliminar")
    .addEventListener("click", confirmarEliminarAction);
  buscadorPrincipal.addEventListener("input", (e) => filtrarAhorros(e.target.value));
  estadoFilter.addEventListener("change", () => cargarAhorros());

  function filtrarAhorros(texto) {
    const termino = texto.toLowerCase();
    const tarjetas = document.querySelectorAll(".ahorro-card");

    tarjetas.forEach((tarjeta) => {
      const concepto = tarjeta.querySelector(".card-title")?.innerText.toLowerCase() || "";
      const visible = concepto.includes(termino);
      tarjeta.style.display = visible ? "" : "none";
    });
  }

  // --- Funciones Principales ---

  function cargarAhorros() {
    mostrarLoading(true);
    const estado = estadoFilter.value;
    const url = estado ? `${API_BASE}?estado=${estado}` : API_BASE;

    fetch(url, { credentials: "include" })
      .then((res) => res.json())
      .then((ahorros) => {
        renderData(ahorros);
        mostrarLoading(false);
      })
      .catch((err) => {
        console.error("Error cargando ahorros:", err);
        mostrarLoading(false);
      });
  }

  function renderData(ahorros) {
    if (!ahorros || ahorros.length === 0) {
      ahorrosGrid.innerHTML =
        '<div class="col-12"><p class="text-center text-muted">No hay ahorros registrados</p></div>';
      return;
    }

    ahorrosGrid.innerHTML = "";
    ahorros.forEach((ahorro) => {
      const n = normalizeAhorro(ahorro);
      const tarjeta = crearTarjetaAhorro(n);
      ahorrosGrid.appendChild(tarjeta);
    });

    actualizarDashboard(ahorros);
  }

  function crearTarjetaAhorro(ahorro) {
    const montoMeta = ahorro.montoMeta ?? 0;
    const totalAcumulado = ahorro.totalAcumulado ?? 0;
    const porcentaje = montoMeta && montoMeta !== 0 ? (totalAcumulado / montoMeta) * 100 : 0;
    const badgeClass =
      ahorro.estado === "ACTIVO"
        ? "bg-success"
        : ahorro.estado === "COMPLETADO"
        ? "bg-info"
        : ahorro.estado === "ABANDONADO"
        ? "bg-danger"
        : "bg-secondary";

    const col = document.createElement("div");
    col.className = "col-md-6 col-lg-4";

    const idVal = ahorro.ahorroId ?? ahorro.id ?? "null";

    col.innerHTML = `
      <div class="card ahorro-card border-0 shadow-sm h-100">
        <div class="card-body">
          <div class="d-flex justify-content-between align-items-start mb-3">
            <div>
              <h5 class="card-title mb-1">${ahorro.nombreConcepto || "Sin nombre"}</h5>
              <small class="text-muted">${ahorro.descripcion || "Sin descripción"}</small>
            </div>
            <span class="badge ${badgeClass}">${ahorro.estado}</span>
          </div>

          <!-- Progreso -->
          <div class="mb-3">
            <div class="d-flex justify-content-between mb-2">
              <small class="text-muted">Progreso</small>
              <small class="fw-bold">${porcentaje.toFixed(1)}%</small>
            </div>
            <div class="progress" style="height: 8px;">
              <div
                class="progress-bar bg-warning"
                role="progressbar"
                style="width: ${Math.min(porcentaje, 100)}%"
              ></div>
            </div>
          </div>

          <!-- Montos -->
          <div class="mb-3 pb-3 border-bottom">
            <div class="d-flex justify-content-between mb-2">
              <span class="text-muted">Objetivo:</span>
              <strong>$ ${formatoMoneda(montoMeta)}</strong>
            </div>
            <div class="d-flex justify-content-between">
              <span class="text-muted">Acumulado:</span>
              <strong class="text-warning-custom">$ ${formatoMoneda(totalAcumulado)}</strong>
            </div>
          </div>

          <!-- Botones de acción (aquí en la tarjeta) -->
          <div class="btn-group w-100" role="group">
            <button
              type="button"
              class="btn btn-sm btn-outline-info"
              onclick="abrirDetalleAhorro(${idVal})"
              title="Ver detalles"
            >
              <i class="bi bi-eye"></i> Detalle
            </button>
            <button
              type="button"
              class="btn btn-sm btn-outline-warning"
              onclick="abrirModalEditar(${idVal})"
              title="Editar"
            >
              <i class="bi bi-pencil"></i> Editar
            </button>
            <button
              type="button"
              class="btn btn-sm btn-outline-danger"
              onclick="abrirModalEliminar(${idVal})"
              title="Eliminar"
            >
              <i class="bi bi-trash"></i> Eliminar
            </button>
          </div>
        </div>
      </div>
    `;

    return col;
  }

  function actualizarDashboard(ahorros) {
    let totalAhorrado = 0;
    let metasActivas = 0;
    let completadas = 0;
    let proximaMeta = null;

    ahorros.forEach((aRaw) => {
      const a = normalizeAhorro(aRaw);
      totalAhorrado += parseFloat(a.totalAcumulado || 0);
      if (a.estado === "ACTIVO") metasActivas++;
      if (a.estado === "COMPLETADO") completadas++;
      if (a.fechaMeta) {
        if (!proximaMeta || new Date(a.fechaMeta) < new Date(proximaMeta.fechaMeta)) {
          proximaMeta = a;
        }
      }
    });

    document.getElementById("totalAhorrado").innerText = `$ ${formatoMoneda(totalAhorrado)}`;
    document.getElementById("statMetas").innerText = ahorros.length;
    document.getElementById("statCompletadas").innerText = completadas;
    document.getElementById("statProximaMeta").innerText = proximaMeta
      ? `${proximaMeta.nombreConcepto || "(sin nombre)"} — ${new Date(proximaMeta.fechaMeta).toLocaleDateString("es-ES")}`
      : "N/A";
  }

  function formatoMoneda(cantidad) {
    if (!cantidad) return "0.00";
    return parseFloat(cantidad).toLocaleString("es-ES", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });
  }

  // --- Modal Detalle ---

  window.abrirDetalleAhorro = function (id) {
    currentAhorroId = id;
    if (!id) {
      console.error("abrirDetalleAhorro llamado sin id");
      alert("No se pudo abrir detalle: id inválido");
      return;
    }

    fetch(`${API_BASE}/${id}`, { credentials: "include" })
      .then((r) => {
        if (!r.ok) return r.json().then(j => { throw j; });
        return r.json();
      })
      .then((ahorro) => {
        const n = normalizeAhorro(ahorro);
        // Llenar datos del ahorro
        document.getElementById("detalleConcepto").innerText = n.nombreConcepto || "N/A";
        document.getElementById("detalleDescripcion").innerText = n.descripcion || "Sin descripción";
        document.getElementById("detalleMontoMeta").innerText = `$ ${formatoMoneda(n.montoMeta)}`;
        document.getElementById("detalleTotalAcumulado").innerText = `$ ${formatoMoneda(n.totalAcumulado)}`;
        document.getElementById("detalleFrecuencia").innerText = n.frecuencia || "N/A";
        document.getElementById("detalleFechaMeta").innerText = n.fechaMeta ? new Date(n.fechaMeta).toLocaleDateString("es-ES") : "-";
        document.getElementById("detalleCantidadCuotas").innerText = n.cantidadCuotas ?? "N/A";
        document.getElementById("detalleEstado").innerText = n.estado || "-";
        document.getElementById("detalleEstado").className =
          "badge " +
          (n.estado === "ACTIVO"
            ? "bg-success"
            : n.estado === "COMPLETADO"
            ? "bg-info"
            : n.estado === "ABANDONADO"
            ? "bg-danger"
            : "bg-secondary");

        // Cargar cuotas
        cargarCuotasDetalle(n.ahorroId ?? n.id);

        modalDetalle.show();
      })
      .catch((err) => {
        console.error("Error obteniendo ahorro:", err);
        alert("Error al obtener el detalle del ahorro. Revisa la consola para más información.");
      });
  };

  function cargarCuotasDetalle(id) {
    fetch(`${API_BASE}/cuotas/${id}`, { credentials: "include" })
      .then((r) => {
        if (!r.ok) return r.json().then(j => { throw j; });
        return r.json();
      })
      .then((cuotas) => {
        if (!Array.isArray(cuotas)) {
          console.error('Respuesta de cuotas no es arreglo:', cuotas);
          document.getElementById("tablaCuotasBody").innerHTML = '<tr><td colspan="5">No hay cuotas o la respuesta es inválida</td></tr>';
          document.getElementById("resumenCuotas").innerHTML = '';
          return;
        }
        renderizarCuotas(cuotas);
      })
      .catch((err) => console.error("Error cargando cuotas:", err));
  }

  function renderizarCuotas(cuotas) {
    const tablaCuotas = document.getElementById("tablaCuotasBody");
    tablaCuotas.innerHTML = "";
    if (!cuotas || cuotas.length === 0) {
      tablaCuotas.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Sin cuotas</td></tr>';
      document.getElementById("resumenCuotas").innerHTML = '';
      return;
    }

    let pagadas = 0;
    let perdidas = 0;

    cuotas.forEach((cuota, index) => {
      const estado = cuota.estadoAp ?? cuota.estado ?? 'PENDIENTE';
      if (estado === 'APORTADO') pagadas++;
      if (estado === 'PERDIDO') perdidas++;

      const badgeClass = estado === 'APORTADO' ? 'bg-success' : estado === 'PERDIDO' ? 'bg-danger' : 'bg-warning';

      const numero = `${(index + 1).toString().padStart(2, '0')}/${cuotas.length.toString().padStart(2, '0')}`;

      const fila = document.createElement("tr");
      fila.innerHTML = `
        <td class="fw-bold">${numero}</td>
        <td>$ ${formatoMoneda(cuota.aporteAsignado ?? cuota.aporte_asignado ?? 0)}</td>
        <td>${(cuota.aporte ?? cuota.aporte) ? `$ ${formatoMoneda(cuota.aporte ?? cuota.aporte)}` : "---"}</td>
        <td>${cuota.fechaLimite ?? cuota.fecha_limite ?? '-'}</td>
        <td><span class="badge ${badgeClass}">${estado}</span></td>
      `;
      tablaCuotas.appendChild(fila);
    });

    // Mostrar resumen de cuotas
    document.getElementById("resumenCuotas").innerHTML = `
      <div class="alert alert-info mb-0">
        <strong>Resumen:</strong> ${pagadas} pagadas | ${perdidas} perdidas | ${cuotas.length - pagadas - perdidas} pendientes
      </div>
    `;
  }

  // --- Crear/Editar Ahorro ---

  window.abrirModalCrear = function () {
    currentAhorroId = null;
    document.getElementById("formAhorroTitle").innerText = "Nuevo Ahorro";
    document.getElementById("btnGuardarAhorro").innerText = "Crear";
    resetForm();
    cargarConceptos();
    modalForm.show();
  };

  window.abrirModalEditar = function (id) {
    currentAhorroId = id;
    document.getElementById("formAhorroTitle").innerText = "Editar Ahorro";
    document.getElementById("btnGuardarAhorro").innerText = "Actualizar";
    if (!id) {
      alert('Id inválido para editar');
      return;
    }
    fetch(`${API_BASE}/${id}`, { credentials: "include" })
      .then((r) => {
        if (!r.ok) return r.json().then(j => { throw j; });
        return r.json();
      })
      .then((ahorro) => {
        const n = normalizeAhorro(ahorro);
        document.getElementById("ahorroId").value = n.ahorroId || n.id || "";
        document.getElementById("conceptoId").value = n.conceptoId || "";
        document.getElementById("conceptoId").disabled = true;
        document.getElementById("montoMeta").value = n.montoMeta || "";
        document.getElementById("frecuencia").value = n.frecuencia || "";
        document.getElementById("cantidadCuotas").value = n.cantidadCuotas || "";
        document.getElementById("fechaMeta").value = n.fechaMeta || "";
        document.getElementById("descripcion").value = n.descripcion || "";

        cargarConceptos(n.conceptoId);
        modalForm.show();
      })
      .catch(err => {
        console.error('Error al cargar ahorro para editar:', err);
        alert('Error al cargar ahorro para editar');
      });
  };

  function cargarConceptos(selectedId = null) {
    fetch(`${API_CONCEPTOS}/tipo/AHORRO`, { credentials: "include" })
      .then((r) => r.json())
      .then((conceptos) => {
        const select = document.getElementById("conceptoId");
        select.innerHTML = '<option value="">Selecciona un concepto...</option>';
        conceptos.forEach((c) => {
          const option = document.createElement("option");
          option.value = c.id;
          option.innerText = c.nombre;
          if (selectedId && c.id === selectedId) option.selected = true;
          select.appendChild(option);
        });
      });
  }

  function handleFormSubmit(e) {
    e.preventDefault();

    const dto = {
      conceptoId: document.getElementById("conceptoId").value,
      montoMeta: document.getElementById("montoMeta").value,
      frecuencia: document.getElementById("frecuencia").value,
      cantidadCuotas: document.getElementById("cantidadCuotas").value || null,
      fechaMeta: document.getElementById("fechaMeta").value || null,
      descripcion: document.getElementById("descripcion").value,
    };

    const isEditing = currentAhorroId !== null;
    const method = isEditing ? "PUT" : "POST";
    const url = isEditing ? `${API_BASE}/${currentAhorroId}` : API_BASE;

    fetch(url, {
      method: method,
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify(dto),
    })
      .then(async (r) => {
        if (!r.ok) {
          let text = '';
          try { text = await r.text(); } catch (e) { /* ignore */ }
          let payload;
          try { payload = JSON.parse(text); } catch (e) { payload = text; }
          throw payload || { message: 'Error en la solicitud' };
        }
        return r.json();
      })
      .then(() => {
        modalForm.hide();
        resetForm();
        cargarAhorros();
      })
      .catch((err) => {
        console.error("Error en la solicitud:", err);
        const msg = err?.message || (typeof err === 'string' ? err : JSON.stringify(err));
        alert("Error en la solicitud: " + msg);
      });
  }

  function resetForm() {
    formAhorro.reset();
    document.getElementById("conceptoId").disabled = false;
    document.getElementById("ahorroId").value = "";
  }

  // --- Eliminar Ahorro ---

  window.abrirModalEliminar = function (id) {
    currentDeleteId = id;
    modalEliminar.show();
  };

  function confirmarEliminarAction() {
    fetch(`${API_BASE}/${currentDeleteId}`, {
      method: "DELETE",
      credentials: "include",
    })
      .then(async (r) => {
        if (!r.ok) {
          const text = await r.text().catch(() => '');
          throw text || 'Error eliminando';
        }
        modalEliminar.hide();
        cargarAhorros();
      })
      .catch((err) => {
        console.error("Error eliminando:", err);
        alert('Error eliminando: ' + (err?.message || err));
      });
  }

  // --- Exportación ---

  window.exportarReporte = function (tipo) {
    const ahorros = Array.from(document.querySelectorAll(".ahorro-card"))
      .filter((card) => card.style.display !== "none")
      .map((card) => {
        return {
          concepto: card.querySelector(".card-title")?.innerText,
          descripcion: card.querySelector("small")?.innerText,
        };
      });

    if (tipo === "pdf") {
      exportarPDF(ahorros);
    } else if (tipo === "excel") {
      exportarExcel(ahorros);
    } else if (tipo === "csv") {
      exportarCSV(ahorros);
    }
  };

  function exportarPDF(datos) {
    try {
      if (!window.jspdf) throw new Error('Falta la librería jsPDF');
      const { jsPDF } = window.jspdf;
      const doc = new jsPDF();
      doc.text("Reporte de Ahorros", 10, 10);
      doc.save("ahorros.pdf");
    } catch (e) {
      console.error('Error exportando PDF:', e);
      alert('No se pudo exportar a PDF. ' + (e.message || e));
    }
  }

  function exportarExcel(datos) {
    try {
      if (!window.XLSX) throw new Error('Falta la librería XLSX');
      const ws = XLSX.utils.json_to_sheet(datos);
      const wb = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(wb, ws, "Ahorros");
      XLSX.writeFile(wb, "ahorros.xlsx");
    } catch (e) {
      console.error('Error exportando Excel:', e);
      alert('No se pudo exportar a Excel. ' + (e.message || e));
    }
  }

  function exportarCSV(datos) {
    const csv = [
      ["Concepto", "Descripción"],
      ...datos.map((d) => [d.concepto, d.descripcion]),
    ]
      .map((row) => row.join(","))
      .join("\n");
    const blob = new Blob([csv], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "ahorros.csv";
    a.click();
  }

  function mostrarLoading(show) {
    if (loadingSpinner) {
      loadingSpinner.style.display = show ? "block" : "none";
    }
  }
});