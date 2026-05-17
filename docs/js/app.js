// --- Configuración ---

const API_BASE = 'https://todo-rest-latest.onrender.com';

// --- Estado de la aplicación ---

const state = {
  auth: null,            // { username, password }
  role: 'USER',          // 'USER' | 'GESTOR' | 'ADMIN'
  tasks: [],
  categories: [],
  tags: [],
  users: [],
  editingTaskId: null,
  editingCategoryId: null,
  editingTagId: null,
  editingUserId: null,
  pendingDeleteId: null,
  pendingDeleteType: null,  // 'task' | 'category' | 'tag' | 'user'
  autoRefreshInterval: null,
  currentSection: 'tasks'
};

// --- Autenticación ---

function saveAuth(username, password) {
  state.auth = { username, password };
  localStorage.setItem('todo_auth', JSON.stringify(state.auth));
}

function loadAuth() {
  const stored = localStorage.getItem('todo_auth');
  if (stored) state.auth = JSON.parse(stored);
}

function clearAuth() {
  state.auth = null;
  localStorage.removeItem('todo_auth');
}

function getAuthHeader() {
  if (!state.auth) return {};
  const token = btoa(`${state.auth.username}:${state.auth.password}`);
  return { Authorization: `Basic ${token}` };
}

// --- Peticiones a la API ---

async function request(method, path, body) {
  const options = {
    method,
    headers: { 'Content-Type': 'application/json', ...getAuthHeader() }
  };
  if (body !== undefined) options.body = JSON.stringify(body);
  const response = await fetch(`${API_BASE}${path}`, options);
  if (response.status === 204) return null;
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    const msg = data?.message || data?.error || `Error ${response.status}`;
    throw new Error(msg);
  }
  return data;
}

const api = {
  get:    path        => request('GET',    path),
  post:   (path, b)  => request('POST',   path, b),
  put:    (path, b)  => request('PUT',    path, b),
  delete: path        => request('DELETE', path)
};

// --- Detección de rol ---

async function detectRole() {
  try {
    await api.get('/admin/users');
    return 'ADMIN';
  } catch {
    try {
      await api.get('/manager/categories');
      return 'GESTOR';
    } catch {
      return 'USER';
    }
  }
}

function getCategoryBase() {
  if (state.role === 'ADMIN')   return '/admin/categories';
  if (state.role === 'GESTOR')  return '/manager/categories';
  return '/categories';
}

// --- Navegación por secciones ---

function showSection(name) {
  const sections = ['tasks', 'dashboard', 'tags', 'profile', 'categories', 'users'];
  sections.forEach(s => {
    document.getElementById(`section-${s}`).classList.toggle('d-none', s !== name);
  });
  document.querySelectorAll('#main-tabs .nav-link').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.section === name);
  });
  state.currentSection = name;

  if (name === 'dashboard')   loadDashboard();
  if (name === 'tags')        loadTags();
  if (name === 'categories')  loadAdminCategories();
  if (name === 'users')       loadUsers();
}

function applyRoleUI() {
  const badge = document.getElementById('nav-role-badge');
  if (state.role === 'ADMIN') {
    badge.textContent = 'ADMIN';
    badge.className = 'badge bg-danger';
    document.getElementById('tab-categories-item').classList.remove('d-none');
    document.getElementById('tab-users-item').classList.remove('d-none');
  } else if (state.role === 'GESTOR') {
    badge.textContent = 'GESTOR';
    badge.className = 'badge bg-warning text-dark';
    document.getElementById('tab-categories-item').classList.remove('d-none');
    document.getElementById('tab-users-item').classList.add('d-none');
  } else {
    badge.textContent = 'USER';
    badge.className = 'badge bg-secondary';
    document.getElementById('tab-categories-item').classList.add('d-none');
    document.getElementById('tab-users-item').classList.add('d-none');
  }
}

// --- Tareas ---

async function fetchTasks()       { return api.get('/task'); }
async function createTask(data)   { return api.post('/task', data); }
async function updateTask(id, d)  { return api.put(`/task/${id}`, d); }
async function deleteTask(id)     { return api.delete(`/task/${id}`); }
async function fetchCategories()  { return api.get('/categories'); }

async function searchTasks(params) {
  const q = new URLSearchParams();
  if (params.title)      q.set('title',      params.title);
  if (params.priority)   q.set('priority',   params.priority);
  if (params.category)   q.set('category',   params.category);
  if (params.completed !== undefined && params.completed !== '') q.set('completed', params.completed);
  if (params.importante) q.set('importante', 'true');
  const qs = q.toString();
  return api.get(qs ? `/task/search?${qs}` : '/task');
}

function priorityBadgeClass(p) { return `badge-${p.toLowerCase()}`; }

