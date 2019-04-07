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

(defn content
  [loc]
  (let [c (->> loc
               (iterate zip/next)
               (take-while (complement zip/end?))
               (map zip/node)
               (filter string?)
               (str/join " "))]
    (str/replace c re-spaces " ")))
