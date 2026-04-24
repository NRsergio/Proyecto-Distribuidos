/**
 * Cliente REST para comunicarse con el Backend Express.
 * El backend actúa como intermediario hacia el App-Server JAVA.
 * 
 * Flujo: Cliente Web (React) → Backend Express REST → App-Server JAVA SOAP
 */
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:3001/api'

/**
 * Helper para requests HTTP al backend
 */
async function callAPI(endpoint, method = 'GET', body = null, token = null) {
  const headers = {
    'Content-Type': 'application/json'
  }

  // Agregar token si está disponible
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const options = {
    method,
    headers
  }

  if (body) {
    options.body = JSON.stringify(body)
  }

  const response = await fetch(`${API_URL}${endpoint}`, options)
  const data = await response.json()

  if (!response.ok) {
    throw new Error(data.error || `HTTP error: ${response.status}`)
  }

  return data.data
}

/**
 * Login: authentication con email y password
 */
export async function login(email, password) {
  const resultado = await callAPI('/auth/login', 'POST', { email, password })
  return resultado
}

/**
 * Register: crear nuevo usuario
 */
export async function register(email, password, nombre) {
  const resultado = await callAPI('/auth/register', 'POST', { email, password, nombre })
  return resultado
}

/**
 * Upload: subir archivos de imagen
 */
export async function uploadImagenes(token, files) {
  const formData = new FormData()

  // Agregar archivos al FormData
  for (const file of files) {
    formData.append('imagenes', file)
  }

  const headers = {
    'Authorization': `Bearer ${token}`
  }

  // No enviar Content-Type, dejar que el navegador lo establezca automáticamente
  const response = await fetch(`${API_URL}/upload`, {
    method: 'POST',
    headers,
    body: formData
  })

  const data = await response.json()

  if (!response.ok) {
    throw new Error(data.error || `HTTP error: ${response.status}`)
  }

  return data.data
}

/**
 * Enviar lote: procesa imagenes con transformaciones
 */
export async function enviarLote(token, imagenes) {
  const resultado = await callAPI(
    '/batch/enviar',
    'POST',
    { token, imagenes },
    token
  )
  return resultado
}

/**
 * Consultar progreso: obtiene estado de un lote
 */
export async function consultarProgreso(token, idLote) {
  const resultado = await callAPI(`/batch/progreso/${idLote}`, 'GET', null, token)
  return resultado
}
