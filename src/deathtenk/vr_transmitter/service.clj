(ns deathtenk.vr-transmitter.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [kafka-messenger.serializer :as ser]
            [kafka-messenger.producer :as p]
            [clojure.spec.alpha :as s]
            [ring.util.response :as rr]
            [io.pedestal.http.body-params :as bp]
            [io.pedestal.interceptor :as i]))

;; Spec
(s/def ::timestamp int?)
(s/def ::appID int?)
(s/def ::deviceName string?)
(s/def ::steamUserID string?)
(s/def ::x float?)
(s/def ::y float?)
(s/def ::z float?)
(s/def ::qw float?)
(s/def ::qx float?)
(s/def ::qy float?)
(s/def ::qz float?)

(s/def ::openvr-data (s/keys :req-un [::timestamp
                                      ::appID
                                      ::deviceName
                                      ::steamUserID
                                      ::x
                                      ::y
                                      ::z
                                      ::qw
                                      ::qx
                                      ::qy
                                      ::qz]))


(comment
  (s/explain ::openvr-data {:timestamp 12345 
                            :appID 12345
                            :deviceName "foo"
                            :steamUserID "bar"
                            :x 0.0
                            :y 0.0
                            :z 0.0
                            :qw 0.0
                            :qx 0.0
                            :qy 0.0
                            :qz 0.0}))

;; ENV vars
(def bootstrap-server (or (System/getenv "BOOTSTRAP_SERVER") "localhost:9999"))
(def input-topic (or (System/getenv "INPUT_TOPIC") "vrdata"))

;; Kafka
(def producer (p/create! {:config
                           {:producer.config/bootstrap-servers-config bootstrap-server}}))


(defn kafka-send [producer data]
  (p/async-send! producer {:topic input-topic
                           :key nil
                           :value data}))

(def attach-producer
  (i/interceptor {:name ::attach-producer
                  :enter (fn [ctx]
                           (assoc ctx ::producer producer))}))
                    

(def parse 
  (i/interceptor {:name ::parse
                  :enter (fn [{:keys [request] :as ctx}]
                           (let [vrdata (get request :json-params)]
                             (assoc ctx ::vrdata vrdata)))}))

(def validate
  (i/interceptor {:name ::validate
                  :enter (fn [ctx]
                           (let [vrdata (get ctx ::vrdata)]
                             (if (s/valid? ::openvr-data vrdata)
                               ctx
                               (assoc ctx ::invalid? true
                                          ::reason (s/explain-data ::openvr-data vrdata)))))}))


(def result
  (i/interceptor {:name ::result
                  :enter (fn [ctx]
                           (let [invalid? (get ctx ::invalid?)
                                 reason (get ctx ::reason)]
                             (if invalid?
                               (do
                                (println "BAD REQUEST! reason: " reason)
                                (assoc ctx :response (rr/bad-request reason)))
                               (let [vrdata (get ctx ::vrdata)
                                     producer (get ctx ::producer)]
                                 (kafka-send producer vrdata)
                                 (assoc ctx :response (rr/response vrdata))))))}))
                                
                              
(defn hello-world
  [request]
  {:status 200
   :body "healthy!"})


(def common-interceptors 
  [(bp/body-params)])

(def routes
  #{["/" :get `hello-world]
    ["/vr" :post 
     (conj common-interceptors
           attach-producer
           parse
           validate
           result)]})

(def service {:env                 :prod
              ::http/routes        routes
              ::http/resource-path "/public"
              ::http/type          :jetty
              ::http/port          8080})
