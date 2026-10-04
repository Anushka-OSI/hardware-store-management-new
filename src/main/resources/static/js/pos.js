/* FAST POS logic. Expects: posSearch, posGrid, billItems, discType, discVal, payMethod buttons, amtPaid, billSummary, receiptModal */
window.POS = (() => {
  let items = []; // {id,name,price,qty,stock}
  let payMethod = 'CASH';
  async function search(q) {
    const grid = document.getElementById('posGrid'); if (!grid) return;
    const qs = new URLSearchParams({ keyword: q || '', page: 0, size: 24 });
    try {
      const r = await App.get('/api/public/products?' + qs.toString());
      const { items: list } = App.unwrapPage(r.data);
      grid.innerHTML = (list || []).filter(p => (p.currentStock || 0) > 0).map(p => `
        <button onclick='POS.pick(${JSON.stringify({ id: p.id, name: p.name, sellingPrice: p.sellingPrice, currentStock: p.currentStock })})'
          class="bg-white border rounded-xl p-3 text-left hover:border-orange-500 hover:shadow">
          <div class="font-semibold text-sm line-clamp-2">${App.escapeHtml(p.name)}</div>
          <div class="text-orange-600 font-bold text-sm mt-1">${App.money(p.sellingPrice)}</div>
          <div class="text-xs text-slate-500">Stock: ${p.currentStock ?? 0}</div>
        </button>`).join('') || '<div class="text-slate-400">No products</div>';
    } catch (e) { grid.innerHTML = '<div class="text-red-500">Load failed</div>'; }
  }
  function pick(p) {
    const f = items.find(i => String(i.id) === String(p.id));
    const price = Number(p.sellingPrice || 0);
    if (f) { if (f.qty + 1 > (p.currentStock || 9999)) return App.toast('Exceeds stock', 'warn'); f.qty++; }
    else items.push({ id: p.id, name: p.name, price, qty: 1, stock: p.currentStock });
    render();
  }
  function chQty(id, d) {
    const it = items.find(i => String(i.id) === String(id)); if (!it) return;
    it.qty += d;
    if (it.qty <= 0) items = items.filter(i => String(i.id) !== String(id));
    if (it.qty > (it.stock || 9999)) { it.qty = it.stock; App.toast('Max stock reached', 'warn'); }
    render();
  }
  function rm(id) { items = items.filter(i => String(i.id) !== String(id)); render(); }
  function setPay(m) {
    payMethod = m;
    document.querySelectorAll('[data-pay]').forEach(b => {
      const on = b.dataset.pay === m;
      b.className = 'flex-1 px-3 py-2 rounded-lg text-sm font-semibold ' + (on ? 'bg-orange-500 text-white' : 'bg-slate-100 hover:bg-slate-200');
    });
    render();
  }
  function totals() {
    const sub = items.reduce((s, i) => s + i.qty * Number(i.price || 0), 0);
    const dt = (document.getElementById('discType') || {}).value || 'NONE';
    const dv = Number((document.getElementById('discVal') || {}).value || 0);
    let disc = 0;
    if (dt === 'PERCENT') disc = sub * dv / 100;
    else if (dt === 'FIXED') disc = dv;
    disc = Math.min(disc, sub);
    return { sub, disc, total: sub - disc };
  }
  function render() {
    const box = document.getElementById('billItems'); if (!box) return;
    box.innerHTML = items.length ? items.map(i => `
      <div class="flex items-center gap-2 py-2 border-b text-sm">
        <div class="flex-1"><div class="font-medium">${App.escapeHtml(i.name)}</div>
        <div class="text-xs text-slate-500">${App.money(i.price)} each</div></div>
        <button onclick="POS.chQty(${i.id},-1)" class="w-7 h-7 bg-slate-100 rounded">−</button>
        <span class="w-8 text-center font-bold">${i.qty}</span>
        <button onclick="POS.chQty(${i.id},1)" class="w-7 h-7 bg-slate-100 rounded">+</button>
        <button onclick="POS.rm(${i.id})" class="text-red-500 px-1">✕</button>
      </div>`).join('') : '<div class="text-center text-slate-400 py-8">No items — tap products to add</div>';
    const t = totals();
    const paid = Number((document.getElementById('amtPaid') || {}).value || 0);
    const s = document.getElementById('billSummary');
    if (s) s.innerHTML = `
      <div class="flex justify-between text-sm"><span>Subtotal</span><span>${App.money(t.sub)}</span></div>
      <div class="flex justify-between text-sm"><span>Discount</span><span>− ${App.money(t.disc)}</span></div>
      <div class="flex justify-between font-extrabold text-lg"><span>Total</span><span class="text-orange-600">${App.money(t.total)}</span></div>
      <div class="flex justify-between text-sm"><span>Paid</span><span>${App.money(paid)}</span></div>
      <div class="flex justify-between text-sm font-bold"><span>Change</span><span>${App.money(Math.max(0, paid - t.total))}</span></div>`;
  }
  async function complete() {
    if (!items.length) return App.toast('Bill is empty', 'warn');
    const t = totals();
    const paid = Number((document.getElementById('amtPaid') || {}).value || 0);
    if (paid < t.total) return App.toast('Amount paid is less than total', 'error');
    const dt = (document.getElementById('discType') || {}).value || 'NONE';
    const payload = {
      discountType: dt === 'NONE' ? 'NONE' : dt,
      discountAmount: dt === 'PERCENT' ? Number((document.getElementById('discVal') || {}).value || 0) : t.disc,
      paymentMethod: payMethod, amountPaid: paid,
      notes: (document.getElementById('posNotes') || {}).value || '',
      items: items.map(i => ({ productId: i.id, quantity: i.qty }))
    };
    try {
      const r = await App.post('/api/sales', payload);
      showReceipt(r.data, paid);
      items = []; render();
      const ap = document.getElementById('amtPaid'); if (ap) ap.value = '';
    } catch (e) { /* toast shown */ }
  }
  function showReceipt(sale, paid) {
    const t = totals();
    const html = `<div id="receiptPrint" class="text-sm">
      <div class="text-center font-extrabold text-lg">GURUGE HARDWARE</div>
      <div class="text-center text-xs text-slate-500 mb-3">Receipt #${sale.receiptNumber || sale.id} • ${new Date().toLocaleString()}</div>
      ${(sale.items || []).map(it => `<div class="flex justify-between"><span>${App.escapeHtml(it.productName || '')} × ${it.quantity}</span><span>${App.money(it.lineTotal)}</span></div>`).join('')}
      <hr class="my-2"/>
      <div class="flex justify-between font-bold"><span>Total</span><span>${App.money(sale.totalAmount ?? t.total)}</span></div>
      <div class="flex justify-between"><span>Paid (${payMethod})</span><span>${App.money(paid)}</span></div>
      <div class="flex justify-between"><span>Change</span><span>${App.money(Math.max(0, paid - Number(sale.totalAmount ?? t.total)))}</span></div>
      <div class="text-center text-xs mt-3 text-slate-500">Thank you, come again!</div>
      <div class="flex gap-2 mt-4 no-print">
        <button onclick="window.print()" class="flex-1 bg-slate-900 text-white py-2 rounded-lg">🖨 Print</button>
        <button onclick="App.closeModal()" class="flex-1 bg-slate-100 py-2 rounded-lg">Close</button>
      </div></div>`;
    App.openModal(html);
  }
  return { search, pick, chQty, rm, setPay, render, complete, totals };
})();
