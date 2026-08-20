#!/usr/bin/env python3
"""Generate GitHub Pages index from versions.json (releases + commit messages + times)."""

from __future__ import annotations

import html
import json
import sys
from datetime import datetime
from pathlib import Path

GROUP_ID = "com.corecmp"
ARTIFACT_ID = "shared"
SITE_URL = "https://deepak-choudharyy.github.io/CoreCMP/"
MAVEN_REPO_URL = "https://deepak-choudharyy.github.io/CoreCMP/maven-repo/"
GITHUB_REPO = "https://github.com/Deepak-Choudharyy/CoreCMP"


def format_when(iso_value: str) -> str:
    if not iso_value:
        return "—"
    try:
        normalized = iso_value.replace("Z", "+00:00")
        dt = datetime.fromisoformat(normalized)
        return dt.strftime("%d %b %Y, %H:%M UTC")
    except ValueError:
        return iso_value


def calculate_update_frequency(releases: list[dict]) -> str:
    valid_times = []
    for r in releases:
        pub = r.get("publishedAt")
        if pub:
            try:
                pub_clean = pub.replace("Z", "+00:00")
                dt = datetime.fromisoformat(pub_clean)
                valid_times.append(dt)
            except Exception:
                pass
    if len(valid_times) < 2:
        return "Initial release cycle"
    
    valid_times.sort()
    intervals = []
    for i in range(len(valid_times) - 1):
        diff = (valid_times[i+1] - valid_times[i]).total_seconds()
        intervals.append(diff)
        
    avg_seconds = sum(intervals) / len(intervals)
    avg_days = avg_seconds / 86400.0
    
    if avg_days < 0.04:  # less than ~1 hour
        minutes = avg_days * 1440.0
        return f"Every {max(1, int(minutes))} mins"
    elif avg_days < 1:
        hours = avg_days * 24.0
        return f"Every {hours:.1f} hours"
    elif avg_days < 7:
        return f"Every {avg_days:.1f} days"
    else:
        weeks = avg_days / 7.0
        return f"Every {weeks:.1f} weeks"


def load_releases(data: dict) -> list[dict]:
    releases = list(data.get("releases") or [])
    if not releases:
        return [
            {
                "version": version,
                "message": f"Release CoreCmp {version}",
                "publishedAt": "",
                "commit": "",
            }
            for version in data.get("versions") or []
        ]

    latest = data.get("latest") or ""
    releases.sort(key=lambda r: r.get("publishedAt", ""), reverse=True)
    if latest:
        for index, release in enumerate(releases):
            if release.get("version") == latest:
                releases.insert(0, releases.pop(index))
                break
    return releases


def commit_link(sha: str) -> str:
    if not sha:
        return ""
    short = sha[:7]
    return f'<a href="{GITHUB_REPO}/commit/{sha}" title="{html.escape(sha)}" target="_blank" class="commit-link">{short}</a>'


def render_rows(releases: list[dict], latest: str) -> str:
    if not releases:
        return '<tr><td colspan="4" class="no-data">No versions published yet.</td></tr>'

    rows: list[str] = []
    for release in releases:
        version = release.get("version", "")
        message = html.escape(release.get("message") or f"Release CoreCmp {version}")
        when = format_when(release.get("publishedAt", ""))
        commit = release.get("commit", "")
        commit_cell = commit_link(commit) if commit else "—"
        latest_badge = ' <span class="badge badge-success">latest</span>' if version == latest else ""
        dependency = f'implementation("{GROUP_ID}:{ARTIFACT_ID}:{version}")'

        rows.append(
            f"""
        <tr{' class="latest-row"' if version == latest else ''}>
          <td class="version-cell"><code>{html.escape(version)}</code>{latest_badge}</td>
          <td class="message">{message}</td>
          <td class="when">{when}<br/><span class="muted">{commit_cell}</span></td>
          <td class="code-cell">
            <div class="code-copy-container">
              <code class="snippet-code" id="dep-{html.escape(version)}">{html.escape(dependency)}</code>
              <button class="btn-copy" onclick="copyCode('dep-{html.escape(version)}')" data-copy-id="dep-{html.escape(version)}">Copy</button>
            </div>
          </td>
        </tr>"""
        )
    return "\n".join(rows)


