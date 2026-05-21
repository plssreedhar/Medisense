import { useEffect, useRef, useState } from 'react'
import styles from './ClaimSense.module.css'

type Policy = { policyId: string; name: string }

interface RecentClaimItem {
  id: number
  fileName: string
  verdict: string
  explanation: string
  totalAmount: number | null
  claimableAmount: number | null
  createdAt: string
}

function timeAgo(iso: string) {
  const diff = Date.now() - new Date(iso).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1)  return 'just now'
  if (mins < 60) return `${mins}m ago`
  const hrs = Math.floor(mins / 60)
  if (hrs < 24)  return `${hrs}h ago`
  return `${Math.floor(hrs / 24)}d ago`
}

type LineItem = {
  description: string
  amount: number
  verdict: 'CLAIMABLE' | 'PARTIAL' | 'EXCLUDED'
  reason: string
}

type ClaimVerdict = {
  fileName: string
  verdict: string
  confidence: number | null
  ruleApplied: string | null
  explanation: string
  totalAmount?: number
  claimableAmount?: number
  lineItems?: LineItem[]
}

const STEPS = ['Policy Fetched', 'Bill Classified', 'Cross-referenced', 'Verdict Ready']

const LANGUAGES = [
  'English', 'Hindi', 'Tamil', 'Telugu', 'Kannada', 'Malayalam', 'Bengali', 'Marathi',
]

const fmt = (n: number) =>
  '₹' + n.toLocaleString('en-IN', { maximumFractionDigits: 2 })

