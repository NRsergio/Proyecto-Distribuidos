import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { consultarProgreso } from '../services/soapClient.js'

const ESTADO_COLOR = {
  PENDIENTE: '#f59e0b',
  EN_PROCESO: '#3b82f6',
  COMPLETADO: '#10b981',
  ERROR: '#ef4444'
}

export default function BatchProgress() {
  const { id } = useParams()
  const { user } = useAuth()
  const [progreso, setProgreso] = useState(null)
  const [error, setError] = useState('')

  const cargar = async () => {
    try {
      const data = await consultarProgreso(user.token, id)
      setProgreso(data)
    } catch {
      setError('No se pudo cargar el progreso')
    }
  }

  useEffect(() => {
    cargar()
    const intervalo = setInterval(cargar, 5000) // polling cada 5s
    return () => clearInterval(intervalo)
  }, [id])

  if (error) return <div style={{padding:40, color:'red'}}>{error}</div>
  if (!progreso) return <div style={{padding:40}}>Cargando...</div>

  const pct = progreso.porcentajeProgreso || 0

  return (
    <div style={styles.page}>
      <div style={styles.container}>
        <Link to="/" style={styles.back}>← Volver al panel</Link>
        <h2 style={styles.h2}>Lote #{id}</h2>

        <div style={styles.card}>
          <div style={styles.cardHeader}>
            <span style={{...styles.badge, background: ESTADO_COLOR[progreso.estadoLote] || '#888'}}>
              {progreso.estadoLote}
            </span>
            <span style={styles.pctText}>{pct.toFixed(1)}%</span>
          </div>
          <div style={styles.barBg}>
            <div style={{...styles.barFill, width:`${pct}%`}} />
          </div>
          <div style={styles.stats}>
            <span>Total: {progreso.totalImagenes}</span>
            <span style={{color:'#10b981'}}>✔ {progreso.imagenesCompletadas}</span>
            <span style={{color:'#ef4444'}}>✖ {progreso.imagenesError}</span>
          </div>
        </div>

        <h3 style={{marginBottom:16}}>Imagenes</h3>
        {(progreso.imagenes || []).map((img, i) => (
          <div key={i} style={styles.imgRow}>
            <div>
              <strong style={{fontSize:14}}>{img.nombreArchivo}</strong>
            </div>
            <span style={{...styles.badge, background: ESTADO_COLOR[img.estado] || '#888'}}>
              {img.estado}
            </span>
            {img.estado === 'COMPLETADO' && img.rutaResultado && (
              <a href={`/api/download?ruta=${encodeURIComponent(img.rutaResultado)}`}
                 style={styles.downloadBtn}>⬇ Descargar</a>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}

const styles = {
  page: { minHeight:'100vh', background:'#f0f2f5', padding:40 },
  container: { maxWidth:720, margin:'0 auto' },
  back: { color:'#4f46e5', fontSize:14, textDecoration:'none' },
  h2: { fontSize:22, fontWeight:600, margin:'16px 0 24px' },
  card: { background:'#fff', borderRadius:12, padding:24, boxShadow:'0 2px 10px rgba(0,0,0,.08)', marginBottom:32 },
  cardHeader: { display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:16 },
  badge: { padding:'4px 12px', borderRadius:20, color:'#fff', fontSize:12, fontWeight:600 },
  pctText: { fontSize:22, fontWeight:700, color:'#4f46e5' },
  barBg: { background:'#e5e7eb', borderRadius:8, height:10, marginBottom:16 },
  barFill: { background:'#4f46e5', height:'100%', borderRadius:8, transition:'width .5s' },
  stats: { display:'flex', gap:24, fontSize:14 },
  imgRow: { background:'#fff', borderRadius:8, padding:'14px 20px', marginBottom:10,
            display:'flex', alignItems:'center', gap:16, boxShadow:'0 1px 4px rgba(0,0,0,.06)' },
  downloadBtn: { marginLeft:'auto', padding:'6px 14px', background:'#e0e7ff', borderRadius:6,
                 textDecoration:'none', fontSize:13, color:'#4f46e5' }
}
