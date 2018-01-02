(ns playout.reader.core
  (:require [hickory.core :refer [as-hickory parse]]
            [hickory.zip :refer [hickory-zip]]
            [clj-http.client :as client]
            [clojure.zip :as zip]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [hickory.select :as s]
            [environ.core :refer [env]]
            [medley.core :refer [take-upto distinct-by]]
            [slingshot.slingshot :refer [try+ throw+]]
            [playout.reader.tagger :as tagger]))

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

(defn cleanup-input
  [url]
  (cond
    (re-find #"^https?://" url) url
    :else (str "http://" url)))

(defn url->hickory
  [url]
  (-> url
      client/get
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
  20)

(defn address-label-like
  [hzip-loc]
  (some #(and
          ;; Matches re-address
          (re-find re-address %)
          ;; Does not have too many words (not likely to be labels)
          (< (tagger/count-words %) 4))
        (->> (zip/node hzip-loc)
             :content
             (filter string?))))

;; TODO rename this function
(defn get-postal-code-locs
  "Given a hickory, find all the locs containing postal codes"
  [hickory]
  (let [;; Contains postal code
        locs-postal-code (s/select-locs
                          (s/find-in-text re-postal-code) hickory)
        ;; Find regions of content near labels like "Address"
        locs-address (s/select-locs
                      (s/or (s/has-child (s/has-child address-label-like))
                            (s/has-child address-label-like)) hickory)
        locs-address-filtered (filter #(> address-cap ((comp count :content zip/node) %))
                                      locs-address)]
    (clojure.set/union (set locs-postal-code) (set locs-address-filtered))))

(defn get-earlier-header
  "Given a loc, find the header just above or before this loc.
  Limit the search backwards to earlier-header-steps"
  [hloc-zip]
  (s/prev-pred hloc-zip
               (apply s/or (map s/tag [:h1 :h2 :h3 :h4]))))

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

(defn loc->addresses
  [loc]
  (let [addresses (->> loc
                       tagger/loc->buckets
                       tagger/buckets->addresses)]
    (if (seq addresses)
      ;; If addresses is not empty
      (let [max-address-value (second (apply max-key second addresses))]
        (->> addresses
             (filter #(= max-address-value (second %)))
             (map first)))
      addresses)))

(def uninteresting-tags [:ins :script :noscript :img :iframe :head :link :footer :header])

(defn update-if-exists
  [map key f]
  (if (key map)
    (update map key f)
    map))

(defn simplify-datum
  "Use this to reduce the verbosity of datum (good for pprinting)"
  ([datum] (simplify-datum 0 datum))
  ([verbosity datum]
   (let [locs [:postal-code-loc :header-loc]]
     (cond
       (> verbosity 1) datum
       (> verbosity 0) (reduce #(update-if-exists %1 %2 zip/node) datum locs)
       :else (reduce #(update-if-exists %1 %2 tagger/count-locs-walked) datum locs)))))

(defn tag-with
  [tag info & datum]
  (assoc datum tag info))

(defn update-with-tag
  "Given an old tag in a map m, 
     get the value for the old tag in m, 
     apply f on it,
     associate the new value back into m with key new-tag."
  [new-tag old-tag f m]
  (let [old-info (old-tag m)
        new-info (f old-info)]
    (assoc m new-tag new-info)))

(defn update-with-tag-seq
  "Given an old tag in a map m, 
     get the value for the old tag in m, 
     apply f on it, (f returns a seq)
     clone m and
     associate the new value back into m's clones with key new-tag."
  [new-tag old-tag f m]
  (let [old-info (old-tag m)
        new-info (f old-info)]
    (map (partial assoc m new-tag) new-info)))

(defn loc->place
  [loc]
  (-> loc
      zip/node
      get-content
      (str/replace re-spaces " ")
      str/trim))

(defn hickory->data
  "Takes a hickory and returns a data of all the places and addresses on the page"
  [hickory]
  (->> hickory
       (remove-tags uninteresting-tags)
       get-postal-code-locs
       (map (partial tag-with :postal-code-loc))
       (map (partial update-with-tag :header-loc :postal-code-loc get-earlier-header))
       ;; If we can't find the header, don't display it
       ;; (filter :header-loc)
       (map (partial update-with-tag :place :header-loc loc->place))
       ;; Uncomment the following two for debugging
       ;; (map (partial update-with-tag :buckets :postal-code-loc tagger/loc->buckets))
       ;; (map (partial update-with-tag :addresses :buckets tagger/buckets->addresses))
       (mapcat (partial update-with-tag-seq :address :postal-code-loc loc->addresses))
       ;; Some postal-code-locs are misidentified, hence addresses cannot be found
       (filter :address)))

(defn data-lookup
  [data place]
  (filter #(= place (:place %)) data))

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
  (when address
    (if-let [postal-code (re-find re-postal-code address)]
      (geocode-onemap postal-code)
      (geocode-google address))))

(defn data-add-geocode
  "Takes a data and adds on geocoding"
  [data]
  (doall
   (pmap
    (fn [{:keys [place address] :as d}]
      (assoc d :latlng (geocode address)))
    data)))

(defn get-index [header]
  (and header
       (if-let [num (re-find #"(\d+)\." header)]
         (Integer/parseInt (get num 1))
         nil)))

(defn publish
  [data]
  (->> data
       (filter #(:latlng %))
       (map #(select-keys % [:place :address :latlng]))))

(defn process
  [hickory]
  (let [raw-result (->> hickory
                        hickory->data
                        (distinct-by (fn [d] [(:place d) (:address d)]))
                        (map (partial update-with-tag :latlng :address geocode)))
        result (publish raw-result)]
    (pprint (->> raw-result
                 (map (partial simplify-datum))
                 (sort-by (comp get-index :place))))
    result))

(defn handle
  [url]
  (println "incoming" url)
  (->> url
       cleanup-input
       url->hickory
       process))
