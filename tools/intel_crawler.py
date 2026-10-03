#!/usr/bin/env python3
"""
SatarkPay M2 · Intel Desk crawler  (Feature 2: "pura internet pe se naya scam dhoondhna")

Kya karta hai
-------------
1. Public RSS/Atom sources + Google News query-feeds se *naye* fraud headlines uthata hai.
   (sirf title / link / date — article body scrape NAHI karta: DPDP + copyright safe.)
2. Fraud-relevance filter (keywords EN + HI).
3. Dedupe (URL + fuzzy title match).
4. Rule-based "family" classification -> SatarkPay threat library ke F1..F14 par map.
5. Novelty check: jo item kisi known pattern se match na kare usko UNCATEGORIZED mark karta hai
   -> ops console ke "New Pattern Review" queue me jata hai (human 15-second verdict).
6. Structured extract: platform (WhatsApp/Telegram/SMS/call), vector (APK, QR, collect, autopay...),
   amount mentions, apps mentioned, aur ek "signal hint" = woh metadata jo phone app dekh sakta hai.
7. threat_feed.json likhta hai (app + console dono isi ko padhte hain).

LLM mode (optional, recommended in production)
----------------------------------------------
env SATARKPAY_LLM=1 + SATARKPAY_LLM_CMD="python3 llm_extract.py" -> har relevant item ko
LLM ko bhejkar structured JSON mangwate hain (better extraction, multi-line modus). Bina LLM ke
rule-based fallback chalta hai, isliye ye script offline demo me bhi chalti hai.

Usage:  python3 intel_crawler.py [--max-age-days 45] [--out threat_feed.json]
"""
from __future__ import annotations

import argparse
import hashlib
import html
import json
import os
import re
import subprocess
import sys
import urllib.parse
import urllib.request
import xml.etree.ElementTree as ET
from datetime import datetime, timedelta, timezone
from difflib import SequenceMatcher
from email.utils import parsedate_to_datetime
from pathlib import Path

HERE = Path(__file__).parent
UA = "Mozilla/5.0 (SatarkPay-IntelDesk/2.0; +https://satarkpay.example/intel)"

# --------------------------------------------------------------------------- taxonomy
FAMILIES = {
    "F1": {"name": "Digital arrest / fake officer", "reason": "unknown_call_exposure", "tier": "T3"},
    "F2": {"name": "Remote access / APK / KYC-expiry", "reason": "screen_share_remote", "tier": "T3"},
    "F3": {"name": "Investment / task / pig-butchering", "reason": "escalation_pattern", "tier": "T2"},
    "F4": {"name": "Impersonation emergency / voice clone", "reason": "new_payee", "tier": "T3"},
    "F5": {"name": "Fake collect / QR / 'receive money'", "reason": "direction_mismatch", "tier": "T1"},
    "F6": {"name": "Fake customer care / refund / prize", "reason": "purpose_mismatch", "tier": "T2"},
    "F7": {"name": "AutoPay / e-mandate hijack", "reason": "mandate_unexpected", "tier": "T2"},
    "F8": {"name": "Job / mule / rent-an-account", "reason": "received_money_guard", "tier": "T1"},
    "F9": {"name": "Fake payment screenshot / merchant fraud", "reason": "no_credit_in_your_app", "tier": "T1"},
    "F10": {"name": "Screen-share / frozen-screen lure", "reason": "screen_share_remote", "tier": "T3"},
    "F11": {"name": "Fake challan / FASTag / bill portal", "reason": "lookalike_domain", "tier": "T2"},
    "F12": {"name": "AI voice / deepfake / AI app", "reason": "safe_word_required", "tier": "T3"},
    "F13": {"name": "Wallet / UPI-app connect abuse", "reason": "linked_app_risk", "tier": "T2"},
    "F14": {"name": "New / unclassified pattern", "reason": "novel_pattern_hold", "tier": "T2"},
    "F15": {"name": "Sextortion / loan-app / blackmail payment", "reason": "coercion_pressure", "tier": "T3"},
    "F16": {"name": "SIM swap / OTP / Aadhaar-identity takeover", "reason": "device_or_identity_change", "tier": "T3"},
    "F17": {"name": "Romance / matrimonial / gift-parcel fraud", "reason": "escalation_pattern", "tier": "T2"},
    "F18": {"name": "Unspecified online-financial fraud (watchlist)", "reason": "watch_only", "tier": "T1"},
}

