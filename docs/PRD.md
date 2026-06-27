# Product Requirements Document: Behavior Coach

**Author:** K K K Ekanayake
**Last Updated:** 2026-06-27
**Status:** Active
**Version:** 1.0

---

## 1. Executive Summary & Product Vision

**Vision:** Behavior Coach is an AI-driven habit formation app that helps users build lasting routines by combining intelligent natural language input with adaptive behavioral coaching. Unlike traditional habit trackers that simply log completions, Behavior Coach analyzes friction patterns and proactively recommends schedule adjustments and micro-increments to keep users on track.

**Problem:** 80% of people fail to maintain new habits beyond the first month. Traditional habit trackers are passive — they record failures but don't help users adapt. Users need intelligent guidance that responds to their actual behavior patterns.

**Solution:** An app that lets users describe habits in natural language, automatically extracts structured schedule data via Gemini AI, tracks daily completions on a 7-day grid, and generates adaptive coaching recommendations when friction is detected.

**Why now:** Advances in LLM structured output (Gemini's JSON schema mode) make it possible to extract precise habit metadata from conversational input on-device, without requiring a backend server.

---

## 2. Target Audience & User Personas

### Persona 1: "Striving Sam"
- **Demographics:** 28-year-old software developer, urban, tech-savvy
- **Goals:** Build a morning routine (exercise, meditation, reading)
- **Frustrations:** Starts strong for 2 weeks then loses momentum; doesn't know how to recover
- **Tech comfort:** High — comfortable with AI tools
- **Jobs-to-be-Done:**
  - Functional: Track multiple habits with different schedules
  - Emotional: Feel supported when motivation drops, not judged
  - Social: Share progress with accountability partner
- **Context of use:** Checks app every morning after waking up; reviews weekly on Sunday evening

### Persona 2: "Busy Parent Bailey"
- **Demographics:** 36-year-old parent of two, part-time worker
- **Goals:** Fit in exercise and personal development around family schedule
- **Frustrations:** Unpredictable schedule makes fixed-time habits impossible
- **Tech comfort:** Moderate — uses phone apps but dislikes complex setup
- **Jobs-to-be-Done:**
  - Functional: Quick habit entry without navigating complex forms
  - Emotional: Flexibility when life gets in the way
  - Social: Model healthy behavior for children
- **Context of use:** Uses app during commute or after kids' bedtime; prefers voice-like natural input

### Persona 3: "Wellness-Focused Wendy"
- **Demographics:** 45-year-old yoga instructor, health-conscious
- **Goals:** Maintain consistency with meditation, hydration, journaling
- **Frustrations:** Existing apps are too gamified; wants science-backed guidance
- **Tech comfort:** Low-moderate — prefers simplicity
- **Jobs-to-be-Done:**
  - Functional: See behavioral insights tied to psychology research
  - Emotional: Feel understood by the app's recommendations
  - Social: N/A (private practice)
- **Context of use:** Morning and evening check-ins; values behavioral tips

---

## 3. Market Research & Competitive Analysis

### Market Size
- Global habit tracking market: $1.2B (2024), projected $2.8B by 2029 (CAGR 18.5%)
- Mobile wellness apps: 4.5B downloads annually
- Post-pandemic focus on mental health and self-improvement driving growth

### Top 3 Competitors

| Competitor | Strengths | Weaknesses | Differentiation |
|---|---|---|---|
| **Streaks** | Beautiful UI, Apple ecosystem integration, streak visualization | iOS only, no AI, rigid scheduling, no adaptive coaching | Behavior Coach adapts to failures instead of just showing them |
| **Habitica** | Gamification, social features, RPG mechanics | Complex, overwhelming for casual users, no intelligent recommendations | Behavior Coach uses AI coaching, not gamification |
| **Loop Habit Tracker** | Free, open-source, simple, Android-native | No AI, no natural language, basic UI, no behavioral insights | Behavior Coach has conversational intake and adaptive coach |

### Competitive Advantage (Moat)
- **AI-native approach:** Natural language intake removes friction of manual form filling
- **Adaptive coaching:** Proactive recommendations based on actual behavior patterns (not just streak counting)
- **Offline-first with AI enhancement:** Works fully offline; AI is a bonus, not a requirement
- **Behavioral science integration:** Tips based onBJ Fogg's Tiny Habits methodology and habit anchoring

### Opportunities
- Growing demand for AI-powered wellness tools
- Underserved market for adaptive (not just tracking) habit apps
- Potential for B2B (corporate wellness programs)

### Threats**
- Apple/Google may build habit tracking into OS-level wellness features
- Gemini API pricing changes could affect cost structure
- Data privacy concerns with AI processing personal habits

---

## 4. User Stories with Acceptance Criteria

### US-001: Natural Language Habit Creation
**As a** Striving Sam
**I want** to type my habits in plain English
**So that** I don't have to fill out complex forms

**Priority:** P0
**Story Points:** 8
**Acceptance Criteria:**
- GIVEN I am on the AI Intake tab WHEN I type "Read a finance book for 20 minutes every Monday and Wednesday night at 10 PM" AND I tap "Analyze Blueprint" THEN the app extracts title="Read A Finance Book For 20 Minutes", frequency="Weekly", activeDays=["Monday","Wednesday"], targetTime="22:00", and a behavioral tip
- GIVEN the Gemini API key is not configured WHEN I submit natural language input THEN the app uses the local regex parser and displays a notice about local parsing
- GIVEN the API returns successfully WHEN I view the extracted habit THEN I see title, schedule, time, and an AI behavioral tip with options to "Deploy" or "Discard"

**Business Value:** Reduces habit creation friction by 70% compared to form-based entry
**Success Metric:** 80% of new habits created via AI Intake (not manual)

---

### US-002: Daily Habit Tracking
**As a** Busy Parent Bailey
**I want** to see my habits for the day and tap to mark complete
**So that** I can quickly log my progress

**Priority:** P0
**Story Points:** 5
**Acceptance Criteria:**
- GIVEN I have habits configured WHEN I open the Daily Coach tab THEN I see all active habits with a 7-day completion grid
- GIVEN I tap a day circle for a habit WHEN the day is not yet marked THEN it shows a checkmark and turns teal
- GIVEN I tap a completed day circle WHEN it is already marked THEN the completion is removed (toggled off)
- GIVEN a habit has no completions in the past 7 days on scheduled days THEN no special indicator is shown

**Business Value:** Core engagement loop — daily return visits
**Success Metric:** D7 retention > 40%

---

### US-003: Adaptive Coach Recommendations
**As a** Striving Sam
**I want** the app to notice when I'm struggling and suggest adjustments
**So that** I don't give up on habits entirely

**Priority:** P0
**Story Points:** 8
**Acceptance Criteria:**
- GIVEN I have missed a habit on 2+ scheduled days in the past 7 days WHEN I open the Daily Coach tab THEN I see a "Friction Drop" recommendation card
- GIVEN the habit is scheduled for morning hours (5-10 AM) WHEN a friction recommendation is generated THEN it suggests SHIFT_TIME to 09:30
- GIVEN the habit is scheduled for non-morning hours WHEN a friction recommendation is generated THEN it suggests MICRO_INCREMENT (scale to 5-minute version)
- GIVEN I tap "Shift schedule" on a recommendation WHEN the action completes THEN the habit's targetTime is updated and a confirmation toast shows

**Business Value:** Differentiator — no competitor offers adaptive behavioral coaching
**Success Metric:** 30% of users who see a recommendation apply it within 24 hours

---

### US-004: Manual Habit Entry
**As a** Wellness-Focused Wendy
**I want** to add habits manually with specific details
**So that** I have full control over my habit definitions

**Priority:** P1
**Story Points:** 3
**Acceptance Criteria:**
- GIVEN I tap the "+" (manual) button WHEN the dialog opens THEN I see fields for Habit Name, Target Time, Active Days, and Action Tip
- GIVEN I fill in all required fields and tap "Confirm" WHEN the dialog closes THEN the habit appears in my Daily Coach list
- GIVEN I leave the habit name blank WHEN I tap "Confirm" THEN no habit is added

**Business Value:** Accessibility for users who prefer explicit control
**Success Metric:** < 20% of habits created manually (indicates AI intake is preferred)

---

### US-005: Behavioral Tips Display
**As a** Wellness-Focused Wendy
**I want** to see science-backed tips for each habit
**So that** I understand how to make the habit stick

**Priority:** P1
**Story Points:** 3
**Acceptance Criteria:**
- GIVEN a habit has a behavioral tip WHEN I tap the habit card THEN the tip expands showing "AI Behavioral Strategy" with the tip text
- GIVEN I tap the expanded card again WHEN the tip is visible THEN it collapses

**Business Value:** Educational component increases user confidence and habit success rate
**Success Metric:** 50% of users expand at least one tip per session

---

### US-006: Demo Mode (Failure Simulation)
**As a** Any persona during onboarding
**I want** to simulate habit failures to see the coach in action
**So that** I can understand the app's value before committing real data

**Priority:** P2
**Story Points:** 2
**Acceptance Criteria:**
- GIVEN I have no habits WHEN I tap the "Simulate Friction" button THEN a demo habit "Morning Cardio Gym Session" is created and all past 7 day completions are cleared
- GIVEN I have existing habits WHEN I tap the button THEN only the past 7 day completions for the first habit are cleared
- GIVEN the simulation completes WHEN the coach analyzes the data THEN a friction recommendation card appears

**Business Value:** Onboarding conversion — users see value immediately
**Success Metric:** 60% of new users trigger demo mode in first session

---

### US-007: Habit Deletion
**As a** Striving Sam
**I want** to remove habits I no longer want to track
**So that** my list stays relevant

**Priority:** P1
**Story Points:** 2
**Acceptance Criteria:**
- GIVEN I long-press a habit card WHEN the deletion triggers THEN the habit and its completion logs are removed from the database
- GIVEN I tap the delete icon on a habit card WHEN the deletion triggers THEN the habit is removed

**Business Value:** Data hygiene — users can curate their habit list
**Success Metric:** N/A (hygiene feature)

---

## 5. Functional Requirements

| ID | Priority | Requirement | Rationale |
|---|---|---|---|
| FR-001 | P0 | Natural language habit intake via Gemini AI with structured JSON output | Core differentiator |
| FR-002 | P0 | Local regex fallback parser when Gemini unavailable | Offline-first principle |
| FR-003 | P0 | Daily completion tracking with 7-day retroactive grid | Core tracking loop |
| FR-004 | P0 | Friction analysis algorithm (2+ misses trigger recommendation) | Adaptive coaching |
| FR-005 | P0 | SHIFT_TIME recommendation for morning habits (5-10 AM) | Behavioral science |
| FR-006 | P0 | MICRO_INCREMENT recommendation for non-morning habits | Behavioral science |
| FR-007 | P1 | Manual habit entry dialog | Accessibility |
| FR-008 | P1 | Expandable behavioral tips on habit cards | Education |
| FR-009 | P1 | Habit deletion (long-press and icon) | Data hygiene |
| FR-010 | P2 | Demo failure simulation | Onboarding |
| FR-011 | P2 | User profile with avatar display | Personalization |
| FR-012 | P2 | API key absence warning banner | Transparency |

---

## 6. Non-Functional Requirements

| Category | Requirement |
|---|---|
| **Performance** | App cold start < 2s; habit list scroll 60fps; AI intake response < 5s (network dependent) |
| **Security** | API key stored in BuildConfig via Secrets Gradle Plugin (not hardcoded); no user data transmitted except to Gemini API; local database only |
| **Accessibility** | WCAG 2.1 AA; all interactive elements have contentDescription; minimum touch target 44dp; high contrast text |
| **Localization** | English only (v1.0); strings externalized in strings.xml for future i18n |
| **Offline** | Full habit tracking works offline; AI intake degrades to local regex parser offline |
| **Scalability** | Designed for single-user local use; Room handles 1000+ habits without performance degradation |
| **Compatibility** | Android 7.0+ (API 24); tested on Pixel 8 (API 35/36) |
| **Battery** | No background services; no alarms; minimal battery impact |

---

## 7. MVP Scope Definition

**In Scope (MVP — v1.0):**
- US-001: Natural language habit creation (with fallback)
- US-002: Daily habit tracking with 7-day grid
- US-003: Adaptive coach (SHIFT_TIME, MICRO_INCREMENT)
- US-004: Manual habit entry
- US-005: Behavioral tips display
- US-006: Demo mode
- US-007: Habit deletion

**Out of Scope (Post-MVP):**
- User authentication / multi-user support
- Cloud sync across devices
- Push notifications / reminders
- Social features (sharing, challenges)
- Analytics dashboard / streak statistics
- Wearable integration (Wear OS, Apple Watch)
- iOS version
- Subscription / monetization
- Habit categories / tags
- Recurring habit templates
- Export / backup functionality

---

## 8. Product Roadmap

**Now (MVP, Weeks 1-8):**
- Core habit tracking (create, log, delete)
- AI natural language intake
- Adaptive coach recommendations
- Manual entry fallback
- Demo mode

**Next (V1.1, Weeks 9-16):**
- Push notification reminders
- Habit categories and filtering
- Weekly summary view
- Streak tracking and visualization
- Export to CSV

**Later (V2+):**
- Cloud sync (Firebase)
- Multi-device support
- Social accountability features
- Wearable integration
- Subscription tier (advanced AI coaching)
- iOS port (Kotlin Multiplatform or native Swift)

---

## 9. Technical Architecture Overview

- **Platform:** Native Android (Kotlin 2.2.10)
- **UI:** Jetpack Compose with Material Design 3
- **Architecture:** MVVM with Repository pattern
- **Database:** Room 2.7.0 with KSP code generation
- **Networking:** Retrofit 2.12.0 + Moshi for Gemini API
- **State Management:** Kotlin StateFlow + Compose state
- **AI:** Gemini GenerateContent API with structured JSON schema output
- **Build:** AGP 9.1.1, Gradle 9.3.1, Java 17 required

See `docs/architecture/ARCHITECTURE.md` for full technical details.

---

## 10. Risks, Assumptions, Dependencies

| Risk | Probability | Impact | Mitigation |
|---|---|---|---|
| Gemini API pricing changes | Medium | High | Local fallback parser ensures core functionality without API |
| Gemini API rate limits | Low | Medium | Request queuing; graceful error messages |
| User doesn't have API key | High | Low | Clear onboarding; local parser handles all core features |
| Java 26 incompatibility on user machines | Medium | High | Document Java 17 requirement; provide build.sh helper |
| R8/DEX non-deterministic build failures | Low | Medium | Retry build; documented in troubleshooting |
| Room schema migration needs | Low | Medium | exportSchema = false for v1; add migrations in v2 |
| Gemini model deprecation | Low | High | Abstract AI interface; swap model via config |

---

## 11. Success Metrics / KPIs

| Metric | Target | Measurement |
|---|---|---|
| **North Star** | Daily active habits logged per user | Average habits completed per day |
| **Activation** | 70% create first habit within 5 min of install | Onboarding funnel |
| **Retention (D7)** | 40% return after 7 days | Cohort analysis |
| **Retention (D30)** | 25% return after 30 days | Cohort analysis |
| **AI Intake Adoption** | 80% of habits created via AI (not manual) | Feature usage ratio |
| **Coach Engagement** | 30% of recommendations applied | Recommendation tap rate |
| **Crash-free rate** | > 99.5% | Firebase Crashlytics (future) |

---

## 12. Reporting

**Author:** K K K Ekanayake
**Document Status:** Active
**Review Cycle:** Bi-weekly during active development
**Distribution:** Internal team, stakeholders
