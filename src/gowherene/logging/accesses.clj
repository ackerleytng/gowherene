(ns gowherene.logging.accesses
  (:require [clojure.pprint :refer [pprint]]
            [environ.core :refer [env]]
            [gowherene.logging.db-utils :refer [db-spec clear-accesses-table!]]
            [korma.db :refer [defdb postgres]]
            [korma.core :refer [defentity select insert values]]))

(defdb logs-db (postgres db-spec))

(defentity accesses)

(defn log-access [time uri src]
  (insert accesses
          (values [{:time time :uri (or uri "") :src (or src "unknown")}])))

(defn show-accesses []
  (pprint (select accesses)))

(defn cleanup-accesses []
  (clear-accesses-table!))
