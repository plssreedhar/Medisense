import { useState } from 'react'
import styles from './Login.module.css'

const DEMO_EMAIL = 'demo@medisense.ai'
const DEMO_PASSWORD = 'demo123'

type Props = {
  onLogin: () => void
}

function Login({ onLogin }: Props) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (email === DEMO_EMAIL && password === DEMO_PASSWORD) {
      onLogin()
    } else {
      setError('Invalid credentials. Use demo@medisense.ai / demo123')
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <div className={styles.logoArea}>
          <div className={styles.logoCircle}>M</div>
          <span className={styles.logoName}>MediSense</span>
          <span className={styles.tagline}>AI-Powered Health Intelligence</span>
        </div>

        <h1 className={styles.heading}>Welcome back</h1>

        <form className={styles.form} onSubmit={handleSubmit}>
          <div className={styles.field}>
            <label className={styles.label}>Email</label>
            <input
              type="email"
              className={styles.input}
              value={email}
              onChange={e => { setEmail(e.target.value); setError(null) }}
              placeholder="you@example.com"
              autoComplete="email"
            />
          </div>

          <div className={styles.field}>
            <label className={styles.label}>Password</label>
            <input
              type="password"
              className={styles.input}
              value={password}
              onChange={e => { setPassword(e.target.value); setError(null) }}
              placeholder="••••••••"
              autoComplete="current-password"
            />
          </div>

          {error && <div className={styles.error}>{error}</div>}

          <button type="submit" className={styles.button}>Sign In</button>
        </form>

        <p className={styles.hint}>Demo: demo@medisense.ai / demo123</p>
      </div>
    </div>
  )
}

export default Login
