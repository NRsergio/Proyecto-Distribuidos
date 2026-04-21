import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { enviarLote } from '../services/soapClient.js'

const TRANSFORMACIONES_DISPONIBLES = [
  'ESCALA_GRISES', 'REDIMENSIONAR', 'RECORTAR', 'ROTAR', 'REFLEJAR',
  'DESENFOCAR', 'NITIDEZ', 'BRILLO_CONTRASTE', 'MARCA_DE_AGUA', 'CONVERSION_FORMATO'
]

export default function BatchUpload() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [imagenes, setImagenes] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const agregarTransformacion = (idxImg) => {
    setImagenes(imgs => imgs.map((img, i) => i === idxImg
      ? { ...img, transformaciones: [...img.transformaciones, { tipo: 'ESCALA_GRISES', orden: img.transformaciones.length + 1, parametros: '{}' }] }
      : img))
  }

  const onFilesChange = (e) => {
    const files = Array.from(e.target.files)
    setImagenes(files.map(f => ({
      nombreArchivo: f.name,
      rutaOriginal: `/uploads/${f.name}`,
      transformaciones: []
    })))
  }

  const handleSubmit = async () => {
    if (imagenes.length === 0) { setError('Selecciona al menos una imagen'); return }
    setLoading(true); setError('')
    try {
      const { idLote } = await enviarLote(user.token, imagenes)
      navigate(`/batch/${idLote}`)
    } catch (e) {
      setError('Error al enviar el lote: ' + e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={styles.page}>
      <div style={styles.container}>
        <h2 style={styles.h2}>Enviar Lote de Imagenes</h2>
        {error && <div style={styles.error}>{error}</div>}

        <label style={styles.dropzone}>
          <input type="file" multiple accept="image/*" onChange={onFilesChange} style={{display:'none'}} />
          <span style={{fontSize:36}}>📁</span>
          <p>Haz clic o arrastra imagenes aqui</p>
          <small style={{color:'#888'}}>JPG, PNG, TIF soportados</small>
        </label>

        {imagenes.map((img, i) => (
          <div key={i} style={styles.imgCard}>
            <div style={styles.imgHeader}>
              <strong>🖼 {img.nombreArchivo}</strong>
              <button style={styles.btnAdd} onClick={() => agregarTransformacion(i)}>
                + Agregar Transformacion
              </button>
            </div>
            {img.transformaciones.map((t, j) => (
              <div key={j} style={styles.trans}>
                <select style={styles.select}
                  value={t.tipo}
                  onChange={e => setImagenes(imgs => imgs.map((im, ii) => ii === i
                    ? { ...im, transformaciones: im.transformaciones.map((tt, jj) => jj === j ? {...tt, tipo: e.target.value} : tt) }
                    : im))}>
                  {TRANSFORMACIONES_DISPONIBLES.map(op => <option key={op} value={op}>{op}</option>)}
                </select>
                <span style={{fontSize:12, color:'#888'}}>Orden: {t.orden}</span>
              </div>
            ))}
          </div>
        ))}

        <button style={styles.btnEnviar} onClick={handleSubmit} disabled={loading}>
          {loading ? 'Enviando...' : '📤 Enviar Lote'}
        </button>
      </div>
    </div>
  )
}

const styles = {
  page: { minHeight:'100vh', background:'#f0f2f5', padding:40 },
  container: { maxWidth:720, margin:'0 auto' },
  h2: { fontSize:22, fontWeight:600, marginBottom:24 },
  dropzone: { display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center',
              border:'2px dashed #c7d2fe', borderRadius:12, padding:40, marginBottom:24, cursor:'pointer',
              background:'#fafafa', gap:8 },
  imgCard: { background:'#fff', borderRadius:10, padding:20, marginBottom:16, boxShadow:'0 1px 6px rgba(0,0,0,.08)' },
  imgHeader: { display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:12 },
  trans: { display:'flex', alignItems:'center', gap:16, marginBottom:8 },
  select: { padding:'6px 12px', borderRadius:6, border:'1px solid #ddd', fontSize:13 },
  btnAdd: { padding:'6px 14px', background:'#e0e7ff', border:'none', borderRadius:6, cursor:'pointer', fontSize:13, color:'#4f46e5' },
  btnEnviar: { width:'100%', padding:14, background:'#4f46e5', color:'#fff', border:'none',
               borderRadius:10, fontSize:16, cursor:'pointer', marginTop:8 },
  error: { background:'#fee2e2', color:'#991b1b', padding:10, borderRadius:8, marginBottom:16, fontSize:14 }
}
