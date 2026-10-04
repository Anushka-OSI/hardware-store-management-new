/* Guruge Hardware — shared helpers. ApiResponse {success, message, data} */
window.App = (() => {
  async function api(url, opts = {}) {
    const res = await fetch(url, {
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json', ...(opts.headers || {}) },
      ...opts,
    });
    const ct = res.headers.get('content-type') || '';
    let body = null;
    try { body = ct.includes('json') ? await res.json() : await res.text(); } catch (e) { /* ignore */ }
    if (!res.ok) {
      const msg = (body && body.message) || ('Request failed: ' + res.status);
      toast(msg, 'error');
      throw new Error(msg);
    }
    return body;
  }
  const get = (u) => api(u);
  const post = (u, d) => api(u, { method: 'POST', body: JSON.stringify(d) });
  const put = (u, d) => api(u, { method: 'PUT', body: JSON.stringify(d) });
  const patch = (u, d) => api(u, { method: 'PATCH', body: JSON.stringify(d) });
  const del = (u) => api(u, { method: 'DELETE' });

  function toast(msg, type = 'info') {
    const wrap = document.getElementById('toastWrap');
    if (!wrap) { alert(msg); return; }
    const colors = { success: 'bg-green-600', error: 'bg-red-600', info: 'bg-slate-900', warn: 'bg-amber-500 text-slate-900' };
    const el = document.createElement('div');
    el.className = `${colors[type] || colors.info} text-white px-4 py-2.5 rounded-lg shadow text-sm max-w-xs`;
    el.textContent = msg;
    wrap.appendChild(el);
    setTimeout(() => { el.style.opacity = '0'; el.style.transition = 'opacity .4s'; setTimeout(() => el.remove(), 400); }, 3200);
  }

  function openModal(html) {
    let w = document.getElementById('modalWrap');
    if (!w) { w = document.createElement('div'); w.id = 'modalWrap'; document.body.appendChild(w); }
    w.innerHTML = `<div class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/50" onclick="App.closeModal()"></div>
      <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto p-5">${html}</div></div>`;
  }
  function closeModal() { const w = document.getElementById('modalWrap'); if (w) w.innerHTML = ''; }

  function badge(status) {
    const s = String(status || '').toUpperCase();
    const map = {
      'IN_STOCK': 'badge-green', 'ACTIVE': 'badge-green', 'DELIVERED': 'badge-green', 'RECEIVED': 'badge-green',
      'COMPLETED': 'badge-green', 'PAID': 'badge-green', 'RESPONDED': 'badge-green', 'CONFIRMED': 'badge-green',
      'LOW_STOCK': 'badge-amber', 'LOW': 'badge-amber', 'PENDING': 'badge-amber', 'SENT': 'badge-amber',
      'NEW': 'badge-blue', 'IN_PROGRESS': 'badge-blue',
      'OUT_OF_STOCK': 'badge-red', 'OUT': 'badge-red', 'CANCELLED': 'badge-red', 'INACTIVE': 'badge-red',
    };
    return `<span class="${map[s] || 'badge-slate'}">${escapeHtml(status || '-')}</span>`;
  }
  function escapeHtml(v) { return String(v ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c])); }
  function money(v) { const n = Number(v || 0); return 'Rs. ' + n.toLocaleString('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }
  function img(url, alt) {
    const src = url || 'https://placehold.co/400x300?text=No+Image';
    return `<img src="${escapeHtml(src)}" alt="${escapeHtml(alt || '')}" loading="lazy" onerror="this.src='https://placehold.co/400x300?text=No+Image'" class="object-cover"/>`;
  }
  function pageParams(page, size) { return `page=${page || 0}&size=${size || 10}`; }
  function unwrapPage(data) {
    if (!data) return { items: [], page: null };
    if (Array.isArray(data)) return { items: data, page: null };
    if (Array.isArray(data.content)) return { items: data.content, page: data };
    return { items: [], page: null };
  }
  return { api, get, post, put, patch, del, toast, openModal, closeModal, badge, escapeHtml, money, img, pageParams, unwrapPage };
})();
function toast(m, t) { App.toast(m, t); }
