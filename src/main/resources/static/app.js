const appRoot = document.getElementById('todoApp');
const taskInput = document.getElementById('taskInput');
const addButton = document.getElementById('addButton');
const statusMessage = document.getElementById('statusMessage');
const todoList = document.getElementById('todoList');

const SUPABASE_URL = appRoot?.dataset.supabaseUrl || '';
const SUPABASE_ANON_KEY = appRoot?.dataset.supabaseAnonKey || '';

let supabaseClient = null;

function setStatus(message, state = '') {
    statusMessage.textContent = message;
    if (state) {
        statusMessage.dataset.state = state;
    } else {
        delete statusMessage.dataset.state;
    }
}

function displayTodos(todos) {
    if (!todos.length) {
        todoList.innerHTML = '<li>No tasks yet.</li>';
        return;
    }

    todoList.innerHTML = todos
        .map((todo) => `<li>${escapeHtml(todo.title ?? '')}</li>`)
        .join('');
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

async function fetchTodos() {
    if (!supabaseClient) {
        displayTodos([]);
        return;
    }

    setStatus('Loading...');
    const { data, error } = await supabaseClient
        .from('todo')
        .select('id, title')
        .order('id', { ascending: false });

    if (error) {
        console.error('Supabase fetch error:', error);
        setStatus(`Load failed: ${error.message}`, 'error');
        displayTodos([]);
        return;
    }

    displayTodos(data ?? []);
    setStatus(`Loaded ${data?.length ?? 0} tasks.`, 'success');
}

async function addTodo() {
    if (!supabaseClient) {
        setStatus('Supabase configuration is missing.', 'error');
        return;
    }

    const title = taskInput.value.trim();
    if (!title) {
        setStatus('Enter a task title.', 'error');
        return;
    }

    addButton.disabled = true;
    setStatus('Adding...');

    const { error } = await supabaseClient
        .from('todo')
        .insert([{ title }]);

    addButton.disabled = false;

    if (error) {
        console.error('Supabase insert error:', error);
        setStatus(`Add failed: ${error.message}`, 'error');
        return;
    }

    taskInput.value = '';
    setStatus('Added successfully.', 'success');
    await fetchTodos();
}

function initializeSupabase() {
    if (!SUPABASE_URL || !SUPABASE_ANON_KEY) {
        setStatus('SUPABASE_URL or SUPABASE_ANON_KEY is not set.', 'error');
        displayTodos([]);
        return;
    }

    supabaseClient = window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);
    fetchTodos();
}

taskInput?.addEventListener('keydown', (event) => {
    if (event.key === 'Enter') {
        event.preventDefault();
        addTodo();
    }
});

initializeSupabase();
