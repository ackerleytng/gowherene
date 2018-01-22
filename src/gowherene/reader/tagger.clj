(ns gowherene.reader.tagger
  (:require [hickory.core :refer [parse parse-fragment as-hickory]]
            [hickory.zip :refer [hickory-zip]]
            [medley.core :refer [map-vals]]
            [clojure.zip :as zip]
            [clojure.string :as str]))

;;;; General Algorithm

;;; We make the assumption here that people will use the same
;;;   html tags, classes and attributes for a single address.
;;;
;;; We also make the assumption that we have already "zoomed in" to tags surrounding an address.
;;; The loc provided to loc->buckets should already be quite close to
;;;   where the address is suspected to be.
;;;
;;; 1. Get content of tags
;;; 2. For each content, get the path (see loc->path)
;;; 3. Bucket the content according to path
;;; 4. Add content to bucket only if it increases the address value
;;; 5. Address value is determined by existence of keywords in the string
;;;    + Postal code.
;;;    + Keywords like "Road" "Jalan"
;;;    + Numbers with # in it
;;;    + Numbers prior to road
;;; 6. Our address has 4 "slots"
;;;    + Unit Number
;;;    + House Number
;;;    + Road Name
;;;    + Postal Code
;;; If the slot has been 'taken', the address value does not increase anymore.
;;; (Hence it is not added to the bucket)


;; Functions to find parts of an address

(def re-house-number
  "Regex to get house or block numbers"
  #"(?i)(?:block |blk )?\b\d{1,4}(?:\s?[a-z])?\b")

(defn find-house-number
  [s]
  (re-find re-house-number s))

(def re-postal-code
  "Regex that matches Singapore postal codes.
     According to URA, the largest postal code prefix in Singapore is 83
     (74 is not a valid prefix, but it is included in this regex)"
  #"(?i)(?:\bsingapore\s|\bs\s|\bs|\b)\(?(?:[0-7][0-9]|8[0-3])\d{4}\b\)?")

(defn find-postal-code
  [s]
  (re-find re-postal-code s))

(def re-unit-number
  "Regex that matches unit numbers
     May include & or and to join unit numbers and slashes to indicate more units"
  (let [unit "#?b?\\d{1,4}\\s*-\\s*[a-z]?[\\d/]{1,6}[a-z]?"]
    (re-pattern (str "(?i)" unit "(?:\\s+(?:and|&)\\s+" unit ")*"))))

(defn find-unit-number
  [s]
  (re-find re-unit-number s))

(def malay-prefix-patterns
  (map #(re-pattern
         ;; Maximum of 3 words after the prefix
         ;; except for the word the
         (str "(?i)\\b" %
              "\\b(?: \\b(?!the\\b)(?:[a-z']+\\b|\\d+)\\b){1,3}"))
       ["bukit" "bt" "jalan" "jln"
        "kampong" "kg" "lengkok"
        "lorong" "lor"
        "padang" "taman"
        "tanjong" "tg"]))

(def english-suffix-patterns
  (map #(re-pattern
         ;; maximum of 3 words before the suffix
         ;; except for the word the
         (str "(?i)(?:\\b(?!the\\b)[a-z']+\\b ){1,3}\\b" %
              "\\b(?: \\b\\d{1,3}\\b)?"))
       ;; Since words like Gardens could appear as area information in addresses
       ;;   next to road names, such as Some Road in Serangoon Gardens,
       ;;   we should place suffixes with higher probabilities
       ;;   of being real road names ahead.
       ;; This list is ordered by gut feel, in the absence of further data exploration
       ["road" "rd"
        "street" "st"
        "avenue" "ave"
        "way"
        "boulevard" "blvd"
        "close" "cl"
        "crescent" "cres"
        "drive" "dr"
        "place" "pl"
        "alley" "bank"
        "bow" "central" "circle" "circuit" "circus"
        "concourse" "court" "cross" "crossing"
        "east" "estate" "expressway" "e'way"
        "farmway" "field" "garden" "gardens" "gate" "gateway"
        "grande" "green" "grove" "height" "heights"
        "highway" "hway" "hill" "island" "junction" "lane" "link"
        "loop" "mall" "mount" "mt" "north" "park" "parkway" "path"
        "plain" "plains" "plaza" "promenade" "quay"
        "ridge" "ring" "rise" "sector" "south"
        "square" "sq" "terrace" "track" "turn"
        "vale" "valley" "view" "vista" "walk" "west" "wood"]))

(def exact-matches
  (let [names-with-the ["the inglewood" "the knolls" "the oval"]
        single-word-names ["bishopsgate" "bishopswalk" "causeway" "piccadilly" "queensway"]
        without-generic-element ["geylang bahru" "geylang serai"
                                 "kallang bahru" "kallang tengah"
                                 "lengkong dua" "lengkong empat"
                                 "lengkong enam" "lengkong lima"
                                 "lengkong satu" "lengkong tiga"
                                 "lengkong tujoh" "wholesale centre"]]
    (map #(re-pattern (str "(?i)\\b" % "\\b"))
         (concat names-with-the
                 single-word-names
                 without-generic-element))))

