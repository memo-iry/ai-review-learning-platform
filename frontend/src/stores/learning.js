import { reactive } from 'vue'

export const learningState = reactive({
  userId: 2,
  lecture: null,
  reflection: null,
  analysis: null,
})

export function selectLecture(lecture) {
  learningState.lecture = lecture
  learningState.reflection = null
  learningState.analysis = null
}

export function completeAnalysis(reflection, analysis) {
  learningState.reflection = reflection
  learningState.analysis = analysis
}

export function resetLearning() {
  learningState.lecture = null
  learningState.reflection = null
  learningState.analysis = null
}