function formatDeadline(iso) {
  if (!iso) return { formatted: '—', overdue: false };
  const date = new Date(iso);
  return {
    formatted: date.toLocaleDateString('es-ES', { day:'2-digit', month:'2-digit', year:'numeric', hour:'2-digit', minute:'2-digit' }),
    overdue: date < new Date()
  };
}

function toInputDatetime(iso) { return iso ? iso.substring(0, 16) : ''; }
function toApiDatetime(val)   { return val ? `${val}:00` : null; }

function renderTaskCard(task) {
  const { formatted, overdue } = formatDeadline(task.deadline);
  const deadlineHtml = task.deadline
    ? `<span class="${overdue ? 'deadline-overdue' : 'text-muted'}">
         <i class="fa-regular fa-clock me-1"></i>${formatted}${overdue ? ' (vencida)' : ''}
       </span>`
    : '<span class="text-muted">Sin fecha límite</span>';

  const catName  = task.category ? task.category.title : 'Sin categoría';
  const tagsHtml = task.tags.length > 0
    ? task.tags.map(t => `<span class="badge bg-secondary me-1">${t.name}</span>`).join('')
    : '<span class="text-muted small">Sin etiquetas</span>';

  return `
    <div class="card task-card mb-3 priority-${task.priority.toLowerCase()} ${task.completed ? 'completed-task' : ''}" data-id="${task.id}">
      <div class="card-body py-3">
        <div class="d-flex justify-content-between align-items-start gap-2 mb-1">
          <div class="d-flex align-items-start gap-2 flex-grow-1">
            <div class="form-check mt-1 mb-0">
              <input class="form-check-input task-check" type="checkbox"
                     ${task.completed ? 'checked' : ''}
                     onchange="handleToggleComplete(${task.id}, ${task.completed})" />
            </div>
            <span class="task-title ${task.completed ? 'completed' : ''}">${escapeHtml(task.title)}</span>
          </div>
          <div class="d-flex gap-1 flex-shrink-0">
            <span class="badge ${priorityBadgeClass(task.priority)}">${task.priority}</span>
            ${task.importante ? '<span class="badge bg-warning text-dark"><i class="fa-solid fa-star me-1"></i>Imp.</span>' : ''}
          </div>
        </div>
        ${task.description ? `<p class="mb-2 text-muted small ms-4">${escapeHtml(task.description)}</p>` : ''}
        <div class="d-flex flex-wrap gap-3 align-items-center ms-4 mb-2 small">
          <span><i class="fa-solid fa-folder-open me-1 text-primary"></i>${escapeHtml(catName)}</span>
          <span>${deadlineHtml}</span>
        </div>
        <div class="ms-4 mb-2 small">
          <i class="fa-solid fa-tags me-1 text-secondary"></i>${tagsHtml}
        </div>
        <div class="d-flex gap-2 ms-4">
          <button class="btn btn-outline-primary btn-sm" onclick="openEditModal(${task.id})">
            <i class="fa-solid fa-pen me-1"></i>Editar
          </button>
          <button class="btn btn-outline-danger btn-sm" onclick="handleDeleteTask(${task.id})">
            <i class="fa-solid fa-trash me-1"></i>Eliminar
          </button>
        </div>
      </div>
    </div>`;
}

function renderTaskList(tasks) {
  const container = document.getElementById('task-list');
  document.getElementById('task-count').textContent = tasks.length;
  if (tasks.length === 0) {
    container.innerHTML = `
      <div class="text-center text-muted py-5">
        <i class="fa-regular fa-face-smile fa-3x mb-3 d-block"></i>
        <p>No hay tareas. ¡Crea la primera!</p>
      </div>`;
    return;
  }
  container.innerHTML = tasks.map(renderTaskCard).join('');
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.appendChild(document.createTextNode(text || ''));
  return div.innerHTML;
}

// --- Dashboard ---

async function loadDashboard() {
  const el = document.getElementById('dashboard-cards');
  el.innerHTML = `<div class="col-12 text-center py-4"><div class="spinner-border text-primary" role="status"></div></div>`;
  try {
    const d = await api.get('/dashboard');
    const cards = [
      { label: 'Total',       value: d.totalTareas,        icon: 'fa-list',          color: 'primary' },
      { label: 'Completadas', value: d.tareasCompletadas,  icon: 'fa-circle-check',  color: 'success' },
      { label: 'Pendientes',  value: d.tareasPendientes,   icon: 'fa-clock',         color: 'warning' },
      { label: 'Vencidas',    value: d.tareasVencidas,     icon: 'fa-circle-xmark',  color: 'danger'  },
      { label: 'Importantes', value: d.tareasImportantes,  icon: 'fa-star',          color: 'info'    },
    ];
    el.innerHTML = cards.map(c => `
      <div class="col-6 col-md-4 col-lg-2">
        <div class="card text-center shadow-sm h-100">
          <div class="card-body py-4">
            <i class="fa-solid ${c.icon} fa-2x text-${c.color} mb-2"></i>
            <div class="fs-2 fw-bold text-${c.color}">${c.value ?? 0}</div>
            <div class="small text-muted">${c.label}</div>
          </div>
        </div>
      </div>`).join('');
  } catch (err) {
    el.innerHTML = `<div class="col-12"><div class="alert alert-danger">Error al cargar el dashboard: ${err.message}</div></div>`;
  }
}

