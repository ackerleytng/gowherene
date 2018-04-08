(ns gowherene.logging
  (:require [clojure.pprint :refer [pprint]]
            [environ.core :refer [env]]
            [gowherene.db-utils :refer [db-spec]]
            [korma.db :refer [defdb postgres]]
            [korma.core :refer [defentity select insert values]]
            [monger.core :as mg]
            [monger.collection :as mc]))

(defdb logs-db (postgres db-spec))

(defentity accesses)

(defn log-access [time uri src]
  (insert accesses
          (values [{:time time :uri (or uri "") :src (or src "unknown")}])))

(defn show-accesses []
  (pprint (select accesses)))

(defn log-request [request response]
  (let [uri (env :requests-database-url)
        {:keys [conn db]} (mg/connect-via-uri uri)]
    (mc/insert db "requests"
               {:request request
                :response response})
    (mg/disconnect conn)))

(defn show-requests []
  (let [uri (env :requests-database-url)
        {:keys [conn db]} (mg/connect-via-uri uri)]
    (pprint (mc/find-maps db "requests"))
    (mg/disconnect conn)))
