#!/usr/bin/env python3
"""threat_feed.json -> demo/_intel.json (chhota, family-diverse snapshot jo HTML me embed hota hai).
Run: python3 make_demo_snapshot.py
"""
import json
from pathlib import Path

H = Path(__file__).parent
feed = json.loads((H / 'threat_feed.json').read_text(encoding='utf-8'))
items = feed['items']

def score(x):
    return (len(x['platforms']) * 2 + len(x['vectors']) + len(x.get('matched_keywords', []))
            + (1 if x['status'] == 'NEW' else 0))

seen, sel = {}, []
for x in sorted(items, key=score, reverse=True):
    cap = 2 if x['family'] in ('F18', 'F14', 'F1') else 1
    if seen.get(x['family'], 0) >= cap:
        continue
    if len(sel) >= 14:
        break
    seen[x['family']] = seen.get(x['family'], 0) + 1
    sel.append(x)
sel.sort(key=lambda x: x['date'] or '', reverse=True)

out = {
    "generated_at": feed['generated_at'],
    "counts": {k: feed['counts'][k] for k in ('items', 'review_queue', 'by_family', 'raw_fetched')},
    "items": [{"t": x['title'][:112], "f": x['family'], "fn": x['family_name'], "d": (x['date'] or '')[:10],
               "src": (x['source'] or '')[:32], "pl": x['platforms'][:2], "ve": x['vectors'][:2],
               "sg": x['signal_hints'][:2], "st": x['status'], "sev": x.get('severity', 'medium'),
               "kw": x.get('matched_keywords', [])[:3]} for x in sel],
}
(H.parent / 'demo' / '_intel.json').write_text(json.dumps(out, ensure_ascii=False, indent=1), encoding='utf-8')
print('demo snapshot:', len(out['items']), 'items')