RULES = [
    ("F1", r"digital arrest|fake (police|cbi|customs|officer)|safe account|video call.*(police|arrest)|पुलिस|साइबर सेल"),
    ("F2", r"anydesk|teamviewer|remote access|\bapk\b|\.apk\b|sideload|kyc.{0,15}(expir|update|link)|screen shar"),
    ("F10", r"frozen screen|freeze.{0,10}screen|screen.{0,10}freeze|black screen|ब्लैक स्क्रीन"),
    ("F3", r"task scam|investment scam|trading app|pig butcher|stock tip|telegram.{0,20}(invest|task|trading)|part.?time job.{0,20}(scam|fraud)"),
    ("F4", r"voice clon|deepfake|impersonat|papa.{0,10}accident|cloned voice"),
    ("F5", r"collect request|scan.{0,15}qr|qr code.{0,15}(scam|fraud)|receive money|request money|request money"),
    ("F6", r"customer care|fake (helpline|refund)|refund.{0,15}(scam|fraud)|prize|lottery|cashback.{0,10}link"),
    ("F7", r"autopay|auto.?pay|e-?mandate|mandate|recurring (debit|payment)|स्वतः भुगतान"),
    ("F8", r"mule account|rent.{0,15}account|job.{0,15}(deposit|registration fee)|money mule|झूठी नौकरी"),
    ("F9", r"fake (payment )?(screenshot|receipt)|payment screenshot|merchant.{0,15}(dupe|scam)|received money.{0,15}not"),
    ("F11", r"challan|fastag|e-?challan|electricity bill.{0,15}link|fake (bill|portal)"),
    ("F13", r"wallet.{0,20}(link|connect)|upi app.{0,15}(link|connect)|delegate payment|upi circle"),
    ("F12", r"ai (voice|call|video)|deepfake"),
    ("F2", r"loan app|screen.?record"),
    ("F15", r"sextortion|intimate (photo|video)|loan app.{0,15}(harass|threat)|blackmail"),
    ("F16", r"sim swap|otp.{0,15}(share|stolen|fraud)|aadhaar.{0,15}(misuse|fraud)|identity theft"),
    ("F17", r"romance scam|matrimonial|prepaid parcel|customs.{0,15}parcel|foreign (friend|gift)|dating app.{0,15}fraud"),
    ("F18", r"cyber ?(fraud|crime)|online fraud|bank fraud|duped|dupe[ds]? of|cheated of|lost.{0,15}(rs|₹|crore|lakh)|held for|arrested.{0,25}fraud|fraud case"),
]

VECTOR_MAP = {
    "APK / sideload": r"apk|sideload|install.{0,15}app",
    "Remote-access app": r"anydesk|teamviewer|rustdesk|quick ?support",
    "Screen share": r"screen shar|screen.?record",
    "QR code": r"\bqr\b|scan.{0,10}code",
    "Collect request": r"collect request|request money|receive money",
    "AutoPay / mandate": r"autopay|auto.?pay|mandate|recurring debit",
    "UPI deep link": r"upi://|deep ?link|payment link",
    "Lookalike website": r"fake (website|site|portal|domain)|look.?alike|typo.?squat|clone.{0,10}(site|website)",
    "Fake screenshot": r"screenshot|receipt|scrnshot",
    "Voice/Voice-note": r"voice|audio clip|call recording",
    "SMS / DLT header": r"\bsms\b|sender id|dlt|header",
}
PLATFORM_MAP = {
    "WhatsApp": r"whatsapp|व्हाट्सएप",
    "Telegram": r"telegram|टेलीग्राम",
    "Instagram/Facebook": r"instagram|facebook|meta|reels",
    "Phone call": r"phone call|video call|call.{0,10}(fraud|scam)|कॉल",
    "SMS": r"\bsms\b|text message|मैसेज",
    "Email": r"\bemail\b|phishing mail",
    "Search ad / fake care number": r"google search|search result|fake number|helpline number",
    "Game/app store": r"play store|apk site|mod apk",
}
SIGNAL_HINTS = [
    ("app_switch_whatsapp_upi", r"whatsapp.{0,60}(upi|pay|transfer)|(upi|pay).{0,60}whatsapp"),
    ("chat_then_pay", r"(chat|message).{0,30}(then|after).{0,20}(pay|transfer)"),
    ("screenshot_burst", r"multiple.{0,20}screenshot|payment proof|screenshot.{0,20}proof"),
    ("new_payee", r"new (upi id|account|number|payee)|first.{0,10}time.{0,10}(pay|transfer)"),
    ("lookalike_domain", r"fake (website|portal|domain|site)|look.?alike"),
    ("mandate", r"autopay|mandate|recurring"),
    ("amount_escalation", r"escalat|increas.{0,20}amount|1k.*5k.*20k|small.{0,20}then.{0,20}large"),
]

FRAUD_KW = re.compile(
    r"scam|fraud|cheat|dupe|thug|phish|cyber ?crime|ठगी|धोख|फ्रॉड|जालसाज|ठग|साइबर|हनी ?ट्रैप|fraudster",
    re.I,
)
MONEY = re.compile(r"(?:₹|rs\.?|inr)\s?([\d,]+(?:\.\d+)?)\s*(lakh|crore|k|लाख|करोड़)?", re.I)


