import { useEffect, useRef, useState } from 'react'
import styles from './MediSummarize.module.css'

interface SummarizeResult {
  extractedJson: string
  summary: string
}

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
}

const SUGGESTED = [
  'What are my key findings?',
  'Should I be concerned about anything?',
  'What medications am I prescribed?',
]

const LANGUAGES = [
  'English', 'Hindi', 'Tamil', 'Telugu', 'Kannada', 'Malayalam', 'Bengali', 'Marathi',
]

function formatBytes(n: number) {
  return n < 1024 * 1024 ? `${(n / 1024).toFixed(1)} KB` : `${(n / (1024 * 1024)).toFixed(1)} MB`
}

function getPatientName(json: string): string | null {
  try {
    const parsed = JSON.parse(json)
    return parsed.patient_name ?? null
  } catch {
    return null
  }
}

function MediSummarize() {
  const [file, setFile] = useState<File | null>(null)
  const [dragOver, setDragOver] = useState(false)
  const [language, setLanguage] = useState('English')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<SummarizeResult | null>(null)
  const [error, setError] = useState<string | null>(null)

  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([])
  const [chatInput, setChatInput] = useState('')
  const [chatLoading, setChatLoading] = useState(false)
  const chatEndRef = useRef<HTMLDivElement>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [chatMessages])

  function pickFile(f: File | null) {
    if (!f) return
    setFile(f)
    setResult(null)
    setError(null)
    setChatMessages([])
  }

  async function handleSubmit() {
    if (!file) return
    setLoading(true)
    setResult(null)
    setError(null)
    setChatMessages([])

    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('language', language)
      const res = await fetch('/api/summarize', { method: 'POST', body: formData })
      if (res.ok) {
        setResult(await res.json())
      } else {
        setError((await res.text()) || 'An unexpected error occurred')
      }
    } catch {
      setError('An unexpected error occurred')
    } finally {
      setLoading(false)
    }
  }

  async function sendChat(message: string) {
    if (!message.trim() || !result) return
    const userMsg: ChatMessage = { role: 'user', content: message }
    setChatMessages(prev => [...prev, userMsg])
    setChatInput('')
    setChatLoading(true)

    try {
      const res = await fetch('/api/summarize/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message, extractedJson: result.extractedJson, language }),
      })
      const data = res.ok ? await res.json() : null
      setChatMessages(prev => [
        ...prev,
        { role: 'assistant', content: data?.response ?? 'Sorry, I could not process that.' },
      ])
    } catch {
      setChatMessages(prev => [
        ...prev,
        { role: 'assistant', content: 'Sorry, an error occurred.' },
      ])
    } finally {
      setChatLoading(false)
    }
  }

  const patientName = result ? getPatientName(result.extractedJson) : null

  return (
    <div className={styles.root}>

      {/* Top bar */}
      <div className={styles.topBar}>
        <div className={styles.topBarTitle}>📄 MediSummarize</div>
        <select
          className={styles.langSelect}
          value={language}
          onChange={e => setLanguage(e.target.value)}
        >
          {LANGUAGES.map(l => <option key={l} value={l}>{l}</option>)}
        </select>
      </div>

      {/* Body — CSS Grid */}
      <div className={`${styles.body} ${result ? styles.bodyExpanded : ''}`}>

        {/* Upload panel */}
        <div className={styles.uploadPanel}>
          <div
            className={`${styles.dropZone} ${dragOver ? styles.dragOver : ''}`}
            onClick={() => fileInputRef.current?.click()}
            onDragOver={e => { e.preventDefault(); setDragOver(true) }}
            onDragLeave={() => setDragOver(false)}
            onDrop={e => { e.preventDefault(); setDragOver(false); pickFile(e.dataTransfer.files?.[0] ?? null) }}
          >
            <input
              ref={fileInputRef}
              type="file"
              accept=".pdf"
              style={{ display: 'none' }}
              onChange={e => pickFile(e.target.files?.[0] ?? null)}
            />
            {file ? (
              <div className={styles.fileChosen}>
                <div className={styles.fileCheck}>✓</div>
                <span className={styles.fileName}>{file.name}</span>
                <span className={styles.fileSize}>{formatBytes(file.size)}</span>
              </div>
            ) : (
              <>
                <span className={styles.dropIcon}>📄</span>
                <span className={styles.dropText}>Drop your PDF here or <span className={styles.dropLink}>click to browse</span></span>
                <span className={styles.dropHint}>Lab Reports · Prescriptions · Discharge Summaries</span>
              </>
            )}
          </div>

          <button
            className={styles.analyzeBtn}
            onClick={handleSubmit}
            disabled={!file || loading}
          >
            {loading ? (
              <><span className={styles.spinner} /> Analyzing…</>
            ) : 'Analyze Document'}
          </button>

          {error && (
            <div className={styles.errorBar}>
              <span>⚠ {error}</span>
              <button onClick={() => setError(null)}>✕</button>
            </div>
          )}

          {result && (
            <details className={styles.rawSection}>
              <summary>{'{ }'} View Raw Extracted Data</summary>
              <pre>{result.extractedJson}</pre>
            </details>
          )}
        </div>

        {/* Summary panel — visible only after analysis */}
        {result ? (
          <div className={styles.summaryPanel}>
            <div className={styles.summaryCard}>
              <div className={styles.summaryCardHeader}>
                <span className={styles.summaryCardTitle}>📋 Summary</span>
                {patientName && <span className={styles.patientBadge}>👤 {patientName}</span>}
              </div>
              <div className={styles.summaryCardBody}>
                <p className={styles.summaryText}>{result.summary}</p>
              </div>
            </div>
          </div>
        ) : loading ? (
          <div className={styles.summaryPanel}>
            <div className={styles.summaryCard}>
              <div className={styles.loadingState}>
                <span className={styles.spinner} />
                <span>AI agents are reading your document…</span>
              </div>
            </div>
          </div>
        ) : (
          <div className={styles.summaryPanel}>
            <div className={styles.summaryPlaceholder}>
              <span>🩺</span>
              <p>Your summary will appear here after analysis</p>
            </div>
          </div>
        )}

        {/* Chat panel — full width bottom, only after analysis */}
        {(result || chatMessages.length > 0) && (
          <div className={styles.chatPanel}>
            <div className={styles.chatHeader}>
              <span>💬</span>
              <span className={styles.chatHeaderTitle}>Ask about your report</span>
            </div>

            <div className={styles.chatMessages}>
              {chatMessages.length === 0 && (
                <div className={styles.chips}>
                  {SUGGESTED.map(q => (
                    <button key={q} className={styles.chip} onClick={() => sendChat(q)}>{q}</button>
                  ))}
                </div>
              )}
              {chatMessages.map((m, i) => (
                <div key={i} className={`${styles.bubble} ${m.role === 'user' ? styles.bubbleUser : styles.bubbleAssistant}`}>
                  {m.content}
                </div>
              ))}
              {chatLoading && (
                <div className={`${styles.bubble} ${styles.bubbleAssistant}`}>
                  <div className={styles.typingDots}>
                    <span className={styles.typingDot} />
                    <span className={styles.typingDot} />
                    <span className={styles.typingDot} />
                  </div>
                </div>
              )}
              <div ref={chatEndRef} />
            </div>

            <div className={styles.chatInputRow}>
              <input
                type="text"
                className={styles.chatInput}
                placeholder="Ask a question about your report…"
                value={chatInput}
                onChange={e => setChatInput(e.target.value)}
                onKeyDown={e => { if (e.key === 'Enter') sendChat(chatInput) }}
                disabled={chatLoading || !result}
              />
              <button
                className={styles.chatSendBtn}
                onClick={() => sendChat(chatInput)}
                disabled={!chatInput.trim() || chatLoading || !result}
              >
                ➤
              </button>
            </div>
          </div>
        )}

      </div>
    </div>
  )
}

export default MediSummarize
