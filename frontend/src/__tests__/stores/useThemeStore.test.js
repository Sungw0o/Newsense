import { describe, it, expect, beforeEach } from 'vitest'
import { useThemeStore } from '../../stores/useThemeStore'

describe('useThemeStore - initTheme', () => {
  let store

  beforeEach(() => {
    localStorage.clear()
    document.documentElement.className = ''
    store = useThemeStore()
  })

  it('저장된 테마가 없으면 light로 초기화하고 dark 클래스를 제거한다', () => {
    store.initTheme()

    expect(store.theme).toBe('light')
    expect(document.documentElement.classList.contains('dark')).toBe(false)
  })

  it('저장된 테마가 light면 light로 초기화한다', () => {
    localStorage.setItem('theme', 'light')

    store.initTheme()

    expect(store.theme).toBe('light')
    expect(document.documentElement.classList.contains('dark')).toBe(false)
  })

  it('저장된 테마가 dark면 dark로 초기화하고 DOM에 dark 클래스를 추가한다', () => {
    localStorage.setItem('theme', 'dark')

    store.initTheme()

    expect(store.theme).toBe('dark')
    expect(document.documentElement.classList.contains('dark')).toBe(true)
  })
})

describe('useThemeStore - toggleTheme', () => {
  let store

  beforeEach(() => {
    localStorage.clear()
    document.documentElement.className = ''
    store = useThemeStore()
  })

  it('dark에서 light로 변경하고 localStorage와 DOM을 갱신한다', () => {
    store.theme = 'dark'

    store.toggleTheme()

    expect(store.theme).toBe('light')
    expect(localStorage.getItem('theme')).toBe('light')
    expect(document.documentElement.classList.contains('dark')).toBe(false)
  })

  it('light에서 dark로 변경하고 localStorage와 DOM을 갱신한다', () => {
    store.theme = 'light'

    store.toggleTheme()

    expect(store.theme).toBe('dark')
    expect(localStorage.getItem('theme')).toBe('dark')
    expect(document.documentElement.classList.contains('dark')).toBe(true)
  })
})
