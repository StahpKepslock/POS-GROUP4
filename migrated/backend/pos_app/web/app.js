document.addEventListener('DOMContentLoaded', () => {
    const api = {
        getProducts: () => fetch('/api/products').then(r => r.json()),
        createProduct: (p) => fetch('/api/products', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(p) }).then(r => r.json()),
        updateProduct: (id, p) => fetch(`/api/products/${id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(p) }).then(r => r.json()),
        deleteProduct: (id) => fetch(`/api/products/${id}`, { method: 'DELETE' }).then(r => r.json()),
        createSale: (items) => fetch('/api/sales', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ items }) }).then(r => r.json()),
        getSales: () => fetch('/api/sales').then(r => r.json())
    };

    // Sound Effects
    const sounds = {
        click: new Audio('/sfx/click.mp3'),
        sale: new Audio('/sfx/sale.mp3'),
        ping: new Audio('/sfx/ping.mp3')
    };
    
    function playSound(sound) {
        sounds[sound].currentTime = 0;
        sounds[sound].play();
    }
    
    // Navigation
    const pages = {
        manage: document.getElementById('manage'),
        newSale: document.getElementById('newSale'),
        viewSales: document.getElementById('viewSales')
    };
    document.getElementById('btn-manage').addEventListener('click', () => { playSound('click'); show('manage'); });
    document.getElementById('btn-new').addEventListener('click', () => { playSound('click'); show('newSale'); });
    document.getElementById('btn-sales').addEventListener('click', () => { playSound('click'); show('viewSales'); });
    document.getElementById('btn-notifications').addEventListener('click', () => { playSound('ping'); showNotifications(); });

    function show(pageName) {
        Object.values(pages).forEach(p => p.style.display = 'none');
        pages[pageName].style.display = 'block';
        loadCurrent(pageName);
    }

    // Theme toggle
    const themeToggle = document.getElementById('themeToggle');
    if (localStorage.getItem('theme') === 'dark') {
        document.documentElement.classList.add('dark');
        themeToggle.checked = true;
    }
    themeToggle.addEventListener('change', () => {
        playSound('click');
        if (themeToggle.checked) {
            document.documentElement.classList.add('dark');
            localStorage.setItem('theme', 'dark');
        } else {
            document.documentElement.classList.remove('dark');
            localStorage.setItem('theme', 'light');
        }
    });

    // Product Management
    const productsTableBody = document.querySelector('#productsTable tbody');
    const productForm = document.getElementById('productForm');
    const productIdField = document.getElementById('productId');
    const productNameField = document.getElementById('productName');
    const productPriceField = document.getElementById('productPrice');
    const productAmountField = document.getElementById('productAmount');
    let productsCache = [];

    async function loadProducts() {
        productsCache = await api.getProducts();
        renderProductsTable();
        checkLowStock();
    }

    function renderProductsTable() {
        productsTableBody.innerHTML = '';
        productsCache.forEach(p => {
            const tr = document.createElement('tr');
            tr.innerHTML = `<td>${p.id}</td><td>${p.name}</td><td>${p.price.toFixed(2)}</td><td>${p.amount}</td><td><button data-id="${p.id}" class="edit">Edit</button> <button data-id="${p.id}" class="del">Delete</button></td>`;
            productsTableBody.appendChild(tr);
        });
    }

    function showEditModal(product) {
        const overlay = document.createElement('div');
        overlay.className = 'modal-overlay';
        
        const modal = document.createElement('div');
        modal.className = 'modal-content';
        
        modal.innerHTML = `
            <h3>Edit Product</h3>
            <div class="form-group">
                <label>Name:</label>
                <input type="text" id="edit-name" value="${product.name}">
            </div>
            <div class="form-group">
                <label>Price:</label>
                <input type="number" step="0.01" id="edit-price" value="${product.price}">
            </div>
            <div class="form-group">
                <label>Amount:</label>
                <input type="number" id="edit-amount" value="${product.amount}">
            </div>
            <div class="modal-actions">
                <button id="save-edit">Save</button>
                <button id="cancel-edit-modal">Cancel</button>
            </div>
        `;
        
        overlay.appendChild(modal);
        document.body.appendChild(overlay);
    
        document.getElementById('save-edit').addEventListener('click', async () => {
            playSound('click');
            const productData = {
                name: document.getElementById('edit-name').value,
                price: parseFloat(document.getElementById('edit-price').value),
                amount: parseInt(document.getElementById('edit-amount').value)
            };
    
            if (productData.name && !isNaN(productData.price) && !isNaN(productData.amount)) {
                await api.updateProduct(product.id, productData);
                document.body.removeChild(overlay);
                await loadProducts();
            } else {
                alert('Please fill all fields with valid data.');
            }
        });
    
        document.getElementById('cancel-edit-modal').addEventListener('click', () => {
            playSound('click');
            document.body.removeChild(overlay);
        });
    }
    
    function showInfoModal(message, onOk) {
        const overlay = document.createElement('div');
        overlay.className = 'modal-overlay';
        
        const modal = document.createElement('div');
        modal.className = 'modal-content';
        
        modal.innerHTML = `
            <p>${message}</p>
            <div class="modal-actions">
                <button id="ok-action">OK</button>
            </div>
        `;
        
        overlay.appendChild(modal);
        document.body.appendChild(overlay);
    
        document.getElementById('ok-action').addEventListener('click', () => {
            playSound('click');
            if (onOk) onOk();
            document.body.removeChild(overlay);
        });
    }

    function showConfirmModal(message, onConfirm) {
        const overlay = document.createElement('div');
        overlay.className = 'modal-overlay';
        
        const modal = document.createElement('div');
        modal.className = 'modal-content';
        
        modal.innerHTML = `
            <p>${message}</p>
            <div class="modal-actions">
                <button id="confirm-action">Confirm</button>
                <button id="cancel-action">Cancel</button>
            </div>
        `;
        
        overlay.appendChild(modal);
        document.body.appendChild(overlay);
    
        document.getElementById('confirm-action').addEventListener('click', () => {
            playSound('click');
            onConfirm();
            document.body.removeChild(overlay);
        });
    
        document.getElementById('cancel-action').addEventListener('click', () => {
            playSound('click');
            document.body.removeChild(overlay);
        });
    }
    
    productsTableBody.addEventListener('click', (e) => {
        const target = e.target;
        const id = target.dataset.id;
    
        if (target.classList.contains('edit')) {
            playSound('click');
            const product = productsCache.find(p => p.id == id);
            if (product) {
                showEditModal(product);
            }
        } else if (target.classList.contains('del')) {
            playSound('click');
            showConfirmModal('Are you sure you want to delete this product?', async () => {
                await api.deleteProduct(id);
                await loadProducts();
                productForm.reset();
            });
        }
    });

    productForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        playSound('click');
        const productData = {
            name: productNameField.value,
            price: parseFloat(productPriceField.value),
            amount: parseInt(productAmountField.value)
        };

        await api.createProduct(productData);

        productForm.reset();
        await loadProducts();
    });

    document.getElementById('cancelEdit').addEventListener('click', () => {
        playSound('click');
        productForm.reset();
        productIdField.value = '';
    });

    // New Sale
    const productsListDiv = document.getElementById('productsList');
    const cartDiv = document.getElementById('cart');
    const totalValueSpan = document.getElementById('totalValue');
    let cart = [];

    async function loadProductsForSale() {
        if (productsCache.length === 0) {
            productsCache = await api.getProducts();
        }
        renderProductsForSale();
    }

    function renderProductsForSale() {
        productsListDiv.innerHTML = '';
        productsCache.forEach(p => {
            const div = document.createElement('div');
            div.className = 'productItem';
            div.innerHTML = `<div><strong>${p.name}</strong> ₱${p.price.toFixed(2)} (<span class="stock">${p.amount}</span>)</div><div><input type="number" min="1" max="${p.amount}" value="1" style="width:60px"> <button class="addBtn" data-id="${p.id}">Add</button></div>`;
            productsListDiv.appendChild(div);
        });
    }

    productsListDiv.addEventListener('click', (e) => {
        if (e.target.classList.contains('addBtn')) {
            playSound('click');
            const id = Number(e.target.dataset.id);
            const input = e.target.previousElementSibling;
            const qty = Number(input.value);
            const prod = productsCache.find(x => x.id === id);
            if (prod && qty >= 1 && qty <= prod.amount) {
                addToCart({ product_id: id, qty, price: prod.price, name: prod.name });
            } else {
                alert('Invalid quantity or product not found');
            }
        }
    });

    function addToCart(item) {
        const existing = cart.find(c => c.product_id === item.product_id);
        if (existing) {
            existing.qty += item.qty;
        } else {
            cart.push(item);
        }
        renderCart();
    }

    function renderCart() {
        cartDiv.innerHTML = '';
        let total = 0;
        cart.forEach((it, idx) => {
            const div = document.createElement('div');
            const subtotal = it.qty * it.price;
            total += subtotal;
            div.innerHTML = `<span>${it.name} ₱${it.price.toFixed(2)} x <button class="minus" data-idx="${idx}">-</button> ${it.qty} <button class="plus" data-idx="${idx}">+</button> = ₱${subtotal.toFixed(2)} <button class="remove" data-idx="${idx}">Remove</button></span>`;
            cartDiv.appendChild(div);
        });
        totalValueSpan.textContent = total.toFixed(2);
    }

    cartDiv.addEventListener('click', (e) => {
        const idx = e.target.dataset.idx;
        if (!cart[idx]) return;
        playSound('click');
    
        const item = cart[idx];
        const product = productsCache.find(p => p.id === item.product_id);
    
        if (e.target.classList.contains('plus')) {
            if (product && item.qty < product.amount) {
                item.qty++;
                renderCart();
            } else {
                alert('Not enough stock');
            }
        } else if (e.target.classList.contains('minus')) {
            if (item.qty > 1) {
                item.qty--;
                renderCart();
            }
        } else if (e.target.classList.contains('remove')) {
            cart.splice(idx, 1);
            renderCart();
        }
    });

    document.getElementById('finalizeSale').addEventListener('click', async () => {
        if (cart.length === 0) {
            alert('Cart is empty');
            return;
        }
        const outOfStockItems = cart.filter(item => {
            const product = productsCache.find(p => p.id === item.product_id);
            return !product || item.qty > product.amount;
        });

        if (outOfStockItems.length > 0) {
            alert('One or more items in your cart exceed the available stock.');
            return;
        }

        const sale = await api.createSale(cart);
        if (sale && sale.success) {
            playSound('sale');
            showInfoModal('Sale completed successfully!', () => {
                cart = [];
                renderCart();
                show('manage');
            });
        } else {
            alert('Failed to complete sale. ' + (sale.message || ''));
        }
    });

    // Sales View
    async function loadSales() {
        const salesList = document.getElementById('salesList');
        salesList.innerHTML = '';
        const sales = await api.getSales();
        sales.forEach(sale => {
            const saleDiv = document.createElement('div');
            saleDiv.className = 'sale-record';
    
            const saleHeader = document.createElement('div');
            saleHeader.className = 'sale-header';
            saleHeader.innerHTML = `
                <strong>Sale #${sale.id}</strong> - 
                <span>${new Date(sale.created_at).toLocaleString()}</span> - 
                <strong>Total: ₱${sale.total.toFixed(2)}</strong>
            `;
            saleDiv.appendChild(saleHeader);
    
            const itemsTable = document.createElement('table');
            itemsTable.className = 'sale-items-table';
            itemsTable.innerHTML = `
                <thead>
                    <tr>
                        <th>Product ID</th>
                        <th>Product Name</th>
                        <th>Quantity</th>
                        <th>Price at Sale</th>
                        <th>Subtotal</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            `;
            const itemsTbody = itemsTable.querySelector('tbody');
    
            sale.items.forEach(item => {
                const itemRow = document.createElement('tr');
                const subtotal = item.qty * item.price_at_sale;
                itemRow.innerHTML = `
                    <td>${item.product_id}</td>
                    <td>${item.product_name}</td>
                    <td>${item.qty}</td>
                    <td>₱${item.price_at_sale.toFixed(2)}</td>
                    <td>₱${subtotal.toFixed(2)}</td>
                `;
                itemsTbody.appendChild(itemRow);
            });
    
            saleDiv.appendChild(itemsTable);
            salesList.appendChild(saleDiv);
        });
    }

    // Notifications
    const notificationsModal = document.getElementById('notifications-modal');
    const notificationsList = document.getElementById('notifications-list');
    const notificationBadge = document.getElementById('notification-badge');
    let lowStockNotifications = [];

    function checkLowStock() {
        const newLowStock = productsCache.filter(p => p.amount < 3);
        if (newLowStock.length > lowStockNotifications.length) {
            playSound('ping');
        }
        lowStockNotifications = newLowStock;
        updateNotificationBadge();
        renderNotifications();
    }

    function updateNotificationBadge() {
        if (lowStockNotifications.length > 0) {
            notificationBadge.textContent = lowStockNotifications.length;
            notificationBadge.style.display = 'inline';
        } else {
            notificationBadge.style.display = 'none';
        }
    }

    function renderNotifications() {
        notificationsList.innerHTML = '';
        if (lowStockNotifications.length === 0) {
            notificationsList.innerHTML = '<p>No low stock alerts.</p>';
            return;
        }
        lowStockNotifications.forEach(p => {
            const item = document.createElement('div');
            item.className = 'notification-item';
            item.textContent = `Product "${p.name}" (ID: ${p.id}) is low on stock: ${p.amount} left.`;
            notificationsList.appendChild(item);
        });
    }

    function showNotifications() {
        renderNotifications();
        notificationsModal.style.display = 'flex';
    }

    document.getElementById('close-notifications-modal').addEventListener('click', () => {
        playSound('click');
        notificationsModal.style.display = 'none';
    });

    
    function loadCurrent(pageName) {
        if (pageName === 'manage') loadProducts();
        if (pageName === 'newSale') loadProductsForSale();
        if (pageName === 'viewSales') loadSales();
    }

    // Initial load
    show('manage');
});