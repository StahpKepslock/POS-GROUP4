const express = require('express')
const path = require('path')
const cors = require('cors')
const db = require('./db')

const app = express()
app.use(cors())
app.use(express.json())

const WEB_DIR = path.join(__dirname, 'web')
const ROOT_DIR = path.join(__dirname, '..', '..', '..', '..', 'POSmain')
app.use(express.static(WEB_DIR))
app.use('/sfx', express.static(ROOT_DIR))


app.get('/api/products', (req, res) => {
  db.allProducts((err, rows) => {
    if (err) return res.status(500).json({ error: err.message })
    res.json(rows)
  })
})

app.post('/api/products', (req, res) => {
  const { name, price, amount } = req.body
  db.addProduct({ name, price: Number(price), amount: Number(amount) }, (err, result) => {
    if (err) return res.status(500).json({ error: err.message })
    const id = result.id
    db.getProduct(id, (e, row) => res.json(row))
  })
})

app.put('/api/products/:id', (req, res) => {
  const id = Number(req.params.id)
  const { name, price, amount } = req.body
  db.updateProduct(id, { name, price: Number(price), amount: Number(amount) }, (err) => {
    if (err) return res.status(500).json({ error: err.message })
    db.getProduct(id, (e, row) => res.json(row))
  })
})

app.delete('/api/products/:id', (req, res) => {
  const id = Number(req.params.id)
  db.removeProduct(id, (err) => {
    if (err) return res.status(500).json({ error: err.message })
    res.json({ success: true })
  })
})

app.get('/api/sales', (req, res) => {
  db.allSales((err, rows) => {
    if (err) return res.status(500).json({ error: err.message })
    res.json(rows)
  })
})

app.post('/api/sales', (req, res) => {
  const { items } = req.body // items: [{product_id, qty, price}]
  db.createSale(items, (err, result) => {
    if (err) return res.status(500).json({ error: err.message })
    res.json(result)
  })
})

function startServer(port = 3000, cb) {
  db.init()
  const server = app.listen(port, () => {
    console.log('Server listening on', port)
    if (cb) cb()
  })
  return server
}

module.exports = { startServer }