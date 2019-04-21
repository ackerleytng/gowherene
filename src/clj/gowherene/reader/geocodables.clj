(ns gowherene.reader.geocodables
  (:require [clojure.string :as string]
            [clojure.zip :as zip]
            [gowherene.reader.regexes
             :refer [re-postal-code
                     re-label
                     re-label-s
                     re-spaces]]
            [gowherene.reader.utils :refer
             [subtree content prune-before prune-after]]))

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
    (and (string? node) (re-find re-label node) (not (re-find #"(?i)email\s+address" node)))))

(defn- remove-label
  [string]
  (-> string
      (string/replace (re-pattern (str ".*" re-label-s)) "")
      (string/replace re-spaces " ")
      string/trim))

(defn nearest-boundary-sibling
  "Finds the nearest sibling to loc that is a boundary tag. Returns nil if this loc has no siblings that have boundary tags"
  [loc]
  (let [boundary-tags #{:br :hr}]
    (->> (zip/right loc)
         (iterate zip/right)
         (take-while (complement nil?))
         (filter (fn [l]
                   (let [node (zip/node l)]
                     (and (map? node) (boundary-tags (:tag node))))))
         first)))

(defn prune-out-loc-to-boundary
  "Prunes out (keep) only locs between loc and a sibling boundary tag (:br, :hr), returns loc in the pruned tree"
  [loc]
  (let [lefts-pruned (prune-before loc)]
    (if-let [boundary (nearest-boundary-sibling lefts-pruned)]
      (zip/leftmost (prune-after boundary))
      lefts-pruned)))

(defn labelled-info
  "Builds info of type labelled. If given one loc, it will set value based on loc. If given loc and trimmed-loc, it will set loc to loc (retaining record of location in original tree, but set value using the trimmed-loc"
  ([loc]
   {:loc loc :type :labelled :value (remove-label (content (subtree loc)))})
  ([loc trimmed-loc]
   {:loc loc :type :labelled :value (remove-label (content (subtree trimmed-loc)))
    :trimmed-loc (prune-out-loc-to-boundary trimmed-loc)}))

(defn find-labelled
  [page-zipper]
  (->> page-zipper
       (iterate zip/next)
       (take-while (complement zip/end?))
       (filter node-contains-label?)
       (map labelled-info)))

(defn geocodables
  [page-zipper]
  (concat
   (find-postal-codes page-zipper)
   (find-labelled page-zipper)))
