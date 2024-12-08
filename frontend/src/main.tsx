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
      console.log('세션스토리지 초기화됨!')
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
