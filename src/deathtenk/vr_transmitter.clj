(ns deathtenk.vr-transmitter
  (:gen-class)
  (:require [deathtenk.vr-transmitter.server :as s]))

(def dev? (System/getenv "DEV"))

(defn -main
  "Entrypoint for vr-transmitter"
  [& args]
  (if dev?
    (s/run-dev)
    (s/run)))
