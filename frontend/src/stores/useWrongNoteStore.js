import { defineStore } from 'pinia'
import { wrongNoteApi } from '../api/wrongNoteApi'

export const useWrongNoteStore = defineStore('wrongNote', {
  state: () => ({
    wrongNotes: [],
    isLoading: false,
    error: null
  }),
  actions: {
    /**
     * 오답노트 목록 로드
     */
    async fetchWrongNotes(params = {}) {
      this.isLoading = true
      this.error = null
      try {
        const response = await wrongNoteApi.getWrongNotes(params)
        const data = response.data || response
        this.wrongNotes = Array.isArray(data.content) ? data.content : (Array.isArray(data) ? data : [])
      } catch (err) {
        this.error = err.message || '오답노트를 불러오지 못했습니다.'
        throw err
      } finally {
        this.isLoading = false
      }
    },

    /**
     * 오답 해결 상태 토글
     */
    async toggleResolve(id) {
      try {
        await wrongNoteApi.toggleResolve(id)
        // 로컬 상태에서 해당 오답노트의 해결 여부 반전
        const note = this.wrongNotes.find(n => n.id === id)
        if (note) {
          note.isResolved = !note.isResolved
        }
      } catch (err) {
        console.error('Toggle resolve error:', err)
        throw err
      }
    },

    /**
     * 오답 삭제
     */
    async deleteWrongNote(id) {
      try {
        await wrongNoteApi.deleteWrongNote(id)
        // 로컬 상태에서 즉시 제거
        this.wrongNotes = this.wrongNotes.filter(n => n.id !== id)
      } catch (err) {
        console.error('Delete wrong note error:', err)
        throw err
      }
    }
  }
})

export default useWrongNoteStore
