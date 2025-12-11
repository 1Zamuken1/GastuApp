document.addEventListener("DOMContentLoaded", () => {
    // Variables globales y elementos DOM
    const cardsContainer = document.getElementById("cardsPresupuestos");
    const buscarInput = document.getElementById("buscarPresupuestos");
    const limpiarBusqueda = document.getElementById("limpiarBusquedaPres");
    const btnCrear = document.getElementById("btnCrearPresupuesto");
    const btnAbrirConceptoCrear = document.getElementById("btnAbrirConceptoCrear");
    const btnAbrirConceptoEditar = document.getElementById("btnAbrirConceptoEditar");
    const abrirConceptoModalEl = document.getElementById("abrirConceptoModal");
    const listaConceptosEl = document.getElementById("listaConceptos");
    const buscarConcepto = document.getElementById("buscarConcepto");

    let presupuestosCache = [];
    let conceptoSeleccionTarget = null;

    // Inicialización
    cargarPresupuestos();
    cargarConceptosEnSelects();
    inicializarEventListeners();

    // Inicializa todos los event listeners de la aplicación
    function inicializarEventListeners() {
        buscarInput.addEventListener("input", handleBuscarPresupuestos);
        limpiarBusqueda.addEventListener("click", handleLimpiarBusqueda);
        btnCrear.addEventListener("click", handleAbrirModalCrear);
        document.querySelector("#formCrearPresupuesto").addEventListener("submit", handleCrearPresupuesto);
        document.querySelector("#formEditarPresupuesto").addEventListener("submit", handleActualizarPresupuesto);

        if (btnAbrirConceptoCrear) {
            btnAbrirConceptoCrear.addEventListener("click", () => {
                conceptoSeleccionTarget = 'crear';
                new bootstrap.Modal(abrirConceptoModalEl).show();
            });
        }

        if (btnAbrirConceptoEditar) {
            btnAbrirConceptoEditar.addEventListener("click", () => {
                conceptoSeleccionTarget = 'editar';
                new bootstrap.Modal(abrirConceptoModalEl).show();
            });
        }

        if (buscarConcepto) {
            buscarConcepto.addEventListener("input", handleBuscarConcepto);
        }
    }

    // Realiza fetch de todos los presupuestos con progreso desde el API
    function cargarPresupuestos() {
        fetch("/api/presupuestos/progreso")
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

    // Renderiza la lista de presupuestos como tarjetas Bootstrap
    function renderPresupuestos(list) {
        console.log("Presupuestos recibidos:", list);

        cardsContainer.innerHTML = "";
        if (!list || list.length === 0) {
            cardsContainer.innerHTML =
                `<div class="col-12 text-center text-muted py-4">No hay presupuestos registrados</div>`;
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
            fechas.textContent = `Desde: ${p.fechaInicio || '--'} · Hasta: ${p.fechaFin || '--'}`;

            const badge = document.createElement("div");
            badge.innerHTML =
                `<span class="badge ${p.activo ? 'bg-success' : 'bg-secondary'}">${p.activo ? 'Activo' : 'Inactivo'}</span>`;

            const gastado = parseFloat(p.gastado || 0);
            const limite = parseFloat(p.limite || 1);
            const porcentaje = Math.min((gastado / limite) * 100, 100).toFixed(2);

            const textoPorcentaje = document.createElement("p");
            textoPorcentaje.className = "text-muted small mb-1";
            textoPorcentaje.textContent = `${porcentaje}% del límite`;

            const progressContainer = document.createElement("div");
            progressContainer.className = "progress mb-3";
            progressContainer.style.height = "7px";

            const progressBar = document.createElement("div");
            progressBar.className = "progress-bar";
            progressBar.style.width = `${porcentaje}%`;

            if (porcentaje < 70) progressBar.classList.add("bg-success");
            else if (porcentaje < 100) progressBar.classList.add("bg-warning");
            else progressBar.classList.add("bg-danger");

            progressContainer.appendChild(progressBar);

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

            if (!p.activo) {
                const btnActivar = document.createElement("button");
                btnActivar.className = "btn btn-sm btn-success btnActivar";
                btnActivar.dataset.id = p.id;
                btnActivar.innerHTML = `<i class="bi bi-check-circle"></i> Activar`;
                accionDiv.appendChild(btnActivar);
            }
            accionDiv.appendChild(btnEdit);
            accionDiv.appendChild(btnDelete);

            cardBody.appendChild(titulo);
            cardBody.appendChild(monto);
            cardBody.appendChild(fechas);
            cardBody.appendChild(badge);
            cardBody.appendChild(textoPorcentaje);
            cardBody.appendChild(progressContainer);
            cardBody.appendChild(accionDiv);

            card.appendChild(cardBody);
            col.appendChild(card);
            cardsContainer.appendChild(col);
        });

        document.querySelectorAll(".btnEditar").forEach(btn => {
            btn.addEventListener("click", () => abrirModalEditar(btn.dataset.id));
        });

        document.querySelectorAll(".btnEliminar").forEach(btn => {
            btn.addEventListener("click", () => eliminarPresupuesto(btn.dataset.id));
        });

        document.querySelectorAll(".btnActivar").forEach(btn => {
            btn.addEventListener("click", () => activarPresupuesto(btn.dataset.id));
        });
    }

    // Filtra presupuestos localmente según término de búsqueda
    function handleBuscarPresupuestos() {
        const q = buscarInput.value.trim().toLowerCase();
        if (!q) return renderPresupuestos(presupuestosCache);
        const filtrados = presupuestosCache.filter(p => (p.conceptoNombre || '').toLowerCase().includes(q));
        renderPresupuestos(filtrados);
    }

    // Limpia el campo de búsqueda y muestra todos los presupuestos
    function handleLimpiarBusqueda() {
        buscarInput.value = "";
        renderPresupuestos(presupuestosCache);
    }

    // Abre el modal de creación con valores por defecto
    function handleAbrirModalCrear() {
        const form = document.querySelector("#formCrearPresupuesto");
        form.reset();
        document.getElementById("fechaInicio").valueAsDate = new Date();
        document.getElementById("activo").checked = true;

        const modal = new bootstrap.Modal(document.getElementById("crearModal"));
        modal.show();
    }

    // Procesa el formulario y envía POST al API para crear presupuesto
    function handleCrearPresupuesto(e) {
        e.preventDefault();
        const form = e.target;

        const dto = {
            limite: parseFloat(form.limite.value),
            activo: form.activo.checked,
            conceptoId: parseInt(form.conceptoId.value),
            fechaInicio: form.fechaInicio.value || null,
            fechaFin: form.fechaFin.value || null
        };

        console.log("Enviando DTO:", dto);

        fetch("/api/presupuestos", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(dto)
        })
        .then(async res => {
            const text = await res.text();
            console.log("Respuesta raw:", text);
            
            let body;
            try {
                body = text ? JSON.parse(text) : {};
            } catch (e) {
                body = { Mensaje: text || "Error desconocido del servidor" };
            }
            
            if (!res.ok) {
                procesarErroresValidacion(body);
                throw new Error("Validación");
            }
            return body;
        })
        .then(() => {
            cargarPresupuestos();
            bootstrap.Modal.getInstance(document.getElementById("crearModal")).hide();
            alertaSuccess("Presupuesto creado exitosamente");
        })
        .catch(err => {
            if (err.message !== "Validación") {
                console.error("Error al crear:", err);
                alertaError("Error al crear el presupuesto");
            }
        });
    }

    // Carga datos del presupuesto por ID y abre modal de edición
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
                alertaError('Error al cargar el presupuesto');
            });
    }

    // Procesa el formulario y envía PUT al API para actualizar presupuesto
    function handleActualizarPresupuesto(e) {
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

        console.log("Actualizando DTO:", dto);

        fetch(`/api/presupuestos/${id}`, {
            method: "PUT",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(dto)
        })
        .then(async res => {
            const text = await res.text();
            console.log("Respuesta raw:", text);
            
            let body;
            try {
                body = text ? JSON.parse(text) : {};
            } catch (e) {
                body = { Mensaje: text || "Error desconocido del servidor" };
            }
            
            if (!res.ok) {
                procesarErroresValidacion(body);
                throw new Error("Validación");
            }
            return body;
        })
        .then(() => {
            cargarPresupuestos();
            bootstrap.Modal.getInstance(document.getElementById("editarModal")).hide();
            alertaSuccess("Presupuesto actualizado exitosamente");
        })
        .catch(err => {
            if (err.message !== "Validación") {
                console.error("Error al actualizar:", err);
                alertaError("Error al actualizar el presupuesto");
            }
        });
    }

    // Solicita confirmación y envía DELETE al API para eliminar presupuesto
    function eliminarPresupuesto(id) {
        alertaConfirm("¿Está seguro de eliminar este presupuesto?")
            .then(result => {
                if (!result.isConfirmed) return;

                fetch(`/api/presupuestos/${id}`, { method: "DELETE" })
                    .then(res => {
                        if (!res.ok) throw new Error('Error al eliminar presupuesto');
                        cargarPresupuestos();
                        alertaSuccess('Presupuesto eliminado exitosamente');
                    })
                    .catch(err => {
                        console.error("Error eliminando presupuesto:", err);
                        alertaError('Error al eliminar el presupuesto');
                    });
            });
    }

    // Solicita confirmación y envía PATCH al API para activar presupuesto
    function activarPresupuesto(id) {
        Swal.fire({
            title: "¿Activar presupuesto?",
            text: "Este presupuesto pasará a estar activo.",
            icon: "question",
            showCancelButton: true,
            confirmButtonText: "Sí, activar",
            cancelButtonText: "Cancelar"
        }).then(result => {
            if (!result.isConfirmed) return;

            fetch(`/api/presupuestos/activar/${id}`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                    "usuarioId": localStorage.getItem("usuarioId")
                }
            })
            .then(async res => {
                if (!res.ok) {
                    const rawMessage = await res.text();
                    let mensaje = "Ya existe un presupuesto activo con el mismo concepto.";

                    if (res.status === 409) {
                        mensaje = "Ya existe un presupuesto activo con el mismo concepto.";
                    }

                    throw new Error(mensaje);
                }

                return res.json();
            })
            .then(() => {
                Swal.fire({
                    title: "Activado",
                    text: "El presupuesto fue activado correctamente.",
                    icon: "success",
                    timer: 1500,
                    showConfirmButton: false
                });

                cargarPresupuestos();
            })
            .catch(err => {
                Swal.fire({
                    title: "Error al activar",
                    text: err.message,
                    icon: "error"
                });
            });
        });
    }

    // Carga conceptos desde API y los renderiza en los selects de formularios
    function cargarConceptosEnSelects() {
        fetch("/api/presupuestos/conceptos")
            .then(res => {
                if (!res.ok) throw new Error('Error al cargar conceptos');
                return res.json();
            })
            .then(list => {
                const selCrear = document.getElementById("conceptoId");
                const selEditar = document.getElementById("editConceptoId");
                if (selCrear) {
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
                renderListaConceptos(list);
            })
            .catch(err => {
                console.error("Error cargando conceptos:", err);
            });
    }

    // Renderiza la lista de conceptos como tarjetas seleccionables en modal
    function renderListaConceptos(list) {
        listaConceptosEl.innerHTML = "";
        if (!list || list.length === 0) {
            listaConceptosEl.innerHTML = `<div class="text-center text-muted p-3">No hay conceptos</div>`;
            return;
        }

        list.forEach(c => {
            const col = document.createElement("div");
            col.className = "col-12 col-md-6 col-lg-4";

            const card = document.createElement("div");
            card.className = "card border-0 shadow-sm p-3 mb-3 rounded-4 concepto-card hover-shadow";
            card.style.cursor = "pointer";
            card.dataset.id = c.id;
            card.dataset.nombre = c.nombre;

            card.innerHTML = `
                <div class="d-flex align-items-center gap-3">
                    <div class="bg-primary bg-opacity-10 p-3 rounded-4 d-flex align-items-center justify-content-center">
                        <i class="bi bi-tag-fill text-primary fs-3"></i>
                    </div>
                    <div class="flex-grow-1">
                        <h6 class="mb-1 fw-semibold text-dark">${c.nombre}</h6>
                        <small class="text-muted">Seleccionar</small>
                    </div>
                    <i class="bi bi-chevron-right text-muted fs-5"></i>
                </div>
            `;

            card.addEventListener("click", () => {
                if (conceptoSeleccionTarget === "crear") document.getElementById("conceptoId").value = c.id;
                else if (conceptoSeleccionTarget === "editar") document.getElementById("editConceptoId").value = c.id;
                bootstrap.Modal.getInstance(abrirConceptoModalEl).hide();
            });

            col.appendChild(card);
            listaConceptosEl.appendChild(col);
        });
    }

    // Filtra conceptos en modal según término de búsqueda normalizado
    function handleBuscarConcepto() {
        const qRaw = buscarConcepto.value.trim().toLowerCase();
        const q = qRaw.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
        const items = listaConceptosEl.querySelectorAll('[data-nombre]');
        let anyVisible = false;

        items.forEach(item => {
            const col = item.closest('.col-12') || item.parentElement;
            const nombreRaw = (item.dataset.nombre || '').toLowerCase();
            const nombre = nombreRaw.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
            const visible = q === '' || nombre.includes(q);
            if (col) col.style.display = visible ? '' : 'none';
            else item.style.display = visible ? '' : 'none';
            if (visible) anyVisible = true;
        });

        const noResultId = 'conceptosNoResult';
        let noResultEl = document.getElementById(noResultId);
        if (!anyVisible) {
            if (!noResultEl) {
                noResultEl = document.createElement('div');
                noResultEl.id = noResultId;
                noResultEl.className = 'text-center text-muted p-3';
                noResultEl.innerText = 'No se encontraron conceptos';
                listaConceptosEl.appendChild(noResultEl);
            }
        } else {
            if (noResultEl) noResultEl.remove();
        }
    }

    // Procesa respuesta de error del backend y extrae mensajes de validación
    function procesarErroresValidacion(body) {
        console.log("=== PROCESANDO ERRORES ===");
        console.log("Body completo:", JSON.stringify(body, null, 2));
        
        const mensajesPersonalizados = {
            conceptoId: "Es obligatorio seleccionar un concepto.",
            limite: "El valor del límite excede la cantidad permitida (máximo 12 cifras).",
            fechaInicio: "La fecha de inicio no puede ser posterior a la fecha actual.",
            fechaFin: "La fecha de finalización no puede ser anterior a la fecha actual.",
            activo: "El estado indicado no es válido."
        };
        
        let erroresProcesados = {};
        const camposTecnicos = ['status', 'timestamp', 'path', 'trace', 'error', 'message'];
        
        if (body && typeof body === 'object') {
            if (body.errores && typeof body.errores === 'object') {
                console.log("✓ Detectado formato: errores (español)");
                
                for (const campo in body.errores) {
                    const mensaje = body.errores[campo];
                    console.log(`  Campo: ${campo} → Mensaje: ${mensaje}`);
                    
                    if (mensajesPersonalizados[campo]) {
                        erroresProcesados[campo] = mensajesPersonalizados[campo];
                        console.log(`    ✓ Usando mensaje personalizado`);
                    } else {
                        erroresProcesados[campo] = mensaje;
                        console.log(`    ⚠ Usando mensaje del backend`);
                    }
                }
            }
            else if (body.errors && typeof body.errors === 'object') {
                console.log("✓ Detectado formato: errors (inglés)");
                
                for (const campo in body.errors) {
                    const mensajes = body.errors[campo];
                    const listaMensajes = Array.isArray(mensajes) ? mensajes : [mensajes];
                    
                    if (mensajesPersonalizados[campo]) {
                        erroresProcesados[campo] = mensajesPersonalizados[campo];
                    } else {
                        erroresProcesados[campo] = listaMensajes.join(', ');
                    }
                }
            }
            else {
                console.log("✓ Buscando campos de validación directos...");
                
                for (const key in body) {
                    const valor = body[key];
                    
                    if (camposTecnicos.includes(key.toLowerCase())) {
                        console.log(`  ✗ Ignorando campo técnico: ${key}`);
                        continue;
                    }
                    
                    if (typeof valor === 'string' && valor.trim() !== '') {
                        console.log(`  ✓ Campo encontrado: ${key} → ${valor}`);
                        
                        if (mensajesPersonalizados[key]) {
                            erroresProcesados[key] = mensajesPersonalizados[key];
                        } else {
                            erroresProcesados[key] = valor;
                        }
                    }
                    else if (Array.isArray(valor) && valor.length > 0) {
                        console.log(`  ✓ Campo con array: ${key}`);
                        if (mensajesPersonalizados[key]) {
                            erroresProcesados[key] = mensajesPersonalizados[key];
                        } else {
                            erroresProcesados[key] = valor.join(', ');
                        }
                    }
                }
            }
        }
        else if (typeof body === 'string' && body.trim() !== '') {
            console.log("✓ Body es string plano");
            erroresProcesados['Mensaje'] = body;
        }
        
        if (Object.keys(erroresProcesados).length === 0) {
            console.log("⚠ No se encontraron errores específicos, usando mensaje genérico");
            
            let mensajeGenerico = 'No se pudo completar la operación. Verifique los datos ingresados.';
            if (body && body.error) mensajeGenerico = body.error;
            else if (body && body.message) mensajeGenerico = body.message;
            
            erroresProcesados['Mensaje'] = mensajeGenerico;
        }
        
        console.log("=== ERRORES FINALES A MOSTRAR ===");
        console.log(erroresProcesados);
        
        alertaValidaciones(erroresProcesados);
    }

    // Muestra errores de validación formateados en SweetAlert2
    function alertaValidaciones(errors) {
        console.log("=== MOSTRANDO ERRORES AL USUARIO ===");
        console.log("Errores recibidos:", errors);
        
        let html = "<ul class='text-start' style='list-style: none; padding-left: 0; margin: 0;'>";
        let contador = 0;
        
        const nombresLegibles = {
            conceptoId: 'Concepto',
            limite: 'Límite',
            fechaInicio: 'Fecha de Inicio',
            fechaFin: 'Fecha Final',
            activo: 'Estado',
            Mensaje: ''
        };
        
        for (const campo in errors) {
            const mensajes = errors[campo];
            const nombreCampo = nombresLegibles[campo] || campo;
            const listaMensajes = Array.isArray(mensajes) ? mensajes : [mensajes];
            
            listaMensajes.forEach(mensaje => {
                if (mensaje && typeof mensaje === 'string' && mensaje.trim() !== '') {
                    if (nombreCampo === '') {
                        html += `<li class='mb-2 py-1'>
                            <i class='bi bi-exclamation-circle text-warning me-2'></i>
                            ${mensaje}
                        </li>`;
                    } else {
                        html += `<li class='mb-2 py-1'>
                            <i class='bi bi-exclamation-circle text-warning me-2'></i>
                            <strong>${nombreCampo}:</strong> ${mensaje}
                        </li>`;
                    }
                    contador++;
                }
            });
        }
        
        html += "</ul>";
        
        console.log(`Total de errores mostrados: ${contador}`);

        Swal.fire({
            icon: 'warning',
            title: contador === 1 ? 'Error de validación' : 'Errores de validación',
            html: html,
            confirmButtonColor: '#d33',
            confirmButtonText: 'Entendido',
            width: '600px',
            customClass: {
                popup: 'animated fadeIn'
            }
        });
    }

    // Muestra mensaje de éxito con SweetAlert2
    function alertaSuccess(text) {
        Swal.fire({ 
            icon: 'success', 
            title: 'Éxito', 
            text, 
            confirmButtonColor: '#3085d6' 
        });
    }

    // Muestra mensaje de error con SweetAlert2
    function alertaError(text) {
        Swal.fire({ 
            icon: 'error', 
            title: 'Error', 
            text, 
            confirmButtonColor: '#d33' 
        });
    }

    // Muestra diálogo de confirmación con SweetAlert2
    function alertaConfirm(text) {
        return Swal.fire({
            icon: 'warning',
            title: 'Confirmación',
            text,
            showCancelButton: true,
            confirmButtonText: 'Sí, continuar',
            cancelButtonText: 'Cancelar',
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33'
        });
    }
});