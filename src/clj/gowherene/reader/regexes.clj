(ns gowherene.reader.regexes)

(def re-postal-code
  "Regex that matches Singapore postal codes.
     According to URA, the largest postal code prefix in Singapore is 83
     (74 is not a valid prefix, but it is included in this regex)"
  #"\b(?:[0-7][0-9]|8[0-3])\d{4}\b")

(def re-label-s
  "(?:[Ww]here|[Aa]ddress)\\s*:")

(def re-label
  "Regex to match labels in nodes"
  (re-pattern re-label-s))

(def re-spaces
  "Regex to be used to replace all &nbsp;s as well as spaces"
  #"[\u00a0 ]+")

(def re-unit-number-s
  (let [number "[a-z]?\\d{1,4}[a-z]?"
        separator " ?[-/] ?"
        unit (str "#?b?" number "(?:" separator number ")+")]
    (str "(?i)" unit "(?:\\s+(?:and|&)\\s+" unit ")*")))

(def re-unit-number
  "Regex that matches unit numbers
     May include & or and to join unit numbers and slashes to indicate more units"
  (re-pattern re-unit-number-s))
