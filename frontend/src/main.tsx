import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App.tsx'
import './styles/index.css'

// 개발 환경에서만 동작하도록
if (import.meta.env.DEV) {
  if (import.meta.hot) {
    import.meta.hot.accept(() => {
      sessionStorage.clear()
    })
  }
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
)
