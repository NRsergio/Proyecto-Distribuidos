/**
 * Rutas de Lotes de Procesamiento
 * POST /api/batch/enviar - Envía lote de imagenes
 * GET /api/batch/progreso/:idLote - Consulta progreso
 */

import express from 'express'
import * as soapService from '../services/soapService.js'
import { verificarToken } from '../middleware/authMiddleware.js'

const router = express.Router()

/**
 * POST /api/batch/enviar
 * Envía lote de imágenes con transformaciones
 * Body: { token, imagenes: [{nombreArchivo, rutaOriginal, transformaciones}] }
 */
router.post('/enviar', verificarToken, async (req, res) => {
  try {
    const { token, imagenes } = req.body

    if (!imagenes || !Array.isArray(imagenes) || imagenes.length === 0) {
      return res.status(400).json({
        success: false,
        error: 'Se requiere array de imágenes'
      })
    }

    // Validar que cada imagen tenga campos requeridos
    for (const img of imagenes) {
      if (!img.nombreArchivo || !img.rutaOriginal) {
        return res.status(400).json({
          success: false,
          error: 'Cada imagen debe tener nombreArchivo y rutaOriginal'
        })
      }
      if (!img.transformaciones) {
        img.transformaciones = []
      }
    }

    const resultado = await soapService.enviarLote(token, imagenes)

    return res.json({
      success: true,
      data: resultado
    })
  } catch (error) {
    console.error('Error en enviarLote:', error.message)
    return res.status(500).json({
      success: false,
      error: error.message
    })
  }
})

/**
 * GET /api/batch/progreso/:idLote
 * Consulta el progreso de un lote
 * Headers: Authorization: Bearer <token>
 */
router.get('/progreso/:idLote', verificarToken, async (req, res) => {
  try {
    const { idLote } = req.params
    const token = req.headers.authorization?.replace('Bearer ', '')

    if (!token) {
      return res.status(401).json({
        success: false,
        error: 'Token requerido'
      })
    }

    if (!idLote) {
      return res.status(400).json({
        success: false,
        error: 'ID de lote requerido'
      })
    }

    const resultado = await soapService.consultarProgreso(token, idLote)

    return res.json({
      success: true,
      data: resultado
    })
  } catch (error) {
    console.error('Error en consultarProgreso:', error.message)
    return res.status(500).json({
      success: false,
      error: error.message
    })
  }
})

export default router
