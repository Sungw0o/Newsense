// 전역 테스트 셋업
// Pinia를 매 테스트마다 초기화하기 위한 설정
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach } from 'vitest'

beforeEach(() => {
  setActivePinia(createPinia())
})
