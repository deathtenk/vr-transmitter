.PHONY: run-dev

run-dev: 
	DEV=true clj -A:test -m deathtenk.vr-transmitter
nrepl:
	DEV=true clj -A:test:nrepl
