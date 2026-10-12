# RAKSHAM 2026: round-1 notes (PS 02)

Organised by ARIES and DevClub, IIT Delhi, with Amazon. Supported by Learning Links Foundation, with I4C (MHA) and #ScamSmartIndia.
Team SCΛMURΛI: Nishant Kumar · Prince Kumar Singh · Kartik Singh · Gourvendra Kumar Jha.

## Where each judging criterion is answered

| Criterion | Pitch PDF | Repo |
|---|---|---|
| Problem fit & user understanding (20) | pp. 1–3 | README "The problem in one minute" |
| Technical architecture & detection logic (20) | pp. 4, 6, 7 | `thehro/engine.py`, `ThehroEngine.kt`, `playbooks.json` |
| Solution value & differentiation (15) | pp. 4, 8 | README "How it works" |
| Feasibility & finale plan (15) | p. 10 | section "Proof plan" below, `thehro/simulate.py` |
| Safety, privacy & responsible AI (15) | p. 9 | section "Build for trust" below, `docs/PRIVACY.md` |
| Adoption, integration & scale (10) | pp. 6, 10 | section "Integration" below |
| Communication (5) | all | `webapp/thehro.html` demo |

## Product in one line

**Input** (payment-session metadata) → **assessment** (playbook kill-chain + fusion + floors) → **action** (T0–T4, one alert at most) → **verification** (call the real person on their saved number, reconfirm after the call ends) → **evidence record** (1930 / NCRP pack, built only if the user asks).

## Detection logic