def render_failures(failures: list[dict]) -> str:
    if not failures:
        return """
      <div class="no-failures" style="grid-column: 1 / -1; text-align: center; padding: 2rem; background: rgba(16, 185, 129, 0.02); border: 1px solid rgba(16, 185, 129, 0.08); border-radius: 12px; color: var(--accent-green); font-weight: 500; display: flex; flex-direction: column; align-items: center; gap: 0.5rem; width: 100%;">
        <span class="no-failures-icon">✓</span>
        No recent build failures. All deployment runs completed successfully!
      </div>
        """
    cards = []
    for f in failures:
        version = html.escape(f.get("version", ""))
        commit = html.escape(f.get("commit", "")[:7])
        published_at = format_when(f.get("publishedAt", ""))
        log_file = html.escape(f.get("logFile", ""))
        
        # Link to the log file (absolute via MAVEN_REPO_URL to be universally accessible)
        log_url = f"{MAVEN_REPO_URL}{log_file}" if not log_file.startswith("http") else log_file
        
        cards.append(f"""
        <div class="failed-run-card">
          <div class="failed-run-header">
            <span class="failed-version">Build Run (Version {version})</span>
            <span class="failed-time">{published_at}</span>
          </div>
          <div class="failed-commit">
            Commit: <code>{commit}</code>
          </div>
          <div class="failed-action">
            <a href="{log_url}" target="_blank" class="btn-error-logs">View Error Logs &rarr;</a>
          </div>
        </div>
        """)
    return "\n".join(cards)


