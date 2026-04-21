import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { enviarLote } from '../services/soapClient.js'

const TRANSFORMACIONES = [
  { valor: 'ESCALA_GRISES', label: 'Escala de grises' },
  { valor: 'REDIMENSIONAR', label: 'Redimensionar', params: { ancho: 800, alto: 600 } },
  { valor: 'ROTAR', label: 'Rotar', params: { grados: 90 } },
  { valor: 'RECORTAR', label: 'Recortar', params: { x: 0, y: 0, ancho: 400, alto: 300 } },
  { valor: 'REFLEJAR', label: 'Reflejar', params: { eje: 'HORIZONTAL' } },
  { valor: 'DESENFOCAR', label: 'Desenfocar', params: { radio: 3 } },
  { valor: 'NITIDEZ', label: 'Nitidez' },
  { valor: 'BRILLO_CONTRASTE', label: 'Brillo y contraste', params: { brillo: 10, contraste: 10 } },
  { valor: 'MARCA_DE_AGUA', label: 'Marca de agua', params: { texto: 'CONFIDENCIAL' } },
  { valor: 'CONVERSION_FORMATO', label: 'Convertir formato', params: { formato: 'PNG' } },
]

export default function BatchUpload() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [archivos, setArchivos] = useState([])   // { file, transformaciones: [] }
  const [loading, setLoading] = useState(false)
  const [fase, setFase] = useState('')
  const [error, setError] = useState('')

  const onFilesChange = (e) => {
    const files = Array.from(e.target.files)
    setArchivos(files.map(f => ({ file: f, transformaciones: [] })))
    setError('')
  }

  const agregarTransformacion = (idx) => {
    if (archivos[idx].transformaciones.length >= 5) {
      setError('Máximo 5 transformaciones por imagen'); return
    }
    setArchivos(prev => prev.map((a, i) => i === idx ? {
      ...a,
      transformaciones: [...a.transformaciones, {
        tipo: 'ESCALA_GRISES',
        orden: a.transformaciones.length + 1,
        parametros: '{}'
      }]
    } : a))
  }

  const cambiarTipoTransformacion = (idxImg, idxTrans, nuevoTipo) => {
    const trans = TRANSFORMACIONES.find(t => t.valor === nuevoTipo)
    const params = trans?.params ? JSON.stringify(trans.params) : '{}'
    setArchivos(prev => prev.map((a, i) => i === idxImg ? {
      ...a,
      transformaciones: a.transformaciones.map((t, j) => j === idxTrans
        ? { ...t, tipo: nuevoTipo, parametros: params } : t)
    } : a))
  }

  const eliminarTransformacion = (idxImg, idxTrans) => {
    setArchivos(prev => prev.map((a, i) => i === idxImg ? {
      ...a,
      transformaciones: a.transformaciones
        .filter((_, j) => j !== idxTrans)
        .map((t, j) => ({ ...t, orden: j + 1 }))
    } : a))
  }

  const handleSubmit = async () => {
    if (archivos.length === 0) { setError('Selecciona al menos una imagen'); return }
    const sinTransf = archivos.find(a => a.transformaciones.length === 0)
    if (sinTransf) { setError(`"${sinTransf.file.name}" no tiene transformaciones`); return }

    setLoading(true); setError('')

    try {
      // FASE 1: Subir archivos al servidor
      setFase('Subiendo archivos...')
      const formData = new FormData()
      archivos.forEach(a => formData.append('files', a.file))

      const uploadRes = await fetch('/api/files/upload', {
        method: 'POST', body: formData
      })
      if (!uploadRes.ok) throw new Error('Error al subir los archivos')
      const uploaded = await uploadRes.json()

      // FASE 2: Enviar lote via SOAP
      setFase('Enviando lote al servidor...')
      const imagenesPayload = archivos.map((a, i) => ({
        nombreArchivo: a.file.name,
        rutaOriginal: uploaded[i].rutaAlmacenada,
        transformaciones: a.transformaciones
      }))

      const { idLote } = await enviarLote(user.token, imagenesPayload)
      navigate(`/batch/${idLote}`)
    } catch (e) {
      setError('Error: ' + e.message)
    } finally {
      setLoading(false); setFase('')
    }
  }

  return (
    <div style={styles.page}>
      <div style={styles.container}>
        <h2 style={styles.h2}>📤 Enviar Lote de Imágenes</h2>
        {error && <div style={styles.error}>{error}</div>}

        <label style={styles.dropzone}>
          <input type="file" multiple accept="image/jpeg,image/png,image/tiff"
            onChange={onFilesChange} style={{display:'none'}} />
          <span style={{fontSize:40}}>📁</span>
          <p style={{fontWeight:600}}>Haz clic para seleccionar imágenes</p>
          <small style={{color:'#888'}}>JPG, PNG, TIF — máx. 20 MB por archivo</small>
        </label>

        {archivos.map((a, i) => (
          <div key={i} style={styles.imgCard}>
            <div style={styles.imgHeader}>
              <div>
                <strong>🖼 {a.file.name}</strong>
                <span style={styles.fileSize}> ({(a.file.size / 1024).toFixed(0)} KB)</span>
              </div>
              <button style={styles.btnAdd} onClick={() => agregarTransformacion(i)}
                disabled={a.transformaciones.length >= 5}>
                + Transformación
              </button>
            </div>

            {a.transformaciones.length === 0 && (
              <p style={{color:'#f59e0b', fontSize:13}}>⚠ Agrega al menos una transformación</p>
            )}

            {a.transformaciones.map((t, j) => (
              <div key={j} style={styles.transRow}>
                <span style={styles.ordenBadge}>{t.orden}</span>
                <select style={styles.select} value={t.tipo}
                  onChange={e => cambiarTipoTransformacion(i, j, e.target.value)}>
                  {TRANSFORMACIONES.map(op =>
                    <option key={op.valor} value={op.valor}>{op.label}</option>)}
                </select>
                <code style={styles.params}>{t.parametros}</code>
                <button style={styles.btnDel} onClick={() => eliminarTransformacion(i, j)}>✕</button>
              </div>
            ))}
          </div>
        ))}

        {archivos.length > 0 && (
          <button style={{...styles.btnEnviar, opacity: loading ? .7 : 1}}
            onClick={handleSubmit} disabled={loading}>
            {loading ? `⏳ ${fase}` : `📤 Enviar ${archivos.length} imagen(es)`}
          </button>
        )}
      </div>
    </div>
  )
}

