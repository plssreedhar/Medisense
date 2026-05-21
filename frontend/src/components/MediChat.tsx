import { useState, useEffect, useRef } from 'react';
import styles from './MediChat.module.css';

const API = 'http://localhost:8080/api';

interface LineItem {
  description: string;
  amount: number;
  verdict: 'CLAIMABLE' | 'PARTIAL' | 'EXCLUDED';
  reason: string;
}

interface ClaimVerdict {
  fileName: string;
  verdict: 'CLAIMABLE' | 'PARTIAL' | 'EXCLUDED';
  totalAmount: number;
  claimableAmount: number;
  lineItems: LineItem[];
  explanation: string;
}

interface Message {
  role: 'user' | 'assistant';
  content: string;
  contentType: 'text' | 'summary' | 'verdict' | 'question';
  attachedFile?: string;
  verdicts?: ClaimVerdict[];
}

interface Session {
  id: string;
  sessionName: string;
  updatedAt: string;
}

const CHIPS = [
  'Upload a medical report',
  'Check my hospital bill',
  'What does my policy cover?',
  'Explain my test results',
];

export default function MediChat() {
  const [sessions, setSessions] = useState<Session[]>([]);
  const [activeSessionId, setActiveSessionId] = useState<string | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [language, setLanguage] = useState('English');
  const [languages, setLanguages] = useState<string[]>(['English']);
  const [loading, setLoading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    fetch(`${API}/chat/languages`)
      .then(r => r.json())
      .then(setLanguages)
      .catch(() => {});
    loadSessions();
  }, []);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  function loadSessions() {
    fetch(`${API}/chat/sessions`)
      .then(r => r.json())
      .then(setSessions)
      .catch(() => {});
  }

  function loadHistory(sessionId: string) {
    fetch(`${API}/chat/sessions/${sessionId}/history`)
      .then(r => r.json())
      .then((msgs: Array<{role: string; content: string; contentType: string; attachedFile?: string}>) => {
        setMessages(msgs.map(m => ({
          role: m.role as 'user' | 'assistant',
          content: m.content,
          contentType: (m.contentType || 'text') as Message['contentType'],
          attachedFile: m.attachedFile,
        })));
      })
      .catch(() => {});
  }

  function startNewChat() {
    setActiveSessionId(null);
    setMessages([]);
    setFile(null);
    setInput('');
  }

  function selectSession(id: string) {
    setActiveSessionId(id);
    loadHistory(id);
  }

  async function sendMessage(text?: string) {
    const msgText = text ?? input.trim();
    if (!msgText && !file) return;

    const userMsg: Message = {
      role: 'user',
      content: msgText || '(file upload)',
      contentType: 'text',
      attachedFile: file?.name,
    };
    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setLoading(true);

    const form = new FormData();
    if (activeSessionId) form.append('sessionId', activeSessionId);
    if (msgText) form.append('message', msgText);
    if (file) form.append('file', file);
    form.append('language', language);
    setFile(null);

    try {
      const res = await fetch(`${API}/chat/message`, { method: 'POST', body: form });
      if (!res.ok) {
        const errText = await res.text().catch(() => res.statusText);
        setMessages(prev => [...prev, {
          role: 'assistant' as const,
          content: `⚠️ Service error (${res.status}): ${errText || 'Please try again.'}`,
          contentType: 'text' as const,
        }]);
        return;
      }
      const data = await res.json();

      setActiveSessionId(data.sessionId);
      const assistantMsg: Message = {
        role: 'assistant',
        content: data.message,
        contentType: data.contentType,
        verdicts: data.verdicts,
      };
      setMessages(prev => [...prev, assistantMsg]);
      loadSessions();
    } catch {
      setMessages(prev => [...prev, {
        role: 'assistant',
        content: '⚠️ Could not reach the server. Please check your connection and try again.',
        contentType: 'text',
      }]);
    } finally {
      setLoading(false);
    }
  }

  function handleKeyDown(e: React.KeyboardEvent) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  }

  function fmt(n: number) {
    return '₹' + n.toLocaleString('en-IN', { maximumFractionDigits: 2 });
  }

  const isEmpty = messages.length === 0;

  return (
    <div className={styles.root}>
      {/* Sidebar */}
      <aside className={styles.sidebar}>
        <div className={styles.sidebarHeader}>
          <button className={styles.newChatBtn} onClick={startNewChat}>
            <span>✦</span> New conversation
          </button>
        </div>
        <div className={styles.sidebarSectionLabel}>Recent</div>
        <div className={styles.sessionList}>
          {sessions.map(s => (
            <div
              key={s.id}
              className={`${styles.sessionItem} ${s.id === activeSessionId ? styles.active : ''}`}
              onClick={() => selectSession(s.id)}
            >
              <div className={styles.sessionItemName}>{s.sessionName || 'Conversation'}</div>
              <div className={styles.sessionItemDate}>
                {new Date(s.updatedAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short' })}
              </div>
            </div>
          ))}
        </div>
      </aside>

      {/* Main */}
      <div className={styles.main}>
        {/* Top bar */}
        <div className={styles.topBar}>
          <span className={styles.topBarTitle}>🏥 MediSense Chat</span>
          <select
            className={styles.langSelect}
            value={language}
            onChange={e => setLanguage(e.target.value)}
          >
            {languages.map(l => <option key={l} value={l}>{l}</option>)}
          </select>
        </div>

        {/* Messages / Empty state */}
        {isEmpty ? (
          <div className={styles.emptyState}>
            <div className={styles.emptyIcon}>🏥</div>
            <div className={styles.emptyTitle}>How can I help you today?</div>
            <div className={styles.emptyDesc}>
              Upload a medical report or hospital bill, or ask about your coverage. I speak 7 Indian languages.
            </div>
            <div className={styles.chips}>
              {CHIPS.map(c => (
                <button key={c} className={styles.chip} onClick={() => sendMessage(c)}>{c}</button>
              ))}
            </div>
          </div>
        ) : (
          <div className={styles.messages}>
            {messages.map((m, i) => (
              <div key={i} className={`${styles.messageRow} ${m.role === 'user' ? styles.user : ''}`}>
                <div className={`${styles.avatar} ${m.role === 'assistant' ? styles.avatarAssistant : styles.avatarUser}`}>
                  {m.role === 'assistant' ? '✦' : '👤'}
                </div>
                <div>
                  <div className={`${styles.bubble} ${m.role === 'assistant' ? styles.bubbleAssistant : styles.bubbleUser}`}>
                    {m.content}
                    {m.attachedFile && (
                      <div className={styles.attachmentTag}>📎 {m.attachedFile}</div>
                    )}
                  </div>
                  {/* Verdict cards */}
                  {m.verdicts && m.verdicts.map((v, vi) => (
                    <div key={vi} className={styles.verdictCard}>
                      <div className={styles.verdictHeader}>
                        <span className={`${styles.verdictBadge} ${styles[v.verdict]}`}>
                          {v.verdict === 'CLAIMABLE' ? '✓' : v.verdict === 'PARTIAL' ? '◑' : '✕'} {v.verdict}
                        </span>
                        <span className={styles.verdictAmounts}>
                          Claimable: <strong>{fmt(v.claimableAmount)}</strong> / {fmt(v.totalAmount)}
                        </span>
                      </div>
                      {v.lineItems && v.lineItems.length > 0 && (
                        <table className={styles.lineItemTable}>
                          <thead>
                            <tr>
                              <th>Charge</th>
                              <th>Amount</th>
                              <th>Status</th>
                            </tr>
                          </thead>
                          <tbody>
                            {v.lineItems.map((li, lii) => (
                              <tr key={lii}>
                                <td>{li.description}</td>
                                <td>{fmt(li.amount)}</td>
                                <td>
                                  <span className={`${styles.lineTag} ${styles[li.verdict]}`}>
                                    {li.verdict}
                                  </span>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            ))}
            {loading && (
              <div className={`${styles.messageRow}`}>
                <div className={`${styles.avatar} ${styles.avatarAssistant}`}>✦</div>
                <div className={`${styles.bubble} ${styles.bubbleAssistant}`}>
                  <div className={styles.typing}>
                    <div className={styles.dot} /><div className={styles.dot} /><div className={styles.dot} />
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>
        )}

        {/* Input bar */}
        <div className={styles.inputBar}>
          {file && (
            <div className={styles.filePreview}>
              📎 {file.name}
              <button className={styles.filePreviewRemove} onClick={() => setFile(null)}>✕</button>
            </div>
          )}
          <div className={styles.inputRow}>
            <button className={styles.attachBtn} onClick={() => fileInputRef.current?.click()} title="Attach PDF">
              📎
            </button>
            <input
              ref={fileInputRef}
              type="file"
              accept=".pdf"
              style={{ display: 'none' }}
              onChange={e => setFile(e.target.files?.[0] ?? null)}
            />
            <textarea
              className={styles.textInput}
              placeholder="Ask about your report, bill, or coverage…"
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              rows={1}
            />
            <button
              className={styles.sendBtn}
              onClick={() => sendMessage()}
              disabled={loading || (!input.trim() && !file)}
            >
              ➤
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
