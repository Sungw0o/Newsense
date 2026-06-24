import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useQuizStore } from '../../stores/useQuizStore'
import axiosInstance from '../../api/axiosInstance'

vi.mock('../../api/axiosInstance', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

describe('useQuizStore DTO contract', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('submits answers with the backend QuizAnswerRequest field and maps QuizAnswerResponse', async () => {
    const store = useQuizStore()
    store.quizzes = [
      {
        id: 10,
        type: 'OX',
        question: '금리가 오르면 대출 이자는 증가한다.',
        options: ['O', 'X'],
      },
    ]
    store.saveAnswer(10, 'O')

    axiosInstance.post.mockResolvedValue({
      success: true,
      data: {
        answerId: 100,
        quizId: 10,
        articleId: 1,
        userAnswer: 'O',
        correctAnswer: 'O',
        correct: true,
        explanation: '기준금리 상승은 대출금리 상승으로 이어질 수 있습니다.',
        wrongNoteRecorded: false,
        submittedAt: '2026-06-24T09:00:00',
      },
    })

    const result = await store.submitAnswers()

    expect(axiosInstance.post).toHaveBeenCalledWith('/quiz/10/answer', { answer: 'O' })
    expect(result.score).toBe(1)
    expect(result.results).toEqual([
      expect.objectContaining({
        quizId: 10,
        userAns: 'O',
        correctAns: 'O',
        isCorrect: true,
        explanation: '기준금리 상승은 대출금리 상승으로 이어질 수 있습니다.',
      }),
    ])
  })
})