def main() -> None:
    site_dir = Path(sys.argv[1] if len(sys.argv) > 1 else "maven-repo")
    site_dir.mkdir(parents=True, exist_ok=True)

    manifest_path = site_dir / "versions.json"
    data: dict = {"latest": "", "versions": [], "releases": []}
    if manifest_path.exists():
        data = json.loads(manifest_path.read_text())

    releases = load_releases(data)
    latest = data.get("latest") or (releases[0]["version"] if releases else "")
    updated_at = format_when(data.get("updatedAt", ""))
    total_downloads = data.get("totalDownloads", "—")
    version_rows = render_rows(releases, latest)
    
    # Calculate release interval frequency
    frequency = calculate_update_frequency(releases)
    
    failures = data.get("failures") or []
    failures_html = render_failures(failures)
    
    setup_repo_snippet = f'maven {{ url = uri("{MAVEN_REPO_URL}") }}'
    setup_dep_snippet = f'implementation("{GROUP_ID}:{ARTIFACT_ID}:{latest or "VERSION"}")'

    index_html = f"""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <title>CoreCmp Maven Repository &amp; Release Dashboard</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&amp;family=JetBrains+Mono:wght@400;500&amp;family=Outfit:wght@400;500;600;700;800&amp;display=swap" rel="stylesheet">
  <style>
    :root {{
      --bg-primary: #030712;
      --bg-secondary: #0b1528;
      --bg-card: rgba(15, 23, 42, 0.65);
      --border-color: rgba(255, 255, 255, 0.08);
      --border-glow: rgba(6, 182, 212, 0.15);
      --text-primary: #f3f4f6;
      --text-secondary: #9ca3af;
      --text-muted: #6b7280;
      --accent-cyan: #06b6d4;
      --accent-purple: #8b5cf6;
      --accent-green: #10b981;
      --accent-red: #ef4444;
      --accent-orange: #f97316;
      --font-sans: 'Outfit', 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
      --font-mono: 'JetBrains Mono', monospace;
      --card-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.7);
    }}

    * {{ box-sizing: border-box; }}
    
    body {{
      background-color: var(--bg-primary);
      background-image: 
      radial-gradient(circle at 15% 15%, rgba(139, 92, 246, 0.08) 0%, transparent 40%),
      radial-gradient(circle at 85% 85%, rgba(6, 182, 212, 0.08) 0%, transparent 40%);
      color: var(--text-primary);
      font-family: var(--font-sans);
      margin: 0;
      padding: 0;
      min-height: 100vh;
      line-height: 1.6;
    }}

    header {{
      background: rgba(3, 7, 18, 0.8);
      backdrop-filter: blur(12px);
      -webkit-backdrop-filter: blur(12px);
      border-bottom: 1px solid var(--border-color);
      position: sticky;
      top: 0;
      z-index: 100;
    }}

    .header-container {{
      max-width: 1200px;
      margin: 0 auto;
      padding: 1rem 1.5rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }}

    .logo-container {{
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }}

    .logo-dot {{
      width: 12px;
      height: 12px;
      background: linear-gradient(135deg, var(--accent-cyan), var(--accent-purple));
      border-radius: 50%;
      box-shadow: 0 0 12px var(--accent-cyan);
    }}

    .logo-text {{
      font-size: 1.5rem;
      font-weight: 800;
      letter-spacing: -0.02em;
      background: linear-gradient(to right, #ffffff, #9ca3af);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }}

    .github-btn-link {{
      display: flex;
      align-items: center;
      gap: 0.5rem;
      background: rgba(255, 255, 255, 0.06);
      border: 1px solid var(--border-color);
      color: var(--text-primary);
      padding: 0.5rem 1rem;
      border-radius: 999px;
      text-decoration: none;
      font-weight: 500;
      font-size: 0.9rem;
      transition: all 0.3s ease;
    }}

    .github-btn-link:hover {{
      background: rgba(255, 255, 255, 0.12);
      border-color: rgba(255, 255, 255, 0.2);
      transform: translateY(-1px);
    }}

    main {{
      max-width: 1200px;
      margin: 2rem auto;
      padding: 0 1.5rem 5rem;
    }}

    .hero-section {{
      text-align: center;
      margin-bottom: 3rem;
    }}

    .hero-title {{
      font-size: 2.75rem;
      font-weight: 800;
      margin: 0 0 0.5rem;
      letter-spacing: -0.03em;
      background: linear-gradient(to right, #ffffff, #a855f7, #06b6d4);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }}

    .hero-desc {{
      color: var(--text-secondary);
      font-size: 1.15rem;
      max-width: 650px;
      margin: 0 auto;
    }}

    .dashboard-grid {{
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
      gap: 1.5rem;
      margin-bottom: 3rem;
    }}

    .card {{
      background: var(--bg-card);
      backdrop-filter: blur(16px);
      -webkit-backdrop-filter: blur(16px);
      border: 1px solid var(--border-color);
      border-radius: 16px;
      padding: 1.5rem;
      box-shadow: var(--card-shadow);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      position: relative;
      overflow: hidden;
    }}

    .card::before {{
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: transparent;
      transition: background 0.3s ease;
    }}

    .card:hover {{
      transform: translateY(-4px);
      border-color: var(--border-glow);
      box-shadow: 0 15px 35px -8px rgba(6, 182, 212, 0.15);
    }}

    .card-latest::before {{ background: linear-gradient(90deg, var(--accent-green), var(--accent-cyan)); }}
    .card-stats::before {{ background: linear-gradient(90deg, var(--accent-purple), var(--accent-cyan)); }}
    .card-metrics::before {{ background: linear-gradient(90deg, var(--accent-cyan), var(--accent-purple)); }}
    .card-build::before {{ background: linear-gradient(90deg, var(--accent-orange), var(--accent-red)); }}

    .card-label {{
      font-size: 0.8rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      color: var(--text-secondary);
      font-weight: 600;
      margin-bottom: 0.75rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }}

    .card-value {{
      font-size: 1.8rem;
      font-weight: 700;
      margin-bottom: 0.5rem;
      font-family: var(--font-sans);
    }}

    .card-subtext {{
      font-size: 0.85rem;
      color: var(--text-muted);
    }}

    .status-badge {{
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.25rem 0.75rem;
      border-radius: 999px;
      font-weight: 600;
      font-size: 0.85rem;
    }}

    .status-badge.loading {{ background: rgba(255, 255, 255, 0.06); color: var(--text-secondary); }}
    .status-badge.success {{ background: rgba(16, 185, 129, 0.1); color: var(--accent-green); }}
    .status-badge.failure {{ background: rgba(239, 68, 68, 0.1); color: var(--accent-red); }}
    .status-badge.warning {{ background: rgba(249, 115, 22, 0.1); color: var(--accent-orange); }}

    .status-indicator {{
      width: 8px;
      height: 8px;
      border-radius: 50%;
      display: inline-block;
    }}
    .status-indicator.success {{ background: var(--accent-green); box-shadow: 0 0 8px var(--accent-green); animation: pulse 2s infinite; }}
    .status-indicator.failure {{ background: var(--accent-red); box-shadow: 0 0 8px var(--accent-red); }}
    .status-indicator.warning {{ background: var(--accent-orange); box-shadow: 0 0 8px var(--accent-orange); }}

    @keyframes pulse {{
      0% {{ transform: scale(0.95); opacity: 0.8; }}
      50% {{ transform: scale(1.1); opacity: 1; }}
      100% {{ transform: scale(0.95); opacity: 0.8; }}
    }}

    .metric-item {{
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.5rem;
    }}

    .section-title {{
      font-size: 1.6rem;
      font-weight: 700;
      margin: 3rem 0 1.5rem;
      display: flex;
      align-items: center;
      gap: 0.75rem;
      letter-spacing: -0.02em;
    }}

    .section-title-icon {{
      width: 8px;
      height: 18px;
      background: var(--accent-cyan);
      border-radius: 4px;
    }}

    .setup-grid {{
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(480px, 1fr));
      gap: 1.5rem;
      margin-bottom: 3.5rem;
    }}

    @media(max-width: 768px) {{
      .setup-grid {{ grid-template-columns: 1fr; }}
    }}

    .setup-box {{
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: 16px;
      padding: 1.5rem;
      box-shadow: var(--card-shadow);
      position: relative;
    }}

    .setup-box-header {{
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
    }}

    .setup-title {{
      font-weight: 600;
      color: var(--text-primary);
    }}

    pre {{
      background: rgba(0, 0, 0, 0.4);
      border: 1px solid rgba(255, 255, 255, 0.05);
      border-radius: 8px;
      padding: 1rem;
      margin: 0;
      overflow-x: auto;
    }}

    code {{
      font-family: var(--font-mono);
      font-size: 0.88rem;
    }}

    .code-copy-container {{
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      background: rgba(0, 0, 0, 0.4);
      border: 1px solid rgba(255, 255, 255, 0.05);
      border-radius: 8px;
      padding: 0.5rem 0.75rem;
      width: 100%;
    }}

    .snippet-code {{
      font-family: var(--font-mono);
      font-size: 0.85rem;
      overflow-x: auto;
      white-space: nowrap;
      flex-grow: 1;
    }}

    .btn-copy {{
      background: rgba(255, 255, 255, 0.06);
      border: 1px solid var(--border-color);
      color: var(--text-primary);
      padding: 0.25rem 0.75rem;
      border-radius: 6px;
      cursor: pointer;
      font-size: 0.8rem;
      font-weight: 500;
      transition: all 0.2s ease;
      white-space: nowrap;
    }}

    .btn-copy:hover {{
      background: rgba(255, 255, 255, 0.12);
      border-color: rgba(255, 255, 255, 0.2);
    }}

    .table-container {{
      width: 100%;
      overflow-x: auto;
      border-radius: 16px;
      border: 1px solid var(--border-color);
      box-shadow: var(--card-shadow);
      background: var(--bg-card);
      backdrop-filter: blur(16px);
      -webkit-backdrop-filter: blur(16px);
    }}

    table {{
      width: 100%;
      border-collapse: collapse;
      text-align: left;
    }}

    th, td {{
      padding: 1rem 1.25rem;
      border-bottom: 1px solid var(--border-color);
      vertical-align: middle;
    }}

    th {{
      background: rgba(3, 7, 18, 0.4);
      font-weight: 600;
      font-size: 0.9rem;
      color: var(--text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }}

    tr:last-child td {{
      border-bottom: none;
    }}

    tr.latest-row {{
      background: rgba(16, 185, 129, 0.03);
    }}

    tr.latest-row:hover {{
      background: rgba(16, 185, 129, 0.06);
    }}

    tr:hover {{
      background: rgba(255, 255, 255, 0.02);
    }}

    .version-cell {{
      font-weight: 700;
    }}

    .badge {{
      display: inline-block;
      padding: 0.15rem 0.5rem;
      border-radius: 999px;
      font-size: 0.7rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.02em;
    }}

    .badge-success {{
      background: rgba(16, 185, 129, 0.15);
      color: #34d399;
      border: 1px solid rgba(16, 185, 129, 0.2);
    }}

    td.message {{
      max-width: 380px;
      font-size: 0.92rem;
      color: var(--text-secondary);
      word-break: break-word;
    }}

    td.when {{
      font-size: 0.88rem;
      color: var(--text-secondary);
      min-width: 180px;
    }}

    .muted {{
      font-size: 0.8rem;
      color: var(--text-muted);
      margin-top: 0.25rem;
      display: block;
    }}

    .commit-link {{
      color: var(--accent-cyan);
      text-decoration: none;
      font-family: var(--font-mono);
      font-weight: 500;
    }}

    .commit-link:hover {{
      text-decoration: underline;
    }}

    .code-cell {{
      min-width: 320px;
    }}

    .no-data {{
      text-align: center;
      color: var(--text-muted);
      padding: 3rem;
    }}

    footer {{
      margin-top: 5rem;
      border-top: 1px solid var(--border-color);
      padding: 2rem 0;
      text-align: center;
      color: var(--text-muted);
      font-size: 0.88rem;
    }}

    footer a {{
      color: var(--accent-cyan);
      text-decoration: none;
    }}

    footer a:hover {{
      text-decoration: underline;
    }}
  </style>
  <script>
    function copyCode(id) {{
      const text = document.getElementById(id).innerText;
      navigator.clipboard.writeText(text).then(() => {{
        const btn = document.querySelector(`[data-copy-id="${{id}}"]`);
        const originalText = btn.innerText;
        btn.innerText = "Copied!";
        btn.style.borderColor = "var(--accent-green)";
        btn.style.color = "var(--accent-green)";
        setTimeout(() => {{
          btn.innerText = originalText;
          btn.style.borderColor = "var(--border-color)";
          btn.style.color = "var(--text-primary)";
        }}, 2000);
      }});
    }}
  </script>
</head>
<body>
  <header>
    <div class="header-container">
      <div class="logo-container">
        <span class="logo-dot"></span>
        <span class="logo-text">CoreCmp</span>
      </div>
      <a href="{GITHUB_REPO}" target="_blank" class="github-btn-link">
        GitHub Repository &rarr;
      </a>
    </div>
  </header>

  <main>
    <section class="hero-section">
      <h1 class="hero-title">Release Dashboard</h1>
      <p class="hero-desc">Kotlin Multiplatform (KMP) toolkit for Android, iOS &amp; JVM. Hosted live on GitHub Pages.</p>
    </section>

    <section class="dashboard-grid">
      <!-- Card 1: Latest Version -->
      <div class="card card-latest">
        <div class="card-label">
          <span>Latest Release</span>
          <span class="status-badge success" style="padding: 0.1rem 0.5rem; font-size: 0.7rem;">
            <span class="status-indicator success"></span> LIVE
          </span>
        </div>
        <div class="card-value" style="font-family: var(--font-mono); font-size: 1.6rem;">{html.escape(latest or "—")}</div>
        <div class="card-subtext">Published: {updated_at or "—"}</div>
      </div>

      <!-- Card 2: Release Statistics -->
      <div class="card card-stats">
        <div class="card-label">Update Frequency</div>
        <div class="card-value">{frequency}</div>
        <div class="card-subtext">Total cataloged releases: {len(releases)}</div>
      </div>

      <!-- Card 3: Setup Coordinates -->
      <div class="card card-metrics">
        <div class="card-label">Repository Details</div>
        <div class="metric-item">
          <span class="card-subtext">Group ID:</span>
          <strong style="color: var(--accent-cyan); font-family: var(--font-mono); font-size: 0.85rem;">{GROUP_ID}</strong>
        </div>
        <div class="metric-item">
          <span class="card-subtext">Artifact ID:</span>
          <strong style="color: var(--accent-purple); font-family: var(--font-mono); font-size: 0.85rem;">{ARTIFACT_ID}</strong>
        </div>
        <div class="metric-item" style="margin-top: 0.25rem;">
          <span class="card-subtext">Total Cataloged:</span>
          <strong style="color: var(--accent-green)">{len(releases)}</strong>
        </div>
      </div>
    </section>

    <h2 class="section-title"><span class="section-title-icon"></span>Gradle Integration</h2>
    <div class="setup-grid">
      <div class="setup-box">
        <div class="setup-box-header">
          <span class="setup-title">1. Add Maven Repo (settings.gradle.kts)</span>
          <button class="btn-copy" onclick="copyCode('snippet-repo')" data-copy-id="snippet-repo">Copy</button>
        </div>
        <pre><code id="snippet-repo">{html.escape(setup_repo_snippet)}</code></pre>
      </div>

      <div class="setup-box">
        <div class="setup-box-header">
          <span class="setup-title">2. Add Dependency (build.gradle.kts)</span>
          <button class="btn-copy" onclick="copyCode('snippet-dep')" data-copy-id="snippet-dep">Copy</button>
        </div>
        <pre><code id="snippet-dep">{html.escape(setup_dep_snippet)}</code></pre>
      </div>
    </div>

    <h2 class="section-title"><span class="section-title-icon"></span>Version History</h2>
    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>Version</th>
            <th>Description / Commit Message</th>
            <th>Released At</th>
            <th>Gradle Snippet</th>
          </tr>
        </thead>
        <tbody>
          {version_rows}
        </tbody>
      </table>
    </div>
  </main>

  <footer>
    <p>Powered by GitHub Pages Maven Repository plugin. Dashboard design by CoreCmp.</p>
  </footer>
</body>
</html>
"""

    (site_dir / "index.html").write_text(index_html)
    print(f"Generated dashboard: {site_dir / 'index.html'}")


if __name__ == "__main__":
    main()
