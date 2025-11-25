const { app, BrowserWindow } = require('electron')
const path = require('path')
const { spawn } = require('child_process')

function createWindow(url) {
  const win = new BrowserWindow({
    width: 1200,
    height: 800,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true
    }
  })
  win.maximize()
  win.loadURL(url)
}

const SERVER_PORT = process.env.PORT || 3000
const SERVER_URL = `http://localhost:${SERVER_PORT}`

app.whenReady().then(() => {
  // start server in this process
  const server = require(path.join(__dirname, 'server'))
  server.startServer(SERVER_PORT, () => {
    createWindow(SERVER_URL)
  })
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit()
})