(def road-name-patterns
  ;; In decreasing order of commonness
  ;; If I see some english suffix, it is likely to be the road name
  ;;   if there are no english suffixes, then the presence of a malay prefix,
  ;;   and so on.
  ;; These help in cases like Some Road, Geylang Serai
  ;;   "Some Road" is the road name we're looking for,
  ;;   and the other terms are more likely to be information about the area
  (concat english-suffix-patterns
          malay-prefix-patterns
          exact-matches))

(defn find-road-name
  "Finds road name patterns with hints from https://www.wikiwand.com/en/Road_names_in_Singapore"
  [s]
  (some #(re-find % s) road-name-patterns))

;; Functions to manipulate locs

;; Thanks! jamesmacaulay/zelkova
(defn loc-after
  "Returns a new zipper location that skips the whole subtree at `loc`."
  [loc]
  (or (zip/right loc)
      (loop [p loc]
        (if (zip/up p)
          (or (zip/right (zip/up p))
              (recur (zip/up p)))
          [(zip/node p) :end]))))

(defn walk-locs
  "Walk locs in this zipper in a lazy way. Only walks this node and all sub nodes."
  [zipper]
  ;; Find loc to stop at
  (let [stop-here (loc-after zipper)]
    (take-while (fn [loc] (not
                           (or (zip/end? loc)
                               (= loc stop-here))))
                (iterate zip/next zipper))))

(defn count-locs-walked
  [loc]
  (count (walk-locs loc)))

(defn loc->path
  [loc]
  (let [path-length 5
        path-with-full-details (->> loc
                                    zip/path
                                    reverse
                                    (take path-length)
                                    reverse)]
    (map #(select-keys % [:tag :attrs]) path-with-full-details)))

(def tagging-functions
  [[:postal-code find-postal-code]
   [:unit-number find-unit-number]
   [:road-name find-road-name]
   [:house-number find-house-number]])

(defn tag-string
  "Tag a string with parts of an address.
     Returns [a vector of labelled pairs, the remaining parts of the string, trimmed]"
  [string]
  (reduce (fn [[v s] [k f]]
            (if-let [match (f s)]
              [;; Add match to vector
               (conj v k match)
               ;; Wipe out whatever was already used
               (str/trim (str/replace-first s match ""))]
              ;; If it couldn't be found, do nothing
              [v s]))
          [[] string]
          tagging-functions))

(def re-spaces
  "Regex to be used to replace all &nbsp;s as well as spaces"
  #"[\u00a0\s]+")

(defn count-words
  [str]
  (-> str
      (str/replace "," "")
      (str/replace re-spaces " ")
      str/trim
      (str/split #" ")
      count))

(def address-parts-scores
  {:postal-code 10
   :unit-number 8
   :road-name 5
   :house-number 2})

(defn assign-points
  "Given [address parts, remaining parts of the string], assign points to this tagging"
  [[address-parts remaining-string]]
  (let [raw-score (reduce (fn [sum [k _]] (+ sum (k address-parts-scores)))
                          0
                          (partition 2 address-parts))]
    (if (pos? raw-score)
      (+ raw-score
         ;; Bonus points based on remaining words
         (let [words-left (count-words remaining-string)]
           (- 4 words-left)))
      raw-score)))

(defn address-value
  "Compute the address value of string"
  [string]
  (-> string
      tag-string
      assign-points))

(defn loc->buckets
  "Bucket all the strings in a loc and below according to loc->path
     Returns a map of loc->path keys to vectors of strings"
  [loc]
  (->> (walk-locs loc)
       ;; Retain only string nodes
       (filter (comp string? zip/node))
       (group-by loc->path)
       (map-vals (partial map zip/node))))

(defn all-partitions
  "Given a vector, return all possible partitions
     For example [:a :b :c] becomes ((:a) (:b) (:c) (:a :b) (:b :c) (:a :b :c))"
  ([v] (all-partitions (inc (count v)) v))
  ([max-partition-size v]
   (mapcat #(partition % 1 v) (range 1 max-partition-size))))

(defn compute-address-values
  [string-vector]
  (let [;; I assume that they won't split addresses into more than max-address-partitions parts
        ;; Setting a cap prevents generating too many partitions for processing
        max-address-partitions 5]
    (->> string-vector
         (all-partitions max-address-partitions)
         (map #(str/join " " %))
         (map (fn [s] [s (address-value s)])))))

(defn clean-bucket
  [[string value]]
  [(str/trim (str/replace string re-spaces " "))
   value])

(defn buckets->addresses
  [buckets]
  (let [address-threshold 8]
    (->> (vals buckets)
         (mapcat compute-address-values)
         (filter #(> (second %) address-threshold))
         (map clean-bucket))))

;; Some functions for debugging

(defn find-nil-tags
  [zipper]
  (reduce (fn [nil-content-tags loc]
            (if-let [content (:content (zip/node loc))]
              nil-content-tags
              (conj nil-content-tags (:tag (zip/node loc)))))
          #{}
          (walk-locs zipper)))

(defn find-tags
  [f zipper]
  (filter f (walk-locs zipper)))
