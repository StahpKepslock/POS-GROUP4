POS App (Electron + Express + SQLite)

Run locally (Windows):

1) Install dependencies (from `migrated/backend/pos_app`):

```powershell
npm install
```

2) Run server only:

```powershell
npm run start
```

3) Run with Electron (if installed globally or via dev dependencies):

```powershell
npm run electron
```

API:
- `GET /api/products`
- `POST /api/products` {name,price,amount}
- `PUT /api/products/:id`
- `DELETE /api/products/:id`
- `GET /api/sales`
- `POST /api/sales` { items: [{product_id,qty,price}] }