// --- Etiquetas ---

let tagModal = null;

async function loadTags() {
  const el = document.getElementById('tags-container');
  el.innerHTML = `<div class="text-center py-4"><div class="spinner-border text-primary" role="status"></div></div>`;
  try {
    state.tags = await api.get('/tag');
    renderTags();
  } catch (err) {
    el.innerHTML = `<div class="alert alert-danger">Error: ${err.message}</div>`;
  }
}

function renderTags() {
  const el = document.getElementById('tags-container');
  if (state.tags.length === 0) {
    el.innerHTML = `<div class="text-center text-muted py-4"><i class="fa-solid fa-tags fa-2x mb-2 d-block"></i>No tienes etiquetas todavía.</div>`;
    return;
  }
  el.innerHTML = `
    <div class="table-responsive">
      <table class="table table-hover align-middle">
        <thead class="table-light">
          <tr><th>#</th><th>Nombre</th><th class="text-end">Acciones</th></tr>
        </thead>
        <tbody>
          ${state.tags.map(t => `
            <tr>
              <td class="text-muted small">${t.id}</td>
              <td><span class="badge bg-secondary fs-6">${escapeHtml(t.name)}</span></td>
              <td class="text-end">
                <button class="btn btn-outline-primary btn-sm me-1" onclick="openTagModal(${t.id})">
                  <i class="fa-solid fa-pen"></i>
                </button>
                <button class="btn btn-outline-danger btn-sm" onclick="handleDeleteTag(${t.id})">
                  <i class="fa-solid fa-trash"></i>
                </button>
              </td>
            </tr>`).join('')}
        </tbody>
      </table>
    </div>`;
}

function openTagModal(id) {
  state.editingTagId = id || null;
  const label = document.getElementById('tag-modal-label');
  label.textContent = id ? 'Editar etiqueta' : 'Nueva etiqueta';
  document.getElementById('tag-form').reset();
  document.getElementById('tag-form').classList.remove('was-validated');
  document.getElementById('tag-modal-error').classList.add('d-none');

  if (id) {
    const tag = state.tags.find(t => t.id === id);
    if (tag) document.getElementById('tag-name').value = tag.name;
  }

  if (!tagModal) tagModal = new bootstrap.Modal(document.getElementById('tag-modal'));
  tagModal.show();
}

async function handleSaveTag(e) {
  e.preventDefault();
  const form = document.getElementById('tag-form');
  form.classList.add('was-validated');
  if (!form.checkValidity()) return;

  const name = document.getElementById('tag-name').value.trim();
  const btn  = document.getElementById('btn-save-tag');
  btn.disabled = true;

  try {
    if (state.editingTagId) {
      await api.put(`/tag/${state.editingTagId}`, { name });
      showAlert('Etiqueta actualizada.');
    } else {
      await api.post('/tag', { name });
      showAlert('Etiqueta creada.');
    }
    tagModal.hide();
    await loadTags();
  } catch (err) {
    const errEl = document.getElementById('tag-modal-error');
    errEl.textContent = err.message;
    errEl.classList.remove('d-none');
  } finally {
    btn.disabled = false;
  }
}

function handleDeleteTag(id) {
  state.pendingDeleteId   = id;
  state.pendingDeleteType = 'tag';
  const tag = state.tags.find(t => t.id === id);
  document.getElementById('delete-modal-body').textContent =
    `¿Seguro que quieres eliminar la etiqueta "${tag?.name}"?`;
  new bootstrap.Modal(document.getElementById('delete-modal')).show();
}

// --- Categorías ---

let categoryModal = null;

async function loadAdminCategories() {
  const el = document.getElementById('categories-container');
  el.innerHTML = `<div class="text-center py-4"><div class="spinner-border text-primary" role="status"></div></div>`;
  try {
    const base = getCategoryBase();
    state.adminCategories = await api.get(base);
    renderAdminCategories();
    state.categories = await fetchCategories();
    populateCategorySelect(state.categories, null);
  } catch (err) {
    el.innerHTML = `<div class="alert alert-danger">Error: ${err.message}</div>`;
  }
}

