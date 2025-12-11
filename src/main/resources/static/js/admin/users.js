document.addEventListener('DOMContentLoaded', function () {
  const tbody = document.querySelector('#usersTable tbody');

  function fetchUsers() {
    fetch('/api/admin/users')
      .then((r) => r.json())
      .then(renderUsers)
      .catch((e) => console.error(e));
  }

  // Fetch available roles for the role select
  let availableRoles = [];
  function fetchRoles() {
    fetch('/api/admin/users/roles')
      .then((r) => r.json())
      .then((roles) => {
        availableRoles = roles || [];
        populateRoleSelect();
      })
      .catch((e) => console.error('Error fetching roles', e));
  }

  function renderUsers(users) {
    tbody.innerHTML = '';
    users.forEach((u) => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td style="display:none" class="user-id">${u.id}</td>
        <td>${escapeHtml(u.username || '')}</td>
        <td>${escapeHtml(u.email || '')}</td>
        <td>${escapeHtml(u.telefono || '')}</td>
        <td>${escapeHtml(u.rol || '')}</td>
        <td>
          <button class="btn btn-sm btn-warning btn-edit">Editar</button>
          <button class="btn btn-sm btn-danger btn-delete">Eliminar</button>
        </td>
      `;
      tbody.appendChild(tr);
    });
    attachHandlers();
  }

  function attachHandlers() {
    // view button removed — no-op

    document.querySelectorAll('.btn-edit').forEach((btn) => {
      btn.addEventListener('click', (e) => {
        const tr = e.target.closest('tr');
        const id = tr.querySelector('.user-id').textContent;
        fetch(`/api/admin/users/${id}`).then((r) => r.json()).then((u) => {
          document.getElementById('editId').value = u.id;
          document.getElementById('editUsername').value = u.username || '';
          document.getElementById('editEmail').value = u.email || '';
          document.getElementById('editTelefono').value = u.telefono || '';
          document.getElementById('editPassword').value = '';
          document.getElementById('editActivo').checked = !!u.activo;
            // select the role if present, otherwise default to first available role
            const rolSelect = document.getElementById('editRol');
            if (u.rol) {
              rolSelect.value = u.rol;
            } else if (availableRoles && availableRoles.length > 0) {
              rolSelect.value = availableRoles[0];
            } else {
              rolSelect.value = '';
            }
          new bootstrap.Modal(document.getElementById('editModal')).show();
        });
      });
    });

    document.querySelectorAll('.btn-delete').forEach((btn) => {
      btn.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
        const tr = e.target.closest('tr');
        const id = tr.querySelector('.user-id').textContent.trim();
        if (!confirm('¿Eliminar usuario? Esta acción no se puede deshacer.')) return;
        fetch(`/api/admin/users/${id}`, { method: 'DELETE' })
          .then((r) => {
            if (r.ok) {
              fetchUsers();
            } else {
              alert('Error al eliminar usuario. Intenta nuevamente.');
            }
          })
          .catch((e) => {
            alert('Error de conexión al eliminar: ' + e.message);
          });
      });
    });
  }

  document.getElementById('saveEditBtn').addEventListener('click', function () {
    const id = document.getElementById('editId').value;
    const payload = {
      username: document.getElementById('editUsername').value,
      email: document.getElementById('editEmail').value,
      telefono: document.getElementById('editTelefono').value,
      rol: document.getElementById('editRol').value,
      activo: document.getElementById('editActivo').checked,
    };
    const pwd = document.getElementById('editPassword').value;
    if (pwd && pwd.trim() !== '') payload.password = pwd;
    fetch(`/api/admin/users/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    }).then((r) => {
      if (r.ok) {
        const modalEl = document.getElementById('editModal');
        const modalInstance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
        modalInstance.hide();
        fetchUsers();
      } else {
        alert('Error al guardar');
      }
    });
  });

  function escapeHtml(text) {
    return text
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }

  function populateRoleSelect() {
    const sel = document.getElementById('editRol');
    if (!sel) return;
    // clear existing options
    sel.innerHTML = '';
    availableRoles.forEach((r) => {
      const opt = document.createElement('option');
      opt.value = r;
      opt.textContent = r;
      sel.appendChild(opt);
    });
    // if no current selection, default to first role
    if (!sel.value && availableRoles.length > 0) sel.value = availableRoles[0];
  }

  fetchUsers();
  fetchRoles();
  // Create user modal handlers
  const newUserBtn = document.getElementById('newUserBtn');
  if (newUserBtn) {
    newUserBtn.addEventListener('click', () => {
      // reset form
      document.getElementById('createUsername').value = '';
      document.getElementById('createEmail').value = '';
      document.getElementById('createTelefono').value = '';
      document.getElementById('createPassword').value = '';
      document.getElementById('createActivo').checked = true;
      // populate roles if available
      const createSel = document.getElementById('createRol');
      if (createSel) {
        createSel.innerHTML = '';
        availableRoles.forEach((r) => {
          const opt = document.createElement('option');
          opt.value = r;
          opt.textContent = r;
          createSel.appendChild(opt);
        });
        if (availableRoles.length > 0) createSel.value = availableRoles[0];
      }
      const modalEl = document.getElementById('createModal');
      const modalInstance = new bootstrap.Modal(modalEl);
      modalInstance.show();
    });
  }

  const saveCreateBtn = document.getElementById('saveCreateBtn');
  if (saveCreateBtn) {
    saveCreateBtn.addEventListener('click', () => {
      const username = document.getElementById('createUsername').value.trim();
      const email = document.getElementById('createEmail').value.trim();
      const password = document.getElementById('createPassword').value;
      const telefono = document.getElementById('createTelefono').value.trim();
      const activo = document.getElementById('createActivo').checked;
      const rol = document.getElementById('createRol').value;

      // Función para validar formato email
      function isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
      }

      // Validaciones cliente
      if (!username) {
        alert('Por favor ingresa un nombre de usuario.');
        return;
      }
      if (!email) {
        alert('Por favor ingresa un correo válido.');
        return;
      }
      if (!isValidEmail(email)) {
        alert('Por favor ingresa un correo con formato válido (ejemplo: usuario@dominio.com).');
        return;
      }
      if (!password) {
        alert('Por favor ingresa una contraseña.');
        return;
      }
      if (password.length < 6) {
        alert('La contraseña debe tener al menos 6 caracteres.');
        return;
      }

      const payload = {
        username: username,
        email: email,
        telefono: telefono,
        password: password,
        activo: activo,
        rol: rol,
      };

      fetch('/api/admin/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
        .then((r) => {
          if (r.ok || r.status === 201) {
            const modalEl = document.getElementById('createModal');
            const modalInstance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
            modalInstance.hide();
            fetchUsers();
            // Reset form
            document.getElementById('createForm').reset();
          } else {
            return r.json().then((errorData) => {
              const errorMsg = errorData.error || 'Error desconocido al crear el usuario.';
              alert('Error: ' + errorMsg);
            }).catch(() => {
              alert('Error al crear el usuario. Intenta nuevamente.');
            });
          }
        })
        .catch((e) => {
          alert('Error de conexión: ' + e.message);
        });
    });
  }
});
