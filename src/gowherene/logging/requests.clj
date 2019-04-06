(ns gowherene.logging.requests
  (:require [clojure.pprint :refer [pprint]]
            [environ.core :refer [env]]
            [monger.core :as mg]
            [monger.collection :as mc]))

(defn log-request [request response time]
  (let [uri               (env :mongodb-uri)
        {:keys [conn db]} (mg/connect-via-uri uri)]
    (mc/insert db "requests"
               {:request  request
                :response response
                :time     time})
    (mg/disconnect conn)))

(defn show-requests []
  (let [uri (env :mongodb-uri)
        {:keys [conn db]} (mg/connect-via-uri uri)]
    (pprint (mc/find-maps db "requests"))
    (mg/disconnect conn)))

(defn cleanup-requests []
  (let [uri (env :mongodb-uri)
        {:keys [conn db]} (mg/connect-via-uri uri)]
    (mc/remove db "requests")
    (mg/disconnect conn)))
