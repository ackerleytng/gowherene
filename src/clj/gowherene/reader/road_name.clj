(ns gowherene.reader.road-name
  (:require [clojure.string :as str]
            [gowherene.reader.regexes :refer [re-unit-number-s]]))

(def ^:private malay-generic-element
  ["bukit" "jalan" "kampong" "lengkok" "lengkong" "lorong" "padang" "taman" "tanjong"])

(def ^:private malay-abbr
  ["bt" "jln" "kg" "lor" "tg"])

(def ^:private english-prefixes
  ["sector" "mount"])

(def ^:private english-prefixes-abbr
  ["mt"])

(def ^:private english-suffixes
  ["alley" "avenue" "bank" "boulevard" "bow" "central" "circle" "circuit" "circus"
   "close" "concourse" "court" "crescent" "cross" "crossing" "drive" "east"
   "estate" "expressway" "farmway" "field" "garden" "gardens" "gate" "gateway"
   "grande" "green" "grove" "heights" "height" "highway" "hill" "island"
   "junction" "lane" "link" "loop" "mall" "north" "park" "parkway" "path" "place"
   "plain" "plains" "plaza" "promenade" "quay" "ridge" "ring" "rise" "road"
   "sector" "south" "square" "street" "terrace" "track" "turn" "vale" "valley"
   "view" "vista" "walk" "way" "west" "wood"])

(def ^:private english-suffixes-abbr
  ["ave" "blvd" "cl" "cres" "dr" "e'way" "hway" "pl" "rd" "sq" "st"])

(defn- sort-longest-first
  [v]
  (sort #(compare (count %2) (count %1)) v))

(def ^:private prefixes
  (->> (concat malay-generic-element malay-abbr english-prefixes english-prefixes-abbr)
       ;; sort to allow regex to get the longest possible match
       sort-longest-first))

(def ^:private suffixes
  (->> (concat english-suffixes english-suffixes-abbr)
       sort-longest-first))

(def ^:private special-cases
  (sort-longest-first
   [;; Road names without any generic element
    "geylang bahru"
    "geylang serai"
    "kallang bahru"
    "kallang tengah"
    "wholesale centre"
    ;; Road names with the definite article "the"
    "the inglewood"
    "the knolls"
    "the oval"
    ;; Road names that consist of a single word
    "bishopsgate"
    "causeway"
    "piccadilly"
    "queensway"]))

(def ^:private spaces-regex
  "[ \\t]")

(def ^:private number-suffix-regex
  "For cases like Ang Mo Kio Ave* 1*"
  (str "(?:" spaces-regex "+\\d+)"))

(def ^:private word-regex
  "For words that can appear in road names, like '*Laurel* *Wood* Avenue' or
  '*one-north* Gateway' or '*Saint* *Anne's* Wood'"
  (let [word-chars "[a-z0-9\\-'’\\.]"]
    (str "(?<!" word-chars ")" word-chars "+(?!" word-chars ")")))

(def ^:private suffix-regex
  (re-pattern
   (str "(?i)"  ; set case insensitive matching
        ;; word that does not contain numbers (to avoid getting the house number in the road name)
        ;;   like *12* Collyer Quay
        "(?:\\b[a-z-'’\\.]+" spaces-regex "+)"
        "(?:" word-regex spaces-regex "+){0,3}"  ; words that come before the suffix (up to 3)
        "(?:" (str/join "|" suffixes) ")"  ; the suffix itself
        "\\b" ; must be a word boundary
        number-suffix-regex "?"  ; any numerical suffix
        )))

(def ^:private prefix-regex
  (re-pattern
   (str "(?i)"  ; set case insensitive matching
        "\\b"  ; must be a word boundary
        "(?:" (str/join "|" prefixes) ")"  ; the prefix itself
        "(?:" spaces-regex "+" word-regex "){1,5}"  ; words that come after the suffix (up to 5)
        )))

(def ^:private special-cases-regex
  (re-pattern (str "(?i)" "(?:" (str/join "|" special-cases) ")")))

(defn has-building-number-before
  [road-name string]
  (let [regex (re-pattern (str "\\d+\\s+" road-name))]
    (and (re-find regex string)
         (not (re-find (re-pattern (str re-unit-number-s "\\s+" road-name)) string)))))

(defn choose-best
  [options choosers]
  ((apply some-fn (conj choosers first)) options))

(defn road-name
  [string]
  (let [prefix-match (re-seq prefix-regex string)
        suffix-match (re-seq suffix-regex string)
        special-case-match (re-seq special-cases-regex string)
        matches (concat prefix-match suffix-match special-case-match)
        n-matches (count matches)]
    (if (> n-matches 1)
      (choose-best
       matches
       [(fn [ms]
          (->> ms
               (filter #(has-building-number-before % string))
               (sort-by count)
               last))
        #(->> %
              (sort-by count)
              last)])
      (first matches))))
