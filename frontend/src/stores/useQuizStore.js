import { defineStore } from 'pinia'
import axiosInstance from '../api/axiosInstance'

export const useQuizStore = defineStore('quiz', {
  state: () => ({
    quizzes: [],
    currentQuizIndex: 0,
    answers: {}, // { quizId: answer }
    quizResults: null, // { score, totalQuestions, results: [ { quizId, isCorrect, explanation, correctAns } ] }
    isLoading: false,
  }),
  getters: {
    currentQuiz: (state) => state.quizzes[state.currentQuizIndex] || null,
    isFirstQuiz: (state) => state.currentQuizIndex === 0,
    isLastQuiz: (state) => state.currentQuizIndex === state.quizzes.length - 1,
    progressPercentage: (state) => {
      if (state.quizzes.length === 0) return 0
      return Math.round(((state.currentQuizIndex + 1) / state.quizzes.length) * 100)
    }
  },
  actions: {
    /**
     * 퀴즈 진행 상태 리셋
     */
    resetQuiz() {
      this.quizzes = []
      this.currentQuizIndex = 0
      this.answers = {}
      this.quizResults = null
    },

    /**
     * 특정 기사의 퀴즈 목록 로드
     */
    async fetchQuizzes(articleId) {
      this.resetQuiz()
      this.isLoading = true
      try {
        const response = await axiosInstance.get(`/articles/${articleId}/quiz`)
        this.quizzes = response.data || response
      } catch (error) {
        console.error('Fetch quizzes error:', error)
        throw error
      } finally {
        this.isLoading = false
      }
    },

    /**
     * 현재 문제에 대한 답변 저장
     */
    saveAnswer(quizId, answer) {
      this.answers[quizId] = answer
    },

    /**
     * 다음 문제로 이동
     */
    nextQuiz() {
      if (this.currentQuizIndex < this.quizzes.length - 1) {
        this.currentQuizIndex++
      }
    },

    /**
     * 이전 문제로 이동
     */
    prevQuiz() {
      if (this.currentQuizIndex > 0) {
        this.currentQuizIndex--
      }
    },

    /**
     * 퀴즈 정답 제출 및 채점 API 호출
     */
    async submitAnswers() {
      if (this.isLoading) return
      this.isLoading = true
      try {
        // 모든 퀴즈에 대한 정답 제출을 하나씩 보내거나 벌크로 보냄.
        // BE-007: POST /api/quiz/{quizId}/answer
        // 퀴즈 결과 리스트 생성
        const results = []
        let correctCount = 0

        for (const quiz of this.quizzes) {
          const answer = this.answers[quiz.id] ?? ''
          const response = await axiosInstance.post(`/quiz/${quiz.id}/answer`, { answer })
          const grading = response.data || response
          
          if (grading.correct) {
            correctCount++
          }

          results.push({
            quizId: quiz.id,
            question: quiz.question,
            type: quiz.type, // OX or MULTIPLE
            options: quiz.options,
            userAns: grading.userAnswer,
            correctAns: grading.correctAnswer,
            isCorrect: grading.correct,
            explanation: grading.explanation,
          })
        }

        this.quizResults = {
          score: correctCount,
          totalQuestions: this.quizzes.length,
          results,
        }

        return this.quizResults
      } catch (error) {
        console.error('Submit quiz answers error:', error)
        throw error
      } finally {
        this.isLoading = false
      }
    }
  }
})

export default useQuizStore