const styles = {
  page: { minHeight:'100vh', background:'#f0f2f5', padding:32 },
  container: { maxWidth:760, margin:'0 auto' },
  h2: { fontSize:22, fontWeight:700, marginBottom:24 },
  dropzone: { display:'flex', flexDirection:'column', alignItems:'center', gap:8,
              border:'2px dashed #c7d2fe', borderRadius:14, padding:40, marginBottom:24,
              cursor:'pointer', background:'#fafbff', textAlign:'center' },
  imgCard: { background:'#fff', borderRadius:12, padding:20, marginBottom:16,
             boxShadow:'0 2px 10px rgba(0,0,0,.07)' },
  imgHeader: { display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:12 },
  fileSize: { color:'#888', fontSize:12 },
  transRow: { display:'flex', alignItems:'center', gap:10, marginBottom:8 },
  ordenBadge: { background:'#e0e7ff', color:'#4f46e5', borderRadius:20, padding:'2px 10px', fontSize:12, fontWeight:700 },
  select: { padding:'6px 10px', borderRadius:6, border:'1px solid #e5e7eb', fontSize:13, flexShrink:0 },
  params: { fontSize:11, color:'#6b7280', background:'#f3f4f6', padding:'3px 8px', borderRadius:4, overflow:'hidden', maxWidth:200 },
  btnDel: { background:'#fee2e2', color:'#dc2626', border:'none', borderRadius:6, padding:'4px 10px', cursor:'pointer', fontSize:12 },
  btnAdd: { padding:'6px 14px', background:'#e0e7ff', border:'none', borderRadius:8, cursor:'pointer', fontSize:13, color:'#4f46e5', fontWeight:600 },
  btnEnviar: { width:'100%', padding:14, background:'#4f46e5', color:'#fff', border:'none',
               borderRadius:12, fontSize:16, cursor:'pointer', marginTop:8, fontWeight:600 },
  error: { background:'#fee2e2', color:'#991b1b', padding:12, borderRadius:8, marginBottom:16, fontSize:14 }
}
