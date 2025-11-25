const sqlite3 = require('sqlite3').verbose()
const path = require('path')
const dbFile = path.join(__dirname, 'pos.db')

let db

function init() {
  db = new sqlite3.Database(dbFile)
  db.serialize(() => {
    db.run(`CREATE TABLE IF NOT EXISTS products (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      price REAL NOT NULL DEFAULT 0,
      amount INTEGER NOT NULL DEFAULT 0
    )`)

    db.run(`CREATE TABLE IF NOT EXISTS sales (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      total REAL NOT NULL
    )`)

    db.run(`CREATE TABLE IF NOT EXISTS sale_items (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      sale_id INTEGER NOT NULL,
      product_id INTEGER NOT NULL,
      qty INTEGER NOT NULL,
      price_at_sale REAL NOT NULL,
      FOREIGN KEY(sale_id) REFERENCES sales(id),
      FOREIGN KEY(product_id) REFERENCES products(id)
    )`)
  })
}

function allProducts(cb) {
  db.all('SELECT * FROM products ORDER BY id', cb)
}

function getProduct(id, cb) {
  db.get('SELECT * FROM products WHERE id = ?', [id], cb)
}

function addProduct({ name, price, amount }, cb) {
  const stmt = db.prepare('INSERT INTO products (name, price, amount) VALUES (?, ?, ?)')
  stmt.run([name, price, amount], function (err) {
    if (err) return cb(err)
    cb(null, { id: this.lastID })
  })
}

function updateProduct(id, { name, price, amount }, cb) {
  db.run('UPDATE products SET name = ?, price = ?, amount = ? WHERE id = ?', [name, price, amount, id], function (err) {
    cb(err, { changes: this.changes })
  })
}

function removeProduct(id, cb) {
  db.run('DELETE FROM products WHERE id = ?', [id], function (err) {
    cb(err, { changes: this.changes })
  })
}

function allSales(cb) {
  db.all('SELECT * FROM sales ORDER BY id', cb)
}

function createSale(cartItems, cb) {
  // cartItems: [{product_id, qty, price}]
  db.serialize(() => {
    const total = cartItems.reduce((s, it) => s + it.qty * it.price, 0)
    db.run('BEGIN TRANSACTION')
    db.run('INSERT INTO sales (total) VALUES (?)', [total], function (err) {
      if (err) return db.run('ROLLBACK', () => cb(err))
      const saleId = this.lastID
      const insertItem = db.prepare('INSERT INTO sale_items (sale_id, product_id, qty, price_at_sale) VALUES (?, ?, ?, ?)')

      let outOfStock = []
      let processed = 0

      cartItems.forEach(item => {
        // check quantity
        db.get('SELECT amount FROM products WHERE id = ?', [item.product_id], (err, row) => {
          if (err) return db.run('ROLLBACK', () => cb(err))
          const available = row ? row.amount : 0
          if (item.qty > available) {
            outOfStock.push({ product_id: item.product_id, available })
            processed++
            if (processed === cartItems.length) {
              db.run('ROLLBACK', () => cb(null, { success: false, outOfStock }))
            }
            return
          }

          insertItem.run([saleId, item.product_id, item.qty, item.price], (err) => {
            if (err) return db.run('ROLLBACK', () => cb(err))
            db.run('UPDATE products SET amount = amount - ? WHERE id = ?', [item.qty, item.product_id], (err2) => {
              if (err2) return db.run('ROLLBACK', () => cb(err2))
              processed++
              if (processed === cartItems.length) {
                insertItem.finalize(() => {
                  db.run('COMMIT', (commitErr) => {
                    if (commitErr) return cb(commitErr)
                    cb(null, { success: true, saleId })
                  })
                })
              }
            })
          })
        })
      })
    })
  })
}

module.exports = { init, allProducts, getProduct, addProduct, updateProduct, removeProduct, allSales, createSale }
