/**
 * Rutas de Autenticación
 * POST /api/auth/login - Login
 * POST /api/auth/register - Registro
 */

import express from 'express'
import * as soapService from '../services/soapService.js'

const router = express.Router()

/**
 * POST /api/auth/login
 * Autentica usuario y devuelve token JWT
 */
router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body

    if (!email || !password) {
      return res.status(400).json({
        success: false,
        error: 'Email y password son requeridos'
      })
    }

    const resultado = await soapService.login(email, password)

    return res.json({
      success: true,
      data: resultado
    })
  } catch (error) {
    console.error('Error en login:', error.message)
    return res.status(401).json({
      success: false,
      error: error.message
    })
  }
})

/**
 * POST /api/auth/register
 * Registra nuevo usuario
 */
router.post('/register', async (req, res) => {
  try {
    const { email, password, nombre } = req.body

    if (!email || !password || !nombre) {
      return res.status(400).json({
        success: false,
        error: 'Email, password y nombre son requeridos'
      })
    }

    const resultado = await soapService.register(email, password, nombre)

    return res.json({
      success: true,
      data: resultado
    })
  } catch (error) {
    console.error('Error en register:', error.message)
    return res.status(400).json({
      success: false,
      error: error.message
    })
  }
})

export default router