function renderAdminCategories() {
  const el = document.getElementById('categories-container');
  const cats = state.adminCategories || [];
  if (cats.length === 0) {
    el.innerHTML = `<div class="text-center text-muted py-4"><i class="fa-solid fa-folder fa-2x mb-2 d-block"></i>No hay categorías.</div>`;
    return;
  }
  el.innerHTML = `
    <div class="table-responsive">
      <table class="table table-hover align-middle">
        <thead class="table-light">
          <tr><th>#</th><th>Nombre</th><th class="text-end">Acciones</th></tr>
        </thead>
        <tbody>
          ${cats.map(c => `
            <tr>
              <td class="text-muted small">${c.id}</td>
              <td><i class="fa-solid fa-folder text-primary me-2"></i>${escapeHtml(c.title)}</td>
              <td class="text-end">
                <button class="btn btn-outline-primary btn-sm me-1" onclick="openCategoryModal(${c.id})">
                  <i class="fa-solid fa-pen"></i>
                </button>
                <button class="btn btn-outline-danger btn-sm" onclick="handleDeleteCategory(${c.id})">
                  <i class="fa-solid fa-trash"></i>
                </button>
              </td>
            </tr>`).join('')}
        </tbody>
      </table>
    </div>`;
}

function openCategoryModal(id) {
  state.editingCategoryId = id || null;
  document.getElementById('category-modal-label').textContent = id ? 'Editar categoría' : 'Nueva categoría';
  document.getElementById('category-form').reset();
  document.getElementById('category-form').classList.remove('was-validated');
  document.getElementById('category-modal-error').classList.add('d-none');

  if (id) {
    const cat = (state.adminCategories || []).find(c => c.id === id);
    if (cat) document.getElementById('category-name').value = cat.title;
  }

  if (!categoryModal) categoryModal = new bootstrap.Modal(document.getElementById('category-modal'));
  categoryModal.show();
}

async function handleSaveCategory(e) {
  e.preventDefault();
  const form = document.getElementById('category-form');
  form.classList.add('was-validated');
  if (!form.checkValidity()) return;

  const title = document.getElementById('category-name').value.trim();
  const base = getCategoryBase();
  const btn  = document.getElementById('btn-save-category');
  btn.disabled = true;

  try {
    if (state.editingCategoryId) {
      await api.put(`${base}/${state.editingCategoryId}`, { title });
      showAlert('Categoría actualizada.');
    } else {
      await api.post(base, { title });
      showAlert('Categoría creada.');
    }
    categoryModal.hide();
    await loadAdminCategories();
  } catch (err) {
    const errEl = document.getElementById('category-modal-error');
    errEl.textContent = err.message;
    errEl.classList.remove('d-none');
  } finally {
    btn.disabled = false;
  }
}

function handleDeleteCategory(id) {
  state.pendingDeleteId   = id;
  state.pendingDeleteType = 'category';
  const cat = (state.adminCategories || []).find(c => c.id === id);
  document.getElementById('delete-modal-body').textContent =
    `¿Seguro que quieres eliminar la categoría "${cat?.name}"?`;
  new bootstrap.Modal(document.getElementById('delete-modal')).show();
}

// --- Usuarios ---

let userModal = null;

async function loadUsers() {
  const el = document.getElementById('users-container');
  el.innerHTML = `<div class="text-center py-4"><div class="spinner-border text-primary" role="status"></div></div>`;
  try {
    state.users = await api.get('/admin/users');
    renderUsers();
  } catch (err) {
    el.innerHTML = `<div class="alert alert-danger">Error: ${err.message}</div>`;
  }
}

function roleBadge(role) {
  const map = { ADMIN: 'danger', GESTOR: 'warning text-dark', USER: 'secondary' };
  return `<span class="badge bg-${map[role] || 'secondary'}">${role}</span>`;
}

function renderUsers() {
  const el = document.getElementById('users-container');
  if (!state.users.length) {
    el.innerHTML = `<div class="text-center text-muted py-4">No hay usuarios.</div>`;
    return;
  }
  el.innerHTML = `
    <div class="table-responsive">
      <table class="table table-hover align-middle">
        <thead class="table-light">
          <tr><th>#</th><th>Usuario</th><th>Email</th><th>Rol</th><th class="text-end">Acciones</th></tr>
        </thead>
        <tbody>
          ${state.users.map(u => `
            <tr>
              <td class="text-muted small">${u.id}</td>
              <td><i class="fa-solid fa-user me-2 text-muted"></i>${escapeHtml(u.username)}</td>
              <td class="text-muted small">${u.email ? escapeHtml(u.email) : '—'}</td>
              <td>${roleBadge(u.role || u.roles?.[0] || 'USER')}</td>
              <td class="text-end">
                <button class="btn btn-outline-primary btn-sm me-1" onclick="openUserModal(${u.id})" title="Editar">
                  <i class="fa-solid fa-pen"></i>
                </button>
                ${(u.role === 'USER' || u.roles?.[0] === 'USER')
                  ? `<button class="btn btn-outline-success btn-sm me-1" onclick="handlePromoteUser(${u.id})" title="Promover a GESTOR">
                       <i class="fa-solid fa-arrow-up"></i>
                     </button>`
                  : (u.role === 'GESTOR' || u.roles?.[0] === 'GESTOR')
                    ? `<button class="btn btn-outline-warning btn-sm me-1" onclick="handleDemoteUser(${u.id})" title="Degradar a USER">
                         <i class="fa-solid fa-arrow-down"></i>
                       </button>`
                    : ''}
                <button class="btn btn-outline-danger btn-sm" onclick="handleDeleteUser(${u.id})" title="Eliminar">
                  <i class="fa-solid fa-trash"></i>
                </button>
              </td>
            </tr>`).join('')}
        </tbody>
      </table>
    </div>`;
}

