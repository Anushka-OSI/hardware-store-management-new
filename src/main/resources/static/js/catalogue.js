/* Catalogue: filters + fetch + render. Expects DOM ids: grid, catSel, brandSel, searchInp, sortSel, minP, maxP, availSel, pageInfo */
window.Catalogue = (() => {
  let page = 0; const size = 12;
  function params() {
    const q = new URLSearchParams(location.search);
    const g = id => (document.getElementById(id) || {}).value || '';
    return {
      keyword: g('searchInp') || q.get('keyword') || '',
      categoryId: g('catSel'), brandId: g('brandSel'),
      minPrice: g('minP'), maxPrice: g('maxP'), sort: g('sortSel'), page, size
    };
  }
  async function loadCats() {
    try {
      const r = await App.get('/api/public/categories');
      const sel = document.getElementById('catSel'); if (!sel) return;
      (r.data || []).forEach(c => { const o = document.createElement('option'); o.value = c.id; o.textContent = c.name; sel.appendChild(o); });
      const q = new URLSearchParams(location.search).get('categoryId'); if (q) sel.value = q;
    } catch (e) { /* ignore */ }
  }
  async function loadBrands() {
    try {
      const r = await App.get('/api/public/brands');
      const sel = document.getElementById('brandSel'); if (!sel) return;
      (r.data || []).forEach(b => { const o = document.createElement('option'); o.value = b.id; o.textContent = b.name; sel.appendChild(o); });
    } catch (e) { /* ignore */ }
  }
  function card(p) {
    const out = p.currentStock <= 0;
    return `<div class="bg-white rounded-xl shadow hover:shadow-lg transition overflow-hidden flex flex-col">
      <a href="/product/${p.id}" class="h-44 overflow-hidden bg-slate-100">${App.img(p.imageUrl, p.name)}</a>
      <div class="p-3 flex-1 flex flex-col gap-1">
        <div class="text-xs text-slate-500">${App.escapeHtml(p.brandName || '')} • ${App.escapeHtml(p.sku || '')}</div>
        <a href="/product/${p.id}" class="font-semibold hover:text-orange-600 line-clamp-2">${App.escapeHtml(p.name)}</a>
        <div class="font-extrabold text-orange-600">${App.money(p.sellingPrice)}</div>
        <div>${App.badge(out ? 'OUT_OF_STOCK' : ((p.currentStock||0) <= (p.minStockLevel||5) ? 'LOW_STOCK' : 'IN_STOCK'))}</div>
        <div class="flex gap-2 mt-2">
          <button ${out ? 'disabled' : ''} onclick='Cart.add(${JSON.stringify({ id: p.id, name: p.name, sellingPrice: p.sellingPrice, imageUrl: p.imageUrl, currentStock: p.currentStock })},1)'
            class="flex-1 text-sm px-3 py-2 rounded-lg ${out ? 'bg-slate-200 text-slate-400' : 'bg-orange-500 text-white hover:bg-orange-600'}">Add to Cart</button>
          <a href="/product/${p.id}" class="text-sm px-3 py-2 rounded-lg bg-slate-100 hover:bg-slate-200">View</a>
        </div>
      </div></div>`;
  }
  async function load(p = 0) {
    page = p;
    const grid = document.getElementById('grid'); if (!grid) return;
    const f = params();
    const qs = new URLSearchParams();
    if (f.keyword) qs.set('keyword', f.keyword);
    if (f.categoryId) qs.set('categoryId', f.categoryId);
    if (f.brandId) qs.set('brandId', f.brandId);
    if (f.minPrice) qs.set('minPrice', f.minPrice);
    if (f.maxPrice) qs.set('maxPrice', f.maxPrice);
    if (f.sort) qs.set('sort', f.sort);
    qs.set('page', page); qs.set('size', size);
    grid.innerHTML = '<div class="col-span-full text-center py-10 text-slate-400">Loading...</div>';
    try {
      const r = await App.get('/api/public/products?' + qs.toString());
      const { items, page: pg } = App.unwrapPage(r.data);
      let list = items;
      const avail = (document.getElementById('availSel') || {}).value || '';
      if (avail === 'IN_STOCK') list = list.filter(x => (x.currentStock || 0) > 0);
      if (avail === 'OUT') list = list.filter(x => (x.currentStock || 0) <= 0);
      grid.innerHTML = list.length ? list.map(card).join('') : '<div class="col-span-full text-center py-10 text-slate-400">No products found.</div>';
      const pi = document.getElementById('pageInfo');
      if (pi && pg) pi.textContent = `Page ${(pg.number || 0) + 1} of ${pg.totalPages || 1} • ${pg.totalElements || 0} items`;
    } catch (e) { grid.innerHTML = '<div class="col-span-full text-center py-10 text-red-500">Failed to load products.</div>'; }
  }
  function loadPage(p) { load(p); }
  return { load, loadPage, loadCats, loadBrands };
})();
window.loadPage = p => Catalogue.loadPage(p);
