document.addEventListener("DOMContentLoaded", () => {
    const cardsContainer = document.getElementById("cardsPresupuestos");
    const buscarInput = document.getElementById("buscarPresupuestos");
    const limpiarBusqueda = document.getElementById("limpiarBusquedaPres");
    const btnCrear = document.getElementById("btnCrearPresupuesto");

    // Modal selects/buttons for concept selection
    const btnAbrirConceptoCrear = document.getElementById("btnAbrirConceptoCrear");
    const btnAbrirConceptoEditar = document.getElementById("btnAbrirConceptoEditar");
    const abrirConceptoModalEl = document.getElementById("abrirConceptoModal");
    const listaConceptosEl = document.getElementById("listaConceptos");
    const buscarConcepto = document.getElementById("buscarConcepto");

    let presupuestosCache = []; // cache para búsquedas
    let conceptoSeleccionTarget = null; // para saber si la selección es para crear o editar

    cargarPresupuestos();
    cargarConceptosEnSelects(); // llena selects inicialmente

    // ----------------------
    // Cargar presupuestos desde API
    // ----------------------
    function cargarPresupuestos() {
        fetch("/api/presupuestos")
            .then(res => {
                if (!res.ok) throw new Error('Error al cargar presupuestos');
                return res.json();
            })
            .then(data => {
                presupuestosCache = data || [];
                renderPresupuestos(presupuestosCache);
            })
            .catch(err => {
                console.error("Error cargando presupuestos:", err);
                cardsContainer.innerHTML = `<div class="col-12 text-center text-danger">Error al cargar datos</div>`;
            });
    }

    // ----------------------
    // Render tarjetas (responsive: col-12 col-md-6 col-lg-4)
    // ----------------------
    function renderPresupuestos(list) {
        cardsContainer.innerHTML = "";
        if (!list || list.length === 0) {
            cardsContainer.innerHTML = `<div class="col-12 text-center text-muted py-4">No hay presupuestos registrados</div>`;
            return;
        }

        list.forEach(p => {
            const col = document.createElement("div");
            col.className = "col-12 col-md-6 col-lg-4";

            const card = document.createElement("div");
            card.className = "card card-presupuesto h-100";

            const cardBody = document.createElement("div");
            cardBody.className = "card-body d-flex flex-column";

            const titulo = document.createElement("h5");
            titulo.className = "card-title";
            titulo.innerHTML = `<i class="bi bi-tags me-2 text-primary"></i> ${p.conceptoNombre || 'Sin concepto'}`;

            const monto = document.createElement("p");
            monto.className = "card-text fs-5 fw-bold text-primary";
            monto.textContent = `$${parseFloat(p.limite).toFixed(2)}`;

            const fechas = document.createElement("p");
            fechas.className = "card-text text-muted small mb-2";
            const start = p.fechaInicio ? p.fechaInicio : '--';
            const end = p.fechaFin ? p.fechaFin : '--';
            fechas.textContent = `Desde: ${start} · Hasta: ${end}`;

            const badge = document.createElement("div");
            badge.innerHTML = `<span class="badge ${p.activo ? 'bg-success' : 'bg-secondary'}">${p.activo ? 'Activo' : 'Inactivo'}</span>`;

            // acciones
            const accionDiv = document.createElement("div");
            accionDiv.className = "mt-auto d-flex gap-2";

            const btnEdit = document.createElement("button");
            btnEdit.className = "btn btn-sm btn-outline-warning btnEditar";
            btnEdit.dataset.id = p.id;
            btnEdit.innerHTML = `<i class="bi bi-pencil"></i> Editar`;

            const btnDelete = document.createElement("button");
            btnDelete.className = "btn btn-sm btn-outline-danger btnEliminar";
            btnDelete.dataset.id = p.id;
            btnDelete.innerHTML = `<i class="bi bi-trash"></i> Eliminar`;

            accionDiv.appendChild(btnEdit);
            accionDiv.appendChild(btnDelete);

            cardBody.appendChild(titulo);
            cardBody.appendChild(monto);
            cardBody.appendChild(fechas);
            cardBody.appendChild(badge);
            cardBody.appendChild(accionDiv);

            card.appendChild(cardBody);
            col.appendChild(card);
            cardsContainer.appendChild(col);
        });

        // delegar eventos
        document.querySelectorAll(".btnEditar").forEach(btn => {
            btn.addEventListener("click", () => abrirModalEditar(btn.dataset.id));
        });
        document.querySelectorAll(".btnEliminar").forEach(btn => {
            btn.addEventListener("click", () => eliminarPresupuesto(btn.dataset.id));
        });
    }

    // ----------------------
    // Búsqueda local (por nombre de concepto)
    // ----------------------
    buscarInput.addEventListener("input", () => {
        const q = buscarInput.value.trim().toLowerCase();
        if (!q) return renderPresupuestos(presupuestosCache);
        const filtrados = presupuestosCache.filter(p => (p.conceptoNombre || '').toLowerCase().includes(q));
        renderPresupuestos(filtrados);
    });
    limpiarBusqueda.addEventListener("click", () => {
        buscarInput.value = "";
        renderPresupuestos(presupuestosCache);
    });

    // ----------------------
    // Abrir modal Crear
    // ----------------------
    btnCrear.addEventListener("click", () => {
        const form = document.querySelector("#formCrearPresupuesto");
        form.reset();
        document.getElementById("fechaInicio").valueAsDate = new Date();
        // set default activo
        document.getElementById("activo").checked = true;

        const modal = new bootstrap.Modal(document.getElementById("crearModal"));
        modal.show();
    });

    // ----------------------
    // Crear Presupuesto (igual que antes)
    // ----------------------
    document.querySelector("#formCrearPresupuesto").addEventListener("submit", e => {
        e.preventDefault();
        const form = e.target;

        const dto = {
            limite: parseFloat(form.limite.value),
            activo: form.activo.checked,
            conceptoId: parseInt(form.conceptoId.value),
            fechaInicio: form.fechaInicio.value || null,
            fechaFin: form.fechaFin.value || null
        };

        fetch("/api/presupuestos", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(dto)
        })
            .then(res => {
                if (!res.ok) throw new Error('Error al crear presupuesto');
                return res.json();
            })
            .then(() => {
                cargarPresupuestos();
                bootstrap.Modal.getInstance(document.getElementById("crearModal")).hide();
                alert('Presupuesto creado exitosamente');
            })
            .catch(err => {
                console.error("Error creando presupuesto:", err);
                alert('Error al crear el presupuesto');
            });
    });

    // ----------------------
    // Abrir modal Editar (igual que antes)
    // ----------------------
    function abrirModalEditar(id) {
        fetch(`/api/presupuestos/${id}`)
            .then(res => {
                if (!res.ok) throw new Error('Error al cargar presupuesto');
                return res.json();
            })
            .then(p => {
                const form = document.querySelector("#formEditarPresupuesto");
                form.id.value = p.id;
                document.getElementById("editLimite").value = p.limite;
                document.getElementById("editActivo").checked = p.activo;
                document.getElementById("editConceptoId").value = p.conceptoId || "";
                document.getElementById("editFechaInicio").value = p.fechaInicio || "";
                document.getElementById("editFechaFin").value = p.fechaFin || "";

                const modal = new bootstrap.Modal(document.getElementById("editarModal"));
                modal.show();
            })
            .catch(err => {
                console.error("Error cargando presupuesto:", err);
                alert('Error al cargar el presupuesto');
            });
    }

    // ----------------------
    // Actualizar Presupuesto (igual)
    // ----------------------
    document.querySelector("#formEditarPresupuesto").addEventListener("submit", e => {
        e.preventDefault();
        const form = e.target;
        const id = form.id.value;

        const dto = {
            limite: parseFloat(document.getElementById("editLimite").value),
            activo: document.getElementById("editActivo").checked,
            conceptoId: parseInt(document.getElementById("editConceptoId").value),
            fechaInicio: document.getElementById("editFechaInicio").value || null,
            fechaFin: document.getElementById("editFechaFin").value || null
        };

        fetch(`/api/presupuestos/${id}`, {
            method: "PUT",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(dto)
        })
            .then(res => {
                if (!res.ok) throw new Error('Error al actualizar presupuesto');
                return res.json();
            })
            .then(() => {
                cargarPresupuestos();
                bootstrap.Modal.getInstance(document.getElementById("editarModal")).hide();
                alert('Presupuesto actualizado exitosamente');
            })
            .catch(err => {
                console.error("Error actualizando presupuesto:", err);
                alert('Error al actualizar el presupuesto');
            });
    });

    // ----------------------
    // Eliminar Presupuesto (igual)
    // ----------------------
    function eliminarPresupuesto(id) {
        if (!confirm("¿Está seguro de eliminar este presupuesto?")) return;

        fetch(`/api/presupuestos/${id}`, { method: "DELETE" })
            .then(res => {
                if (!res.ok) throw new Error('Error al eliminar presupuesto');
                cargarPresupuestos();
                alert('Presupuesto eliminado exitosamente');
            })
            .catch(err => {
                console.error("Error eliminando presupuesto:", err);
                alert('Error al eliminar el presupuesto');
            });
    }

    // ----------------------
    // Cargar conceptos en los selects (crear/editar)
    // ----------------------
    function cargarConceptosEnSelects() {
        fetch("/api/presupuestos/conceptos")
            .then(res => {
                if (!res.ok) throw new Error('Error al cargar conceptos');
                return res.json();
            })
            .then(list => {
                // llenar selects en crear y editar (si existen)
                const selCrear = document.getElementById("conceptoId");
                const selEditar = document.getElementById("editConceptoId");
                if (selCrear) {
                    // eliminar todo menos el first
                    selCrear.querySelectorAll("option:not([value=''])").forEach(o => o.remove());
                    list.forEach(c => {
                        const opt = document.createElement("option");
                        opt.value = c.id;
                        opt.textContent = c.nombre;
                        selCrear.appendChild(opt);
                    });
                }
                if (selEditar) {
                    selEditar.querySelectorAll("option:not([value=''])").forEach(o => o.remove());
                    list.forEach(c => {
                        const opt = document.createElement("option");
                        opt.value = c.id;
                        opt.textContent = c.nombre;
                        selEditar.appendChild(opt);
                    });
                }

                // llenar lista de abrirConcepto modal
                renderListaConceptos(list);
            })
            .catch(err => {
                console.error("Error cargando conceptos:", err);
            });
    }

    // ----------------------
    // Abrir modal de conceptos y selección
    // ----------------------
    function renderListaConceptos(list) {
    listaConceptosEl.innerHTML = "";

    if (!list || list.length === 0) {
        listaConceptosEl.innerHTML = `<div class="text-center text-muted p-3">No hay conceptos</div>`;
        return;
    }

    list.forEach(c => {
        // columna responsive
        const col = document.createElement("div");
        col.className = "col-12 col-md-6";

        // tarjeta estilo moderno
        const card = document.createElement("div");
        card.className = "p-3 concept-card d-flex align-items-center";
        card.style.cursor = "pointer";
        card.dataset.id = c.id;
        card.dataset.nombre = c.nombre;

        // contenido
        card.innerHTML = `
            <i class="bi bi-tag-fill concept-icon me-3"></i>
            <div>
                <h6 class="mb-1">${c.nombre}</h6>
                <span class="text-muted small">ID #${c.id}</span>
            </div>
        `;

        // selección al hacer clic
        card.addEventListener("click", () => {
            if (conceptoSeleccionTarget === 'crear') {
                const sel = document.getElementById("conceptoId");
                if (sel) sel.value = c.id;
            } else if (conceptoSeleccionTarget === 'editar') {
                const sel = document.getElementById("editConceptoId");
                if (sel) sel.value = c.id;
            }
            bootstrap.Modal.getInstance(abrirConceptoModalEl).hide();
        });

        col.appendChild(card);
        listaConceptosEl.appendChild(col);
    });
}


    // abrir modal cuando se presiona el icono de buscar concepto
    if (btnAbrirConceptoCrear) {
        btnAbrirConceptoCrear.addEventListener("click", () => {
            conceptoSeleccionTarget = 'crear';
            const modal = new bootstrap.Modal(abrirConceptoModalEl);
            modal.show();
        });
    }
    if (btnAbrirConceptoEditar) {
        btnAbrirConceptoEditar.addEventListener("click", () => {
            conceptoSeleccionTarget = 'editar';
            const modal = new bootstrap.Modal(abrirConceptoModalEl);
            modal.show();
        });
    }

    // buscar concepto dentro del modal (client-side)
    if (buscarConcepto) {
        buscarConcepto.addEventListener("input", () => {
            const q = buscarConcepto.value.trim().toLowerCase();
           const items = listaConceptosEl.querySelectorAll(".concept-card");

            items.forEach(it => {
                const nombre = it.dataset.nombre.toLowerCase();
                it.style.display = nombre.includes(q) ? "" : "none";
            });
        });
    }

    // ----------------------
    // Opcional: control para crear programacion (solo UI)
    // ----------------------
    const btnCrearProgramacion = document.getElementById("btnCrearProgramacion");
    if (btnCrearProgramacion) {
        btnCrearProgramacion.addEventListener("click", () => {
            alert("Funcionalidad de Programaciones aún no implementada en el backend. UI lista.");
        });
    }

});