function openUserModal(id) {
  state.editingUserId = id;
  document.getElementById('user-form').reset();
  document.getElementById('user-form').classList.remove('was-validated');
  document.getElementById('user-modal-error').classList.add('d-none');

  const user = state.users.find(u => u.id === id);
  if (user) {
    document.getElementById('user-username').value = user.username || '';
    document.getElementById('user-email').value    = user.email    || '';
  }

  if (!userModal) userModal = new bootstrap.Modal(document.getElementById('user-modal'));
  userModal.show();
}

async function handleSaveUser(e) {
  e.preventDefault();
  const form = document.getElementById('user-form');
  form.classList.add('was-validated');
  if (!form.checkValidity()) return;

  const body = {
    username: document.getElementById('user-username').value.trim(),
    email:    document.getElementById('user-email').value.trim() || null,
  };
  const btn = document.getElementById('btn-save-user');
  btn.disabled = true;

  try {
    await api.put(`/admin/users/${state.editingUserId}`, body);
    showAlert('Usuario actualizado.');
    userModal.hide();
    await loadUsers();
  } catch (err) {
    const errEl = document.getElementById('user-modal-error');
    errEl.textContent = err.message;
    errEl.classList.remove('d-none');
  } finally {
    btn.disabled = false;
  }
}

async function handlePromoteUser(id) {
  try {
    await api.post(`/admin/users/${id}/promote`);
    showAlert('Usuario promovido a GESTOR.');
    await loadUsers();
  } catch (err) {
    showAlert(`Error: ${err.message}`, 'danger');
  }
}

async function handleDemoteUser(id) {
  try {
    await api.post(`/admin/users/${id}/demote`);
    showAlert('Usuario degradado a USER.');
    await loadUsers();
  } catch (err) {
    showAlert(`Error: ${err.message}`, 'danger');
  }
}

function handleDeleteUser(id) {
  state.pendingDeleteId   = id;
  state.pendingDeleteType = 'user';
  const user = state.users.find(u => u.id === id);
  document.getElementById('delete-modal-body').textContent =
    `¿Seguro que quieres eliminar al usuario "${user?.username}"? Esta acción no se puede deshacer.`;
  new bootstrap.Modal(document.getElementById('delete-modal')).show();
}

// --- Perfil ---

async function handleChangePassword(e) {
  e.preventDefault();
  const form = document.getElementById('profile-password-form');
  form.classList.add('was-validated');
  if (!form.checkValidity()) return;

  const currentPassword = document.getElementById('profile-current-password').value;
  const newPassword     = document.getElementById('profile-new-password').value;
  const confirmPassword = document.getElementById('profile-confirm-password').value;
  const alertEl         = document.getElementById('profile-alert');

  if (newPassword !== confirmPassword) {
    alertEl.className = 'alert alert-danger mb-3';
    alertEl.textContent = 'Las contraseñas nuevas no coinciden.';
    alertEl.classList.remove('d-none');
    return;
  }

  try {
    await api.put('/user/password', { oldPassword: currentPassword, newPassword });
    alertEl.className = 'alert alert-success mb-3';
    alertEl.textContent = 'Contraseña cambiada correctamente. Vuelve a iniciar sesión.';
    alertEl.classList.remove('d-none');
    form.reset();
    form.classList.remove('was-validated');
    saveAuth(state.auth.username, newPassword);
  } catch (err) {
    alertEl.className = 'alert alert-danger mb-3';
    alertEl.textContent = `Error: ${err.message}`;
    alertEl.classList.remove('d-none');
  }
}

// --- Modal de tarea ---

let taskModal = null;

function populateCategorySelect(categories, selectedId) {
  const configs = [
    { id: 'task-category',   emptyLabel: 'Sin categoría' },
    { id: 'filter-category', emptyLabel: 'Todas'         }
  ];
  configs.forEach(({ id, emptyLabel }) => {
    const sel = document.getElementById(id);
    if (!sel) return;
    sel.innerHTML = `<option value="">${emptyLabel}</option>`;
    (categories || []).forEach(cat => {
      const opt = document.createElement('option');
      opt.value = cat.id;
      opt.textContent = cat.title || cat.name || '';
      if (selectedId && String(cat.id) === String(selectedId)) opt.selected = true;
      sel.appendChild(opt);
    });
  });
}

