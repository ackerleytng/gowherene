(ns gowherene.reader.location
  (:require [clojure.zip :as zip]))

(defn add-location
  [{:keys [loc type] :as input}]
  (case type
    :postal-code (assoc input :location (zip/node loc))
    input))
