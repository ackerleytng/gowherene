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

(defn building-number
  [string road-name]
  (when-let [matches (re-find (re-pattern (str "(\\d+)\\s*" road-name)) string)]
    (second matches)))

(defn address-parts
  [string]
  (let [postal-code* (postal-code string)
        unit-number* (unit-number string)
        road-name* (road-name string)
        parts {:postal-code postal-code*
               :unit-number unit-number*
               :road-name road-name*}]
    (assoc
     parts
     :building-number
     (when road-name*
       (building-number string road-name*)))))

(defn add-location
  [{:keys [loc type] :as input}]
  (case type
    :postal-code (assoc input :location (address-parts (zip/node loc)))
    :labelled (assoc input :location (address-parts (:value input)))
    input))
