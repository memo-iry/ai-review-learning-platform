export default function AnalysisPage({ analysis, onOpenReview }) {
  return (
    <section className="analysis-layout">
      <div className="page-heading">
        <div>
          <p className="eyebrow">AI 이해도 분석</p>
          <h1>회고록을 바탕으로 학습 상태를 분석했습니다.</h1>
        </div>
      </div>

      <div className="score-card">
        <div className="score-circle">{analysis.understandingScore}%</div>
        <div>
          <h2>현재 이해도</h2>
          <p>{analysis.analysisReason}</p>
        </div>
      </div>

      <div className="result-grid">
        <article className="panel">
          <h2>이해한 부분</h2>
          <p>{analysis.understoodSummary}</p>
        </article>
        <article className="panel warning-panel">
          <h2>보완이 필요한 부분</h2>
          <p>{analysis.weaknessSummary}</p>
        </article>
      </div>

      <button className="primary-button" onClick={onOpenReview}>맞춤형 복습자료 보기</button>
    </section>
  )
}

