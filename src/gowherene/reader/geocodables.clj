(ns gowherene.reader.geocodables
  (:require [gowherene.reader.regexes :refer [re-postal-code]]
            [clojure.zip :as zip]))

(defn- node-contains-postal-code?
  [loc]
  (let [node (zip/node loc)]
    (and (string? node) (re-find re-postal-code node))))

(defn- find-postal-codes
  [page-zipper]
  (->> page-zipper
       (iterate zip/next)
       (take-while (complement zip/end?))
       (filter node-contains-postal-code?)
       (map (fn [loc] {:loc loc :type :postal-code :value (re-find re-postal-code (zip/node loc))}))))

(defn geocodables
  [page-zipper]
  (into []
        (find-postal-codes page-zipper)))

(comment
  (->> gowherene.reader.core/page
       gowherene.reader.core/hickory-zipper
       gowherene.reader.core/cleanup
       geocodables
       (map #(update % :loc zip/node))
       )



  )
