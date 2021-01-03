(ns user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  (:require
   [clojure.java.io :as io]
   [clojure.java.javadoc :refer [javadoc]]
   [clojure.pprint :refer [pprint]]
   [clojure.reflect :refer [reflect]]
   [clojure.repl :refer [apropos dir doc find-doc pst source]]
   [clojure.set :as set]
   [clojure.zip :as zip]
   [clojure.string :as string]
   [clojure.test :as test]
   [clojure.tools.namespace.repl :refer [clear]]

   [hickory.zip :refer [hickory-zip]]
   [hickory.convert :refer [hickory-to-hiccup]]
   [gowherene.reader.core :refer :all]
   [gowherene.reader.geocoding :refer [geocode-onemap geocode-google add-latlng]]
   [gowherene.reader.regexes :refer [re-postal-code re-spaces]]
   [gowherene.reader.geocodables :refer [geocodables]]
   [gowherene.reader.location :refer [add-location]]
   [gowherene.reader.label :refer :all]
   [gowherene.reader.utils :refer [content subtree]]
   [gowherene.core :refer [start-gowherene]])
  (:import [java.lang Math]))

(defn all-tags
  [loc]
  (->> loc
       (iterate zip/next)
       (take-while (complement zip/end?))
       (map (comp :tag zip/node))
       (into #{})
       (filter identity)))

(defn list-data-files []
  (->> (io/file "data/files")
       file-seq
       (filter #(.isFile %))
       (map #(str "data/files/"
                  (.getName %)))))

(defn generate
  [function]
  (let [files (list-data-files)
        prep (fn [f] (->> (slurp f)
                          hickory-zipper
                          cleanup))]
    (interleave
     files
     (map (comp function prep) files))))

(defn get-index
  [header]
  (and
   header
   (when-let [num (re-find #"(\d+)\." header)]
     (Integer/parseInt (get num 1)))))

(comment
  (generate (fn [z] (->> z
                         process-clean-zipper
                         (group-by :type)
                         (map (fn [[k v]] [k (count v)]))
                         (into {}))))

  (let [files ["data/files/11-budget-buffets-in-singapore-20-and-below.html"
               "data/files/affordable-seafood-buffets.html"
               "data/files/best-burgers.html"
               "data/files/no-gst-restaurants.html"
               "data/files/cheap-orchard-buffets.html"
               "data/files/dim-sum-restaurants-singapore.html"
               "data/files/local-breakfast-east-singapore.html"
               "data/files/singapore-cafes-with-no-gst.html"
               "data/files/no-gst-restaurants.html"
               "data/files/snippets-the-best-buffets-in-singapore-the-definitive-guide-because-you-shouldnt-waste-your-calories-and-moolah.html"
               "data/files/the-ultimate-guide-to-local-breakfast-in-singapore.html"
               "data/files/tiffany-singapore.html"]]
    (->> files
         (mapcat #(generate % all-tags))
         (into #{}))
    (interleave files (map #(generate ) files)))


  (def page (slurp "data/files/cheap-food-orchard.html"))
  (def page (slurp "data/files/11-budget-buffets-in-singapore-20-and-below.html"))
  (def page (slurp "data/files/dim-sum-restaurants-singapore.html"))
  (def page (slurp "data/files/no-gst-restaurants.html"))
  (def page (slurp "data/files/snippets-the-best-buffets-in-singapore-the-definitive-guide-because-you-shouldnt-waste-your-calories-and-moolah.html"))
  (def page (slurp "data/files/the-ultimate-guide-to-local-breakfast-in-singapore.html"))
  (def page (slurp "data/files/best-burgers.html"))

  (->> page
       hickory-zipper
       cleanup
       geocodables
       (map add-location)
       (map add-label)
       (pmap add-latlng)
       (map #(dissoc % :loc :trimmed-loc)))

  (->> page
       hickory-zipper
       cleanup
       process-clean-zipper
       (map #(dissoc % :loc :trimmed-loc))
       (group-by :label)
       (map (fn [[k v]] [(get-index k) v]))
       sort
       ;;(map #(dedupe-by-location (second %)))
       ;;remove-duplicates
       )



  (->> page
       hickory-zipper
       cleanup
       process-clean-zipper
       remove-duplicates
       (map #(dissoc % :loc :trimmed-loc)))

  (def zzz (process page))
  (->> zzz
       (map #(assoc % :index (get-index (:label %))))
       (group-by :index))

  (stop)
  )
