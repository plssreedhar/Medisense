import { useState } from 'react'
import Login from './pages/Login'
import AppShell from './components/AppShell'

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false)

  if (!isLoggedIn) {
    return <Login onLogin={() => setIsLoggedIn(true)} />
  }

  return <AppShell onLogout={() => setIsLoggedIn(false)} />
}

export default App
