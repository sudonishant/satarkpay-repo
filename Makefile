# SatarkPay · dev shortcuts
.PHONY: help demo check eval zip clean thehro thehro-test parity serve
help:
	@echo "make thehro      → THEHRO simulated evaluation (RAKSHAM PS 02)"
	@echo "make thehro-test → THEHRO unit tests"
	@echo "make parity      → JS port vs Python, 250 sessions"
	@echo "make serve       → http://localhost:8000/webapp/thehro.html"
	@echo "make demo   → offline demo browser me kholo (file path print hota hai)"
	@echo "make check  → 144 automated checks (66 v2 + 78 THEHRO, jsdom)"
	@echo "make eval   → 33-message eval harness (P/R/F + latest_results.json)"
	@echo "make zip    → submission zip banao"

demo:
	@echo "web/satarkpay_m2.html — browser me kholo (offline chalta hai)"
	@test -f web/satarkpay_m2.html && echo "✓ file present" || (echo "web/satarkpay_m2.html missing" && exit 1)

check:
	cd web && ./run_smoke.sh

eval:
	python3 eval/run_eval.py

zip:
	cd .. && rm -f satarkpay-repo.zip && zip -qr satarkpay-repo.zip satarkpay-repo -x "satarkpay-repo/.git/*"
	@ls -la ../satarkpay-repo.zip

clean:
	rm -f eval/latest_results.json thehro/results/parity_ref.json

thehro:
	python3 -m thehro.simulate

thehro-test:
	python3 -m unittest discover -s thehro/tests -t .

parity:
	python3 tools/export_parity.py && node tools/parity_check.js

serve:
	python3 -m http.server 8000
