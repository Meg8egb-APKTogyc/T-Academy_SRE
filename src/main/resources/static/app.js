const STATUS_LABELS = { NEW: 'Новая', IN_PROGRESS: 'В работе', DONE: 'Выполнена' };
const PRIORITY_LABELS = { LOW: 'Низкий', MEDIUM: 'Средний', HIGH: 'Высокий' };

const $ = (id) => document.getElementById(id);

async function api(path, options = {}) {
    const response = await fetch('/api' + path, {
        headers: { 'Content-Type': 'application/json' },
        ...options
    });
    if (!response.ok) {
        let message = 'Ошибка ' + response.status;
        try {
            const body = await response.json();
            if (body.message) message = body.message;
        } catch (_) { /* ignore */ }
        throw new Error(message);
    }
    if (response.status === 204) return null;
    return response.json();
}

function toast(text, isError = false) {
    const el = $('toast');
    el.textContent = text;
    el.style.background = isError ? '#b91c1c' : '#111827';
    el.classList.remove('hidden');
    setTimeout(() => el.classList.add('hidden'), 3000);
}

function escapeHtml(value) {
    if (value == null) return '';
    return String(value).replace(/[&<>"']/g,
        (ch) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[ch]));
}

// ---------- Табы ----------
document.querySelectorAll('.tab').forEach((tab) => {
    tab.addEventListener('click', () => {
        document.querySelectorAll('.tab').forEach((t) => t.classList.toggle('active', t === tab));
        document.querySelectorAll('.tab-content').forEach((c) =>
            c.classList.toggle('active', c.id === 'tab-' + tab.dataset.tab));
    });
});

// ---------- Задачи ----------
async function loadTasks() {
    const status = $('filter-status').value;
    const query = status ? '?status=' + encodeURIComponent(status) : '';
    const tasks = await api('/tasks' + query);
    const tbody = $('tasks-body');
    tbody.innerHTML = '';
    for (const task of tasks) {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${task.id}</td>
            <td><strong>${escapeHtml(task.title)}</strong><br>
                <small style="color:var(--muted)">${escapeHtml(task.description)}</small></td>
            <td>${escapeHtml(task.projectName ?? '—')}</td>
            <td><span class="badge badge-${task.status}">${STATUS_LABELS[task.status]}</span></td>
            <td><span class="badge badge-${task.priority}">${PRIORITY_LABELS[task.priority]}</span></td>
            <td>${task.dueDate ?? '—'}</td>
            <td><div class="actions-cell">
                <button class="small" data-edit="${task.id}">✏️</button>
                <button class="danger" data-delete="${task.id}">🗑</button>
            </div></td>`;
        tr.querySelector('[data-edit]').addEventListener('click', () => editTask(task));
        tr.querySelector('[data-delete]').addEventListener('click', () => deleteTask(task.id));
        tbody.appendChild(tr);
    }
}

function editTask(task) {
    $('task-id').value = task.id;
    $('task-title').value = task.title;
    $('task-description').value = task.description ?? '';
    $('task-status').value = task.status;
    $('task-priority').value = task.priority;
    $('task-due-date').value = task.dueDate ?? '';
    $('task-project').value = task.projectId ?? '';
    $('task-form-title').textContent = 'Редактирование задачи #' + task.id;
    $('task-cancel').classList.remove('hidden');
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function resetTaskForm() {
    $('task-form').reset();
    $('task-id').value = '';
    $('task-form-title').textContent = 'Новая задача';
    $('task-cancel').classList.add('hidden');
}

async function deleteTask(id) {
    if (!confirm('Удалить задачу #' + id + '?')) return;
    try {
        await api('/tasks/' + id, { method: 'DELETE' });
        toast('Задача удалена');
        await loadTasks();
    } catch (e) {
        toast(e.message, true);
    }
}

$('task-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const id = $('task-id').value;
    const payload = {
        title: $('task-title').value.trim(),
        description: $('task-description').value.trim() || null,
        status: $('task-status').value,
        priority: $('task-priority').value,
        dueDate: $('task-due-date').value || null,
        projectId: $('task-project').value ? Number($('task-project').value) : null
    };
    try {
        if (id) {
            await api('/tasks/' + id, { method: 'PUT', body: JSON.stringify(payload) });
            toast('Задача обновлена');
        } else {
            await api('/tasks', { method: 'POST', body: JSON.stringify(payload) });
            toast('Задача создана');
        }
        resetTaskForm();
        await loadTasks();
    } catch (e) {
        toast(e.message, true);
    }
});

$('task-cancel').addEventListener('click', resetTaskForm);
$('filter-status').addEventListener('change', loadTasks);

// ---------- Проекты ----------
async function loadProjects() {
    const projects = await api('/projects');

    const select = $('task-project');
    const selected = select.value;
    select.innerHTML = '<option value="">— без проекта —</option>';
    for (const p of projects) {
        const option = document.createElement('option');
        option.value = p.id;
        option.textContent = p.name;
        select.appendChild(option);
    }
    select.value = selected;

    const tbody = $('projects-body');
    tbody.innerHTML = '';
    for (const p of projects) {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${p.id}</td>
            <td><strong>${escapeHtml(p.name)}</strong></td>
            <td>${escapeHtml(p.description)}</td>
            <td><div class="actions-cell">
                <button class="small" data-edit="${p.id}">✏️</button>
                <button class="danger" data-delete="${p.id}">🗑</button>
            </div></td>`;
        tr.querySelector('[data-edit]').addEventListener('click', () => editProject(p));
        tr.querySelector('[data-delete]').addEventListener('click', () => deleteProject(p.id));
        tbody.appendChild(tr);
    }
}

function editProject(p) {
    $('project-id').value = p.id;
    $('project-name').value = p.name;
    $('project-description').value = p.description ?? '';
    $('project-form-title').textContent = 'Редактирование проекта #' + p.id;
    $('project-cancel').classList.remove('hidden');
}

function resetProjectForm() {
    $('project-form').reset();
    $('project-id').value = '';
    $('project-form-title').textContent = 'Новый проект';
    $('project-cancel').classList.add('hidden');
}

async function deleteProject(id) {
    if (!confirm('Удалить проект #' + id + '? Задачи останутся без проекта.')) return;
    try {
        await api('/projects/' + id, { method: 'DELETE' });
        toast('Проект удалён');
        await Promise.all([loadProjects(), loadTasks()]);
    } catch (e) {
        toast(e.message, true);
    }
}

$('project-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const id = $('project-id').value;
    const payload = {
        name: $('project-name').value.trim(),
        description: $('project-description').value.trim() || null
    };
    try {
        if (id) {
            await api('/projects/' + id, { method: 'PUT', body: JSON.stringify(payload) });
            toast('Проект обновлён');
        } else {
            await api('/projects', { method: 'POST', body: JSON.stringify(payload) });
            toast('Проект создан');
        }
        resetProjectForm();
        await Promise.all([loadProjects(), loadTasks()]);
    } catch (e) {
        toast(e.message, true);
    }
});

$('project-cancel').addEventListener('click', resetProjectForm);

// ---------- Инициализация ----------
(async function init() {
    try {
        const info = await api('/info');
        $('instance').textContent = 'экземпляр: ' + info.instanceId + ' (' + info.hostname + ')';
        await loadProjects();
        await loadTasks();
    } catch (e) {
        toast('Не удалось загрузить данные: ' + e.message, true);
    }
})();
