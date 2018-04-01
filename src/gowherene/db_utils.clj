(ns gowherene.db-utils
  (:require [clojure.java.jdbc :as j]
            [environ.core :refer [env]]))

(defonce db-spec {:dbtype   (env :db-type)
                  :dbname   (env :db-name)
                  :host     (env :db-host)
                  :user     (env :db-user)
                  :password (env :db-password)})

(defonce accesses-spec [[:id :serial]
                        [:time :bigint]
                        [:uri "varchar(64)"]
                        [:src "varchar(15)"]])

(defn create-accesses-table!
  []
  (j/db-do-commands db-spec [(j/create-table-ddl :accesses accesses-spec
                                                 {:conditional? true})
                             "CREATE INDEX IF NOT EXISTS id ON accesses ( id );"]))

(defn drop-accesses-table!
  []
  (j/db-do-commands db-spec ["DROP INDEX IF EXISTS id;"
                             (j/drop-table-ddl :accesses
                                               {:conditional? true})]))

(defn clear-accesses-table!
  []
  (do (drop-accesses-table!)
      (create-accesses-table!)))

(def migrate create-accesses-table!)
(def rollback drop-accesses-table!)
(def cleanup clear-accesses-table!)
