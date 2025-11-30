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
  const listaConceptosSeleccion = document.getElementById(
    "listaConceptosSeleccion"
  );
  const buscadorPrincipal = document.getElementById("buscadorPrincipal");
  const buscadorModal = document.getElementById("buscadorModal");

  // Cargar datos iniciales
  cargarDatosMensuales();

  // Event Listeners
  formEgreso.addEventListener("submit", handleFormSubmit);
  document
    .getElementById("btnConfirmarEliminar")
    .addEventListener("click", confirmarEliminarAction);
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

  function cargarDatosMensuales() {
    mostrarLoading(true);

    // Fetch all movements and concepts in parallel
    const pMovimientos = fetch(API_BASE, { credentials: "include" }).then(
      (res) => {
        if (!res.ok) throw new Error("Error cargando movimientos");
        return res.json();
      }
    );

    const pConceptos = fetch(`${API_CONCEPTOS}/tipo/EGRESO`, {
      credentials: "include",
    }).then((res) => {
      if (!res.ok) throw new Error("Error cargando conceptos");
      return res.json();
    });

    Promise.all([pMovimientos, pConceptos])
      .then(([movimientos, conceptosList]) => {
        // Filter for current month
        const now = new Date();
        const currentMonth = now.getMonth();
        const currentYear = now.getFullYear();

        const movimientosMes = movimientos.filter((m) => {
          const d = new Date(m.fechaRegistro);
          return (
            d.getMonth() === currentMonth && d.getFullYear() === currentYear
          );
        });

        // Map concepts for easy lookup
        const conceptosMap = {};
        conceptosList.forEach((c) => (conceptosMap[c.id] = c));

        // 1. Update Dashboard Stats
        actualizarDashboard(movimientosMes);

        // 2. Group by Concept for the Grid
        const conceptosAgrupados = agruparPorConcepto(movimientosMes);

        // 3. Render Grid with Monthly Data
        const totalMes = movimientosMes.reduce((sum, m) => sum + m.monto, 0);

        const dataParaGrid = Object.keys(conceptosAgrupados).map((id) => {
          const concepto = conceptosMap[id] || {
            nombre: "Desconocido",
            descripcion: "",
          };
          return {
            conceptoId: id,
            nombre: concepto.nombre,
            descripcion: concepto.descripcion,
            cantidadRegistros: conceptosAgrupados[id].cantidad,
            totalAcumulado: conceptosAgrupados[id].total,
            movimientos: conceptosAgrupados[id].movimientos,
          };
        });

        renderizarConceptos(dataParaGrid, totalMes);
      })
      .catch((error) => {
        console.error("Error:", error);
        mostrarLoading(false);
        if (conceptosGrid && conceptosGrid.innerHTML === "") {
          emptyState.classList.remove("d-none");
        }
      })
      .finally(() => mostrarLoading(false));
  }

  function agruparPorConcepto(movimientos) {
    const agrupado = {};
    movimientos.forEach((m) => {
      if (!agrupado[m.conceptoId]) {
        agrupado[m.conceptoId] = {
          cantidad: 0,
          total: 0,
          movimientos: [],
        };
      }
      agrupado[m.conceptoId].cantidad++;
      agrupado[m.conceptoId].total += m.monto;
      agrupado[m.conceptoId].movimientos.push(m);
    });
    return agrupado;
  }

  function actualizarDashboard(movimientos) {
    const total = movimientos.reduce((sum, m) => sum + m.monto, 0);
    const cantidad = movimientos.length;
    const conceptosUnicos = new Set(movimientos.map((m) => m.conceptoId)).size;

    // Last record calculation
    let ultimoRegistroTexto = "-";
    if (movimientos.length > 0) {
      const sorted = [...movimientos].sort(
        (a, b) => new Date(b.fechaRegistro) - new Date(a.fechaRegistro)
      );
      const lastRecordDate = new Date(sorted[0].fechaRegistro);
      const now = new Date();
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
      const recordDate = new Date(
        lastRecordDate.getFullYear(),
        lastRecordDate.getMonth(),
        lastRecordDate.getDate()
      );

      const diffTime = today - recordDate;
      const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

      if (diffDays === 0) {
        ultimoRegistroTexto = "Hoy";
      } else if (diffDays === 1) {
        ultimoRegistroTexto = "Ayer";
      } else {
        ultimoRegistroTexto = `Hace ${diffDays} días`;
      }
    }

    // Update DOM
    const totalEl = document.getElementById("totalMesActual");
    if (totalEl) totalEl.textContent = `$${formatoMoneda(total)}`;

    const regEl = document.getElementById("statRegistros");
    if (regEl) regEl.textContent = cantidad;

    const concEl = document.getElementById("statConceptos");
    if (concEl) concEl.textContent = conceptosUnicos;

    const lastEl = document.getElementById("statUltimoRegistro");
    if (lastEl) lastEl.textContent = ultimoRegistroTexto;
  }

  function formatoMoneda(cantidad) {
    return cantidad.toLocaleString("es-ES", {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    });
  }

  function renderizarConceptos(conceptos, totalMes) {
    if (!conceptosGrid) return;
    conceptosGrid.innerHTML = "";

    if (!conceptos || conceptos.length === 0) {
      conceptosGrid.classList.add("d-none");
      emptyState.classList.remove("d-none");
      return;
    }

    emptyState.classList.add("d-none");
    conceptosGrid.classList.remove("d-none");

    conceptos.forEach((c) => {
      const porcentaje = totalMes > 0 ? (c.totalAcumulado / totalMes) * 100 : 0;

      const card = document.createElement("div");
      card.className = "col-md-4 col-lg-3";
      card.innerHTML = `
                <div class="card h-100 border-0 shadow-sm hover-card" style="cursor: pointer; transition: transform 0.2s;">
                    <div class="card-body">
                        <div class="d-flex align-items-center mb-3">
                            <div class="rounded bg-danger text-white d-flex align-items-center justify-content-center me-3" style="width: 50px; height: 50px;">
                                <i class="bi bi-wallet2 fs-4"></i>
                            </div>
                            <div>
                                <h5 class="card-title fw-bold text-dark mb-0">${
                                  c.nombre
                                }</h5>
                                <small class="text-muted">${
                                  c.descripcion || "Sin descripción"
                                }</small>
                            </div>
                            <span class="badge bg-danger-subtle text-danger ms-auto">${
                              c.cantidadRegistros
                            } Reg.</span>
                        </div>
                        
                        <h3 class="fw-bold text-danger mb-1">$${formatoMoneda(
                          c.totalAcumulado
                        )}</h3>
                        <div class="d-flex justify-content-between small text-muted mb-2">
                            <span>${porcentaje.toFixed(0)}% del total</span>
                        </div>
                        
                        <div class="progress" style="height: 8px;">
                            <div class="progress-bar bg-danger" role="progressbar" 
                                 style="width: 0%; transition: width 1s ease-in-out;" 
                                 aria-valuenow="${porcentaje}" aria-valuemin="0" aria-valuemax="100"></div>
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

      // Trigger animation
      setTimeout(() => {
        const bar = card.querySelector(".progress-bar");
        if (bar) bar.style.width = `${porcentaje}%`;
      }, 100);
    });
  }

  function abrirDetalleConcepto(concepto) {
    document.getElementById("modalDetalleTitle").textContent = concepto.nombre;
    document.getElementById(
      "modalDetalleTotal"
    ).textContent = `Total: $${formatoMoneda(concepto.totalAcumulado)}`;
    const tbody = document.getElementById("tablaDetalleBody");
    tbody.innerHTML = "";

    if (concepto.movimientos && concepto.movimientos.length > 0) {
      concepto.movimientos.forEach((egreso) => {
        const tr = document.createElement("tr");
        const fecha = new Date(egreso.fechaRegistro).toLocaleDateString();
        tr.innerHTML = `
                      <td>${fecha}</td>
                      <td>${egreso.descripcion || "-"}</td>
                      <td class="text-end fw-bold text-danger">$${formatoMoneda(
                        egreso.monto
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
    } else {
      tbody.innerHTML =
        '<tr><td colspan="4" class="text-center">No hay registros este mes.</td></tr>';
    }

    modalDetalle.show();
  }

  // --- Funciones de Formulario (Crear/Editar) ---

  window.abrirModalCrear = function () {
    document.getElementById("modalFormTitle").textContent = "Nuevo Egreso";
    formEgreso.reset();
    document.getElementById("egresoId").value = "";
    document.getElementById("conceptoId").value = "";
    document.getElementById("conceptoInput").value = "";

    const conceptoInputField = document.getElementById("conceptoInput");
    conceptoInputField.style.cursor = "pointer";
    conceptoInputField.style.backgroundColor = "white";
    conceptoInputField.onclick = () => abrirModalSeleccionConcepto();

    modalForm.show();
  };

  window.abrirModalSeleccionConcepto = function () {
    listaConceptosSeleccion.innerHTML =
      '<div class="text-center w-100"><div class="spinner-border text-success" role="status"></div></div>';
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
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    })
      .then((response) => {
        if (!response.ok) throw new Error("Error guardando egreso");
        return response.json();
      })
      .then(() => {
        modalForm.hide();
        cargarDatosMensuales();
        formEgreso.reset();
      })
      .catch((error) => console.error("Error:", error));
  }

  function confirmarEliminarAction() {
    if (!currentDeleteId) return;

    fetch(`${API_BASE}/${currentDeleteId}`, {
      method: "DELETE",
      credentials: "include",
    })
      .then((response) => {
        if (!response.ok) throw new Error("Error eliminando");
        modalEliminar.hide();
        modalDetalle.hide();
        cargarDatosMensuales();
      })
      .catch((err) => console.error(err));
  }

  function mostrarLoading(show) {
    if (loadingSpinner) {
      if (show) {
        loadingSpinner.classList.remove("d-none");
        if (conceptosGrid) conceptosGrid.classList.add("d-none");
        if (emptyState) emptyState.classList.add("d-none");
      } else {
        loadingSpinner.classList.add("d-none");
      }
    }
  }

  // --- Funciones de Exportación ---

  window.exportarReporte = function (tipo) {
    // Fetch all data for export
    fetch(API_BASE, { credentials: "include" })
      .then((res) => res.json())
      .then((data) => {
        // Group by concept for the report
        const grouped = {};
        data.forEach((m) => {
          if (!grouped[m.conceptoId])
            grouped[m.conceptoId] = { cantidad: 0, total: 0 };
          grouped[m.conceptoId].cantidad++;
          grouped[m.conceptoId].total += m.monto;
        });

        // Need concept names, so fetch them too
        fetch(`${API_CONCEPTOS}/tipo/EGRESO`, { credentials: "include" })
          .then((res) => res.json())
          .then((conceptos) => {
            const reportData = conceptos
              .map((c) => {
                const stats = grouped[c.id] || { cantidad: 0, total: 0 };
                return {
                  nombre: c.nombre,
                  cantidadRegistros: stats.cantidad,
                  totalAcumulado: stats.total,
                };
              })
              .filter((r) => r.totalAcumulado > 0);

            if (tipo === "pdf") exportarPDF(reportData);
            else if (tipo === "excel") exportarExcel(reportData);
            else if (tipo === "csv") exportarCSV(reportData);
          });
      })
      .catch((err) => console.error("Error exportando:", err));
  };

  function exportarPDF(data) {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();

    doc.setFontSize(18);
    doc.text("Reporte de Egresos", 14, 22);
    doc.setFontSize(11);
    doc.text(`Fecha: ${new Date().toLocaleDateString()}`, 14, 30);

    const tableData = data.map((item) => [
      item.nombre,
      item.cantidadRegistros,
      `$${item.totalAcumulado.toFixed(2)}`,
    ]);

    doc.autoTable({
      head: [["Concepto", "Registros", "Total"]],
      body: tableData,
      startY: 40,
      theme: "grid",
      headStyles: { fillColor: [220, 53, 69] },
    });

    doc.save("reporte_egresos.pdf");
  }

  function exportarExcel(data) {
    const ws = XLSX.utils.json_to_sheet(
      data.map((item) => ({
        Concepto: item.nombre,
        "Cantidad Registros": item.cantidadRegistros,
        "Total Acumulado": item.totalAcumulado,
      }))
    );
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Egresos");
    XLSX.writeFile(wb, "reporte_egresos.xlsx");
  }

  function exportarCSV(data) {
    const ws = XLSX.utils.json_to_sheet(
      data.map((item) => ({
        Concepto: item.nombre,
        "Cantidad Registros": item.cantidadRegistros,
        "Total Acumulado": item.totalAcumulado,
      }))
    );
    const csv = XLSX.utils.sheet_to_csv(ws);
    const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
    const link = document.createElement("a");
    const url = URL.createObjectURL(blob);
    link.setAttribute("href", url);
    link.setAttribute("download", "reporte_egresos.csv");
    link.style.visibility = "hidden";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
});
