/**
 * Rutas de Subida de Archivos
 * POST /api/upload - Sube archivos de imagen
 */

import express from 'express'
import multer from 'multer'
import * as fileService from '../services/fileService.js'
import { verificarToken } from '../middleware/authMiddleware.js'

const router = express.Router()

// Configurar multer para subida en memoria
const upload = multer({
  storage: multer.memoryStorage(),
  limits: {
    fileSize: 20 * 1024 * 1024 // 20MB
  },
  fileFilter: (req, file, cb) => {
    // Validar solo imágenes
    if (file.mimetype.startsWith('image/')) {
      cb(null, true)
    } else {
      cb(new Error('Solo se permiten archivos de imagen'))
    }
  }
})

/**
 * POST /api/upload
 * Sube una o más imágenes
 * Multipart form-data con campo 'imagenes'
 */
router.post('/', verificarToken, upload.array('imagenes', 10), (req, res) => {
  try {
    if (!req.files || req.files.length === 0) {
      return res.status(400).json({
        success: false,
        error: 'No se han subido archivos'
      })
    }

    const archivosGuardados = fileService.guardarArchivos(req.files)

    return res.json({
      success: true,
      data: {
        cantidad: archivosGuardados.length,
        archivos: archivosGuardados
      }
    })
  } catch (error) {
    console.error('Error en upload:', error.message)
    return res.status(400).json({
      success: false,
      error: error.message
    })
  }
})

export default router
