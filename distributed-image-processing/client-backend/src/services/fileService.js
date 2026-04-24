/**
 * Servicio de Archivo - Maneja subidas y almacenamiento
 */

import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const uploadsDir = process.env.UPLOAD_DIR || path.join(__dirname, '../../uploads')

// Crear directorio si no existe
if (!fs.existsSync(uploadsDir)) {
  fs.mkdirSync(uploadsDir, { recursive: true })
}

/**
 * Guarda un archivo subido
 */
export function guardarArchivo(file) {
  try {
    const filename = `${Date.now()}-${file.originalname}`
    const filepath = path.join(uploadsDir, filename)

    fs.writeFileSync(filepath, file.buffer)

    return {
      nombreArchivo: file.originalname,
      rutaOrigen: filepath,
      nombreGuardado: filename,
      size: file.size
    }
  } catch (error) {
    throw new Error(`Error guardando archivo: ${error.message}`)
  }
}

/**
 * Guarda múltiples archivos
 */
export function guardarArchivos(files) {
  return files.map((file) => guardarArchivo(file))
}

/**
 * Obtiene información de un archivo
 */
export function obtenerInfoArchivo(filename) {
  const filepath = path.join(uploadsDir, filename)

  try {
    if (!fs.existsSync(filepath)) {
      throw new Error('Archivo no encontrado')
    }

    const stats = fs.statSync(filepath)
    return {
      nombre: filename,
      ruta: filepath,
      size: stats.size,
      creado: stats.birthtimeMs
    }
  } catch (error) {
    throw new Error(`Error obteniendo info: ${error.message}`)
  }
}

/**
 * Elimina un archivo
 */
export function eliminarArchivo(filename) {
  try {
    const filepath = path.join(uploadsDir, filename)

    if (fs.existsSync(filepath)) {
      fs.unlinkSync(filepath)
    }
  } catch (error) {
    console.warn(`Error eliminando archivo ${filename}:`, error.message)
  }
}

/**
 * Obtiene ruta completa de un archivo
 */
export function obtenerRutaArchivo(filename) {
  return path.join(uploadsDir, filename)
}

export default {
  guardarArchivo,
  guardarArchivos,
  obtenerInfoArchivo,
  eliminarArchivo,
  obtenerRutaArchivo
}
