import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

export default function Register() {
  const [form, setForm] = useState({ nombre: '', email: '', password: '' })
  const [msg, setMsg] = useState('')
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    // TODO: llamar endpoint REST /api/auth/register
    setMsg('Registro exitoso. Ahora puedes iniciar sesion.')
    setTimeout(() => navigate('/login'), 2000)
  }

  return (
    <div style={styles.container}>
      <form onSubmit={handleSubmit} style={styles.card}>
        <h2 style={styles.title}>Crear Cuenta</h2>
        {msg && <div style={styles.success}>{msg}</div>}
        <input style={styles.input} placeholder="Nombre completo" value={form.nombre}
          onChange={e => setForm({...form, nombre: e.target.value})} required />
        <input style={styles.input} type="email" placeholder="Email" value={form.email}
          onChange={e => setForm({...form, email: e.target.value})} required />
        <input style={styles.input} type="password" placeholder="Contrasena" value={form.password}
          onChange={e => setForm({...form, password: e.target.value})} required />
        <button style={styles.btn} type="submit">Registrarse</button>
        <p style={{textAlign:'center', marginTop: 12}}>
          ¿Ya tienes cuenta? <Link to="/login">Inicia sesion</Link>
        </p>
      </form>
    </div>
  )
}

const styles = {
  container: { display:'flex', justifyContent:'center', alignItems:'center', minHeight:'100vh', background:'#f0f2f5' },
  card: { background:'#fff', padding:40, borderRadius:12, boxShadow:'0 4px 20px rgba(0,0,0,.1)', width:380 },
  title: { fontSize:24, fontWeight:700, marginBottom:24 },
  input: { display:'block', width:'100%', padding:'10px 14px', marginBottom:16, border:'1px solid #ddd', borderRadius:8, fontSize:14 },
  btn: { width:'100%', padding:'12px', background:'#4f46e5', color:'#fff', border:'none', borderRadius:8, fontSize:16, cursor:'pointer' },
  success: { background:'#d1fae5', color:'#065f46', padding:10, borderRadius:8, marginBottom:16, fontSize:14 }
}
