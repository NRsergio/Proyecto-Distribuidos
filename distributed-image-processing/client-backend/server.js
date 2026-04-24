/**
 * Backend Intermedio - Express.js
 * Comunica cliente web con App-Server JAVA
 *
 * Flujo:
 * Cliente Web (React) → Backend Express REST → App-Server SOAP
 */

import 'dotenv/config'
import express from 'express'
import cors from 'cors'
import authRoutes from './src/routes/auth.js'
import batchRoutes from './src/routes/batch.js'
import uploadRoutes from './src/routes/upload.js'

const app = express()
const PORT = process.env.BACKEND_PORT || 3001

// ═══════════════════════════════════════════════════════════════
// MIDDLEWARE
// ═══════════════════════════════════════════════════════════════

app.use(express.json())
app.use(express.urlencoded({ extended: true }))
app.use(
  cors({
    origin: ['http://localhost:3000', 'http://localhost:5173'], // Vite dev server
    credentials: true
  })
)

// Logging middleware
app.use((req, res, next) => {
  console.log(
    `[${new Date().toISOString()}] ${req.method} ${req.path} - IP: ${req.ip}`
  )
  next()
})

// ═══════════════════════════════════════════════════════════════
// RUTAS
// ═══════════════════════════════════════════════════════════════

// Health check
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    javaAppServer: process.env.JAVA_APP_SERVER_URL
  })
})

// API v1 routes
app.use('/api/auth', authRoutes)
app.use('/api/batch', batchRoutes)
app.use('/api/upload', uploadRoutes)

// ═══════════════════════════════════════════════════════════════
// ERROR HANDLING
// ═══════════════════════════════════════════════════════════════

// 404 - Not Found
app.use((req, res) => {
  res.status(404).json({
    success: false,
    error: 'Ruta no encontrada',
    path: req.path
  })
})

// Error global handler
app.use((err, req, res, next) => {
  console.error('Error:', err)
  res.status(500).json({
    success: false,
    error: err.message || 'Error interno del servidor'
  })
})

// ═══════════════════════════════════════════════════════════════
// INICIAR SERVIDOR
// ═══════════════════════════════════════════════════════════════

const server = app.listen(PORT, () => {
  const config = {
    'Backend Puerto': PORT,
    'App-Server JAVA': process.env.JAVA_APP_SERVER_URL || 'http://localhost:8080',
    'Cliente Web': 'http://localhost:3000 (Vite: http://localhost:5173)',
    'Upload Dir': process.env.UPLOAD_DIR || './uploads'
  }

  console.log('\n════════════════════════════════════════════════════════════')
  console.log('Backend Intermedio - Express.js')
  console.log('════════════════════════════════════════════════════════════')
  Object.entries(config).forEach(([key, value]) => {
    console.log(`${key.padEnd(20)}: ${value}`)
  })
  console.log('════════════════════════════════════════════════════════════\n')

  console.log(`✅ Servidor escuchando en http://localhost:${PORT}`)
  console.log(`📡 API disponible en http://localhost:${PORT}/api`)
  console.log(`🏥 Health check en http://localhost:${PORT}/health\n`)
})

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('SIGTERM recibido, cerrando servidor...')
  server.close(() => {
    console.log('Servidor cerrado')
    process.exit(0)
  })
})

export default app