def log(*a):
    print(*a, file=sys.stderr)


def fetch(url: str, timeout: int = 20) -> str | None:
    req = urllib.request.Request(url, headers={"User-Agent": UA, "Accept": "application/rss+xml, application/xml, text/xml, */*"})
    try:
        with urllib.request.urlopen(req, timeout=timeout) as r:
            raw = r.read()
        return raw.decode("utf-8", errors="replace")
    except Exception as e:  # noqa: BLE001
        log(f"  ! fetch fail {url[:70]} :: {type(e).__name__}: {e}")
        return None


def parse_feed(xml_text: str, fallback_source: str):
    items = []
    try:
        root = ET.fromstring(xml_text.lstrip("\ufeff"))
    except ET.ParseError as e:
        log(f"  ! parse fail {fallback_source}: {e}")
        return items
    ns = {"atom": "http://www.w3.org/2005/Atom", "dc": "http://purl.org/dc/elements/1.1/"}
    nodes = root.findall(".//item") or root.findall(".//atom:entry", ns)
    for n in nodes:
        def txt(*tags):
            for t in tags:
                try:
                    el = n.find(t, ns) if ":" in t else n.find(t)
                except SyntaxError:
                    el = None
                if el is not None and (el.text or "").strip():
                    return html.unescape(el.text.strip())
            return ""
        title = txt("title", "atom:title")
        link = txt("link", "atom:link")
        if not link:
            el = n.find("atom:link", ns)
            link = (el.get("href") if el is not None else "") or ""
        date = txt("pubDate", "atom:updated", "dc:date", "published")
        desc = txt("description", "atom:summary")
        src = txt("source", "atom:author/atom:name") or fallback_source
        if title:
            desc = re.sub(r"<[^>]+>", " ", desc)
            desc = re.sub(r"\s+", " ", html.unescape(desc)).strip()[:160]
            items.append({"title": title, "link": link, "date_raw": date, "source": src, "snippet": desc})
    return items


def parse_date(s: str) -> datetime | None:
    if not s:
        return None
    for fn in (lambda x: parsedate_to_datetime(x),
               lambda x: datetime.fromisoformat(x.replace("Z", "+00:00"))):
        try:
            d = fn(s)
            return d if d.tzinfo else d.replace(tzinfo=timezone.utc)
        except Exception:  # noqa: BLE001
            continue
    return None


def classify(title: str) -> tuple[str, str, list[str], list[str], list[str]]:
    """returns family_id, family_name, vectors, platforms, signal_hints"""
    fam, best = "F14", 0
    for fid, pat in RULES:
        m = re.search(pat, title, re.I)
        if m:
            score = len(m.group(0))
            if score > best:
                fam, best = fid, score
    vectors = [k for k, p in VECTOR_MAP.items() if re.search(p, title, re.I)]
    platforms = [k for k, p in PLATFORM_MAP.items() if re.search(p, title, re.I)]
    hints = [k for k, p in SIGNAL_HINTS if re.search(p, title, re.I)]
    return fam, FAMILIES[fam]["name"], vectors, platforms, hints


def amounts(title: str) -> list[str]:
    out = []
    for num, unit in MONEY.findall(title):
        out.append((num + (" " + unit if unit else "")).strip())
    return out[:3]


def fingerprint(title: str) -> str:
    norm = re.sub(r"[^a-z0-9\u0900-\u097f ]+", " ", title.lower())
    norm = re.sub(r"\s+", " ", norm).strip()
    return hashlib.sha1(norm.encode()).hexdigest()[:12]


def llm_extract(items: list[dict]) -> list[dict]:
    """Optional: hand relevant items to an LLM for structured modus extraction."""
    if os.environ.get("SATARKPAY_LLM") != "1":
        return items
    cmd = os.environ.get("SATARKPAY_LLM_CMD")
    if not cmd:
        log("  ! SATARKPAY_LLM=1 but no SATARKPAY_LLM_CMD; skipping LLM extraction")
        return items
    payload = json.dumps([{"title": i["title"], "source": i["source"]} for i in items], ensure_ascii=False)
    try:
        p = subprocess.run(cmd, input=payload, capture_output=True, text=True, timeout=180, shell=True)
        enriched = json.loads(p.stdout)
        if isinstance(enriched, list) and len(enriched) == len(items):
            for a, b in zip(items, enriched):
                a.update({k: v for k, v in b.items() if k in {"family", "modus", "signals", "reason_code", "severity"}})
            log(f"  ✓ LLM extraction applied to {len(items)} items")
    except Exception as e:  # noqa: BLE001
        log(f"  ! LLM extraction failed ({type(e).__name__}) -> keyless rule-based output used")
    return items


