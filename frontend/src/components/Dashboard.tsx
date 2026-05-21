import styles from './Dashboard.module.css'

type Tab = 'dashboard' | 'summarize' | 'claimsense'

type Props = {
  navigateTo: (tab: Tab) => void
}

function Dashboard({ navigateTo }: Props) {
  return (
    <div className={styles.container}>
      <div className={styles.hero}>
        <div className={styles.eyebrow}>
          <span className={styles.eyebrowDot} />
          AI-Powered · EPAM POC
        </div>
        <h1 className={styles.heroTitle}>
          Good day! Welcome to <span>MediSense</span>
        </h1>
        <p className={styles.heroSub}>Your AI-powered health intelligence platform</p>
        <div className={styles.heroBtns}>
          <button className={styles.btnPrimary} onClick={() => navigateTo('summarize')}>
            Summarize a Report
          </button>
          <button className={styles.btnSecondary} onClick={() => navigateTo('claimsense')}>
            Check a Claim
          </button>
        </div>
      </div>

      <div className={styles.cards}>
        <div className={styles.card} onClick={() => navigateTo('summarize')}>
          <div className={styles.cardIcon}>📄</div>
          <h2 className={styles.cardTitle}>MediSummarize</h2>
          <p className={styles.cardDesc}>
            Upload any medical PDF — lab report, prescription, or discharge summary — and get a
            clear, plain-English explanation in seconds.
          </p>
          <span className={styles.cardLink}>Try MediSummarize →</span>
        </div>

        <div className={styles.card} onClick={() => navigateTo('claimsense')}>
          <div className={styles.cardIcon}>🧾</div>
          <h2 className={styles.cardTitle}>ClaimSense</h2>
          <p className={styles.cardDesc}>
            Submit your medical bills and let AI cross-reference them against your MediShield
            policy to instantly assess claimability.
          </p>
          <span className={styles.cardLink}>Try ClaimSense →</span>
        </div>
      </div>

      <div className={styles.howSection}>
        <h2 className={styles.sectionTitle}>How it works</h2>
        <div className={styles.steps}>
          <div className={styles.step}>
            <div className={styles.stepNum}>1</div>
            <div className={styles.stepText}>
              <strong>Upload your document</strong>
              <span>PDF lab report, bill, or prescription</span>
            </div>
          </div>
          <div className={styles.stepArrow}>→</div>
          <div className={styles.step}>
            <div className={styles.stepNum}>2</div>
            <div className={styles.stepText}>
              <strong>AI reads &amp; understands</strong>
              <span>Claude extracts key data instantly</span>
            </div>
          </div>
          <div className={styles.stepArrow}>→</div>
          <div className={styles.step}>
            <div className={styles.stepNum}>3</div>
            <div className={styles.stepText}>
              <strong>Get your answer</strong>
              <span>Plain-English summary or claim verdict</span>
            </div>
          </div>
        </div>
      </div>

      <p className={styles.disclaimer}>
        MediSense provides AI-assisted assessments for informational purposes only.
        Always consult your insurance provider for final claim decisions.
      </p>
    </div>
  )
}

export default Dashboard
