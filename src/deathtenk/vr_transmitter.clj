(ns deathtenk.vr-transmitter
  (:gen-class)
  (:require [deathtenk.vr-transmitter.server :as s]
            [kafka-server.core :as ks]))

(def dev? (System/getenv "DEV"))

(defn kserver []
  (ks/system-startup! {}))

(defn -main
  "Entrypoint for vr-transmitter"
  [& args]
  (if dev?
    (do (kserver)
        (s/run))
    (s/run)))
