/* Admin dashboard charts: GET /api/reports/dashboard/admin */
window.Dashboard = (() => {
  let charts = [];
  async function loadAdmin() {
    let d;
    try { const r = await App.get('/api/reports/dashboard/admin'); d = r.data || {}; }
    catch (e) { App.toast('Dashboard load failed', 'error'); return; }
    const set = (id, v) => { const el = document.getElementById(id); if (el) el.textContent = v; };
    set('stProducts', d.totalProducts ?? '—');
    set('stStockVal', App.money(d.stockValue));
    set('stLow', d.lowStockCount ?? '—');
    set('stOut', d.outOfStock ?? '—');
    set('stTodaySales', d.todaySales ?? 0);
    set('stTodayRev', App.money(d.todayRevenue));
    set('stMonthRev', App.money(d.monthlyRevenue));
    set('stPO', d.pendingPOs ?? 0);
    set('stReq', d.pendingRequests ?? 0);
    charts.forEach(c => c.destroy()); charts = [];
    // daily sales bar
    const ds = d.dailySales || [];
    const c1 = document.getElementById('chDaily');
    if (c1) charts.push(new Chart(c1, { type: 'bar',
      data: { labels: ds.map(x => x.date), datasets: [{ label: 'Revenue (Rs.)', data: ds.map(x => Number(x.revenue || 0)), backgroundColor: '#f97316' }] },
      options: { responsive: true, plugins: { legend: { display: false } } } }));
    // payment pie
    const pb = d.paymentBreakdown || {};
    const c2 = document.getElementById('chPay');
    if (c2) charts.push(new Chart(c2, { type: 'doughnut',
      data: { labels: Object.keys(pb), datasets: [{ data: Object.values(pb).map(Number), backgroundColor: ['#f97316', '#0ea5e9', '#22c55e', '#a855f7'] }] },
      options: { responsive: true } }));
    // monthly trend (reuse daily as line)
    const c3 = document.getElementById('chTrend');
    if (c3) charts.push(new Chart(c3, { type: 'line',
      data: { labels: ds.map(x => x.date), datasets: [{ label: 'Orders', data: ds.map(x => x.count || 0), borderColor: '#0ea5e9', tension: .3 }] },
      options: { responsive: true } }));
    // top products via low-stock? use popular API as proxy
    try {
      const pr = await App.get('/api/public/products/popular');
      const tb = document.getElementById('topProducts');
      if (tb) tb.innerHTML = (pr.data || []).slice(0, 8).map(p =>
        `<tr class="border-t"><td class="py-2">${App.escapeHtml(p.name)}</td><td>${App.escapeHtml(p.sku || '')}</td><td class="text-right font-semibold">${App.money(p.sellingPrice)}</td><td class="text-right">${p.currentStock ?? 0}</td></tr>`).join('')
        || '<tr><td colspan="4" class="py-4 text-center text-slate-400">No data</td></tr>';
    } catch (e) { /* ignore */ }
  }
  return { loadAdmin };
})();
