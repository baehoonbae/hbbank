import { useState } from 'react'
import './App.css'

function App() {
  const [count, setCount] = useState(0)

  return (
    <>
      <div>
        <a href="https://banking.hbbank.co.kr" target="_blank">
          <img src="/hbbank-logo.png" className="logo" alt="HB은행 로고" />
        </a>
      </div>
      <h1>HB은행에 오신 것을 환영합니다</h1>
      <div className="card">
        <button onClick={() => setCount((count) => count + 1)}>
          거래 횟수: {count}
        </button>
        <p>
          안전한 금융거래를 위해 <code>보안카드</code> 인증이 필요합니다
        </p>
      </div>
      <p className="read-the-docs">
        HB은행과 함께 더 나은 금융생활을 시작하세요
      </p>
    </>
  )
}

export default App
