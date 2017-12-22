(ns playout.reader.core
  (:require [hickory.core :refer [as-hickory parse]]
            [hickory.zip :refer [hickory-zip]]
            [clj-http.client :as client]
            [clojure.zip :as zip]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [hickory.select :as s]
            [environ.core :refer [env]]))

(def re-postal-code #"[sS]\(?\d{6}\)?")

(defn url->hickory
  [url]
  (-> (client/get url)
      :body
      parse
      as-hickory))

(defn- get-postal-code-locs
  "Find all the locs containing postal codes"
  [hloc-zip]
  (s/select-locs (s/find-in-text re-postal-code) hloc-zip))

(defn- get-earlier-header
  "Given a loc, find the header just above or before this loc"
  [hloc-zip]
  (s/left-pred hloc-zip (apply s/or (map s/tag [:h1 :h2 :h3 :h4]))))

(defn- get-content
  "Given a node, return all content in a string"
  [node]
  (if-let [content (:content node)]
    (str/join (map get-content content))
    node))

(defn hickory->data
  "Takes a hickory and returns a data of all the places and addresses on the page"
  [hickory]
  (let [postal-code-locs (get-postal-code-locs hickory)
        header-locs (map get-earlier-header postal-code-locs)
        headers (->> header-locs
                     (map zip/node)
                     (map get-content)
                     (map #(str/replace % #"[\u00a0\s]+" " "))
                     (map str/trim))
        addresses (->> postal-code-locs
                       (map zip/node)
                       (map get-content)
                       (map #(str/replace % #"[\u00a0\s]+" " "))
                       (map str/trim))]
    (map #(reduce (fn [r [k v]] (assoc r k v)) {} (partition 2 %)) 
         (partition 4 (interleave (repeat :place) headers
                                  (repeat :address) addresses)))))

(defn data-add-geocode
  "Takes a data and adds on geocoding"
  [data]
  (pmap (fn [{:keys [place address] :as d}] 
          (assoc d :latlng (geocode address)))
        data))

(defn geocode
  [address]
  (-> (client/get "https://maps.googleapis.com/maps/api/geocode/json" 
                  {:query-params {:address address 
                                  :key (env :google-api-token)}})
      :body
      json/read-json
      (get-in [:results 0 :geometry :location])))