function ClaimSense() {
  const [userName, setUserName] = useState('')
  const [policyId, setPolicyId] = useState('')
  const [policies, setPolicies] = useState<Policy[]>([])
  const [files, setFiles] = useState<File[]>([])
  const [language, setLanguage] = useState('English')
  const [loading, setLoading] = useState(false)
  const [loadingStep, setLoadingStep] = useState(0)
  const [results, setResults] = useState<ClaimVerdict[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [dragOver, setDragOver] = useState(false)
  const [recentClaims, setRecentClaims] = useState<RecentClaimItem[]>([])
  const [selectedClaimId, setSelectedClaimId] = useState<number | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const stepTimerRef = useRef<ReturnType<typeof setInterval> | null>(null)

  useEffect(() => {
    fetch('/api/claims/policies')
      .then(r => r.json())
      .then(setPolicies)
      .catch(() => {})
    fetch('/api/claims/recent')
      .then(r => r.ok ? r.json() : [])
      .then(setRecentClaims)
      .catch(() => {})
  }, [])

  useEffect(() => {
    if (loading) {
      setLoadingStep(0)
      stepTimerRef.current = setInterval(() => {
        setLoadingStep(s => Math.min(s + 1, STEPS.length - 1))
      }, 2000)
    } else {
      if (stepTimerRef.current) clearInterval(stepTimerRef.current)
    }
    return () => { if (stepTimerRef.current) clearInterval(stepTimerRef.current) }
  }, [loading])

  function addFiles(incoming: FileList | null) {
    if (!incoming) return
    const pdfs = Array.from(incoming).filter(f => f.name.toLowerCase().endsWith('.pdf'))
    setFiles(prev => {
      const names = new Set(prev.map(f => f.name))
      return [...prev, ...pdfs.filter(f => !names.has(f.name))]
    })
    setResults(null)
    setError(null)
  }

  function removeFile(name: string) {
    setFiles(prev => prev.filter(f => f.name !== name))
  }

  async function handleSubmit() {
    if (files.length === 0 || !userName.trim() || !policyId) return
    setLoading(true)
    setResults(null)
    setError(null)

    try {
      const formData = new FormData()
      formData.append('policyId', policyId)
      formData.append('userName', userName)
      formData.append('language', language)
      files.forEach(f => formData.append('bills', f))

      const res = await fetch('/api/claims/analyze', { method: 'POST', body: formData })
      if (res.ok) {
        setResults(await res.json())
        fetch('/api/claims/recent')
          .then(r => r.ok ? r.json() : [])
          .then(setRecentClaims)
          .catch(() => {})
      } else {
        setError((await res.text()) || 'An unexpected error occurred')
      }
    } catch {
      setError('An unexpected error occurred')
    } finally {
      setLoading(false)
    }
  }

  const isDisabled = !userName.trim() || !policyId || files.length === 0 || loading

  function verdictBannerClass(verdict: string) {
    if (verdict === 'CLAIMABLE') return styles.bannerCLAIMABLE
    if (verdict === 'EXCLUDED')  return styles.bannerEXCLUDED
    return styles.bannerPARTIAL
  }

  function verdictBannerText(verdict: string) {
    if (verdict === 'CLAIMABLE') return '✓ This bill is covered under your policy'
    if (verdict === 'EXCLUDED')  return '✗ This bill is not covered'
    return '⚠ Partial coverage applies'
  }

  return (
    <div className={styles.root}>

      {/* Sidebar */}
      <aside className={styles.sidebar}>
        <div className={styles.sidebarHeader}>
          <div className={styles.sidebarTitle}>Policy</div>
          <div className={styles.sidebarSelectWrapper}>
            <select
              className={styles.sidebarSelect}
              value={policyId}
              onChange={e => { setPolicyId(e.target.value); setResults(null); setError(null) }}
            >
              <option value="">— select policy —</option>
              {policies
                .filter(p => !p.policyId.startsWith('medishield'))
                .map(p => (
                  <option key={p.policyId} value={p.policyId}>{p.name}</option>
                ))}
            </select>
          </div>
        </div>
        <div className={styles.sidebarSection}>
          <div className={styles.sidebarSectionLabel}>Recent Claims</div>
          {recentClaims.length === 0 ? (
            <div className={styles.recentPlaceholder}>No recent claims</div>
          ) : (
            recentClaims.map(rc => (
              <div
                key={rc.id}
                className={`${styles.recentClaimItem} ${selectedClaimId === rc.id ? styles.recentClaimItemActive : ''}`}
                onClick={() => setSelectedClaimId(selectedClaimId === rc.id ? null : rc.id)}
              >
                <div className={styles.recentClaimName} title={rc.fileName}>
                  {rc.fileName.length > 22 ? rc.fileName.slice(0, 20) + '…' : rc.fileName}
                </div>
                <div className={styles.recentClaimMeta}>
                  <span className={`${styles.recentBadge} ${styles[('badge' + rc.verdict) as keyof typeof styles]}`}>
                    {rc.verdict}
                  </span>
                  <span>{timeAgo(rc.createdAt)}</span>
                </div>
                {selectedClaimId === rc.id && rc.explanation && (
                  <div className={styles.recentClaimDetail}>{rc.explanation}</div>
                )}
              </div>
            ))
          )}
        </div>
      </aside>

      {/* Main */}
      <div className={styles.main}>
        <div className={styles.topBar}>
          <div className={styles.topBarTitle}>⚕️ ClaimSense</div>
          <select
            className={styles.langSelect}
            value={language}
            onChange={e => setLanguage(e.target.value)}
          >
            {LANGUAGES.map(l => (
              <option key={l} value={l}>{l}</option>
            ))}
          </select>
        </div>

        <div className={styles.scrollArea}>

          {/* Form */}
          <div className={styles.formCard}>
            <div className={styles.fieldGroup}>
              <label className={styles.fieldLabel}>Your Name</label>
              <input
                type="text"
                className={styles.nameInput}
                placeholder="e.g. Priya Sharma"
                value={userName}
                onChange={e => { setUserName(e.target.value); setResults(null); setError(null) }}
              />
            </div>

            <div
              className={`${styles.dropZone} ${dragOver ? styles.dropZoneDragOver : ''}`}
              onClick={() => fileInputRef.current?.click()}
              onDragOver={e => { e.preventDefault(); setDragOver(true) }}
              onDragLeave={() => setDragOver(false)}
              onDrop={e => { e.preventDefault(); setDragOver(false); addFiles(e.dataTransfer.files) }}
            >
              <input
                ref={fileInputRef}
                type="file"
                accept=".pdf"
                multiple
                style={{ display: 'none' }}
                onChange={e => addFiles(e.target.files)}
              />
              <span className={styles.dropIcon}>📁</span>
              <span className={styles.dropText}>Drop PDF bills here or click to browse</span>
              <span className={styles.dropHint}>Multiple files supported</span>
            </div>

            {files.length > 0 && (
              <ul className={styles.fileList}>
                {files.map(f => (
                  <li key={f.name} className={styles.fileItem}>
                    <span className={styles.fileIcon}>📄</span>
                    <span className={styles.fileName}>{f.name}</span>
                    <button className={styles.removeBtn} onClick={() => removeFile(f.name)}>×</button>
                  </li>
                ))}
              </ul>
            )}

            <button className={styles.analyzeBtn} onClick={handleSubmit} disabled={isDisabled}>
              {loading ? 'Running AI pipeline…' : 'Analyze Claims'}
            </button>

            {loading && (
              <div className={styles.stepBar}>
                {STEPS.map((step, i) => (
                  <div key={step} className={styles.stepRow}>
                    <div className={`${styles.stepDot} ${i <= loadingStep ? styles.stepDotActive : ''}`} />
                    <span className={`${styles.stepLabel} ${i <= loadingStep ? styles.stepLabelActive : ''}`}>{step}</span>
                    {i < STEPS.length - 1 && <span className={styles.stepArrow}>→</span>}
                  </div>
                ))}
              </div>
            )}
          </div>

          {error && (
            <div className={styles.errorBar}>
              <span>⚠ {error}</span>
              <button onClick={() => setError(null)}>✕</button>
            </div>
          )}

          {results && results.map((r, i) => (
            <div key={i} className={styles.verdictCard}>
              <div className={styles.verdictCardHeader}>
                <span className={styles.verdictFileName}>{r.fileName}</span>
                <span className={`${styles.badge} ${styles[r.verdict as keyof typeof styles]}`}>
                  {r.verdict}
                </span>
              </div>

              <div className={styles.infoGrid}>
                {r.totalAmount != null && (
                  <div className={styles.infoCell}>
                    <span className={styles.infoCellLabel}>Total</span>
                    <span className={styles.infoCellValue}>{fmt(r.totalAmount)}</span>
                  </div>
                )}
                {r.claimableAmount != null && (
                  <div className={styles.infoCell}>
                    <span className={styles.infoCellLabel}>Claimable</span>
                    <span className={styles.infoCellValue}>{fmt(r.claimableAmount)}</span>
                  </div>
                )}
                {r.confidence != null && (
                  <div className={styles.infoCell}>
                    <span className={styles.infoCellLabel}>Confidence</span>
                    <span className={styles.infoCellValue}>{Math.round(Number(r.confidence) * 100)}%</span>
                  </div>
                )}
                {r.ruleApplied && (
                  <div className={styles.infoCell}>
                    <span className={styles.infoCellLabel}>Rule</span>
                    <span className={styles.infoCellValue}>{r.ruleApplied}</span>
                  </div>
                )}
              </div>

              <p className={styles.explanation}>{r.explanation}</p>

              {r.lineItems && r.lineItems.length > 0 && (
                <table className={styles.lineItemTable}>
                  <thead>
                    <tr>
                      <th>Charge</th>
                      <th>Amount</th>
                      <th>Status</th>
                      <th>Reason</th>
                    </tr>
                  </thead>
                  <tbody>
                    {r.lineItems.map((item, j) => (
                      <tr key={j}>
                        <td>{item.description}</td>
                        <td>{fmt(item.amount)}</td>
                        <td>
                          <span className={`${styles.lineTag} ${styles[item.verdict as keyof typeof styles]}`}>
                            {item.verdict}
                          </span>
                        </td>
                        <td className={styles.reasonCell}>{item.reason}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}

              <div className={`${styles.banner} ${verdictBannerClass(r.verdict)}`}>
                {verdictBannerText(r.verdict)}
              </div>
            </div>
          ))}

        </div>
      </div>

    </div>
  )
}

export default ClaimSense
