/**
 * Middleware de Autenticación
 * Verifica tokens JWT en headers Authorization
 */

export function verificarToken(req, res, next) {
  try {
    const authHeader = req.headers.authorization

    // Este middleware solo valida que exista un token
    // La validación real se hace en el app-server JAVA
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({
        success: false,
        error: 'Token requerido en Authorization header'
      })
    }

    const token = authHeader.replace('Bearer ', '')

    if (!token) {
      return res.status(401).json({
        success: false,
        error: 'Token vacío'
      })
    }

    // Pasar token a la siguiente middleware/ruta
    req.token = token
    next()
  } catch (error) {
    return res.status(401).json({
      success: false,
      error: 'Error verificando token'
    })
  }
}

export default { verificarToken }
