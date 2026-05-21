import { useEffect, useState } from 'react';
import styles from './AppShell.module.css';
import MediSummarize from './MediSummarize';
import ClaimSense from './ClaimSense';
import MediChat from './MediChat';
import Dashboard from '../pages/Dashboard';

export type Tab = 'dashboard' | 'summarize' | 'claimsense' | 'medichat';

interface Props {
  onLogout: () => void;
}

interface RecentAnalysisItem {
  id: number
  source: 'ClaimSense' | 'MediSummarize'
  fileName: string
  verdict: string | null
  detail: string | null
  createdAt: string
}

const NAV_ITEMS: { id: Tab; label: string; emoji: string; isNew?: boolean }[] = [
  { id: 'dashboard',  label: 'Dashboard',      emoji: '⊞' },
  { id: 'medichat',   label: 'MediChat',        emoji: '💬', isNew: true },
  { id: 'summarize',  label: 'MediSummarize',   emoji: '📄' },
  { id: 'claimsense', label: 'ClaimSense',      emoji: '⚕️' },
];

const TAB_TITLES: Record<Tab, string> = {
  dashboard:  'Dashboard',
  medichat:   'MediChat',
  summarize:  'MediSummarize',
  claimsense: 'ClaimSense',
};

function verdictColor(verdict: string | null) {
  if (!verdict) return undefined;
  if (verdict === 'CLAIMABLE') return '#10b981';
  if (verdict === 'EXCLUDED')  return '#ef4444';
  return '#f59e0b';
}

function timeAgo(iso: string) {
  const diff = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1)  return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24)  return `${hrs}h ago`;
  return `${Math.floor(hrs / 24)}d ago`;
}

export default function AppShell({ onLogout }: Props) {
  const [tab, setTab] = useState<Tab>('dashboard');
  const [recentAnalyses, setRecentAnalyses] = useState<RecentAnalysisItem[]>([]);
  const [selectedItem, setSelectedItem] = useState<RecentAnalysisItem | null>(null);

  useEffect(() => {
    fetch('/api/recent-analyses')
      .then(r => r.ok ? r.json() : [])
      .then(setRecentAnalyses)
      .catch(() => {});
  }, []);

  return (
    <>
      <div className={styles.shell}>

        <aside className={styles.sidebar}>

          <div className={styles.sidebarDecor} aria-hidden="true">
            <span>+</span>
            <span>+</span>
            <span>○</span>
            <span>+</span>
            <span>○</span>
            <span>+</span>
          </div>

          <div className={styles.logoArea}>
            <div className={styles.logoMark}>M</div>
            <div>
              <div className={styles.logoText}>MediSense</div>
              <div className={styles.logoSub}>EPAM AI POC</div>
            </div>
          </div>

          <nav className={styles.nav}>
            <div className={styles.navLabel}>Navigation</div>
            {NAV_ITEMS.map(item => (
                <button
                    key={item.id}
                    className={`${styles.navItem} ${tab === item.id ? styles.navItemActive : ''}`}
                    onClick={() => setTab(item.id)}
                >
                  <span className={styles.navIcon}>{item.emoji}</span>
                  <span className={styles.navLabel2}>{item.label}</span>
                  {item.isNew && <span className={styles.newDot} />}
                </button>
            ))}
          </nav>

          <div className={styles.sessionsArea}>
            <div className={styles.navLabel}>Recent Analyses</div>
            {recentAnalyses.length === 0 ? (
              <div className={styles.sessionSub} style={{ padding: '8px 10px', fontStyle: 'italic' }}>
                No analyses yet
              </div>
            ) : (
              recentAnalyses.map((item, i) => (
                <div
                  key={i}
                  className={`${styles.sessionItem} ${selectedItem?.id === item.id ? styles.sessionActive : ''}`}
                  onClick={() => setSelectedItem(item)}
                  style={{ cursor: 'pointer' }}
                >
                  <div className={styles.sessionLabel} title={item.fileName}>
                    {item.fileName.length > 24 ? item.fileName.slice(0, 22) + '…' : item.fileName}
                  </div>
                  <div className={styles.sessionSub} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span>{item.source} · {timeAgo(item.createdAt)}</span>
                    {item.verdict && (
                      <span style={{
                        fontSize: '9px', fontWeight: 700, padding: '1px 5px',
                        borderRadius: '4px', background: verdictColor(item.verdict) + '22',
                        color: verdictColor(item.verdict),
                      }}>
                        {item.verdict}
                      </span>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>

          <div className={styles.sidebarFooter}>
            <button className={styles.signOutBtn} onClick={onLogout}>
              Sign Out
            </button>
          </div>

        </aside>

        <div className={styles.contentArea}>
          <header className={styles.topbar}>
            <div className={styles.topbarTitle}>{TAB_TITLES[tab]}</div>
            <div className={styles.topbarUser}>demo@medisense.ai</div>
          </header>
          <main className={styles.main}>
            {tab === 'dashboard'  && <Dashboard navigateTo={setTab} />}
            {tab === 'summarize'  && <MediSummarize />}
            {tab === 'claimsense' && <ClaimSense />}
            {tab === 'medichat'   && <MediChat />}
          </main>
        </div>

      </div>

      {selectedItem && (
        <div className={styles.modalBackdrop} onClick={() => setSelectedItem(null)}>
          <div className={styles.modalCard} onClick={e => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <div className={styles.modalTitle}>{selectedItem.fileName}</div>
              <button className={styles.modalClose} onClick={() => setSelectedItem(null)}>✕</button>
            </div>
            <div className={styles.modalMeta}>
              MediSummarize · {timeAgo(selectedItem.createdAt)}
            </div>
            <div className={styles.modalBody}>{selectedItem.detail ?? 'No summary available.'}</div>
          </div>
        </div>
      )}
    </>
  );
}
