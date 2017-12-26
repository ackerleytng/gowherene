(ns playout.reader.tagger
  (:require [hickory.core :refer [parse parse-fragment as-hickory]]
            [hickory.zip :refer [hickory-zip]]
            [clojure.zip :as zip]
            [clojure.set :as set]
            [clojure.pprint :refer [pprint]]))

#_("
   1. Get content of tags
   2. For each content, get the path 5 steps up
   3. Bucket the content according to path 5 steps up
   4. Add to bucket only if it increases the address value

   5. Address value is determined by existence of keywords in the string
   5a. Postal code.
   5b. Keywords like \"Road\" \"Jalan\"
   5c. Numbers with # in it
   5d. Numbers prior to road

   6. An address has \"slots\" , such as
   6a. Postal code
   6b. Unit Number
   If the slot has been 'taken', the address value does not increase anymore.
   (Hence it is not added to the bucket)
")

(defn loc->path
  [loc]
  (let [path-length 5]
    (->> loc
         zip/path
         reverse
         (take path-length)
         reverse
         ;; Keep only the attrs and tag information
         (map (fn [{:keys [tag attrs]}] {:tag tag :attrs attrs})))))

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
  #"(?i)(?:\bsingapore\s|\bs\s|\bs)?\(?\b(?:[0-7][0-9]|8[0-3])\d{4}\b\)?")

(defn find-postal-code
  [s]
  (re-find re-postal-code s))

(def re-unit-number
  "Regex that matches unit numbers
     May include & or and to join unit numbers and slashes to indicate more units"
  #"(?i)#\d{1,4}\s*-\s*[\d/]{1,6}(?:\s+(?:and|&)\s+#\d{1,4}\s*-\s*[\d/]{1,6})*")

(defn find-unit-number
  [s]
  (re-find re-unit-number s))

(defn find-road-name
  "Finds road name patterns with hints from https://www.wikiwand.com/en/Road_names_in_Singapore"
  [s]
  (let [malay-prefix-patterns (map #(re-pattern
                                     ;; maximum of 3 words after the prefix
                                     ;; except for the word the
                                     (str "(?i)\\b" %
                                          "\\b(?: \\b(?!the\\b)(?:[a-z']+\\b|\\d+)\\b){1,3}"))
                                   ["bukit" "bt" "jalan" "jln"
                                    "kampong" "kg" "lengkok"
                                    "lorong" "lor"
                                    "padang" "taman"
                                    "tanjong" "tg"])
        english-suffix-patterns (map #(re-pattern
                                       ;; maximum of 3 words before the suffix
                                       ;; except for the word the
                                       (str "(?i)(?:\\b(?!the\\b)[a-z']+\\b ){1,3}\\b" %
                                            "\\b(?: \\b\\d{1,3}\\b)?"))
                                     ["alley" "avenue" "ave" "bank" "boulevard" "blvd"
                                      "bow" "central" "circle" "circuit" "circus" "close" "cl"
                                      "concourse" "court" "crescent" "cres" "cross" "crossing"
                                      "drive" "dr" "east" "estate" "expressway" "e'way"
                                      "farmway" "field" "garden" "gardens" "gate" "gateway"
                                      "grande" "green" "grove" "height" "heights"
                                      "highway" "hway" "hill" "island" "junction" "lane" "link"
                                      "loop" "mall" "mount" "mt" "north" "park" "parkway" "path"
                                      "place" "pl" "plain" "plains" "plaza" "promenade" "quay"
                                      "ridge" "ring" "rise" "road" "rd" "sector" "south"
                                      "square" "sq" "street" "st" "terrace" "track" "turn"
                                      "vale" "valley" "view" "vista" "walk" "way" "west" "wood"])
        names-with-the ["the inglewood" "the knolls" "the oval"]
        single-word-names ["bishopsgate" "bishopswalk" "causeway" "piccadilly" "queensway"]
        without-generic-element ["geylang bahru" "geylang serai"
                                 "kallang bahru" "kallang tengah"
                                 "lengkong dua" "lengkong empat"
                                 "lengkong enam" "lengkong lima"
                                 "lengkong satu" "lengkong tiga"
                                 "lengkong tujoh" "wholesale centre"]
        exact-matches (map #(re-pattern (str "(?i)\\b" % "\\b"))
                           (set/union (set names-with-the)
                                      (set single-word-names)
                                      (set without-generic-element)))
        patterns (set/union (set malay-prefix-patterns)
                            (set english-suffix-patterns)
                            (set exact-matches))]
    (some identity (map #(re-find % s) patterns))))

(defn get-all-locs
  [zipper]
  (take-while (complement zip/end?) (iterate zip/next zipper)))

(defn find-nil-tags
  [zipper]
  (reduce (fn [nil-content-tags loc]
            (if-let [content (:content (zip/node loc))]
              nil-content-tags
              (conj nil-content-tags (:tag (zip/node loc)))))
          #{}
          (get-all-locs zipper)))

(defn find-tags
  [f zipper]
  (filter f (get-all-locs zipper)))

(defn tag
  [tags loc]
  (if (not (= :element (:type (zip/node loc))))
    (pprint (zip/node loc))))

(def tmp-zipper
  (->> (slurp "test/playout/reader/fixtures/cheap-food-orchard")
       parse
       as-hickory
       hickory-zip))

(def tmp-plain
  (->> "testing"
       parse-fragment
       first
       as-hickory
       hickory-zip))

(defn tmp []
  (->> tmp-zipper
       (find-tags (fn [loc]
                    (let [n (zip/node loc)]
                      (and (= :i (:tag n))
                           (nil? (:content n))))))
       (map zip/node)
       clojure.pprint/pprint)
  )

(defn tmp2 []
  (find-nil-tags tmp-zipper))

(defn tmp3 []
  (clojure.pprint/pprint (zip/up tmp-plain)))