def crawl(max_age_days: int) -> dict:
    cfg = json.loads((HERE / "sources.json").read_text(encoding="utf-8"))
    raw: list[dict] = []
    for q in cfg["queries"]:
        url = ("https://news.google.com/rss/search?q=" + urllib.parse.quote(q["q"]) +
               f"&hl={q['hl']}&gl=IN&ceid=IN:{q['hl'].split('-')[0]}")
        log(f"→ query: {q['q']}")
        x = fetch(url)
        if x:
            for it in parse_feed(x, f"Google News [{q['q']}]"):
                it["trust"] = q["trust"]
                raw.append(it)
    for f in cfg["feeds"]:
        log(f"→ feed: {f['name']}")
        x = fetch(f["url"])
        if x:
            for it in parse_feed(x, f["name"]):
                it["trust"] = f["trust"]
                raw.append(it)

    log(f"raw items: {len(raw)}")
    cutoff = datetime.now(timezone.utc) - timedelta(days=max_age_days)

    seen_fp, seen_url, kept = set(), set(), []
    for it in raw:
        t = it["title"]
        if not FRAUD_KW.search(t + " " + it.get("snippet", "")):
            continue
        dt = parse_date(it.get("date_raw", ""))
        it["date"] = dt.isoformat() if dt else None
        if dt and dt < cutoff:
            continue
        url_key = it["link"].split("?")[0]
        fp = fingerprint(t)
        dup = fp in seen_fp or url_key in seen_url
        if not dup:
            for k in kept:
                if SequenceMatcher(None, k["fp"], fp).ratio() > 0.86:
                    dup = True
                    break
        if dup:
            continue
        seen_fp.add(fp)
        seen_url.add(url_key)
        it["fp"] = fp
        kept.append(it)

    log(f"relevant + fresh: {len(kept)}")

    out = []
    for it in kept:
        hay = it["title"] + " " + it.get("snippet", "")
        fam, fam_name, vectors, platforms, hints = classify(hay)
        out.append({
            "id": it["fp"],
            "title": it["title"],
            "source": it["source"],
            "link": it["link"],
            "date": it["date"],
            "snippet": it.get("snippet", ""),
            "trust": it.get("trust", "news"),
            "family": fam,
            "family_name": fam_name,
            "vectors": vectors,
            "platforms": platforms,
            "amounts_mentioned": amounts(it["title"]),
            "signal_hints": hints,
            "matched_keywords": [k for k in ("apk","anydesk","qr","autopay","mandate","whatsapp","telegram","screenshot","screencast","remote","frozen screen","voice","deepfake","challan","fastag","loan") if k in hay.lower()],
            "suggested_reason_code": FAMILIES[fam]["reason"],
            "suggested_tier": FAMILIES[fam]["tier"],
            "status": ("REVIEW" if fam == "F14" else "WATCH" if fam == "F18" else "NEW"),
            # NEW = known pattern auto-mapped · WATCH = generic, low signal · REVIEW = novel -> human queue
            "severity": ("high" if any(k in hay.lower() for k in ("crore", "lakh", "digital arrest", "voice clon")) else "medium"),
            "reviewed_by": None,
        })
    out = llm_extract(out)
    rank = {"REVIEW": 0, "NEW": 1, "WATCH": 2}
    out.sort(key=lambda x: x["date"] or "", reverse=True)          # newest first
    out.sort(key=lambda x: rank.get(x["status"], 3))               # REVIEW -> NEW -> WATCH

    fam_counts: dict[str, int] = {}
    for o in out:
        fam_counts[o["family"]] = fam_counts.get(o["family"], 0) + 1

    return {
        "generated_at": datetime.now(timezone.utc).isoformat(timespec="seconds"),
        "generator": "SatarkPay Intel Desk crawler v2.0 (keyless rule-based extraction; LLM optional)",
        "window_days": max_age_days,
        "counts": {"items": len(out), "review_queue": sum(1 for o in out if o["status"] == "REVIEW"),
                   "by_family": fam_counts, "raw_fetched": len(raw)},
        "novel_patterns": [o for o in out if o["status"] == "REVIEW"][:12],
        "items": out,
    }


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--max-age-days", type=int, default=45)
    ap.add_argument("--out", default=str(HERE / "threat_feed.json"))
    a = ap.parse_args()
    feed = crawl(a.max_age_days)
    Path(a.out).write_text(json.dumps(feed, ensure_ascii=False, indent=2), encoding="utf-8")
    log(f"\n✓ wrote {a.out}: {feed['counts']}")


if __name__ == "__main__":
    main()
