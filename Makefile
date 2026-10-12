# SatarkPay · dev shortcuts
.PHONY: help demo check eval deck zip clean thehro thehro-test parity serve
help:
	@echo "make thehro      → THEHRO simulated evaluation (RAKSHAM PS 02)"
	@echo "make thehro-test → THEHRO unit tests"
	@echo "make parity      → JS port vs Python, 250 sessions"
	@echo "make serve       → http://localhost:8000/webapp/thehro.html"
	@echo "make demo   → offline demo browser me kholo (file path print hota hai)"
	@echo "make check  → 66 automated checks (jsdom)"
	@echo "make eval   → 33-message eval harness (P/R/F + latest_results.json)"
	@echo "make deck   → PPTX + PDF + HTML deck rebuild"
	@echo "make zip    → submission zip banao"

demo:
	@echo "web/satarkpay_m2.html — browser me kholo (offline chalta hai)"
	@test -f web/satarkpay_m2.html && echo "✓ file present" || (echo "pehle: make deck" && exit 1)

check:
	cd web && ./run_smoke.sh

eval:
	python3 eval/run_eval.py

deck:
	cd web/deck && python3 build_charts.py && python3 build_diagrams.py && python3 build_deck.py

zip:
	cd .. && rm -f satarkpay-repo.zip && zip -qr satarkpay-repo.zip satarkpay-repo -x "satarkpay-repo/.git/*"
	@ls -la ../satarkpay-repo.zip

clean:
	rm -rf web/deck/charts/*.png web/deck/diagrams/*.png eval/latest_results.json

thehro:
	python3 -m thehro.simulate

thehro-test:
	python3 -m unittest discover -s thehro/tests -t .

parity:
	python3 tools/export_parity.py && node tools/parity_check.js

serve:
	python3 -m http.server 8000
