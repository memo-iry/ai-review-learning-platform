import { useEffect, useState } from 'react'
import { api } from '../api/client.js'

export default function ReflectionPage({ lecture, onAnalyzed, onBack }) {
  const [materials, setMaterials] = useState([])
  const [form, setForm] = useState({
    understood: '',
    difficult: '',
    wantsToLearn: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    api.getMaterials(lecture.lectureId).then(setMaterials).catch((requestError) => setError(requestError.message))
  }, [lecture.lectureId])

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
        lectureId: lecture.lectureId,
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
          <p className="eyebrow">{lecture.title}</p>
          <h1>강의자료를 확인하고 회고록을 작성하세요.</h1>
        </div>
      </section>

      <div className="two-column">
        <section className="panel">
          <h2>강의자료</h2>
          <div className="document-list">
            {materials.map((material) => (
              <article className="document-card" key={material.materialId}>
                <h3>{material.title}</h3>
                <p>{material.materialType}</p>
                <small>{material.fileUrl}</small>
              </article>
            ))}
          </div>
        </section>

        <form className="panel reflection-form" onSubmit={submit}>
          <h2>오늘의 회고록</h2>
          <label>
            잘 이해한 내용
            <textarea name="understood" value={form.understood} onChange={updateField}
              placeholder="개념과 실습 내용을 구체적으로 작성해 주세요." required />
          </label>
          <label>
            어려웠던 내용
            <textarea name="difficult" value={form.difficult} onChange={updateField}
              placeholder="헷갈리거나 다시 보고 싶은 내용을 작성해 주세요." required />
          </label>
          <label>
            추가 학습 내용
            <textarea name="wantsToLearn" value={form.wantsToLearn} onChange={updateField}
              placeholder="추가로 공부하고 싶은 내용을 작성해 주세요." />
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
