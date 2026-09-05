const storageKey = 'people-api-credentials';
const $ = selector => document.querySelector(selector);
const loginView = $('#login-view'); const dashboardView = $('#dashboard-view'); const loginForm = $('#login-form');
const personForm = $('#person-form'); const peopleBody = $('#people'); const emptyState = $('#empty-state');
const tableLoading = $('#table-loading'); const peopleCount = $('#people-count'); const apiStatus = $('#api-status');
const modal = $('#modal'); const modalContent = $('#modal-content');
const documentInput = personForm.elements.document;
documentInput.inputMode = 'numeric'; documentInput.minLength = 7; documentInput.maxLength = 12; documentInput.pattern = '[0-9]{7,12}'; documentInput.title = 'Informe de 7 a 12 dígitos numéricos';

function credentials() { return sessionStorage.getItem(storageKey); }
function setLoading(button, loading, label) { if (loading) { button.dataset.label = button.textContent; button.textContent = label; } else button.textContent = button.dataset.label; button.disabled = loading; }
function toast(text, type = 'success') { const item = document.createElement('div'); item.className = `toast ${type}`; item.textContent = text; $('#toast-region').append(item); setTimeout(() => item.remove(), 4500); }
function setApiStatus(online) { apiStatus.className = `status ${online ? 'online' : 'offline'}`; apiStatus.querySelector('span').textContent = online ? 'API online' : 'API offline'; }
function showLogin(message = '') { dashboardView.hidden = true; loginView.hidden = false; $('#login-feedback').textContent = message; $('#password').value = ''; $('#username').focus(); }
function showDashboard() { loginView.hidden = true; dashboardView.hidden = false; loadPeople(); }
function logout(message = '') { sessionStorage.removeItem(storageKey); setApiStatus(false); showLogin(message); }

async function api(path, options = {}) {
  if (!credentials()) throw new Error('Sua sessão não foi encontrada.');
  let response;
  try { response = await fetch(path, { ...options, headers: { Authorization: credentials(), ...(options.headers || {}) } }); }
  catch { setApiStatus(false); throw new Error('Não foi possível comunicar com a API.'); }
  if (response.status === 401) { logout('Sua sessão expirou. Entre novamente.'); throw new Error('Credenciais inválidas ou sessão expirada.'); }
  if (!response.ok) { const payload = await response.json().catch(() => ({})); const error = new Error(payload.message || `Erro HTTP ${response.status}`); error.fields = payload.errors; throw error; }
  setApiStatus(true); return response.status === 204 ? null : response.json();
}

function cell(row, text, className = '') { const element = document.createElement('td'); element.textContent = text; element.className = className; row.append(element); }
function renderPeople(people) {
  peopleBody.replaceChildren(); peopleCount.textContent = people.length; emptyState.hidden = people.length !== 0;
  people.forEach(person => { const row = document.createElement('tr'); cell(row, `#${person.id}`); cell(row, person.name, 'name'); cell(row, person.lastName); cell(row, person.document); cell(row, person.email); const actions = document.createElement('td'); actions.className = 'actions'; const nationality = action('Nacionalidade', 'button ghost table-action', () => showNationality(person)); const remove = action('Excluir', 'button danger table-action', () => confirmDeletion(person)); actions.append(nationality, remove); row.append(actions); peopleBody.append(row); });
}
function action(text, className, handler) { const button = document.createElement('button'); button.type = 'button'; button.className = className; button.textContent = text; button.addEventListener('click', handler); return button; }
async function loadPeople() { tableLoading.hidden = false; emptyState.hidden = true; try { renderPeople(await api('/list')); } catch (error) { peopleBody.replaceChildren(); peopleCount.textContent = '—'; toast(error.message, 'error'); } finally { tableLoading.hidden = true; } }

