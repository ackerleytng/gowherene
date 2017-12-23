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

(def re-postal-code #"\b(?:[0-7][0-9]|8[0-3])\d{4}\b")

(defn url->hickory
  [url]
  (-> (client/get url)
      :body
      parse
      as-hickory))

(defn- get-postal-code-locs
  "Find all the locs containing postal codes"
  [hloc-zip]
  (s/select-locs (s/or
                  ;; Marked by the word address
                  (s/has-child (s/has-child (s/find-in-text #"[Aa]ddress")))
                  ;; Contains postal code
                  (s/and (apply s/and (map (comp s/not s/tag)
                                           ;; and is not in these tags
                                           [:script :img :noscript]))
                         (s/find-in-text re-postal-code)))
                 hloc-zip))

(def earlier-header-steps 64)

(defn- get-earlier-header
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

(defn- get-content
  "Given a node, return all content in a string, until the first <br>
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

(defn- loc->address
  "Given a loc, return the address"
  [hloc-zip]
  (->> hloc-zip
       zip/node
       :content
       (filter string?)
       (str/join "")))

(defn hickory->data
  "Takes a hickory and returns a data of all the places and addresses on the page"
  [hickory]
  (let [postal-code-locs (get-postal-code-locs hickory)
        header-locs (filter identity (map get-earlier-header postal-code-locs))
        headers (->> header-locs
                     (map zip/node)
                     (map get-content)
                     (map #(str/replace % #"[\u00a0\s]+" " "))
                     (map str/trim))
        addresses (->> postal-code-locs
                       (map loc->address)
                       (map #(str/replace % #"[\u00a0\s]+" " "))
                       (map str/trim))]
    (map #(reduce (fn [r [k v]] (assoc r k v)) {} (partition 2 %))
         (partition 4 (interleave (repeat :place) headers
                                  (repeat :address) addresses)))))

(defn geocode-google
  ([address]
   ;; Default to 3 tries
   (geocode address 3))
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
    (clojure.pprint/pprint result)
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
