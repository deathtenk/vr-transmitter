(ns deathtenk.vr-transmitter.server
  (:gen-class) ; for -main method in uberjar
  (:require [io.pedestal.http :as http]
            [deathtenk.vr-transmitter.service :as service]))

;; This is an adapted service map, that can be started and stopped
;; From the REPL you can call http/start and http/stop on this service
(defonce runnable-service (http/create-server service/service))

(defn run-dev
  "The entry-point for 'lein run-dev'"
  [& args]
  (println "\nCreating your [DEV] server...")
  (-> service/service ;; start with production configuration
      (merge {:env :dev
              ;; do not block thread that starts web server
              ::http/join? false
              ;; Routes can be a function that resolve routes,
              ;;  we can use this to set the routes to be reloadable
              ::http/routes #(deref #'service/routes)
              ;; all origins are allowed in dev mode
              ::http/allowed-origins {:creds true :allowed-origins (constantly true)}})
      ;; Wire up interceptor chains
      http/default-interceptors
      http/dev-interceptors
      http/create-server
      http/start))

(defn -main
  "The entry-point for 'lein run'"
  [& args]
  (println "\nCreating your server...")
  (http/start runnable-service))
