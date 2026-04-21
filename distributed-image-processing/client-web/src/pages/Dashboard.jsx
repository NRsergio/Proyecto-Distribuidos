import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function Dashboard() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => { logout(); navigate('/login') }

  return (
    <div style={styles.page}>
      <nav style={styles.nav}>
        <h1 style={styles.logo}>🖼 ImageProcessing</h1>
        <div style={{display:'flex', alignItems:'center', gap:16}}>
          <span style={{fontSize:14, color:'#555'}}>Hola, {user?.nombre}</span>
          <button style={styles.btnLogout} onClick={handleLogout}>Cerrar Sesion</button>
        </div>
      </nav>
      <main style={styles.main}>
        <h2 style={styles.h2}>Panel Principal</h2>
        <div style={styles.grid}>
          <Link to="/upload" style={{textDecoration:'none'}}>
            <div style={styles.card}>
              <span style={styles.icon}>📤</span>
              <h3>Enviar Imagenes</h3>
              <p>Sube un lote de imagenes y define las transformaciones</p>
            </div>
          </Link>
          <div style={{...styles.card, opacity:0.6}}>
            <span style={styles.icon}>📋</span>
            <h3>Historial</h3>
            <p>Consulta tus solicitudes anteriores</p>
          </div>
        </div>
      </main>
    </div>
  )
}

const styles = {
  page: { minHeight:'100vh', background:'#f0f2f5' },
  nav: { background:'#fff', padding:'14px 32px', display:'flex', justifyContent:'space-between',
         alignItems:'center', boxShadow:'0 1px 4px rgba(0,0,0,.1)' },
  logo: { fontSize:20, fontWeight:700, color:'#4f46e5' },
  main: { padding:40 },
  h2: { fontSize:22, fontWeight:600, marginBottom:28 },
  grid: { display:'grid', gridTemplateColumns:'repeat(auto-fill, minmax(220px, 1fr))', gap:20 },
  card: { background:'#fff', padding:28, borderRadius:12, boxShadow:'0 2px 10px rgba(0,0,0,.07)',
          cursor:'pointer', textAlign:'center', color:'inherit' },
  icon: { fontSize:36, display:'block', marginBottom:12 },
  btnLogout: { padding:'8px 16px', background:'transparent', border:'1px solid #ddd',
               borderRadius:8, cursor:'pointer', fontSize:13 }
}
