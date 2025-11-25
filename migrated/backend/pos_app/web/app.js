const api = {
  getProducts: () => fetch('/api/products').then(r=>r.json()),
  createProduct: (p) => fetch('/api/products',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(p)}).then(r=>r.json()),
  updateProduct: (id,p) => fetch(`/api/products/${id}`,{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify(p)}).then(r=>r.json()),
  deleteProduct: (id) => fetch(`/api/products/${id}`,{method:'DELETE'}).then(r=>r.json()),
  createSale: (items) => fetch('/api/sales',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({items})}).then(r=>r.json()),
  getSales: () => fetch('/api/sales').then(r=>r.json())
}

// navigation
const pages = { manage: document.getElementById('manage'), newSale: document.getElementById('newSale'), viewSales: document.getElementById('viewSales') }
document.getElementById('btn-manage').onclick = () => show('manage')
document.getElementById('btn-new').onclick = () => show('newSale')
document.getElementById('btn-sales').onclick = () => show('viewSales')
function show(name){ Object.values(pages).forEach(p=>p.style.display='none'); pages[name].style.display='block'; loadCurrent(name) }

// theme
const themeToggle = document.getElementById('themeToggle')
if(localStorage.getItem('theme')==='dark'){ document.documentElement.classList.add('dark'); themeToggle.checked=true }
themeToggle.addEventListener('change', ()=>{ if(themeToggle.checked){ document.documentElement.classList.add('dark'); localStorage.setItem('theme','dark') } else { document.documentElement.classList.remove('dark'); localStorage.setItem('theme','light') } })

// Manage products
const productsTableBody = document.querySelector('#productsTable tbody')
const productForm = document.getElementById('productForm')
const productId = document.getElementById('productId')
const productName = document.getElementById('productName')
const productPrice = document.getElementById('productPrice')
const productAmount = document.getElementById('productAmount')

async function loadCurrent(name){
  if(name==='manage') await loadProducts()
  if(name==='newSale') await loadProductsForSale()
  if(name==='viewSales') await loadSales()
}

async function loadProducts(){
  const list = await api.getProducts()
  productsTableBody.innerHTML = ''
  list.forEach(p=>{
    const tr = document.createElement('tr')
    tr.innerHTML = `<td>${p.id}</td><td>${p.name}</td><td>${p.price.toFixed(2)}</td><td>${p.amount}</td><td><button data-id="${p.id}" class="edit">Edit</button> <button data-id="${p.id}" class="del">Delete</button></td>`
    productsTableBody.appendChild(tr)
  })
  document.querySelectorAll('.edit').forEach(b=>b.onclick = async (e)=>{
    const id = e.target.dataset.id
    const prod = (await api.getProducts()).find(x=>x.id==id)
    productId.value = prod.id
    productName.value = prod.name
    productPrice.value = prod.price
    productAmount.value = prod.amount
  })
  document.querySelectorAll('.del').forEach(b=>b.onclick = async (e)=>{
    if(!confirm('Delete product?')) return
    await api.deleteProduct(e.target.dataset.id)
    loadProducts()
  })
}

productForm.onsubmit = async (e)=>{
  e.preventDefault()
  const id = productId.value
  const body = { name: productName.value, price: Number(productPrice.value), amount: Number(productAmount.value) }
  if(id){ await api.updateProduct(id, body) } else { await api.createProduct(body) }
  productId.value=''
  productForm.reset()
  loadProducts()
}

document.getElementById('cancelEdit').onclick = ()=>{ productId.value=''; productForm.reset(); }

// New Sale
let productsCache = []
let cart = []
async function loadProductsForSale(){
  productsCache = await api.getProducts()
  const container = document.getElementById('productsList')
  container.innerHTML = ''
  productsCache.forEach(p=>{
    const div = document.createElement('div')
    div.className = 'productItem'
    div.innerHTML = `<div><strong>${p.name}</strong> ₱${p.price.toFixed(2)} (<span class="stock">${p.amount}</span>)</div><div><input type="number" min="1" max="${p.amount}" value="1" style="width:60px"> <button class="addBtn" data-id="${p.id}">Add</button></div>`
    container.appendChild(div)
  })
  container.querySelectorAll('.addBtn').forEach(btn=>{
    btn.onclick = (e)=>{
      const id = Number(e.target.dataset.id)
      const input = e.target.previousElementSibling
      const qty = Number(input.value)
      const prod = productsCache.find(x=>x.id===id)
      if(qty < 1 || qty > prod.amount){ alert('Invalid quantity'); return }
      addToCart({ product_id: id, qty, price: prod.price, name: prod.name })
    }
  })
}

function addToCart(item){
  const existing = cart.find(c=>c.product_id===item.product_id)
  if(existing){ existing.qty += item.qty } else { cart.push(item) }
  renderCart()
}

function renderCart(){
  const container = document.getElementById('cart')
  container.innerHTML = ''
  let total = 0
  cart.forEach((it, idx)=>{
    const div = document.createElement('div')
    const subtotal = (it.qty * it.price)
    total += subtotal
    div.innerHTML = `<span>${it.name} ₱${it.price.toFixed(2)} x <button class="minus" data-idx="${idx}">-</button> ${it.qty} <button class="plus" data-idx="${idx}">+</button> = ₱${subtotal.toFixed(2)} <button class="remove" data-idx="${idx}">Remove</button></span>`
    container.appendChild(div)
  })
  document.querySelector('#totalValue').textContent = total.toFixed(2)
  container.querySelectorAll('.plus').forEach(b=>b.onclick = (e)=>{ const i = e.target.dataset.idx; const it = cart[i]; const prod = productsCache.find(p=>p.id===it.product_id); if(it.qty < prod.amount){ it.qty++; renderCart() } else alert('Not enough stock') })
  container.querySelectorAll('.minus').forEach(b=>b.onclick = (e)=>{ const i = e.target.dataset.idx; const it = cart[i]; if(it.qty>1){ it.qty--; renderCart() } })
  container.querySelectorAll('.remove').forEach(b=>b.onclick = (e)=>{ cart.splice(e.target.dataset.idx,1); renderCart() })
}

document.getElementById('finalizeSale').onclick = async ()=>{
  if(cart.length===0){ alert('Cart empty'); return }
  // validate stock
  const invalid = cart.filter(it => {
    const prod = productsCache.find(p=>p.id===it.product_id)
    return !prod || it.qty > prod.amount
  })
  if(invalid.length) { alert('One or more items exceed stock'); return }
  const res = await api.createSale(cart)
  if(res && res.success){ alert('Sale completed'); cart = []; loadProductsForSale(); show('manage'); } else if(res && !res.success && res.outOfStock){ alert('Out of stock: '+JSON.stringify(res.outOfStock)); }
}

// Sales view
async function loadSales(){
  const rows = await api.getSales()
  const c = document.getElementById('salesList')
  c.innerHTML = ''
  rows.forEach(r=>{
    const d = document.createElement('div')
    d.textContent = `#${r.id} ${r.created_at || ''} ₱${r.total.toFixed(2)}`
    c.appendChild(d)
  })
}

// initial
show('manage')
