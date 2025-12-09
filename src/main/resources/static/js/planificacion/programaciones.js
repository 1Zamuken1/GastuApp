// ============================================
// VARIABLES GLOBALES
// ============================================
let programaciones = [];
let modoActual = 'crear';

// ============================================
// INICIALIZACIÓN
// ============================================
$(document).ready(function() {
    console.log('✅ JS Cargado correctamente');
    establecerFechaMinima();
    cargarProgramaciones();
    inicializarEventos();
});

// ============================================
// EVENTOS
// ============================================
function inicializarEventos() {
    console.log('📌 Inicializando eventos...');
    
    // Botones principales
    $('#btnNuevo').off('click').on('click', function() {
        console.log('🔵 Clic en Nuevo');
        abrirModalCrear();
    });
    
    $('#btnVerPendientes').off('click').on('click', function() {
        console.log('🔵 Clic en Ver Pendientes');
        mostrarPendientes();
    });
    
    $('#btnLimpiarBuscador').off('click').on('click', function() {
        console.log('🔵 Clic en Limpiar');
        limpiarFiltros();
    });
    
    // Búsqueda y filtros
    $('#buscadorProgramacion').off('input').on('input', aplicarFiltros);
    $('#filtroTipo').off('change').on('change', aplicarFiltros);
    $('#filtroEstado').off('change').on('change', aplicarFiltros);
    
    // Modal Crear
    $('#btnSeleccionarConceptoCrear').off('click').on('click', function() {
        console.log('🔵 Abrir conceptos (crear)');
        abrirModalConceptos('crear');
    });
    
    $('#btnGuardarCrear').off('click').on('click', function() {
        console.log('🔵 Guardar crear');
        guardarCrear();
    });
    
    // Modal Editar
    $('#btnSeleccionarConceptoEditar').off('click').on('click', function() {
        console.log('🔵 Abrir conceptos (editar)');
        abrirModalConceptos('editar');
    });
    
    $('#btnGuardarEditar').off('click').on('click', function() {
        console.log('🔵 Guardar editar');
        guardarEditar();
    });
    
    // Delegación para botones dinámicos (MUY IMPORTANTE)
    $(document).off('click', '.btn-activar').on('click', '.btn-activar', function(e) {
        e.preventDefault();
        e.stopPropagation();
        const id = $(this).data('id');
        console.log('🟢 Activar:', id);
        confirmarActivar(id);
    });
    
    $(document).off('click', '.btn-ejecutar').on('click', '.btn-ejecutar', function(e) {
        e.preventDefault();
        e.stopPropagation();
        const id = $(this).data('id');
        console.log('🟢 Ejecutar:', id);
        confirmarEjecutar(id);
    });
    
    $(document).off('click', '.btn-posponer').on('click', '.btn-posponer', function(e) {
        e.preventDefault();
        e.stopPropagation();
        const id = $(this).data('id');
        console.log('🟢 Posponer:', id);
        confirmarPosponer(id);
    });
    
    $(document).off('click', '.btn-editar').on('click', '.btn-editar', function(e) {
        e.preventDefault();
        e.stopPropagation();
        const id = $(this).data('id');
        console.log('🟡 Editar:', id);
        abrirModalEditar(id);
    });
    
    $(document).off('click', '.btn-eliminar').on('click', '.btn-eliminar', function(e) {
        e.preventDefault();
        e.stopPropagation();
        const id = $(this).data('id');
        console.log('🔴 Eliminar:', id);
        confirmarEliminar(id);
    });
    
    // Selección de conceptos
    $(document).off('click', '.concepto-card').on('click', '.concepto-card', function(e) {
        e.preventDefault();
        const data = $(this).data();
        console.log('🏷️ Concepto seleccionado:', data);
        seleccionarConcepto(data.id, data.nombre, data.tipo);
    });
    
    console.log('✅ Eventos inicializados');
}

// ============================================
// CARGA DE DATOS
// ============================================
function cargarProgramaciones() {
    console.log('📡 Cargando programaciones...');
    
    $.ajax({
        url: '/api/programaciones',
        method: 'GET',
        dataType: 'json',
        success: function(data) {
            console.log('✅ Programaciones cargadas:', data);
            programaciones = data || [];
            renderizarProgramaciones(programaciones);
            actualizarEstadisticas();
            actualizarBadgePendientes();
        },
        error: function(xhr, status, error) {
            console.error('❌ Error cargando programaciones:', error);
            Swal.fire('Error', 'No se pudieron cargar las programaciones', 'error');
        }
    });
}

