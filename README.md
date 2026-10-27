# BrowserRouter

BrowserRouter is an Android application that acts as a **smart default browser router**. Instead of opening links directly in a single browser, it intercepts all web links and routes them to different browsers based on user-defined rules.

It is designed to be:
- Fast (instant routing, no unnecessary processing)
- Private (no analytics or tracking)
- Fully local (no backend server required)
- Flexible (wildcard link rules, source-app rules, and optional redirect resolution)

---

## ✨ Features

### 🔀 Smart Browser Routing
Route links to different browsers depending on rules:
- `*.twitter.com/*` → Firefox
- `github.com/daiwaid/*` → Chrome
- Links opened from Slack → Brave

### 🌐 Match Modes
One rule can set a URL pattern, the app the link was opened from, or both. A rule can be saved when at least one of those is set.
- `*.domain.com/*` matches domain.com and its subdomains, on any path
- `domain.com/topic/*` matches every page under that path
- `domain.com/page` matches that page only
- `domain.com` matches only the root of domain.com and www.domain.com
- Leave the pattern blank, shown as `*`, to match every link opened from the selected app

### 🌍 Multi-Browser Support
Choose any installed browser per rule:
- Chrome
- Firefox
- Brave
- Samsung Internet
- Any browser supporting Android intents

### 🔗 Optional Redirect Resolution
Supports resolving shortened or redirect links before routing:
- `t.co`
- `bit.ly`
- `is.gd`

### ⚡ Fast Execution
- No background services
- No polling
- Event-driven only
- Instant intent handling

---

## 📱 How It Works

1. User clicks a link in any app
2. Android sends the link to BrowserRouter (set as default browser)
3. BrowserRouter evaluates routing rules
4. (Optional) resolves redirect links like `t.co`
5. Opens the link directly in that browser
6. Exits immediately

---

## 🧠 Rule System

Each rule consists of:

- Optional wildcard URL pattern
- Optional source app (the app the link was opened from)
- At least one of those is required
- Target browser
- Priority
- Enabled/disabled toggle
