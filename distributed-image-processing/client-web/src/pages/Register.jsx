import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function Register() {
  const [form, setForm] = useState({ nombre: '', email: '', password: '', confirm: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login: authLogin } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (form.password !== form.confirm) {
      setError('Las contraseñas no coinciden'); return
    }
    setLoading(true); setError('')
    try {
      const res = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nombre: form.nombre, email: form.email, password: form.password })
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data.error || 'Error en el registro')
      authLogin(data)        // auto-login tras registro exitoso
      navigate('/')
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={styles.container}>
      <form onSubmit={handleSubmit} style={styles.card}>
        <h2 style={styles.title}>Crear Cuenta</h2>
        <p style={styles.subtitle}>Sistema de Procesamiento de Imágenes</p>
        {error && <div style={styles.error}>{error}</div>}
        <input style={styles.input} placeholder="Nombre completo" value={form.nombre}
          onChange={e => setForm({...form, nombre: e.target.value})} required />
        <input style={styles.input} type="email" placeholder="Email" value={form.email}
          onChange={e => setForm({...form, email: e.target.value})} required />
        <input style={styles.input} type="password" placeholder="Contraseña" value={form.password}
          onChange={e => setForm({...form, password: e.target.value})} required />
        <input style={styles.input} type="password" placeholder="Confirmar contraseña" value={form.confirm}
          onChange={e => setForm({...form, confirm: e.target.value})} required />
        <button style={styles.btn} type="submit" disabled={loading}>
          {loading ? 'Registrando...' : 'Registrarse'}
        </button>
        <p style={{textAlign:'center', marginTop:12}}>
          ¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link>
        </p>
      </form>
    </div>
  )
}

const styles = {
  container: { display:'flex', justifyContent:'center', alignItems:'center', minHeight:'100vh', background:'#f0f2f5' },
  card: { background:'#fff', padding:40, borderRadius:12, boxShadow:'0 4px 20px rgba(0,0,0,.1)', width:400 },
  title: { fontSize:24, fontWeight:700, marginBottom:4 },
  subtitle: { color:'#666', marginBottom:24, fontSize:14 },
  input: { display:'block', width:'100%', padding:'10px 14px', marginBottom:16, border:'1px solid #ddd', borderRadius:8, fontSize:14 },
  btn: { width:'100%', padding:'12px', background:'#4f46e5', color:'#fff', border:'none', borderRadius:8, fontSize:16, cursor:'pointer' },
  error: { background:'#fee2e2', color:'#991b1b', padding:10, borderRadius:8, marginBottom:16, fontSize:14 }
}
