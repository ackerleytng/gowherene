(ns gowherene.reader.location
  (:require [clojure.zip :as zip]
            [gowherene.reader.regexes :refer [re-postal-code re-unit-number]]
            [gowherene.reader.road-name :refer [road-name]]))

(defn postal-code
  [string]
  (re-find re-postal-code string))

(defn unit-number
  [string]
  (re-find re-unit-number string))

(defn address-parts
  [string]
  nil)

(defn add-location
  [{:keys [loc type] :as input}]
  (case type
    :postal-code (assoc input :location (zip/node loc))
    input))
