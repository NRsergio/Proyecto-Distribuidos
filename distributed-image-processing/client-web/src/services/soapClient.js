/**
 * Cliente SOAP para comunicarse con el servidor de aplicacion.
 * Construye el envelope XML y parsea la respuesta.
 */
const SOAP_URL = '/ws/ImageProcessingService'
const NS = 'http://imageprocessing.com/soap'

async function callSoap(operation, bodyXml) {
  const envelope = `<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ns="${NS}">
  <soapenv:Header/>
  <soapenv:Body>
    <ns:${operation}>
      ${bodyXml}
    </ns:${operation}>
  </soapenv:Body>
</soapenv:Envelope>`

  const response = await fetch(SOAP_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'text/xml;charset=UTF-8', 'SOAPAction': '' },
    body: envelope
  })

  if (!response.ok) throw new Error(`SOAP error: ${response.status}`)
  const text = await response.text()
  const parser = new DOMParser()
  return parser.parseFromString(text, 'text/xml')
}

export async function login(email, password) {
  const xml = callSoap('login', `
    <request>
      <email>${email}</email>
      <password>${password}</password>
    </request>`)
  const doc = await xml
  const token = doc.querySelector('token')?.textContent
  const idUsuario = doc.querySelector('idUsuario')?.textContent
  const nombre = doc.querySelector('nombre')?.textContent
  if (!token) throw new Error('Credenciales invalidas')
  return { token, idUsuario, nombre, email }
}

export async function enviarLote(token, imagenes) {
  const imagenesXml = imagenes.map(img => `
    <imagenes>
      <nombreArchivo>${img.nombreArchivo}</nombreArchivo>
      <rutaOriginal>${img.rutaOriginal}</rutaOriginal>
      ${img.transformaciones.map(t => `
      <transformaciones>
        <tipo>${t.tipo}</tipo>
        <orden>${t.orden}</orden>
        <parametros>${t.parametros || '{}'}</parametros>
      </transformaciones>`).join('')}
    </imagenes>`).join('')

  const doc = await callSoap('enviarLote', `
    <request>
      <token>${token}</token>
      ${imagenesXml}
    </request>`)

  const idLote = doc.querySelector('idLote')?.textContent
  return { idLote }
}

export async function consultarProgreso(token, idLote) {
  const doc = await callSoap('consultarProgreso', `
    <idLote>${idLote}</idLote>
    <token>${token}</token>`)

  return {
    idLote,
    estadoLote: doc.querySelector('estadoLote')?.textContent,
    porcentajeProgreso: parseFloat(doc.querySelector('porcentajeProgreso')?.textContent || '0'),
    totalImagenes: parseInt(doc.querySelector('totalImagenes')?.textContent || '0'),
    imagenesCompletadas: parseInt(doc.querySelector('imagenesCompletadas')?.textContent || '0'),
    imagenesError: parseInt(doc.querySelector('imagenesError')?.textContent || '0'),
    imagenes: Array.from(doc.querySelectorAll('imagenes')).map(el => ({
      idImagen: el.querySelector('idImagen')?.textContent,
      nombreArchivo: el.querySelector('nombreArchivo')?.textContent,
      estado: el.querySelector('estado')?.textContent,
      rutaResultado: el.querySelector('rutaResultado')?.textContent
    }))
  }
}
