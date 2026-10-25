'use strict';
document.querySelectorAll('form[data-confirm]').forEach(form => form.addEventListener('submit', event => {
  if (!window.confirm(form.dataset.confirm)) event.preventDefault();
}));
const search = document.querySelector('[data-table-search]');
if (search) search.addEventListener('input', () => {
  const rows = [...document.querySelectorAll('table[data-searchable] tbody tr')];
  const term = search.value.toLocaleLowerCase('fr');
  rows.forEach(row => { row.hidden = !row.textContent.toLocaleLowerCase('fr').includes(term); });
  document.querySelector('.search-empty').hidden = rows.some(row => !row.hidden);
});
const order = document.querySelector('[data-order-form]');
if (order) {
  const body = order.querySelector('[data-lines]');
  const money = new Intl.NumberFormat('fr-MA', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  // Le navigateur donne une estimation ; BigDecimal côté serveur fait foi.
  const refresh = () => {
    let total = 0;
    body.querySelectorAll('[data-line]').forEach(row => {
      const select = row.querySelector('[data-product]');
      const original = row.dataset.originalPrice;
      const price = select.value === row.dataset.originalProduct && original ? Number(original) : Number(select.selectedOptions[0]?.dataset.price || 0);
      const quantity = Number(row.querySelector('[data-quantity]').value || 0);
      const subtotal = price * quantity;
      row.querySelector('[data-price-display]').textContent = money.format(price);
      row.querySelector('[data-subtotal]').textContent = money.format(subtotal);
      total += subtotal;
    });
    order.querySelector('[data-order-total]').textContent = money.format(total);
  };
  const add = () => { if (body.children.length >= 50) return; body.append(document.querySelector('#line-template').content.cloneNode(true)); refresh(); };
  order.querySelector('[data-add-line]').addEventListener('click', add);
  body.addEventListener('input', refresh); body.addEventListener('change', refresh);
  body.addEventListener('click', event => { if (event.target.closest('[data-remove-line]')) { event.target.closest('[data-line]').remove(); if (!body.children.length) add(); refresh(); } });
  if (!body.children.length) add();
  order.addEventListener('submit', event => {
    const selections = [...body.querySelectorAll('[data-product]')].map(select => select.value);
    if (new Set(selections).size !== selections.length) { event.preventDefault(); window.alert('Chaque produit ne peut apparaître qu’une fois. Ajustez sa quantité.'); }
  });
  refresh();
}