function cargarConceptos(tipo, contenedor, empty) {
    console.log('📡 Cargando conceptos tipo:', tipo);
    
    $.ajax({
        url: '/api/conceptos/tipo/' + tipo,
        method: 'GET',
        dataType: 'json',
        success: function(data) {
            console.log('✅ Conceptos cargados:', data);
            
            if (!data || data.length === 0) {
                $(contenedor).empty();
                $(empty).removeClass('d-none');
                return;
            }
            
            $(empty).addClass('d-none');
            const html = data.map(c => crearCardConcepto(c)).join('');
            $(contenedor).html(html);
        },
        error: function(xhr, status, error) {
            console.error('❌ Error cargando conceptos:', error);
            Swal.fire('Error', 'No se pudieron cargar los conceptos', 'error');
        }
    });
}

// ============================================
// RENDERIZADO
// ============================================
function renderizarProgramaciones(lista) {
    console.log('🎨 Renderizando', lista.length, 'programaciones');
    
    const contenedor = $('#contenedorProgramaciones');
    const vacio = $('#estadoVacio');
    
    if (!lista || lista.length === 0) {
        contenedor.empty();
        vacio.removeClass('d-none');
        return;
    }
    
    vacio.addClass('d-none');
    const html = lista.map(p => crearCardProgramacion(p)).join('');
    contenedor.html(html);
}