1. **Predicates.** 22 yes/no checks on metadata, e.g. `unknown_caller`, `long_call` (≥ 30 min), `screen_share`, `fresh_apk` (< 72 h with SMS or accessibility permission), `amount_very_high` (> 10× the user's own p95), `fake_credit` ("credited" SMS but no money in the ledger).
2. **Kill-chain match.** Each playbook card is an ordered list of stages, a `min_match` and an `anchor` stage. Later stages weigh more (1 + 0.25·i). The card is complete only if the anchor and `min_match` stages are seen. This is a simplified left-to-right HMM; the full probabilistic HMM is a finale task.
3. **Risk fusion.** `p = σ(−4.6 + 4.2·stage + 1.5·anomaly + 1.6·payee + 2.4·device)`. The weights are hand-set for v3 and will be refit on finale data.
4. **Floors.** These are rules no model output can override: a complete card's `tier_floor`; fake credit → T3; screen control while paying a non-saved payee → T4; saved payee without takeover → at most T1.
5. **Anti-splitting and retry.** Payments to new payees are summed over 72 h before the amount checks. A retry to a new payee within 24 h of a hold is at least T3.
6. **Tier and budget.** Thresholds are 0.10 / 0.30 / 0.60 / 0.85. T2 questions spend a per-user budget (3 per 30 days). When it is empty, T2 drops to a badge. T3 and T4 are never suppressed.

### Real time and emerging workflows

* `thehro/realtime.py`: an event-driven session tracker. Each event (call_start, screen_share_on, payee_opened, amount_entered, payment_done …) updates the live session, and the engine re-decides on every event. Call length and APK age come from the real clock. The audit log stores only (time, event name, tier).
* `thehro/radar.py`: the "emerging" part of PS 02. A risky session that matched no complete card becomes a signature (sorted predicate set + amount band). If a signature surges (≥ 5 distinct reporters in 24 h and ≥ 3× its 7-day daily average), the radar drafts a candidate card with status `needs_analyst_approval`. One user spamming reports is not a surge, and a steady signature is not "emerging".

### Playbook cards (P1 set)

| Card | Workflow | Anchor | Floor |
|---|---|---|---|
| PB-B1 | Digital arrest (fake CBI / ED / police on video) | long_call | T4 |
| PB-B2 | Fake recovery agent (re-victimisation) | recent_fraud_report | T3 |
| PB-A1 | Family-emergency call, possibly cloned voice | unknown_caller | — |
| PB-A3 | Video call from a "friend or colleague" | video_or_voip_call | — |
| PB-A4 | Fake "credited by mistake" SMS | fake_credit | T3 |
| PB-C1 | Deepfake celebrity investment ad | invest_remark | — |
| PB-C2 | Fake IPO / SEBI advisor | invest_remark | — |
| PB-C3 | Telegram prepaid task scam | small_inbound_credit_unknown | — |
| PB-D1 | Wedding-invite APK takeover | fresh_apk | T4 |
| PB-D2 | Screen-share "KYC / support" | screen_share | T4 |
| PB-E1 | "Scan QR to receive money" | qr_from_gallery | T3 |
| PB-G1 | Fake helpline from search / AI chatbot | unofficial_helpline | — |

We detect the *workflow* around cloned voices and deepfakes. We do not analyse audio or video (that is PS 01).

## Build for trust

| Question | Our answer | Where in code |
|---|---|---|
| **What data do we need?** | Call state flag, call length, caller-in-contacts (lookup only), screen-share / accessibility / new-APK flags, payee age and saved status, amount vs own baseline, "credited" SMS sender and amount (not the body), QR source, the UPI note. No message bodies, no audio, no contact list upload. | `DEFAULT_SESSION` in `engine.py`, `PaymentSession` in Kotlin |
| **What happens when we are wrong?** | *False alarm:* never block on one weak signal. "I know this payee" moves it to the trusted lane. Holds end on their own. *Missed scam:* an "I was scammed" button builds the 1930 pack, and the case becomes a new or updated card. | floors and budget in `decide()` |
| **Who can act on the result?** | The user, always. A guardian only if the user opted in (24 h cooling period before changing the guardian). Banks act only under their own rules. No auto-reporting, no blacklists of people. | — |
| **How will users understand it?** | Every alert has one plain sentence and one next step ("Police never ask you to move money"). T2 is a single neutral question, not a warning. | `reason`, `question` on each card |
| **How do we resist misuse?** | Cards are Ed25519-signed and a human analyst approves each one. Scammers cannot silence alerts by asking the user to "ignore the screen" (T3/T4 can't be turned off mid-call). There is no text model to jailbreak, and the engine reads no text, so it behaves the same in every language. | card signing (finale), floors |

**Bias check (finale):** compare alert rates across age bands, languages and phone price tiers on the 200-payment normal set and the field test. Any group alerted more than 1.5× the median gets its thresholds reviewed.

**Data boundary:** the engine runs on the phone. With consent, only `{p, tier, card_id, hashed payee ID}` is sent to the bank or PSP for payee risk. Raw events are kept locally for 90 days for the user's own baseline, then deleted.

## Integration

* **SDK inside the UPI / bank / wallet app.** `ThehroEngine.decide(session)` is called on the pay screen before the PIN pad opens. It is pure Kotlin with no network call.
* **Bank / PSP side:** payee risk from DoT FRI, MuleHunter.AI-style mule scores and the I4C suspect registry feed `payee_risk`. The PSP ledger confirms real credits for `fake_credit`.
* **Cards over the air:** Threat Radar (crawler → LLM draft → analyst approval → signed push) ships new cards in under 24 h.
* **Scale:** decisions stay on the device, so cost doesn't grow with users. The cloud side is a card registry plus a payee-risk API.

## Proof plan

| Assumption | How we test it | When |
|---|---|---|
| Call state and screen-share apps are visible on Android without mic access | phone test on 5+ phones, 3 brands | finale H0–12 |
| The kill-chain pattern separates scams from normal payments | 50 scam + 200 normal sessions (done, simulated) → baseline: rules only, fixed 3-alert cap | done / finale |
| People act on one clear alert | 10-person test: cancel rate, time to decide | finale H24–36 |

**48-hour finale:** H0–12 real call / VoIP detection and Device Shield on phones · H12–24 fake-credit detector and Truth Ping · H24–36 orchestrator and attention budget in the Android UI · H36–48 evaluation report, red-team run, demo.

## Known limits (we say these out loud)

* The simulated results come from sessions we wrote, so they are not real-world accuracy.
* Fake-helpline scams with no screen share and a normal amount reach only T1 (the one simulated miss).
* `baseline_p95` is a constant in the Android view model for now. It will come from the local payee ledger.
* `ThehroEngine.kt` and `PlaybookCards.kt` compile with kotlinc 2.0 and give the same tier, p and card as Python on spot checks. `CallStateProbe.kt` and the view-model change use Android APIs and were not compiled in our sandbox; run `./gradlew assembleDebug` before a release. Showing the THEHRO tier on the pay screen is a finale task.
* The package name is still `com.example`. It will be renamed before any store release.

## Verification status (12 Oct 2026, 22:10 IST)

| Part | Check | Result |
|---|---|---|
| THEHRO engine, real-time tracker, radar (Python) | `make thehro-test` | ✅ 23/23 tests pass |
| Simulated evaluation | `make thehro` | ✅ 46/50 held at T3+, 0 false holds in 200 normal payments (simulated) |
| JS port vs Python | `make parity` | ✅ 250/250 identical decisions |
| Web checks | `make check` | ✅ 144/144: 66 v2 single-file checks + 78 THEHRO checks (card schema, every card fires, 8 normal payments stay quiet, floors, anti-splitting, retry, 2,000-decision speed, all 9 web pages load with no script errors, demo sessions end at the right tier, offline cache) |
| GitHub Pages | HTTP check of all pages and assets | ✅ all 200 |
| Kotlin engine | `kotlinc` 2.0 compile + spot checks vs Python | ✅ same tier, p and card (incl. split and retry cases) |
| Android full build | `./gradlew assembleDebug` | ⚠️ not run (no Android SDK in our sandbox). Run before a release |
| Android APK release | GitHub Releases | ⚠️ no release published yet |
| Fixed in this pass | | unknown stage in a newer card crashed the JS and Python engines (now ignored, like Kotlin) · an unrelated card's reason was shown when only a safety floor fired · `run_smoke.sh` / `publish.sh` not executable · unsourced "87 %" claim removed |
