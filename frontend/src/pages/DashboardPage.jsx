import { useEffect, useState } from 'react'
import { api } from '../api/client.js'

export default function DashboardPage({ onStart }) {
  const [lectures, setLectures] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    api.getLectures().then(setLectures).catch((requestError) => setError(requestError.message))
  }, [])

  return (
    <>
      <section className="page-heading">
        <div>
          <p className="eyebrow">학습 대시보드</p>
          <h1>오늘 배운 내용을 회고하고 복습해 보세요.</h1>
        </div>
      </section>

      <section className="summary-grid">
        <article className="summary-card"><span>현재 차원</span><strong>2</strong></article>
        <article className="summary-card"><span>학습 진도</span><strong>68%</strong></article>
        <article className="summary-card"><span>최근 이해도</span><strong>74%</strong></article>
      </section>

      <section className="content-section">
        <h2>강의 목록</h2>
        {error && <p className="error-message">{error}</p>}
        <div className="course-list">
          {lectures.map((lecture) => (
            <article className="course-card" key={lecture.lectureId}>
              <div>
                <span className="tag">진행 중</span>
                <h3>{lecture.title}</h3>
                <p>{lecture.description}</p>
                <small>{lecture.lectureDate} {lecture.startTime}~{lecture.endTime}</small>
              </div>
              <button className="primary-button" onClick={() => onStart(lecture)}>
                강의자료 확인
              </button>
            </article>
          ))}
        </div>
      </section>
    </>
  )
}
