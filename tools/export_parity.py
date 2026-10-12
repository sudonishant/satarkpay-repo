"""Write thehro/results/parity_ref.json (Python decisions) for tools/parity_check.js."""
import json, random, sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from thehro.engine import decide, load_cards
from thehro.simulate import normal_sessions, scam_sessions
r = random.Random(7); cards = load_cards(); out = []
for c in scam_sessions(r) + normal_sessions(r):
    d = decide(c["s"], cards); out.append({"s": c["s"], "tier": d.tier, "p": d.p, "card": d.card_id})
p = Path(__file__).resolve().parents[1] / "thehro/results/parity_ref.json"
p.write_text(json.dumps(out)); print("wrote", p, len(out))
