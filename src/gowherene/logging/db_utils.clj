(ns gowherene.logging.db-utils
  (:require [clojure.java.jdbc :as sql]
            [environ.core :refer [env]]))

(def db-spec
  (env :database-url))

(def accesses-spec [[:id :serial]
                    [:time :bigint]
                    [:uri "varchar(64)"]
                    [:src "varchar(15)"]])

(defn create-accesses-table! []
  (sql/db-do-commands
   db-spec
   [(sql/create-table-ddl :accesses accesses-spec {:conditional? true})
    "CREATE INDEX IF NOT EXISTS id ON accesses ( id );"]))

(defn drop-accesses-table! []
  (sql/db-do-commands
   db-spec
   ["DROP INDEX IF EXISTS id;"
    (sql/drop-table-ddl :accesses {:conditional? true})]))

(defn clear-accesses-table!  []
  (do (drop-accesses-table!)
      (create-accesses-table!)))

(sql/db-do-commands
 db-spec
 (sql/create-table-ddl :accesses accesses-spec {:conditional? true}))
