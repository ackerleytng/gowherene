(ns dev
  (:require [mount.core :as mount :refer [defstate]]
            [clojure.tools.namespace.repl :as tn]
            [gowherene.core :refer [start-gowherene]]))

(defstate gowherene-app
  :start (start-gowherene)
  :stop (.stop gowherene-app))

(defn start []
  (mount/start))

(defn stop []
  (mount/stop))

(defn refresh []
  (stop)
  (tn/refresh))

(defn refresh-all []
  (stop)
  (tn/refresh-all))

(defn go
  "starts all states defined by defstate"
  []
  (start)
  :ready)

(defn reset
  "stops all states defined by defstate, reloads modified source files, and restarts the states"
  []
  (stop)
  (tn/refresh :after 'dev/go))
