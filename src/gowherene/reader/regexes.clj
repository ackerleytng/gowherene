(ns gowherene.reader.regexes)

(def re-postal-code
  "Regex that matches Singapore postal codes.
     According to URA, the largest postal code prefix in Singapore is 83
     (74 is not a valid prefix, but it is included in this regex)"
  #"\b(?:[0-7][0-9]|8[0-3])\d{4}\b")

(def re-address
  "Regex to match for address labels in text"
  #"[Aa]ddress:?")

(def re-spaces
  "Regex to be used to replace all &nbsp;s as well as spaces"
  #"[\u00a0 ]+")