function openModal(content) { modalContent.replaceChildren(content); modal.hidden = false; $('.close').focus(); }
function closeModal() { modal.hidden = true; modalContent.replaceChildren(); }
function text(tag, value, className = '') { const element = document.createElement(tag); element.textContent = value; element.className = className; return element; }
async function showNationality(person) {
  const loading = document.createElement('div'); loading.className = 'loading'; loading.innerHTML = '<i></i> Consultando nacionalidade...'; openModal(loading);
  try { const result = await api(`/findNacionalityByPerson/${person.id}`); const content = document.createDocumentFragment(); content.append(text('p', 'ESTIMATIVA DA NATIONALIZE', 'eyebrow'), text('div', result.countryCode, 'country'), text('h2', 'Nacionalidade provável', 'modal-title'), text('p', `${result.personName} ${person.lastName}`, 'modal-person'), text('h3', result.nationality)); content.append(text('div', 'Esta é uma estimativa baseada somente no primeiro nome informado, não uma confirmação de nacionalidade.', 'estimate')); openModal(content); }
  catch (error) { closeModal(); toast(error.message, 'error'); }
}
function confirmDeletion(person) {
  const content = document.createDocumentFragment(); content.append(text('p', 'AÇÃO IRREVERSÍVEL', 'eyebrow'), text('h2', 'Excluir pessoa?', 'modal-title'), text('p', `${person.name} ${person.lastName} será removido(a) permanentemente da sua base.`, 'modal-person')); const actions = document.createElement('div'); actions.className = 'modal-actions'; const cancel = action('Cancelar', 'button ghost', closeModal); const confirm = action('Excluir pessoa', 'button danger', async () => { setLoading(confirm, true, 'Excluindo...'); try { await api(`/list/${person.id}`, { method: 'DELETE' }); closeModal(); toast('Pessoa excluída com sucesso.'); loadPeople(); } catch (error) { toast(error.message, 'error'); setLoading(confirm, false); } }); actions.append(cancel, confirm); content.append(actions); openModal(content);
}
function applyFieldErrors(fields = {}) { personForm.querySelectorAll('.input-error').forEach(input => input.classList.remove('input-error')); Object.keys(fields).forEach(name => personForm.elements[name]?.classList.add('input-error')); }

loginForm.addEventListener('submit', async event => {
  event.preventDefault(); const username = loginForm.elements.username.value.trim(); const password = loginForm.elements.password.value; if (!username || !password) { $('#login-feedback').textContent = 'Informe usuário e senha para continuar.'; return; }
  const submit = loginForm.querySelector('button'); setLoading(submit, true, 'Validando acesso...'); $('#login-feedback').textContent = ''; sessionStorage.setItem(storageKey, `Basic ${btoa(`${username}:${password}`)}`);
  try { await api('/list'); showDashboard(); toast('Acesso realizado com sucesso.'); } catch (error) { sessionStorage.removeItem(storageKey); setApiStatus(false); $('#login-feedback').textContent = error.message; } finally { setLoading(submit, false); }
});
personForm.addEventListener('submit', async event => {
  event.preventDefault(); applyFieldErrors(); const body = Object.fromEntries(new FormData(personForm).entries());
  if (!/^\d{7,12}$/.test(body.document)) { personForm.elements.document.classList.add('input-error'); toast('Documento deve conter entre 7 e 12 dígitos numéricos.', 'error'); return; }
  const submit = personForm.querySelector('button'); setLoading(submit, true, 'Cadastrando...');
  try { await api('/registrarName', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }); personForm.reset(); toast('Pessoa cadastrada com sucesso.'); loadPeople(); } catch (error) { applyFieldErrors(error.fields); toast(error.message, 'error'); } finally { setLoading(submit, false); }
});
$('#load-people').addEventListener('click', loadPeople); $('#logout-button').addEventListener('click', () => logout('Sessão encerrada.')); document.querySelectorAll('[data-close-modal]').forEach(element => element.addEventListener('click', closeModal)); document.addEventListener('keydown', event => { if (event.key === 'Escape' && !modal.hidden) closeModal(); });
if (credentials()) showDashboard(); else showLogin();
