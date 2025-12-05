document.addEventListener("DOMContentLoaded", function () {
    const API_BASE = "/api/ahorros";
    const API_CONCEPTOS = "/api/conceptos";
    let currentDeleteId = null;
    let currentAhorroId = null;

    // Elementos del DOM
    const ahorrosGrid = document.getElementById("ahorrosGrid");
    const loadingSpinner = document.getElementById("loadingSpinner");
    const emptyState = document.getElementById("emptyState");

    // Modales
    const modalDetalle = new bootstrap.Modal(document.getElementById("modalDetalle"));
    const modalFormAhorro = new bootstrap.Modal(document.getElementById("modalFormAhorro"));
    const modalEliminar = new bootstrap.Modal(document.getElementById("modalEliminar"));
    const modalSeleccionConcepto = new bootstrap.Modal(document.getElementById("modalSeleccionConcepto"));
    const modalFormAporte = new bootstrap.Modal(document.getElementById("modalFormAporte"));

    // Formularios
    const formAhorro = document.getElementById("formAhorro");
    const formAporte = document.getElementById("formAporte");
    const listaConceptosSeleccion = document.getElementById("listaConceptosSeleccion");
    const buscadorPrincipal = document.getElementById("buscadorPrincipal");
    const buscadorModal = document.getElementById("buscadorModal");
    const filtroEstado = document.getElementById("filtroEstado");

    // Cargar datos iniciales
    cargarAhorros();

    // Event Listeners
    formAhorro.addEventListener("submit", handleFormAhorroSubmit);
    formAporte.addEventListener("submit", handleFormAporteSubmit);

    document.getElementById("btnConfirmarEliminar").addEventListener("click", confirmarEliminarAction);

    document.getElementById("btnRegistrarAporte").addEventListener("click", () => {
        fetch(`${API_BASE}/cuotas/proxima/${currentAhorroId}`, { credentials: "include" })
            .then(res => {
                if (res.ok) return res.json();
                throw new Error("No hay cuota próxima");
            })
            .then(cuota => {
                abrirModalAporte(cuota);
            })
            .catch(() => {
                alert("No hay cuotas pendientes para esta meta.");
            });
    });

    // Buscadores y Filtros
    buscadorPrincipal.addEventListener("input", (e) => filtrarTarjetas(e.target.value, ahorrosGrid));
    buscadorModal.addEventListener("input", (e) => filtrarTarjetas(e.target.value, listaConceptosSeleccion));
    filtroEstado.addEventListener("change", () => cargarAhorros());

    // --- Funciones Principales ---

    function cargarAhorros() {
        mostrarLoading(true);

        const estado = filtroEstado.value;
        let url = API_BASE;
        if (estado) {
            url += `?estado=${estado}`;
        }

        fetch(url, { credentials: "include" })
            .then((res) => {
                if (!res.ok) throw new Error("Error cargando ahorros");
                return res.json();
            })
            .then((ahorros) => {
                renderizarAhorros(ahorros);
                actualizarDashboard(ahorros);
                mostrarLoading(false);
            })
            .catch((err) => {
                console.error("Error:", err);
                mostrarLoading(false);
            });
    }

    function renderizarAhorros(ahorros) {
        if (!ahorrosGrid) return;
        ahorrosGrid.innerHTML = "";

        if (!ahorros || ahorros.length === 0) {
            ahorrosGrid.classList.add("d-none");
            emptyState.classList.remove("d-none");
            return;
        }

        emptyState.classList.add("d-none");
        ahorrosGrid.classList.remove("d-none");

        fetch(`${API_CONCEPTOS}/tipo/AHORRO`, { credentials: "include" })
            .then(res => res.json())
            .then(conceptos => {
                const conceptosMap = {};
                conceptos.forEach(c => conceptosMap[c.id] = c.nombre);

                ahorros.forEach((ahorro) => {
                    const nombreConcepto = conceptosMap[ahorro.conceptoId] || "Meta de Ahorro";
                    const porcentaje = ahorro.montoMeta > 0 ? (ahorro.totalAcumulado / ahorro.montoMeta) * 100 : 0;

                    const card = document.createElement("div");
                    card.className = "col-md-4 col-lg-3";
                    card.innerHTML = `
                    <div class="card h-100 border-0 shadow-sm hover-card" style="cursor: pointer; transition: transform 0.2s;">
                        <div class="card-body">
                            <div class="d-flex align-items-center mb-3">
                                <div class="rounded bg-warning-custom text-white d-flex align-items-center justify-content-center me-3" style="width: 50px; height: 50px;">
                                    <i class="bi bi-piggy-bank fs-4"></i>
                                </div>
                                <div>
                                    <h5 class="card-title fw-bold text-dark mb-0">${nombreConcepto}</h5>
                                    <small class="text-muted">${ahorro.descripcion || "Sin descripción"}</small>
                                </div>
                            </div>
                            
                            <div class="d-flex justify-content-between align-items-end mb-2">
                                <div>
                                    <small class="text-muted d-block">Ahorrado</small>
                                    <h4 class="fw-bold text-warning-custom mb-0">$${formatoMoneda(ahorro.totalAcumulado)}</h4>
                                </div>
                                <div class="text-end">
                                    <small class="text-muted d-block">Meta</small>
                                    <span class="fw-bold text-dark">$${formatoMoneda(ahorro.montoMeta)}</span>
                                </div>
                            </div>
                            
                            <div class="progress" style="height: 8px;">
                                <div class="progress-bar bg-warning" role="progressbar" 
                                     style="width: 0%; transition: width 1s ease-in-out;" 
                                     aria-valuenow="${porcentaje}" aria-valuemin="0" aria-valuemax="100"></div>
                            </div>
                            <div class="text-end mt-1">
                                <small class="text-muted">${porcentaje.toFixed(0)}%</small>
                            </div>
                        </div>
                    </div>
                `;

                    const cardInner = card.querySelector(".card");
                    cardInner.addEventListener("mouseenter", () => (cardInner.style.transform = "translateY(-5px)"));
                    cardInner.addEventListener("mouseleave", () => (cardInner.style.transform = "translateY(0)"));
                    cardInner.addEventListener("click", () => abrirDetalle(ahorro, nombreConcepto));

                    ahorrosGrid.appendChild(card);

                    setTimeout(() => {
                        const bar = card.querySelector(".progress-bar");
                        if (bar) bar.style.width = `${porcentaje}%`;
                    }, 100);
                });
            });
    }

    function abrirDetalle(ahorro, nombreConcepto) {
        currentAhorroId = ahorro.id;
        document.getElementById("modalDetalleTitle").textContent = nombreConcepto;

        const porcentaje = ahorro.montoMeta > 0 ? (ahorro.totalAcumulado / ahorro.montoMeta) * 100 : 0;
        document.getElementById("modalDetalleProgreso").textContent = `${porcentaje.toFixed(1)}% ($${formatoMoneda(ahorro.totalAcumulado)} / $${formatoMoneda(ahorro.montoMeta)})`;
        document.getElementById("modalProgressBar").style.width = `${porcentaje}%`;

        // Load quotas
        const tbody = document.getElementById("tablaCuotasBody");
        tbody.innerHTML = '<tr><td colspan="5" class="text-center"><div class="spinner-border text-warning spinner-border-sm"></div> Cargando...</td></tr>';

        modalDetalle.show();

        fetch(`${API_BASE}/cuotas/${ahorro.id}`, { credentials: "include" })
            .then(res => res.json())
            .then(cuotas => {
                tbody.innerHTML = "";
                if (cuotas.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="5" class="text-center">No hay cuotas registradas.</td></tr>';
                    return;
                }

                cuotas.forEach(cuota => {
                    const tr = document.createElement("tr");
                    const fecha = new Date(cuota.fechaLimite).toLocaleDateString();
                    const estadoClass = cuota.estado === 'APORTADO' ? 'bg-success' : (cuota.estado === 'PENDIENTE' ? 'bg-warning text-dark' : 'bg-danger');
                    const estadoTexto = cuota.estado === 'APORTADO' ? 'Pagado' : (cuota.estado === 'PENDIENTE' ? 'Pendiente' : 'Vencido');

                    let acciones = '';
                    if (cuota.estado !== 'APORTADO') {
                        acciones = `<button class="btn btn-sm btn-success btn-pagar"><i class="bi bi-cash"></i> Pagar</button>`;
                    } else {
                        acciones = `<span class="text-success"><i class="bi bi-check-circle-fill"></i></span>`;
                    }

                    tr.innerHTML = `
                    <td>${fecha}</td>
                    <td>Cuota programada</td>
                    <td class="text-end fw-bold">$${formatoMoneda(cuota.aporteAsignado)}</td>
                    <td class="text-center"><span class="badge ${estadoClass}">${estadoTexto}</span></td>
                    <td class="text-end">${acciones}</td>
                `;

                    const btnPagar = tr.querySelector(".btn-pagar");
                    if (btnPagar) {
                        btnPagar.addEventListener("click", () => {
                            modalDetalle.hide();
                            abrirModalAporte(cuota, nombreConcepto);
                        });
                    }

                    tbody.appendChild(tr);
                });
            });
    }

    function abrirModalAporte(cuota, nombreMeta) {
        document.getElementById("aporteAhorroId").value = currentAhorroId;
        document.getElementById("aporteCuotaId").value = cuota.aporteAhorroId;
        document.getElementById("aporteMetaNombre").value = nombreMeta || "Meta de Ahorro";
        document.getElementById("aporteMontoInput").value = cuota.aporteAsignado;
        document.getElementById("aporteDescripcionInput").value = "";

        modalFormAporte.show();
    }

    function handleFormAporteSubmit(e) {
        e.preventDefault();

        const ahorroId = document.getElementById("aporteAhorroId").value;
        const cuotaId = document.getElementById("aporteCuotaId").value;

        const data = {
            aporte: parseFloat(document.getElementById("aporteMontoInput").value),
            descripcion: document.getElementById("aporteDescripcionInput").value
        };

        fetch(`${API_BASE}/${ahorroId}/cuotas/${cuotaId}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data)
        })
            .then(res => {
                if (!res.ok) throw new Error("Error registrando aporte");
                return res.json();
            })
            .then(() => {
                modalFormAporte.hide();
                cargarAhorros();
            })
            .catch(err => alert("Error al registrar aporte: " + err.message));
    }

    // --- Funciones de Formulario (Crear/Editar) ---

    window.abrirModalCrear = function () {
        formAhorro.reset();
        document.getElementById("ahorroId").value = "";
        document.getElementById("conceptoId").value = "";
        document.getElementById("conceptoInput").value = "";
        document.getElementById("cuotasInput").value = "";
        document.getElementById("fechaLimiteInput").value = "";

        modalFormAhorro.show();
    };

    window.abrirModalSeleccionConcepto = function () {
        listaConceptosSeleccion.innerHTML = '<div class="text-center w-100"><div class="spinner-border text-warning" role="status"></div></div>';
        buscadorModal.value = "";
        modalSeleccionConcepto.show();

        fetch(`${API_CONCEPTOS}/tipo/AHORRO`, { credentials: "include" })
            .then((res) => res.json())
            .then((conceptos) => {
                listaConceptosSeleccion.innerHTML = "";
                conceptos.forEach((c) => {
                    const col = document.createElement("div");
                    col.className = "col";
                    col.innerHTML = `
                    <div class="card h-100 border-0 shadow-sm hover-card concept-card" style="cursor: pointer;">
                        <div class="card-body text-center">
                            <i class="bi bi-piggy-bank fs-1 text-warning-custom mb-2"></i>
                            <h6 class="card-title fw-bold">${c.nombre}</h6>
                            <p class="card-text small text-muted">${c.descripcion || ""}</p>
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
        modalFormAhorro.show();
    }

    function handleFormAhorroSubmit(e) {
        e.preventDefault();

        const id = document.getElementById("ahorroId").value;
        const isEdit = !!id;
        const url = isEdit ? `${API_BASE}/${id}` : API_BASE;
        const method = isEdit ? "PUT" : "POST";

        const cuotasVal = document.getElementById("cuotasInput").value;
        const fechaVal = document.getElementById("fechaLimiteInput").value;

        const data = {
            conceptoId: parseInt(document.getElementById("conceptoId").value),
            montoMeta: parseFloat(document.getElementById("montoObjetivoInput").value),
            frecuencia: document.getElementById("frecuenciaInput").value,
            cantidadCuotas: cuotasVal ? parseInt(cuotasVal) : null,
            fechaMeta: fechaVal ? fechaVal : null,
            descripcion: document.getElementById("descripcionInput").value,
        };

        fetch(url, {
            method: method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data),
        })
            .then((response) => {
                if (!response.ok) {
                    return response.json().then(err => { throw new Error(err.mensaje || err.error || "Error guardando ahorro"); });
                }
                return response.json();
            })
            .then(() => {
                modalFormAhorro.hide();
                cargarAhorros();
                formAhorro.reset();
            })
            .catch((error) => {
                console.error("Error:", error);
                alert(error.message);
            });
    }

    // Event Listeners for new buttons
    document.getElementById("btnEditarAhorro").addEventListener("click", () => {
        modalDetalle.hide();
        abrirModalEditar(currentAhorroId);
    });

    document.getElementById("btnEliminarAhorro").addEventListener("click", () => {
        modalDetalle.hide();
        currentDeleteId = currentAhorroId;
        modalEliminar.show();
    });

    function abrirModalEditar(id) {
        // Find the ahorro object from the grid or fetch it? 
        // We need the full object. We can fetch it or find it in the rendered list if we stored it.
        // Better to fetch or pass it. 
        // Let's fetch it to be safe or use the one we have if we have it.
        // Since we are in detail view, we have `currentAhorroId`.
        // But we need the data. Let's fetch the list again or find it.
        // Optimization: We can store the current ahorro object when opening detail.

        // For now, let's just find it in the DOM or fetch list? 
        // Actually, we can just fetch the single ahorro if there is an endpoint, or filter from the list if we had it.
        // But we don't have the list globally.
        // Let's just fetch the list again and find it? No, inefficient.
        // Let's assume we can get it.
        // The best way is to store `currentAhorro` object when opening detail.

        // Let's modify `abrirDetalle` to store the object.
        // But for now, let's just fetch the list and find it (quick fix) or fetch by ID if endpoint exists.
        // The controller has `actualizar` but maybe not `obtener` by ID?
        // Checking controller... `listar` gets all. `listarCuotas` gets quotas.
        // There is no `obtener` by ID in the snippet I saw.
        // So I have to use the list.

        // WORKAROUND: Fetch all and find.
        fetch(API_BASE, { credentials: "include" })
            .then(res => res.json())
            .then(ahorros => {
                const ahorro = ahorros.find(a => a.id === id);
                if (!ahorro) return;

                formAhorro.reset();
                document.getElementById("ahorroId").value = ahorro.id;
                document.getElementById("conceptoId").value = ahorro.conceptoId;

                // We need concept name.
                fetch(`${API_CONCEPTOS}/tipo/AHORRO`, { credentials: "include" })
                    .then(res => res.json())
                    .then(conceptos => {
                        const concepto = conceptos.find(c => c.id === ahorro.conceptoId);
                        document.getElementById("conceptoInput").value = concepto ? concepto.nombre : "Concepto";
                    });

                document.getElementById("montoObjetivoInput").value = ahorro.montoMeta;
                document.getElementById("frecuenciaInput").value = ahorro.frecuencia;
                document.getElementById("cuotasInput").value = ahorro.cantidadCuotas;
                document.getElementById("fechaLimiteInput").value = ahorro.fechaMeta;
                document.getElementById("descripcionInput").value = ahorro.descripcion;

                document.getElementById("modalFormTitle").textContent = "Editar Meta de Ahorro";
                modalFormAhorro.show();
            });
    }

    function confirmarEliminarAction() {
        if (!currentDeleteId) return;

        fetch(`${API_BASE}/${currentDeleteId}`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' }
        })
            .then(res => {
                if (res.ok) {
                    modalEliminar.hide();
                    cargarAhorros();
                    currentDeleteId = null;
                } else {
                    alert("Error al eliminar");
                }
            })
            .catch(err => console.error(err));
    }

    // --- Utils ---

    function mostrarLoading(show) {
        if (show) {
            if (loadingSpinner) loadingSpinner.classList.remove("d-none");
            if (ahorrosGrid) ahorrosGrid.classList.add("d-none");
            if (emptyState) emptyState.classList.add("d-none");
        } else {
            if (loadingSpinner) loadingSpinner.classList.add("d-none");
        }
    }

    function formatoMoneda(cantidad) {
        return cantidad.toLocaleString("es-ES", {
            minimumFractionDigits: 0,
            maximumFractionDigits: 2,
        });
    }

    function actualizarDashboard(ahorros) {
        const total = ahorros.reduce((sum, a) => sum + a.totalAcumulado, 0);
        const metas = ahorros.length;
        const completadas = ahorros.filter(a => a.estado === 'COMPLETADO' || a.totalAcumulado >= a.montoMeta).length;

        // Proxima meta: la que tenga fecha limite mas cercana y no este completada
        const pendientes = ahorros.filter(a => a.estado !== 'COMPLETADO' && a.totalAcumulado < a.montoMeta);
        let proxima = "-";
        if (pendientes.length > 0) {
            pendientes.sort((a, b) => new Date(a.fechaMeta) - new Date(b.fechaMeta));
            const days = Math.ceil((new Date(pendientes[0].fechaMeta) - new Date()) / (1000 * 60 * 60 * 24));
            proxima = days > 0 ? `${days} días` : "Vencida";
        }

        document.getElementById("totalAhorrado").textContent = `$${formatoMoneda(total)}`;
        document.getElementById("statMetas").textContent = metas;
        document.getElementById("statCompletadas").textContent = completadas;
        document.getElementById("statProximaMeta").textContent = proxima;
    }

    function filtrarTarjetas(texto, contenedor) {
        const termino = texto.toLowerCase();
        const columnas = contenedor.children;

        Array.from(columnas).forEach((col) => {
            if (!col.classList.contains("col")) return;
            const card = col.querySelector(".card");
            if (!card) return;

            const titulo = card.querySelector(".card-title")?.textContent.toLowerCase() || "";
            const desc = card.querySelector(".card-text")?.textContent.toLowerCase() || "";

            if (titulo.includes(termino) || desc.includes(termino)) {
                col.classList.remove("d-none");
            } else {
                col.classList.add("d-none");
            }
        });
    }
    // --- Funciones de Exportación ---

    window.exportarReporte = function (tipo) {
        const estado = filtroEstado.value;
        let url = API_BASE;
        if (estado) {
            url += `?estado=${estado}`;
        }

        fetch(url, { credentials: "include" })
            .then((res) => res.json())
            .then((ahorros) => {
                // Fetch concepts to map names
                fetch(`${API_CONCEPTOS}/tipo/AHORRO`, { credentials: "include" })
                    .then(res => res.json())
                    .then(conceptos => {
                        const conceptosMap = {};
                        conceptos.forEach(c => conceptosMap[c.id] = c.nombre);

                        const reportData = ahorros.map(a => ({
                            concepto: conceptosMap[a.conceptoId] || "Meta de Ahorro",
                            descripcion: a.descripcion || "",
                            meta: a.montoMeta,
                            acumulado: a.totalAcumulado,
                            estado: a.estado
                        }));

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
        doc.text("Reporte de Ahorros", 14, 22);
        doc.setFontSize(11);
        doc.text(`Fecha: ${new Date().toLocaleDateString()}`, 14, 30);

        const tableData = data.map((item) => [
            item.concepto,
            item.descripcion,
            `$${item.meta.toFixed(2)}`,
            `$${item.acumulado.toFixed(2)}`,
            item.estado
        ]);

        doc.autoTable({
            head: [["Concepto", "Descripción", "Meta", "Acumulado", "Estado"]],
            body: tableData,
            startY: 40,
            theme: "grid",
            headStyles: { fillColor: [255, 193, 7] }, // Warning color
        });

        doc.save("reporte_ahorros.pdf");
    }

    function exportarExcel(data) {
        const ws = XLSX.utils.json_to_sheet(
            data.map((item) => ({
                Concepto: item.concepto,
                Descripción: item.descripcion,
                "Monto Meta": item.meta,
                "Total Acumulado": item.acumulado,
                Estado: item.estado
            }))
        );
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, "Ahorros");
        XLSX.writeFile(wb, "reporte_ahorros.xlsx");
    }

    function exportarCSV(data) {
        const ws = XLSX.utils.json_to_sheet(
            data.map((item) => ({
                Concepto: item.concepto,
                Descripción: item.descripcion,
                "Monto Meta": item.meta,
                "Total Acumulado": item.acumulado,
                Estado: item.estado
            }))
        );
        const csv = XLSX.utils.sheet_to_csv(ws);
        const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
        const link = document.createElement("a");
        const url = URL.createObjectURL(blob);
        link.setAttribute("href", url);
        link.setAttribute("download", "reporte_ahorros.csv");
        link.style.visibility = "hidden";
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }
});
