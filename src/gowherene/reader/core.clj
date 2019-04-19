(ns gowherene.reader.core
  (:require [hickory.core :refer [as-hickory parse]]
            [hickory.zip :refer [hickory-zip]]
            [clojure.set :as set]
            [clojure.zip :as zip]
            [clojure.string :as str]
            [gowherene.reader.geocoding :refer [geocode add-latlng]]
            [gowherene.reader.regexes :refer [re-postal-code re-spaces]]
            [gowherene.reader.geocodables :refer [geocodables]]
            [gowherene.reader.location :refer [add-location]]
            [gowherene.reader.label :refer [add-label]]))

(defn hickory-zipper
  [page]
  (hickory-zip (as-hickory (parse page))))

(def ^:private unimportant-tags
  "These are tags that don't carry information relating to addresses, or are tags we don't need.

  Here are some tags that you think should not carry information, but are actually useful

  <br>: It is sometimes used to indicate the end of an address
        (the text after the <br> might be a phone number, for example
  "
  #{:meta :noscript :script :link :style :header :footer :head :nav :img
    :progress :ins :iframe :svg})

(defn- remove-nodes
  "Remove any node where (pred node) is true from zipper before returning the zipper.
  Returns a hickory-zip.

  Assumes that zipper is at the root, and that this zipper was built from a hickory."
  [pred zipper]
  (->> zipper
       (iterate (fn [loc]
                  (if (pred (zip/node loc))
                    (zip/remove loc)
                    (zip/next loc))))
       (take-while (complement zip/end?))
       last
       zip/root
       hickory-zip))

(defn cleanup
  [page-zipper]
  (remove-nodes
   #(or
     (unimportant-tags (:tag %))
     ;; Remove nodes with nil content
     (and (map? %)
          ;; Be defensive, since some tags, such as <hr> and <br> might be useful information
          (#{:a :div :span :i} (:tag %))
          (nil? (:content %)))
     ;; Remove all nodes that are just spaces (keep \n and \t because they might be useful information
     (and (string? %) (= "" (str/replace % re-spaces "")))
     ;; Actually comments could be useful if, perhaps, they provide labels like title or address
     (= :comment (:type %)))
   page-zipper))

(defn publish
  [data]
  (->> data
       (filter #(:latlng %))
       (map #(select-keys % [:label :location :latlng]))))

(defn process-clean-zipper
  [zipper]
  (->> zipper
       geocodables
       ;; list of geocodables. for each geocodable found
       ;; {:type :postal-code :value <postal-code> :loc <loc in zipper>}
       ;; {:type :address :value <address> :loc <loc in zipper>}
       (map add-location)
       ;; {:type :address :value <address> :loc <loc in zipper>
       ;;  :location <as much location info as possible, usually the full address}
       (map add-label)
       ;; {:type :address :value <address> :loc <loc in zipper>
       ;;  :location <as much location info as possible, usually the full address
       ;;  :location-label <name of shop, event, etc>}
       ))

(defn location-subset?
  "Location a is a subset of b if for the keys :postal-code :unit-number :road-name :building-number, a contains less information about the same location than b. a and b are assumed to be the same location once any of the values are the same between a and b"
  [a b]
  (let [a-set (set (filter second (seq a)))
        b-set (set (filter second (seq b)))]
    (set/subset? a-set b-set)))

(defn dedupe-by-location
  "Removes duplicates in info where the location is the same. Location similarity is determined by location-superset?"
  [info]
  (reduce
   (fn [acc v]
     (let [l
           (:location v)
           superset-and-unrelated
           (remove #(location-subset? (:location %) l) acc)]
       (if (some #(location-subset? l (:location %)) superset-and-unrelated)
         superset-and-unrelated
         (conj superset-and-unrelated v))))
   []
   info))

(defn remove-duplicates
  [info]
  (->> info
       (group-by :label)
       (mapcat #(dedupe-by-location (second %)))))

(defn process
  [page]
  (->> page
       hickory-zipper
       cleanup
       process-clean-zipper
       remove-duplicates
       (pmap add-latlng)
       publish))
