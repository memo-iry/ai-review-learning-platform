export default function ReviewPage({ review, onComplete }) {
  return (
    <>
      <section className="page-heading">
        <div>
          <p className="eyebrow">RAG 맞춤 복습</p>
          <h1>{review.title}</h1>
        </div>
      </section>

      <section className="review-stack">
        <article className="panel">
          <h2>핵심 개념</h2>
          <ol>
            {review.coreConcepts.map((concept) => <li key={concept}>{concept}</li>)}
          </ol>
        </article>

        <article className="panel">
          <h2>예제 코드</h2>
          <pre><code>{review.exampleCode}</code></pre>
        </article>

        <article className="panel">
          <h2>확인 문제</h2>
          {review.quiz.map((item, index) => (
            <details key={item.question}>
              <summary>{index + 1}. {item.question}</summary>
              <p>정답: {item.answer}</p>
            </details>
          ))}
        </article>
      </section>

      <button className="primary-button" onClick={onComplete}>복습 완료 기록</button>
    </>
  )
}

