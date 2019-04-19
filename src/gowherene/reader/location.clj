(ns gowherene.reader.location
  (:require [clojure.zip :as zip]
            [gowherene.reader.geocodables :refer [labelled-info]]
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

(defn handle-labelled
  ([input] (handle-labelled input 0))
  ([{:keys [value loc] :as input} levels-traversed]
   (let [parts (address-parts value)]
     (cond
       ;; Can find address parts, yay!
       (some identity (vals parts))
       (assoc input :location parts)
       ;; Can't find, try upper level
       (< levels-traversed 3)
       (recur (labelled-info (zip/up loc)) (inc levels-traversed))
       ;; Went up so many levels already, so give up searching
       :else input))))

(defn add-location
  [{:keys [loc type] :as input}]
  (case type
    :postal-code (assoc input :location (address-parts (zip/node loc)))
    :labelled (handle-labelled input)
    input))
