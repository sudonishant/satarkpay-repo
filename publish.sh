#!/usr/bin/env bash
# SatarkPay → GitHub par publish karne ke liye (ek baar chalao)
#   ./publish.sh git@github.com:<username>/satarkpay.git
set -e
REMOTE="${1:?usage: ./publish.sh <git-remote-url>}"
cd "$(dirname "$0")"
git init -q
git add -A
git commit -q -m "feat: SatarkPay — SANGYAN submission (7 modules + R38 emergency layer)"
git branch -M main
git remote add origin "$REMOTE" 2>/dev/null || git remote set-url origin "$REMOTE"
git push -u origin main
echo "✓ push ho gaya → $REMOTE"
echo
echo "GitHub repo settings me ye check karo:"
echo "  • About me description + topics: upi-fraud, sebi, sangyan, android, privacy, bharat"
echo "  • Pages (optional): web/ folder se demo host kar sakte ho"
echo "  • Releases: satarkpay_deck.pdf + deck.pptx attach kar do"