function renderTaskTagsCheckboxes(selectedIds = []) {
  const container = document.getElementById('task-tags-container');
  if (!state.tags || state.tags.length === 0) {
    container.innerHTML = '<span class="text-muted small fst-italic">No tienes etiquetas. Créalas en la sección Etiquetas.</span>';
    return;
  }
  container.innerHTML = state.tags.map(t => `
    <div class="form-check form-check-inline mb-0">
      <input class="form-check-input task-tag-check" type="checkbox"
             id="task-tag-${t.id}" value="${t.id}"
             ${selectedIds.includes(t.id) ? 'checked' : ''} />
      <label class="form-check-label small" for="task-tag-${t.id}">
        <span class="badge bg-secondary">${escapeHtml(t.name)}</span>
      </label>
    </div>`).join('');
}

function getSelectedTagIds() {
  return [...document.querySelectorAll('.task-tag-check:checked')].map(el => Number(el.value));
}

function resetTaskForm() {
  const form = document.getElementById('task-form');
  form.reset();
  form.classList.remove('was-validated');
  hideModalError();
  state.editingTaskId = null;
  document.getElementById('task-modal-label').textContent = 'Nueva tarea';
  document.getElementById('btn-save-task').textContent = 'Guardar';
  renderTaskTagsCheckboxes([]);
}

function openCreateModal() {
  resetTaskForm();
  populateCategorySelect(state.categories, null);
  renderTaskTagsCheckboxes([]);
  if (!taskModal) taskModal = new bootstrap.Modal(document.getElementById('task-modal'));
  taskModal.show();
}

function openEditModal(taskId) {
  const task = state.tasks.find(t => t.id === taskId);
  if (!task) return;
  resetTaskForm();
  state.editingTaskId = taskId;
  document.getElementById('task-modal-label').textContent = 'Editar tarea';
  document.getElementById('btn-save-task').textContent    = 'Guardar cambios';
  document.getElementById('task-title').value        = task.title || '';
  document.getElementById('task-description').value  = task.description || '';
  document.getElementById('task-deadline').value     = toInputDatetime(task.deadline);
  document.getElementById('task-priority').value     = task.priority || '';
  document.getElementById('task-importante').checked = task.importante;
  populateCategorySelect(state.categories, task.category?.id);
  renderTaskTagsCheckboxes((task.tags || []).map(t => t.id));
  if (!taskModal) taskModal = new bootstrap.Modal(document.getElementById('task-modal'));
  taskModal.show();
}

// --- Alertas y utilidades de UI ---

function showLogin() {
  document.getElementById('login-section').classList.remove('d-none');
  document.getElementById('app-section').classList.add('d-none');
}

function showApp() {
  document.getElementById('login-section').classList.add('d-none');
  document.getElementById('app-section').classList.remove('d-none');
  document.getElementById('nav-username').textContent = state.auth.username;
}

function showAlert(message, type = 'success') {
  const container = document.getElementById('alert-container');
  const id = `alert-${Date.now()}`;
  container.insertAdjacentHTML('afterbegin', `
    <div id="${id}" class="alert alert-${type} alert-dismissible fade show" role="alert">
      ${message}
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>`);
  setTimeout(() => document.getElementById(id)?.remove(), 4000);
}

function showLoginError(msg) {
  const el = document.getElementById('login-error');
  el.textContent = msg;
  el.classList.remove('d-none');
}
function hideLoginError() { document.getElementById('login-error').classList.add('d-none'); }
function showModalError(msg) {
  const el = document.getElementById('modal-error');
  el.textContent = msg;
  el.classList.remove('d-none');
}
function hideModalError() { document.getElementById('modal-error').classList.add('d-none'); }

function setLoadingTaskList() {
  document.getElementById('task-list').innerHTML = `
    <div class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
    </div>`;
}

// --- Eventos ---

async function handleLogin(e) {
  e.preventDefault();
  hideLoginError();
  const username = document.getElementById('input-username').value.trim();
  const password = document.getElementById('input-password').value;
  if (!username || !password) { showLoginError('Introduce usuario y contraseña.'); return; }

  const btn = document.getElementById('btn-login');
  btn.disabled = true;
  btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Entrando...';

  try {
    state.auth = { username, password };
    await api.get('/task');
    saveAuth(username, password);
    await initApp();
  } catch {
    clearAuth();
    showLoginError('Usuario o contraseña incorrectos.');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Iniciar sesión';
  }
}

function handleShowRegister(e) {
  e.preventDefault();
  document.getElementById('panel-login').classList.add('d-none');
  document.getElementById('panel-register').classList.remove('d-none');
  document.getElementById('register-form').reset();
  document.getElementById('register-form').classList.remove('was-validated');
  document.getElementById('register-error').classList.add('d-none');
  document.getElementById('register-success').classList.add('d-none');
}

function handleShowLogin(e) {
  e.preventDefault();
  document.getElementById('panel-register').classList.add('d-none');
  document.getElementById('panel-login').classList.remove('d-none');
}

