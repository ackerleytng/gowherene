(ns playout.reader.core
  (:require [hickory.core :refer [as-hickory parse]]
            [hickory.zip :refer [hickory-zip]]
            [clj-http.client :as client]
            [clojure.zip :as zip]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [hickory.select :as s]
            [environ.core :refer [env]]
            [medley.core :refer [take-upto]]
            [slingshot.slingshot :refer [try+ throw+]]))

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
  #"[\u00a0\s]+")

(defn url->hickory
  [url]
  (-> (client/get url)
      :body
      parse
      as-hickory))

(defn get-all-tags
  "Given a hickory, get all the tags in this hickory"
  ([hickory] (get-all-tags (hickory-zip hickory) #{}))
  ([loc tags]
   (cond
     (zip/end? loc) tags
     :else (let [tag (:tag (zip/node loc))]
             (recur (zip/next loc) (conj tags tag))))))

(defn remove-tags
  "Given a hickory, return a hickory without the tags in to-remove"
  [to-remove hickory]
  ((fn [loc]
     (if (zip/end? loc)
       ;; zip/root returns just the node, not a full zipper
       (zip/root loc)
       (recur (zip/next (if (some #(= (:tag (zip/node loc)) %)
                                  to-remove)
                          (zip/remove loc)
                          loc)))))
   (hickory-zip hickory)))

(def address-cap
  "We use (s/has-child (s/has-child (s/find-in-text re-address)))
     to match the `Address: ` label followed by the actual address.
     Since re-address could potentially match stray `address` words in text,
     we cap this search at address-cap.
  <blah>
    <blah0>
      <blah>Address: </blah>
    </blah0>
    <blah1 />
    <blah... We don't want too many of these here./>
    <blah... We don't want too many of these here./>
    <blah.address-cap+1 />
  </blah>"
  8)

(defn get-postal-code-locs
  "Given a hickory, find all the locs containing postal codes"
  [hickory]
  (->> hickory
       (s/select-locs (s/or
                       ;; Contains postal code
                       (s/find-in-text re-postal-code)
                       ;; Marked by the word address
                       (s/has-child (s/has-child (s/find-in-text re-address)))))
       ;; Filter out these
       (filter #(> address-cap ((comp count :content zip/node) %)))))

(def earlier-header-steps
  "We use this to cap the search upwards for a header.
     We don't want to find a stray address somewhere and then
     misidentify a random navbar header for this address"
  64)

(defn get-earlier-header
  "Given a loc, find the header just above or before this loc.
  Limit the search backwards to earlier-header-steps"
  [hloc-zip]
  (s/prev-pred hloc-zip
               (let [dist (atom earlier-header-steps)]
                 (fn [loc]
                   (if (zero? @dist) nil
                       (do
                         (swap! dist dec)
                         ((apply s/or (map s/tag [:h1 :h2 :h3 :h4])) loc)))))))

(defn get-content
  "Given a node, return all content in a string, until the first <br>
     or the end of this tree of tags
   The aux function returns a pair (string should-stop) where string
     is the data to be accumulated and should-stop stops execution if necessary"
  [node]
  (first ((fn aux [n]
            (cond (= (:tag n) :br) (list "" true)
                  (= (:type n) :element)
                  (let [useful (take-upto second (map aux (:content n)))]
                    (list (str/join (map first useful))
                          (some second useful)))
                  :else (list n false))) node)))

(defn loc->address
  [loc]
  (-> loc
      zip/node
      get-content
      (str/replace re-spaces " ")
      (str/replace-first re-address "")
      str/trim))

(def uninteresting-tags [:ins :script :noscript :img :iframe :head :link :footer :header])

(defn hickory->data
  "Takes a hickory and returns a data of all the places and addresses on the page"
  [hickory]
  (let [postal-code-locs (get-postal-code-locs
                          (remove-tags uninteresting-tags hickory))
        header-locs (filter identity (map get-earlier-header postal-code-locs))
        headers (->> header-locs
                     (map zip/node)
                     (map get-content)
                     (map #(str/replace % re-spaces " "))
                     (map str/trim))
        addresses (map loc->address postal-code-locs)]
    (map #(reduce (fn [r [k v]] (assoc r k v)) {} (partition 2 %))
         (partition 4 (interleave (repeat :place) headers
                                  (repeat :address) addresses)))))

(defn data-lookup
  [data place]
  (filter #(= place (:place %)) data))

(defn cleanup-addresses
  "Takes data and dedupes according to headers, picks the longer address"
  [data]
  (->> data
       (reduce (fn [accum {:keys [place address]}]
                 (assoc accum place (if-let [existing-address (get accum place)]
                                      (if (> (count existing-address) (count address))
                                        existing-address
                                        address)
                                      address)))
               {})
       (map (fn [[place address]]
              {:place place :address address}))))

(defn geocode-google
  ([address]
   ;; Default to 3 tries
   (geocode-google address 3))
  ([address tries]
   (let [response (try+ (-> (client/get
                             "https://maps.googleapis.com/maps/api/geocode/json"
                             {:query-params {:address address
                                             :key (env :google-api-token)}})
                            :body
                            json/read-json)
                        (catch [:status 400] _
                          (println (str "Issue geocoding |" address "|"))))
         status (:status response)]
     (cond
       (nil? status)
       response

       (= "OK" status)
       (get-in response [:results 0 :geometry :location])

       (and (= "OVER_QUERY_LIMIT" status) (> tries 0))
       (recur address (dec tries))

       (and (= "OVER_QUERY_LIMIT" status) (zero? tries))
       (println (str "Over query limit while geocoding |" address "|"))

       :else (println (str status " while geocoding |" address "|"))))))

(defn geocode-onemap
  [postal-code]
  (let [response (-> (client/get
                      "https://developers.onemap.sg/commonapi/search"
                      {:query-params {:searchVal postal-code
                                      :returnGeom "Y"
                                      :getAddrDetails "Y"}})
                     :body
                     json/read-json)
        result (get-in response [:results 0])]
    {:lat (Float/parseFloat (:LATITUDE result))
     :lng (Float/parseFloat (:LONGITUDE result))}))

(defn geocode
  [address]
  (if-let [postal-code (re-find re-postal-code address)]
    (geocode-onemap postal-code)
    (geocode-google address)))

(defn data-add-geocode
  "Takes a data and adds on geocoding"
  [data]
  (doall
   (pmap
    (fn [{:keys [place address] :as d}]
      (assoc d :latlng (geocode address)))
    data)))

(defn process
  [hickory]
  (let [result (->> hickory
                    hickory->data
                    cleanup-addresses
                    data-add-geocode)
        result-remove-nils (filter  #(:latlng %) result)]
    (clojure.pprint/pprint result)
    result-remove-nils))

(defn handle
  [url]
  (println "incoming" url) 
  (->> url
       url->hickory
       process))
