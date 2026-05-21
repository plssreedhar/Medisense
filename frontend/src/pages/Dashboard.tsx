import styles from './Dashboard.module.css'

type Tab = 'summarize' | 'claimsense' | 'medichat' | 'dashboard'

type Props = {
  navigateTo: (tab: Tab) => void
}

function Dashboard({ navigateTo }: Props) {
  return (
    <div className={styles.page}>

      {/* Hero */}
      <section className={styles.hero}>
        <span className={styles.decor} style={{ top: '12%', left: '6%' }}>♥</span>
        <span className={styles.decor} style={{ top: '60%', left: '3%' }}>✚</span>
        <span className={styles.decor} style={{ top: '20%', right: '8%' }}>✚</span>
        <span className={styles.decor} style={{ top: '70%', right: '5%' }}>♥</span>
        <span className={styles.decor} style={{ top: '40%', left: '15%' }}>♥</span>
        <span className={styles.decor} style={{ top: '30%', right: '18%' }}>✚</span>
        <span className={styles.decor} style={{ top: '80%', left: '25%' }}>♥</span>
        <span className={styles.decor} style={{ top: '10%', right: '30%' }}>✚</span>
        <h1 className={styles.heroTitle}>Good day! Welcome to MediSense</h1>
        <p className={styles.heroSubtitle}>Your AI-powered health intelligence platform</p>
        <div className={styles.heroCtas}>
          <button className={styles.ctaPrimary} onClick={() => navigateTo('summarize')}>
            Summarize a Report
          </button>
          <button className={styles.ctaOutline} onClick={() => navigateTo('claimsense')}>
            Check a Claim
          </button>
          <button className={styles.ctaSecondary} onClick={() => navigateTo('medichat')}>
            Chat with MediSense
          </button>
        </div>
      </section>

      {/* Stats */}
      <div className={styles.statsWrapper}>
        <div className={styles.statsRow}>
          <div className={styles.statCard}>
            <span className={styles.statIcon}>📋</span>
            <span className={styles.statNum}>3</span>
            <span className={styles.statLabel}>Features Available</span>
          </div>
          <div className={styles.statCard}>
            <span className={styles.statIcon}>✨</span>
            <span className={styles.statNum}>7</span>
            <span className={styles.statLabel}>AI Agents</span>
          </div>
          <div className={styles.statCard}>
            <span className={styles.statIcon}>✅</span>
            <span className={styles.statNum}>100%</span>
            <span className={styles.statLabel}>AI Powered</span>
          </div>
        </div>
      </div>

      {/* Feature cards */}
      <section className={styles.featuresSection}>
        <h2 className={styles.sectionHeading}>Our Features</h2>
        <div className={styles.featureCards}>

          <div className={styles.featureCard}>
            <div className={styles.featureIconArea}>📄</div>
            <h3 className={styles.featureTitle}>MediSummarize</h3>
            <p className={styles.featureDesc}>
              Upload any medical report, prescription, or discharge summary.
              Our AI extracts key findings and explains them in plain language — no medical degree required.
            </p>
            <div className={styles.tags}>
              <span className={styles.tag}>Lab Reports</span>
              <span className={styles.tag}>Prescriptions</span>
              <span className={styles.tag}>Discharge Summaries</span>
            </div>
            <button className={styles.featureBtn} onClick={() => navigateTo('summarize')}>
              Get Started →
            </button>
          </div>

          <div className={styles.featureCard}>
            <div className={styles.featureIconArea}>🧾</div>
            <h3 className={styles.featureTitle}>ClaimSense</h3>
            <p className={styles.featureDesc}>
              Submit your hospital bills and let our 4-agent AI pipeline check them against your
              MediShield policy. Get instant verdicts — Claimable, Partial, or Excluded.
            </p>
            <div className={styles.tags}>
              <span className={styles.tag}>Hospital Bills</span>
              <span className={styles.tag}>Pharmacy</span>
              <span className={styles.tag}>Policy Check</span>
            </div>
            <button className={styles.featureBtn} onClick={() => navigateTo('claimsense')}>
              Get Started →
            </button>
          </div>

          <div className={styles.featureCard}>
            <div className={styles.featureIconArea}>💬</div>
            <h3 className={styles.featureTitle}>MediChat</h3>
            <p className={styles.featureDesc}>
              Multi-turn conversation in 7 Indian languages — upload reports or bills and ask
              follow-up questions. Your personal AI health assistant, always available.
            </p>
            <div className={styles.tags}>
              <span className={styles.tag}>7 Languages</span>
              <span className={styles.tag}>Multi-turn Chat</span>
              <span className={styles.tag}>Bill + Reports</span>
            </div>
            <button className={styles.featureBtn} onClick={() => navigateTo('medichat')}>
              Get Started →
            </button>
          </div>

        </div>
      </section>

      {/* How it works */}
      <section className={styles.howSection}>
        <h2 className={styles.sectionHeading}>How it works</h2>
        <div className={styles.steps}>
          <div className={styles.step}>
            <div className={styles.stepCircle}>1</div>
            <strong className={styles.stepTitle}>Upload</strong>
            <span className={styles.stepDesc}>Upload your PDF document</span>
          </div>
          <div className={styles.stepConnector} />
          <div className={styles.step}>
            <div className={styles.stepCircle}>2</div>
            <strong className={styles.stepTitle}>Analyze</strong>
            <span className={styles.stepDesc}>AI agents process and analyze</span>
          </div>
          <div className={styles.stepConnector} />
          <div className={styles.step}>
            <div className={styles.stepCircle}>3</div>
            <strong className={styles.stepTitle}>Understand</strong>
            <span className={styles.stepDesc}>Get plain-language results</span>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className={styles.footer}>
        MediSense — EPAM AI POC | Built with Spring AI + Claude
      </footer>

    </div>
  )
}

export default Dashboard
