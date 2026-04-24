/**
 * Servicio de API - Comunica con el App-Server JAVA via HTTP REST
 * (Antes era SOAP, ahora es JSON/REST)
 */

import axios from 'axios'

const API_URL = `${process.env.JAVA_APP_SERVER_URL}/api`

const api = axios.create({
  baseURL: API_URL,
  timeout: 30000
})

/**
 * Login: autentica usuario y devuelve token
 */
export async function login(email, password) {
  try {
    const response = await api.post('/auth/login', { email, password })
    return response.data
  } catch (error) {
    console.error('Error en login:', error.message)
    throw new Error(`Login fallido: ${error.response?.data?.error || error.message}`)
  }
}

/**
 * Register: crea nuevo usuario
 */
export async function register(email, password, nombre) {
  try {
    const response = await api.post('/auth/register', { email, password, nombre })
    return response.data
  } catch (error) {
    console.error('Error en register:', error.message)
    throw new Error(`Registro fallido: ${error.response?.data?.error || error.message}`)
  }
}

/**
 * Validar token
 */
export async function validarToken(token) {
  try {
    const response = await api.get('/auth/validar', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    return response.data
  } catch (error) {
    console.error('Error validando token:', error.message)
    throw new Error(`Validación fallida: ${error.response?.data?.error || error.message}`)
  }
}

/**
 * Enviar lote: procesa imágenes con transformaciones
 */
export async function enviarLote(token, imagenes) {
  try {
    const response = await api.post(
      '/batch/enviar',
      { imagenes },
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    )
    return response.data
  } catch (error) {
    console.error('Error en enviarLote:', error.message)
    throw new Error(`Error enviando lote: ${error.response?.data?.error || error.message}`)
  }
}

/**
 * Consultar progreso: obtiene estado de un lote
 */
export async function consultarProgreso(token, idLote) {
  try {
    const response = await api.get(`/batch/progreso/${idLote}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    return response.data
  } catch (error) {
    console.error('Error en consultarProgreso:', error.message)
    throw new Error(`Error consultando progreso: ${error.response?.data?.error || error.message}`)
  }
}
