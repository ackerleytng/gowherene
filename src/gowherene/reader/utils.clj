(ns gowherene.reader.utils
  (:require [clojure.string :as str]
            [hickory.zip :refer [hickory-zip]]
            [clojure.zip :as zip]
            [gowherene.reader.regexes :refer [re-spaces]]))

(defn word-count
  [string]
  (let [trimmed (str/replace string re-spaces "")]
    (if (= "" trimmed) 0
        (inc (count (re-seq re-spaces string))))))

(defn subtree
  "Returns the subtree rooted at loc as a new zipper"
  [loc]
  (hickory-zip (zip/node loc)))

(defn prune-before
  "Removes all left siblings before loc and returns the same loc in the pruned tree"
  ([loc] (prune-before loc 1))
  ([loc levels]
   (let [parent (zip/up loc)]
     (if (nil? parent)
       ;; At the top already, remove the changed tag on the subtree and return
       (->> (hickory-zip (zip/root loc))
            (iterate zip/down)
            (take levels)
            last)
       (recur ;; rebuild parent without the lefts of loc
        (zip/edit
         parent
         #(zip/make-node loc % (concat [(zip/node loc)] (zip/rights loc))))
        (inc levels))))))

(defn prune-after
  "Removes all right siblings before loc and returns the same loc in the pruned tree"
  ([loc] (prune-after loc 1))
  ([loc levels]
   (let [parent (zip/up loc)]
     (if (nil? parent)
       ;; At the top already, remove the changed tag on the subtree and return
       (->> (hickory-zip (zip/root loc))
            (iterate (comp zip/rightmost zip/down))
            (take levels)
            last)
       (recur ;; rebuild parent without the rights of loc
        (zip/edit
         parent
         #(zip/make-node loc % (concat (zip/lefts loc) [(zip/node loc)])))
        (inc levels))))))

(defn content
  [loc]
  (let [c (->> loc
               (iterate zip/next)
               (take-while (complement zip/end?))
               (map zip/node)
               (filter string?)
               (str/join " "))]
    (str/replace c re-spaces " ")))
