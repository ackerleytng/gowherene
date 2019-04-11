(ns gowherene.reader.geocodables
  (:require [clojure.string :as string]
            [clojure.zip :as zip]
            [gowherene.reader.regexes
             :refer [re-postal-code
                     re-label
                     re-spaces]]))

(defn- node-contains-postal-code?
  [loc]
  (let [node (zip/node loc)]
    (and (string? node) (re-find re-postal-code node))))

(defn find-postal-codes
  [page-zipper]
  (->> page-zipper
       (iterate zip/next)
       (take-while (complement zip/end?))
       (filter node-contains-postal-code?)
       (map (fn [loc] {:loc loc :type :postal-code :value (re-find re-postal-code (zip/node loc))}))))

(defn- node-contains-label?
  [loc]
  (let [node (zip/node loc)]
    (and (string? node) (re-find re-label node))))

(defn- remove-label
  [string]
  (-> string
      (string/replace re-label "")
      (string/replace re-spaces " ")
      string/trim))

(defn find-labelled
  [page-zipper]
  (->> page-zipper
       (iterate zip/next)
       (take-while (complement zip/end?))
       (filter node-contains-label?)
       (map (fn [loc] {:loc loc :type :labelled :value (remove-label (zip/node loc))}))))

(defn geocodables
  [page-zipper]
  (concat
   (find-postal-codes page-zipper)
   (find-labelled page-zipper)))