async function handleRegister(e) {
  e.preventDefault();
  const form = document.getElementById('register-form');
  form.classList.add('was-validated');
  if (!form.checkValidity()) return;

  const body = {
    username: document.getElementById('reg-username').value.trim(),
    fullname: document.getElementById('reg-fullname').value.trim(),
    email:    document.getElementById('reg-email').value.trim(),
    password: document.getElementById('reg-password').value
  };

  const btn = document.getElementById('btn-register');
  btn.disabled = true;
  btn.textContent = 'Creando cuenta...';

  const errEl = document.getElementById('register-error');
  const okEl  = document.getElementById('register-success');
  errEl.classList.add('d-none');
  okEl.classList.add('d-none');

  try {
    await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    }).then(async res => {
      if (!res.ok) {
        const data = await res.json().catch(() => null);
        throw new Error(data?.message || `Error ${res.status}`);
      }
      return res.json();
    });

    okEl.textContent = '¡Cuenta creada correctamente! Ya puedes iniciar sesión.';
    okEl.classList.remove('d-none');
    form.reset();
    form.classList.remove('was-validated');
    setTimeout(() => handleShowLogin({ preventDefault: () => {} }), 2000);
  } catch (err) {
    errEl.textContent = `Error: ${err.message}`;
    errEl.classList.remove('d-none');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Crear cuenta';
  }
}

function handleLogout() {
  if (state.autoRefreshInterval) { clearInterval(state.autoRefreshInterval); state.autoRefreshInterval = null; }
  document.getElementById('toggle-autorefresh').checked = false;
  clearAuth();
  showLogin();
  document.getElementById('input-username').value = '';
  document.getElementById('input-password').value = '';
}

async function loadTasks(params) {
  setLoadingTaskList();
  try {
    const tasks = params ? await searchTasks(params) : await fetchTasks();
    state.tasks = tasks;
    renderTaskList(tasks);
  } catch (err) {
    document.getElementById('task-list').innerHTML = '';
    showAlert(`Error al cargar las tareas: ${err.message}`, 'danger');
  }
}

function handleAutoRefresh(enabled) {
  if (state.autoRefreshInterval) { clearInterval(state.autoRefreshInterval); state.autoRefreshInterval = null; }
  if (enabled) {
    state.autoRefreshInterval = setInterval(() => loadTasks(), 30000);
    showAlert('Actualización automática activada (cada 30 s)', 'info');
  }
}

async function handleSaveTask(e) {
  e.preventDefault();
  const form = document.getElementById('task-form');
  form.classList.add('was-validated');
  if (!form.checkValidity()) return;

  const title    = document.getElementById('task-title').value.trim();
  const priority = document.getElementById('task-priority').value;
  if (!title || !priority) return;

  const body = {
    title,
    description: document.getElementById('task-description').value.trim() || null,
    deadline:    toApiDatetime(document.getElementById('task-deadline').value),
    priority,
    categoryId:  document.getElementById('task-category').value
                   ? Number(document.getElementById('task-category').value) : null,
    importante:  document.getElementById('task-importante').checked,
    completed:   false
  };

  const btn = document.getElementById('btn-save-task');
  btn.disabled = true;
  btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Guardando...';

  try {
    let savedTask;
    if (state.editingTaskId) {
      const current = state.tasks.find(t => t.id === state.editingTaskId);
      body.completed = current ? current.completed : false;
      savedTask = await updateTask(state.editingTaskId, body);
      showAlert('Tarea actualizada correctamente.');
    } else {
      savedTask = await createTask(body);
      showAlert('Tarea creada correctamente.');
    }
    const tagIds = getSelectedTagIds();
    const taskId = savedTask?.id || state.editingTaskId;
    if (taskId) {
      await api.post(`/task/${taskId}/tags`, { tagIds });
    }
    taskModal.hide();
    await loadTasks();
  } catch (err) {
    showModalError(err.message);
  } finally {
    btn.disabled = false;
    btn.innerHTML = '<i class="fa-solid fa-floppy-disk me-1"></i>Guardar';
  }
}

function handleDeleteTask(taskId) {
  state.pendingDeleteId   = taskId;
  state.pendingDeleteType = 'task';
  const task = state.tasks.find(t => t.id === taskId);
  document.getElementById('delete-modal-body').textContent =
    `¿Seguro que quieres eliminar la tarea "${task?.title}"?`;
  new bootstrap.Modal(document.getElementById('delete-modal')).show();
}

