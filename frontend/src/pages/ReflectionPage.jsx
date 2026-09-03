import { useEffect, useState } from 'react'
import { api } from '../api/client.js'

export default function ReflectionPage({ course, onAnalyzed, onBack }) {
  const [documents, setDocuments] = useState([])
  const [form, setForm] = useState({
    understoodContent: '',
    difficultContent: '',
    questionContent: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    api.getDocuments(course.courseId).then(setDocuments).catch((requestError) => setError(requestError.message))
  }, [course.courseId])

  const updateField = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }))
  }

  const submit = async (event) => {
    event.preventDefault()
    setLoading(true)
    setError('')

    try {
      const reflection = await api.createReflection({
        userId: 2,
        courseId: course.courseId,
        ...form,
      })
      const analysis = await api.analyzeReflection(reflection.reflectionId)
      if (!analysis.reviewMaterial) {
        setError(analysis.weaknessSummary)
        return
      }
      onAnalyzed(analysis)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <button className="text-button" onClick={onBack}>← 대시보드</button>
      <section className="page-heading">
        <div>
          <p className="eyebrow">{course.courseName}</p>
          <h1>강의자료를 확인하고 회고록을 작성하세요.</h1>
        </div>
      </section>

      <div className="two-column">
        <section className="panel">
          <h2>강의자료</h2>
          <div className="document-list">
            {documents.map((document) => (
              <article className="document-card" key={document.documentId}>
                <h3>{document.documentName}</h3>
                <p>{document.documentText}</p>
                <small>{document.fileUrl}</small>
              </article>
            ))}
          </div>
        </section>

        <form className="panel reflection-form" onSubmit={submit}>
          <h2>오늘의 회고록</h2>
          <label>
            잘 이해한 내용
            <textarea name="understoodContent" value={form.understoodContent} onChange={updateField}
              placeholder="개념과 실습 내용을 구체적으로 작성해 주세요." required />
          </label>
          <label>
            어려웠던 내용
            <textarea name="difficultContent" value={form.difficultContent} onChange={updateField}
              placeholder="헷갈리거나 다시 보고 싶은 내용을 작성해 주세요." required />
          </label>
          <label>
            추가 질문
            <textarea name="questionContent" value={form.questionContent} onChange={updateField}
              placeholder="추가로 궁금한 내용을 작성해 주세요." />
          </label>
          {error && <p className="error-message">{error}</p>}
          <button className="primary-button full" type="submit" disabled={loading}>
            {loading ? '회고록과 강의자료 분석 중...' : '이해도 분석 및 복습자료 생성'}
          </button>
        </form>
      </div>
    </>
  )
}

