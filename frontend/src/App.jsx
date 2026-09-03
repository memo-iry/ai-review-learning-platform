import { useState } from 'react'
import Layout from './components/Layout.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import ReflectionPage from './pages/ReflectionPage.jsx'
import AnalysisPage from './pages/AnalysisPage.jsx'
import ReviewPage from './pages/ReviewPage.jsx'
import GrowthPage from './pages/GrowthPage.jsx'

export default function App() {
  const [screen, setScreen] = useState('dashboard')
  const [course, setCourse] = useState(null)
  const [analysis, setAnalysis] = useState(null)

  const startCourse = (selectedCourse) => {
    setCourse(selectedCourse)
    setScreen('reflection')
  }

  const completeAnalysis = (result) => {
    setAnalysis(result)
    setScreen('analysis')
  }

  const currentStep = {
    dashboard: '대시보드',
    reflection: '회고록',
    analysis: 'AI 분석',
    review: '복습자료',
    growth: '내 성장',
  }[screen]

  return (
    <Layout currentStep={currentStep}>
      {screen === 'dashboard' && <DashboardPage onStart={startCourse} />}
      {screen === 'reflection' && course && (
        <ReflectionPage course={course} onAnalyzed={completeAnalysis} onBack={() => setScreen('dashboard')} />
      )}
      {screen === 'analysis' && analysis && (
        <AnalysisPage analysis={analysis} onOpenReview={() => setScreen('review')} />
      )}
      {screen === 'review' && analysis?.reviewMaterial && (
        <ReviewPage review={analysis.reviewMaterial} onComplete={() => setScreen('growth')} />
      )}
      {screen === 'growth' && <GrowthPage onDashboard={() => setScreen('dashboard')} />}
    </Layout>
  )
}

