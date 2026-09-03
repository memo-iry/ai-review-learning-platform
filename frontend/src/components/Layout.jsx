export default function Layout({ currentStep, children }) {
  const steps = ['대시보드', '강의자료', '회고록', 'AI 분석', '복습자료', '내 성장']

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">AI 맞춤 복습</div>
        <nav className="steps" aria-label="학습 단계">
          {steps.map((step) => (
            <span className={step === currentStep ? 'step active' : 'step'} key={step}>
              {step}
            </span>
          ))}
        </nav>
      </header>
      <main className="page">{children}</main>
    </div>
  )
}

