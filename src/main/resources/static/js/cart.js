/* localStorage cart: key guruge_cart -> [{id,name,price,image,qty,stock}] */
window.Cart = (() => {
  const KEY = 'guruge_cart';
  function all() { try { return JSON.parse(localStorage.getItem(KEY)) || []; } catch { return []; } }
  function save(c) { localStorage.setItem(KEY, JSON.stringify(c)); refreshBadge(); }
  function add(p, qty = 1) {
    const c = all();
    const f = c.find(i => String(i.id) === String(p.id));
    const stock = p.currentStock ?? p.stock ?? 999999;
    if (f) {
      if (f.qty + qty > stock) { App.toast('Only ' + stock + ' in stock', 'warn'); return false; }
      f.qty += qty;
    } else {
      if (qty > stock) { App.toast('Only ' + stock + ' in stock', 'warn'); return false; }
      c.push({ id: p.id, name: p.name, price: Number(p.sellingPrice ?? p.price ?? 0), image: p.imageUrl || p.image, qty, stock });
    }
    save(c); App.toast('Added to cart', 'success'); return true;
  }
  function update(id, qty) {
    let c = all();
    if (qty <= 0) return remove(id);
    c = c.map(i => String(i.id) === String(id) ? { ...i, qty: Math.min(qty, i.stock ?? qty) } : i);
    save(c);
  }
  function remove(id) { save(all().filter(i => String(i.id) !== String(id))); }
  function clear() { save([]); }
  function count() { return all().reduce((s, i) => s + i.qty, 0); }
  function total() { return all().reduce((s, i) => s + i.qty * Number(i.price || 0), 0); }
  function refreshBadge() {
    const n = count();
    ['cartCount', 'cartCountM'].forEach(id => { const el = document.getElementById(id); if (el) el.textContent = n; });
  }
  return { all, save, add, update, remove, clear, count, total, refreshBadge, get: all };
})();
