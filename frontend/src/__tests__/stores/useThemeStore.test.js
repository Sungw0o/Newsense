import { describe, it, expect, beforeEach } from 'vitest'
import { useThemeStore } from '../../stores/useThemeStore'

describe('useThemeStore — initTheme', () => {
  let store

  beforeEach(() => {
    localStorage.clear()
    document.documentElement.className = ''
    store = useThemeStore()
  })

  it('저장된 테마 없음: dark로 초기화하고 DOM에 dark 클래스 추가', () => {
    store.initTheme()

    expect(store.theme).toBe('dark')
    expect(document.documentElement.classList.contains('dark')).toBe(true)
  })

  it('저장된 테마 light: light로 초기화하고 DOM에 dark 클래스 없음', () => {
    localStorage.setItem('theme', 'light')

    store.initTheme()

    expect(store.theme).toBe('light')
    expect(document.documentElement.classList.contains('dark')).toBe(false)
  })

  it('저장된 테마 dark: dark로 초기화', () => {
    localStorage.setItem('theme', 'dark')

    store.initTheme()

    expect(store.theme).toBe('dark')
    expect(document.documentElement.classList.contains('dark')).toBe(true)
  })
})

describe('useThemeStore — toggleTheme', () => {
  let store

  beforeEach(() => {
    localStorage.clear()
    document.documentElement.className = ''
    store = useThemeStore()
  })

  it('dark → light: 테마 변경 및 localStorage 저장', () => {
    store.theme = 'dark'

    store.toggleTheme()

    expect(store.theme).toBe('light')
    expect(localStorage.getItem('theme')).toBe('light')
    expect(document.documentElement.classList.contains('dark')).toBe(false)
  })

  it('light → dark: 테마 변경 및 DOM에 dark 클래스 추가', () => {
    store.theme = 'light'

    store.toggleTheme()

    expect(store.theme).toBe('dark')
    expect(localStorage.getItem('theme')).toBe('dark')
    expect(document.documentElement.classList.contains('dark')).toBe(true)
  })
})
