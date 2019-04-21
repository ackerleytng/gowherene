(ns gowherene.reader.label
  (:require [clojure.string :as str]
            [clojure.zip :as zip]
            [hickory.select :as hselect]
            [gowherene.reader.utils :refer [content subtree]]))

;; TODO: room for refactorization here, but implementation cannot combine all predicates into one
;;   because that would check every node for all predicates instead of
;;   checking all nodes for one predicate first, then the next predicate, and so on

(defn earlier-header
  "Given a loc, find the header just above or before this loc."
  [loc]
  (hselect/prev-pred
   loc
   (apply hselect/or (map hselect/tag [:h1 :h2 :h3 :h4]))))

(defn earlier-x-large
  "Given a loc, find the loc before this loc that has a style containing `font-size: x-large`"
  [loc]
  (hselect/prev-pred
   loc
   (hselect/attr :style #(re-find #"font-size\s*:\s*x-large" %))))

(defn earlier-strong
  "Given a loc, find the loc before this loc that has is marked <strong>"
  [loc]
  (hselect/prev-pred
   loc
   (hselect/tag :strong)))

(defn label
  [loc]
  (when-let [found ((some-fn
                     earlier-header
                     earlier-x-large
                     earlier-strong)
                    loc)]
    (str/trim (content (subtree found)))))

(defn add-label
  [{:keys [loc] :as input}]
  (assoc input :label (label loc)))