async function confirmDelete() {
  const id   = state.pendingDeleteId;
  const type = state.pendingDeleteType;
  if (!id) return;

  const btn = document.getElementById('btn-confirm-delete');
  btn.disabled = true;

  try {
    if (type === 'task') {
      await deleteTask(id);
      showAlert('Tarea eliminada.');
      await loadTasks();
    } else if (type === 'tag') {
      await api.delete(`/tag/${id}`);
      showAlert('Etiqueta eliminada.');
      await loadTags();
    } else if (type === 'category') {
      await api.delete(`${getCategoryBase()}/${id}`);
      showAlert('Categoría eliminada.');
      await loadAdminCategories();
    } else if (type === 'user') {
      await api.delete(`/admin/users/${id}`);
      showAlert('Usuario eliminado.');
      await loadUsers();
    }
    bootstrap.Modal.getInstance(document.getElementById('delete-modal')).hide();
  } catch (err) {
    showAlert(`Error al eliminar: ${err.message}`, 'danger');
  } finally {
    btn.disabled = false;
    state.pendingDeleteId   = null;
    state.pendingDeleteType = null;
  }
}

async function handleToggleComplete(taskId, currentCompleted) {
  const task = state.tasks.find(t => t.id === taskId);
  if (!task) return;
  try {
    await updateTask(taskId, {
      title: task.title, description: task.description, deadline: task.deadline,
      priority: task.priority, categoryId: task.category?.id || null,
      importante: task.importante, completed: !currentCompleted
    });
    await loadTasks();
  } catch (err) {
    showAlert(`Error: ${err.message}`, 'danger');
    await loadTasks();
  }
}

function getFilterParams() {
  const params = {};
  const title      = document.getElementById('filter-title').value.trim();
  const priority   = document.getElementById('filter-priority').value;
  const category   = document.getElementById('filter-category').value;
  const completed  = document.getElementById('filter-completed').checked ? 'true' : '';
  const importante = document.getElementById('filter-importante').checked;
  if (title)      params.title      = title;
  if (priority)   params.priority   = priority;
  if (category)   params.category   = category;
  if (completed)  params.completed  = completed;
  if (importante) params.importante = true;
  return params;
}

function handleApplyFilter() {
  const params = getFilterParams();
  loadTasks(Object.keys(params).length > 0 ? params : null);
}

function handleClearFilter() {
  document.getElementById('filter-title').value        = '';
  document.getElementById('filter-priority').value     = '';
  document.getElementById('filter-category').value     = '';
  document.getElementById('filter-completed').checked  = false;
  document.getElementById('filter-importante').checked = false;
  loadTasks();
}

// --- Inicialización ---

async function initApp() {
  showApp();
  setLoadingTaskList();

  state.role = await detectRole();
  applyRoleUI();

  try {
    state.categories = await fetchCategories();
    populateCategorySelect(state.categories, null);
  } catch {
    state.categories = [];
  }
  try {
    state.tags = await api.get('/tag');
  } catch {
    state.tags = [];
  }

  await loadTasks();
}

function bindEvents() {
  document.getElementById('login-form').addEventListener('submit', handleLogin);
  document.getElementById('btn-logout').addEventListener('click', handleLogout);
  document.getElementById('btn-show-register').addEventListener('click', handleShowRegister);
  document.getElementById('btn-show-login').addEventListener('click', handleShowLogin);
  document.getElementById('register-form').addEventListener('submit', handleRegister);

  document.querySelectorAll('#main-tabs .nav-link').forEach(btn => {
    btn.addEventListener('click', () => showSection(btn.dataset.section));
  });

  document.getElementById('btn-new-task').addEventListener('click', openCreateModal);
  document.getElementById('task-form').addEventListener('submit', handleSaveTask);
  document.getElementById('btn-confirm-delete').addEventListener('click', confirmDelete);
  document.getElementById('btn-refresh').addEventListener('click', () => {
    if (state.currentSection === 'tasks') loadTasks();
    else if (state.currentSection === 'dashboard') loadDashboard();
    else if (state.currentSection === 'tags') loadTags();
    else if (state.currentSection === 'categories') loadAdminCategories();
    else if (state.currentSection === 'users') loadUsers();
  });
  document.getElementById('toggle-autorefresh').addEventListener('change', e => handleAutoRefresh(e.target.checked));

  document.getElementById('btn-apply-filter').addEventListener('click', handleApplyFilter);
  document.getElementById('btn-clear-filter').addEventListener('click', handleClearFilter);
  document.getElementById('filter-title').addEventListener('keydown', e => { if (e.key === 'Enter') handleApplyFilter(); });

  document.getElementById('tag-form').addEventListener('submit', handleSaveTag);
  document.getElementById('category-form').addEventListener('submit', handleSaveCategory);
  document.getElementById('user-form').addEventListener('submit', handleSaveUser);
  document.getElementById('profile-password-form').addEventListener('submit', handleChangePassword);
}

document.addEventListener('DOMContentLoaded', async () => {
  bindEvents();
  loadAuth();

  if (state.auth) {
    try {
      await api.get('/task');
      await initApp();
    } catch {
      clearAuth();
      showLogin();
    }
  } else {
    showLogin();
  }
});
