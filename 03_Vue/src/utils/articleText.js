const sentencePattern = /[^.!?。！？\n]+[.!?。！？]?/g

const normalizeSpaces = (value) => value.replace(/\s+/g, ' ').trim()

const splitSentences = (text) => {
  const matches = text.match(sentencePattern) ?? []
  return matches
    .map(normalizeSpaces)
    .filter(Boolean)
}

export const splitArticleParagraphs = (content) => {
  if (!content || !content.trim()) return []

  const paragraphBlocks = content
    .split(/\n{2,}/)
    .map(normalizeSpaces)
    .filter(Boolean)

  if (paragraphBlocks.length >= 3) {
    return paragraphBlocks
  }

  const sentences = splitSentences(content)
  if (sentences.length === 0) {
    return paragraphBlocks
  }

  const paragraphs = []
  for (let index = 0; index < sentences.length; index += 3) {
    paragraphs.push(sentences.slice(index, index + 3).join(' '))
  }
  return paragraphs
}

export const splitSummaryItems = (summary) => {
  if (!summary || !summary.trim()) return []

  const lineItems = summary
    .split(/\n+/)
    .map((line) => line.replace(/^\s*\d+[.)]\s*/, '').trim())
    .filter(Boolean)

  if (lineItems.length > 1) {
    return lineItems.slice(0, 3)
  }

  return splitSentences(summary).slice(0, 3)
}

export const splitStructuredSummaryItems = (summary) => {
  const labels = ['핵심 사건', '주요 수치', '경제적 의미']
  const items = splitSummaryItems(summary)
  return labels.map((label, index) => ({
    label,
    text: items[index] ?? ''
  })).filter(item => item.text)
}
