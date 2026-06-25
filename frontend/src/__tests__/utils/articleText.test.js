import { describe, expect, it } from 'vitest'
import { splitArticleParagraphs, splitSummaryItems } from '../../utils/articleText'

describe('articleText utilities', () => {
  describe('splitArticleParagraphs', () => {
    it('keeps explicit paragraph blocks when enough paragraphs exist', () => {
      const content = '첫 문단입니다.\n\n두 번째 문단입니다.\n\n세 번째 문단입니다.'

      expect(splitArticleParagraphs(content)).toEqual([
        '첫 문단입니다.',
        '두 번째 문단입니다.',
        '세 번째 문단입니다.'
      ])
    })

    it('groups sentences when crawled content has too few paragraph breaks', () => {
      const content = '첫 문장입니다. 둘째 문장입니다. 셋째 문장입니다. 넷째 문장입니다.'

      expect(splitArticleParagraphs(content)).toEqual([
        '첫 문장입니다. 둘째 문장입니다. 셋째 문장입니다.',
        '넷째 문장입니다.'
      ])
    })
  })

  describe('splitSummaryItems', () => {
    it('uses newline-separated summary lines first', () => {
      const summary = '1. 변화가 발생했다.\n2. 근거는 3% 상승이다.\n3. 시장 영향이 커졌다.'

      expect(splitSummaryItems(summary)).toEqual([
        '변화가 발생했다.',
        '근거는 3% 상승이다.',
        '시장 영향이 커졌다.'
      ])
    })

    it('falls back to sentence splitting when summary is one line', () => {
      const summary = '변화가 발생했다. 근거는 3% 상승이다. 시장 영향이 커졌다. 추가 문장이다.'

      expect(splitSummaryItems(summary)).toEqual([
        '변화가 발생했다.',
        '근거는 3% 상승이다.',
        '시장 영향이 커졌다.'
      ])
    })
  })
})
