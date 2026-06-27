# Behavior Coach — User Guide

**Author:** K K K Ekanayake
**Last Updated:** 2026-06-27
**Version:** 1.0

---

## Welcome to Behavior Coach!

Behavior Coach helps you build lasting habits through intelligent tracking and adaptive coaching. This guide will help you get started and make the most of the app.

---

## Getting Started

### First Launch

When you open Behavior Coach for the first time, you'll see:
- A greeting with your name and avatar
- A "Daily Coach" tab (empty — no habits yet)
- A notice if no API key is configured (you can still use the app!)

### Creating Your First Habit

You have two ways to add habits:

#### Option 1: AI Natural Language Intake (Recommended)

1. Tap the **"AI Intake"** tab in the bottom navigation
2. Type your habit in plain English, for example:
   - *"Go for a 30-minute run every Monday, Wednesday, and Friday at 6 AM"*
   - *"Meditate for 10 minutes every morning at 7:30"*
   - *"Read before bed every night at 10 PM"*
3. Tap **"Analyze Blueprint with AI"**
4. Review the extracted habit details:
   - Title
   - Schedule (which days)
   - Target time
   - AI-generated behavioral tip
5. Tap **"Deploy Routines"** to add it to your tracker

**Tips for best results:**
- Include specific days (Monday, Tuesday, etc.) for weekly habits
- Include a time (e.g., "at 7 AM" or "at 19:30")
- Mention duration if relevant ("for 20 minutes")
- Use "every day" or "daily" for daily habits

#### Option 2: Manual Entry

1. On the Daily Coach tab, tap the **"+"** (pencil) icon in the top-right
2. Fill in the fields:
   - **Habit Name:** What you want to do
   - **Target Clock Time:** When to do it (HH:MM format, e.g., 07:30)
   - **Active Days:** Which days (comma-separated, e.g., "Monday, Wednesday, Friday")
   - **Action Tip:** Optional personal strategy note
3. Tap **"Confirm"**

---

## Daily Tracking

### The 7-Day Grid

Each habit shows a row of 7 circles representing the past week:
- **Teal filled circle with checkmark:** Completed that day
- **Empty circle with teal border:** Scheduled but not yet completed
- **Faded circle:** Not scheduled for that day

### Logging Completions

- **Tap any circle** to toggle completion for that day
- You can log completions retroactively (up to 6 days back)
- Today is always the rightmost circle

### Viewing Behavioral Tips

- **Tap a habit card** to expand/collapse the behavioral tip
- Tips are science-backed strategies to help maintain the habit

### Deleting a Habit

- **Long-press** a habit card to delete it
- Or tap the small delete icon in the top-right of the card

---

## Understanding Coach Recommendations

### What is the Adaptive Coach?

The coach analyzes your past 7 days of completions. If you've missed a habit on 2+ scheduled days, it generates a recommendation to help you get back on track.

### Recommendation Types

#### SHIFT_TIME (Morning Habits)

**When:** You're missing habits scheduled between 5-10 AM.

**What it does:** Suggests moving the habit to 09:30 AM.

**Why:** Morning exhaustion is a common friction point. Shifting later can reduce circadian rhythm resistance.

**How to apply:** Tap "Shift schedule to 09:30" on the recommendation card.

#### MICRO_INCREMENT (Non-Morning Habits)

**When:** You're missing habits scheduled for later in the day.

**What it does:** Suggests scaling the habit down to a 5-minute micro-action.

**Why:** Large tasks create cognitive barriers. Starting with just 5 minutes builds activation momentum.

**How to apply:** Tap "Scale down to Micro-Action" on the recommendation card.

### Testing the Coach (Demo Mode)

Want to see the coach in action before you have real data?

1. Tap the **refresh/cycle icon** (orange) in the top-right of the Daily Coach tab
2. This creates a demo habit ("Morning Cardio Gym Session") and clears recent completions
3. The coach immediately detects the friction pattern and shows a recommendation

---

## Understanding the API Key Notice

If you see an orange banner saying *"No custom API Key configured"*:

- The app is using its **local regex parser** instead of Gemini AI
- All core features still work (tracking, coaching, manual entry)
- Natural language intake uses pattern matching instead of AI extraction
- To enable AI features, configure a Gemini API Key (see README)

---

## Tips for Success

1. **Start small:** Use the AI intake to create 1-2 habits first
2. **Be specific:** "Walk for 15 minutes at 7 AM every day" works better than "exercise more"
3. **Check daily:** Even a 10-second check-in builds the tracking habit itself
4. **Trust the coach:** If you see a recommendation, your data is telling you something — try the suggestion for a week
5. **Use retroactive logging:** Forgot to log yesterday? No problem — tap yesterday's circle to mark it

---

## Troubleshooting

| Issue | Solution |
|---|---|
| "Keystore file not found" | Run the build from project root; debug.keystore should be auto-generated |
| "compileSdk 36 not found" | Install Android SDK platform 36 via SDK Manager |
| Build fails with jlink error | Use Java 17, not Java 26 — set JAVA_HOME accordingly |
| AI intake not extracting correctly | Check API key; or try rephrasing with clearer day/time info |
| App shows "No routines yet" | Create your first habit via AI Intake or Manual Entry |

---

## Need More Help?

- **Technical docs:** See `docs/architecture/ARCHITECTURE.md`
- **API reference:** See `docs/API.md`
- **Product requirements:** See `docs/PRD.md`

---

**Author:** K K K Ekanayake
