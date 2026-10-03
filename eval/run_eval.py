#!/usr/bin/env python3
"""Wrapper: python3 eval/run_eval.py  →  node eval/run_eval.js (jsdom chahiye)."""
import pathlib, subprocess, sys, os
here = pathlib.Path(__file__).parent
env = dict(os.environ)
lib = env.get('JSDOM_DIR', '/tmp/domtest')
node_modules = pathlib.Path(lib) / 'node_modules'
if not (node_modules / 'jsdom').exists():
    print(f'→ jsdom install ({lib}) …'); pathlib.Path(lib).mkdir(parents=True, exist_ok=True)
    subprocess.run(['npm', 'install', 'jsdom', '--no-audit', '--no-fund', '--silent'], cwd=lib, check=True)
env['NODE_PATH'] = str(node_modules)
sys.exit(subprocess.run(['node', str(here / 'run_eval.js')], env=env).returncode)