function crearCardProgramacion(p) {
    const tipo = p.tipo || '';
    const esIngreso = tipo === 'INGRESO';
    const colorBorde = esIngreso ? 'success' : 'danger';
    const icono = esIngreso ? 'arrow-up-circle' : 'arrow-down-circle';
    const opacidad = p.activo ? '' : 'opacity-50';
    const badge = p.activo 
        ? '<span class="badge bg-success"><i class="bi bi-check-circle"></i> Activa</span>'
        : '<span class="badge bg-secondary"><i class="bi bi-pause-circle"></i> Inactiva</span>';
    
    const monto = formatearMonto(p.montoProgramado);
    const proxima = p.proximaEjecucion ? formatearFecha(p.proximaEjecucion) : 'Sin definir';
    const descripcion = p.descripcion || '<em class="text-muted">Sin descripción</em>';
    
    const botones = p.activo ? `
        <button class="btn btn-sm btn-outline-success btn-ejecutar" data-id="${p.id}">
            <i class="bi bi-check-lg"></i> Ejecutar
        </button>
        <button class="btn btn-sm btn-outline-secondary btn-posponer" data-id="${p.id}">
            <i class="bi bi-skip-forward"></i> Posponer
        </button>
    ` : `
        <button class="btn btn-sm btn-success btn-activar" data-id="${p.id}">
            <i class="bi bi-play-circle"></i> Activar
        </button>
    `;
    
    return `
        <div class="col-lg-4 col-md-6">
            <div class="card h-100 shadow-sm border-start border-4 border-${colorBorde} ${opacidad}">
                <div class="card-body">
                    <div class="d-flex justify-content-between mb-3">
                        <div>
                            <h5 class="card-title mb-1">
                                <i class="bi bi-${icono} text-${colorBorde}"></i>
                                ${p.conceptoNombre || 'Sin concepto'}
                            </h5>
                            <span class="badge bg-info">${p.frecuencia || ''}</span>
                        </div>
                        ${badge}
                    </div>
                    <div class="mb-3">
                        <h3 class="mb-0 text-${colorBorde}">${monto}</h3>
                        <small class="text-muted">${tipo}</small>
                    </div>
                    <p class="card-text text-muted mb-2" style="min-height:40px">${descripcion}</p>
                    <div class="mb-3">
                        <small class="text-muted">
                            <i class="bi bi-calendar-event"></i>
                            Próxima: <strong>${proxima}</strong>
                        </small>
                    </div>
                    <div class="d-flex gap-2 flex-wrap">
                        ${botones}
                        <button class="btn btn-sm btn-outline-warning btn-editar" data-id="${p.id}">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger btn-eliminar" data-id="${p.id}">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
}

function crearCardConcepto(c) {
    const esIngreso = c.tipo === 'INGRESO';
    const color = esIngreso ? 'success' : 'danger';
    
    return `
        <div class="col-md-6">
            <div class="card h-100 border shadow-sm concepto-card" 
                 style="cursor:pointer"
                 data-id="${c.id}"
                 data-nombre="${c.nombre}"
                 data-tipo="${c.tipo}">
                <div class="card-body">
                    <h6 class="card-title mb-1">
                        <i class="bi bi-tag text-${color}"></i>
                        ${c.nombre}
                    </h6>
                    <span class="badge bg-${color}">${c.tipo}</span>
                </div>
            </div>
        </div>
    `;
}

// ============================================
// ESTADÍSTICAS Y PENDIENTES
// ============================================
function actualizarEstadisticas() {
    const total = programaciones.length;
    const activas = programaciones.filter(p => p.activo).length;
    const ingresos = programaciones
        .filter(p => p.tipo === 'INGRESO' && p.activo)
        .reduce((sum, p) => sum + (p.montoProgramado || 0), 0);
    const egresos = programaciones
        .filter(p => p.tipo === 'EGRESO' && p.activo)
        .reduce((sum, p) => sum + (p.montoProgramado || 0), 0);
    
    $('#totalProgramaciones').text(total);
    $('#totalActivas').text(activas);
    $('#totalIngresos').text(formatearMonto(ingresos));
    $('#totalEgresos').text(formatearMonto(egresos));
}

function actualizarBadgePendientes() {
    const hoy = new Date().toISOString().split('T')[0];
    const pendientes = programaciones.filter(p => 
        p.activo && p.proximaEjecucion && p.proximaEjecucion <= hoy
    ).length;
    $('#badgePendientes').text(pendientes);
}

function mostrarPendientes() {
    console.log('👁️ Mostrando pendientes');
    const hoy = new Date().toISOString().split('T')[0];
    const pendientes = programaciones.filter(p => 
        p.activo && p.proximaEjecucion && p.proximaEjecucion <= hoy
    );
    
    if (pendientes.length === 0) {
        Swal.fire({
            icon: 'info',
            title: 'Sin pendientes',
            text: 'No hay programaciones pendientes de ejecución',
            timer: 2000
        });
        return;
    }
    
    renderizarProgramaciones(pendientes);
}

// ============================================
// FILTROS
// ============================================
function aplicarFiltros() {
    const busqueda = $('#buscadorProgramacion').val().toLowerCase();
    const tipo = $('#filtroTipo').val();
    const estado = $('#filtroEstado').val();
    
    console.log('🔍 Filtros:', { busqueda, tipo, estado });
    
    let resultado = programaciones.filter(p => {
        const matchTipo = !tipo || p.tipo === tipo;
        const matchEstado = estado === '' || p.activo === (estado === 'true');
        const matchBusqueda = !busqueda || 
            (p.descripcion || '').toLowerCase().includes(busqueda) ||
            (p.conceptoNombre || '').toLowerCase().includes(busqueda) ||
            String(p.montoProgramado || '').includes(busqueda) ||
            (p.frecuencia || '').toLowerCase().includes(busqueda);
        
        return matchTipo && matchEstado && matchBusqueda;
    });
    
    renderizarProgramaciones(resultado);
}

function limpiarFiltros() {
    $('#buscadorProgramacion').val('');
    $('#filtroTipo').val('');
    $('#filtroEstado').val('');
    renderizarProgramaciones(programaciones);
}

// ============================================
// MODALES
// ============================================
function abrirModalCrear() {
    console.log('📝 Abriendo modal crear');
    modoActual = 'crear';
    $('#formCrearProgramacion')[0].reset();
    $('#conceptoIdCrear, #tipoCrear, #conceptoNombreCrear').val('');
    $('#formCrearProgramacion .is-invalid').removeClass('is-invalid');
    $('#modalCrear').modal('show');
}

function abrirModalEditar(id) {
    console.log('✏️ Abriendo modal editar, ID:', id);
    const p = programaciones.find(prog => prog.id === Number(id));
    
    if (!p) {
        Swal.fire('Error', 'Programación no encontrada', 'error');
        return;
    }
    
    modoActual = 'editar';
    $('#editarId').val(p.id);
    $('#conceptoIdEditar').val(p.conceptoId);
    $('#tipoEditar').val(p.tipo);
    $('#conceptoNombreEditar').val(p.conceptoNombre || '');
    $('#editarMonto').val(p.montoProgramado);
    $('#editarFrecuencia').val(p.frecuencia);
    $('#editarFechaInicio').val(p.fechaInicio);
    $('#editarProximaEjecucion').val(p.proximaEjecucion);
    $('#editarDescripcion').val(p.descripcion || '');
    $('#formEditarProgramacion .is-invalid').removeClass('is-invalid');
    $('#modalEditar').modal('show');
}

function abrirModalConceptos(modo) {
    console.log('🏷️ Abriendo modal conceptos, modo:', modo);
    modoActual = modo;
    cargarConceptos('INGRESO', '#contenedorConceptosIngresos', '#emptyIngresos');
    cargarConceptos('EGRESO', '#contenedorConceptosEgresos', '#emptyEgresos');
    $('#modalConceptos').modal('show');
}

function seleccionarConcepto(id, nombre, tipo) {
    console.log('✅ Concepto seleccionado:', { id, nombre, tipo });
    
    if (modoActual === 'crear') {
        $('#conceptoIdCrear').val(id);
        $('#conceptoNombreCrear').val(nombre);
        $('#tipoCrear').val(tipo);
    } else {
        $('#conceptoIdEditar').val(id);
        $('#conceptoNombreEditar').val(nombre);
        $('#tipoEditar').val(tipo);
    }
    
    $('#modalConceptos').modal('hide');
    
    Swal.fire({
        icon: 'success',
        title: 'Concepto seleccionado',
        text: `${nombre} - ${tipo}`,
        timer: 1500,
        showConfirmButton: false
    });
}

// ============================================
// CRUD - CREAR Y EDITAR
// ============================================
function guardarCrear() {
    console.log('💾 Guardando nueva programación');
    
    if (!validarFormulario('#formCrearProgramacion')) return;
    
    const conceptoId = $('#conceptoIdCrear').val();
    if (!conceptoId) {
        Swal.fire('Atención', 'Selecciona un concepto', 'warning');
        return;
    }
    
    const data = {
        conceptoId: Number(conceptoId),
        montoProgramado: Number($('#crearMonto').val()),
        frecuencia: $('#crearFrecuencia').val(),
        fechaInicio: $('#crearFechaInicio').val(),
        descripcion: $('#crearDescripcion').val() || null,
        activo: true
    };
    
    console.log('📤 Datos a enviar:', data);
    $('#btnGuardarCrear').prop('disabled', true);
    
    $.ajax({
        url: '/api/programaciones',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function(response) {
            console.log('✅ Creado exitosamente:', response);
            $('#modalCrear').modal('hide');
            Swal.fire('¡Éxito!', 'Programación creada correctamente', 'success');
            cargarProgramaciones();
        },
        error: function(xhr, status, error) {
            console.error('❌ Error al crear:', error);
            Swal.fire('Error', 'No se pudo crear la programación', 'error');
        },
        complete: function() {
            $('#btnGuardarCrear').prop('disabled', false);
        }
    });
}

function guardarEditar() {
    console.log('💾 Guardando edición');
    
    if (!validarFormulario('#formEditarProgramacion')) return;
    
    const id = $('#editarId').val();
    const data = {
        id: Number(id),
        conceptoId: Number($('#conceptoIdEditar').val()),
        tipo: $('#tipoEditar').val(),
        montoProgramado: Number($('#editarMonto').val()),
        frecuencia: $('#editarFrecuencia').val(),
        fechaInicio: $('#editarFechaInicio').val(),
        proximaEjecucion: $('#editarProximaEjecucion').val(),
        descripcion: $('#editarDescripcion').val() || null,
        activo: true
    };
    
    console.log('📤 Datos a enviar:', data);
    $('#btnGuardarEditar').prop('disabled', true);
    
    $.ajax({
        url: `/api/programaciones/${id}`,
        method: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function(response) {
            console.log('✅ Actualizado exitosamente:', response);
            $('#modalEditar').modal('hide');
            Swal.fire('¡Éxito!', 'Programación actualizada correctamente', 'success');
            cargarProgramaciones();
        },
        error: function(xhr, status, error) {
            console.error('❌ Error al actualizar:', error);
            Swal.fire('Error', 'No se pudo actualizar la programación', 'error');
        },
        complete: function() {
            $('#btnGuardarEditar').prop('disabled', false);
        }
    });
}

// ============================================
// ACCIONES
// ============================================
function confirmarActivar(id) {
    Swal.fire({
        title: '¿Cambiar estado?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Sí',
        cancelButtonText: 'Cancelar'
    }).then(result => {
        if (result.isConfirmed) activar(id);
    });
}

function activar(id) {
    console.log('🟢 Activando/Desactivando:', id);
    
    $.ajax({
        url: `/api/programaciones/${id}/estado`,
        method: 'PATCH',
        success: function() {
            console.log('✅ Estado cambiado');
            Swal.fire('¡Listo!', 'Estado actualizado correctamente', 'success');
            cargarProgramaciones();
        },
        error: function(xhr, status, error) {
            console.error('❌ Error:', error);
            Swal.fire('Error', 'No se pudo cambiar el estado', 'error');
        }
    });
}

function confirmarEjecutar(id) {
    Swal.fire({
        title: '¿Ejecutar programación?',
        text: 'Se creará el movimiento correspondiente',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Sí, ejecutar',
        cancelButtonText: 'Cancelar'
    }).then(result => {
        if (result.isConfirmed) ejecutar(id);
    });
}

function ejecutar(id) {
    console.log('▶️ Ejecutando:', id);
    
    $.ajax({
        url: `/api/programaciones/${id}/aceptar`,
        method: 'POST',
        success: function() {
            console.log('✅ Ejecutado');
            Swal.fire('¡Ejecutado!', 'Movimiento creado correctamente', 'success');
            cargarProgramaciones();
        },
        error: function(xhr, status, error) {
            console.error('❌ Error:', error);
            Swal.fire('Error', 'No se pudo ejecutar la programación', 'error');
        }
    });
}

function confirmarPosponer(id) {
    Swal.fire({
        title: '¿Posponer ejecución?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Sí, posponer',
        cancelButtonText: 'Cancelar'
    }).then(result => {
        if (result.isConfirmed) posponer(id);
    });
}

function posponer(id) {
    console.log('⏭️ Posponiendo:', id);
    
    $.ajax({
        url: `/api/programaciones/${id}/rechazar`,
        method: 'POST',
        success: function() {
            console.log('✅ Pospuesto');
            Swal.fire('¡Pospuesto!', 'Próxima ejecución actualizada', 'success');
            cargarProgramaciones();
        },
        error: function(xhr, status, error) {
            console.error('❌ Error:', error);
            Swal.fire('Error', 'No se pudo posponer la ejecución', 'error');
        }
    });
}

function confirmarEliminar(id) {
    Swal.fire({
        title: '¿Estás seguro?',
        text: 'Esta acción no se puede deshacer',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'Cancelar',
        confirmButtonColor: '#d33'
    }).then(result => {
        if (result.isConfirmed) eliminar(id);
    });
}

function eliminar(id) {
    console.log('🗑️ Eliminando:', id);
    
    $.ajax({
        url: `/api/programaciones/${id}`,
        method: 'DELETE',
        success: function() {
            console.log('✅ Eliminado');
            Swal.fire('¡Eliminado!', 'Programación eliminada correctamente', 'success');
            cargarProgramaciones();
        },
        error: function(xhr, status, error) {
            console.error('❌ Error:', error);
            Swal.fire('Error', 'No se pudo eliminar la programación', 'error');
        }
    });
}

// ============================================
// UTILIDADES
// ============================================
function validarFormulario(selector) {
    let valido = true;
    $(selector).find('[required]').each(function() {
        const val = $(this).val();
        if (!val || String(val).trim() === '') {
            $(this).addClass('is-invalid');
            valido = false;
        } else {
            $(this).removeClass('is-invalid');
        }
    });
    
    if (!valido) {
        Swal.fire('Atención', 'Completa todos los campos requeridos', 'warning');
    }
    
    return valido;
}

function formatearMonto(valor) {
    const n = Number(valor) || 0;
    return new Intl.NumberFormat('es-CO', {
        style: 'currency',
        currency: 'COP',
        minimumFractionDigits: 0
    }).format(n);
}

function formatearFecha(fecha) {
    if (!fecha) return 'Sin definir';
    const d = new Date(fecha + 'T00:00:00');
    if (isNaN(d)) return fecha;
    return d.toLocaleDateString('es-CO', { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
    });
}

function establecerFechaMinima() {
    const hoy = new Date().toISOString().split('T')[0];
    $('#crearFechaInicio, #editarFechaInicio, #editarProximaEjecucion').attr('min', hoy);
}