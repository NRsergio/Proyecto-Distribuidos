import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { login } from '../services/soapClient.js'

export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login: authLogin } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true); setError('')
    try {
      const userData = await login(form.email, form.password)
      authLogin(userData)
      navigate('/')
    } catch {
      setError('Credenciales invalidas. Intente de nuevo.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={styles.container}>
      <form onSubmit={handleSubmit} style={styles.card}>
        <h2 style={styles.title}>Iniciar Sesion</h2>
        <p style={styles.subtitle}>Sistema de Procesamiento de Imagenes</p>
        {error && <div style={styles.error}>{error}</div>}
        <input style={styles.input} type="email" placeholder="Email"
          value={form.email} onChange={e => setForm({...form, email: e.target.value})} required />
        <input style={styles.input} type="password" placeholder="Contrasena"
          value={form.password} onChange={e => setForm({...form, password: e.target.value})} required />
        <button style={styles.btn} type="submit" disabled={loading}>
          {loading ? 'Ingresando...' : 'Ingresar'}
        </button>
        <p style={{textAlign:'center', marginTop: 12}}>
          ¿No tienes cuenta? <Link to="/register">Registrate</Link>
        </p>
      </form>
    </div>
  )
}

const styles = {
  container: { display:'flex', justifyContent:'center', alignItems:'center', minHeight:'100vh', background:'#f0f2f5' },
  card: { background:'#fff', padding:40, borderRadius:12, boxShadow:'0 4px 20px rgba(0,0,0,.1)', width:380 },
  title: { fontSize:24, fontWeight:700, marginBottom:4 },
  subtitle: { color:'#666', marginBottom:24, fontSize:14 },
  input: { display:'block', width:'100%', padding:'10px 14px', marginBottom:16, border:'1px solid #ddd', borderRadius:8, fontSize:14 },
  btn: { width:'100%', padding:'12px', background:'#4f46e5', color:'#fff', border:'none', borderRadius:8, fontSize:16, cursor:'pointer' },
  error: { background:'#fee2e2', color:'#991b1b', padding:10, borderRadius:8, marginBottom:16, fontSize:14 }
}
