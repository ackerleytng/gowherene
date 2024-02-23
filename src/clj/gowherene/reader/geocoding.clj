(ns gowherene.reader.geocoding
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-http.client :as client]
            [environ.core :refer [env]]
            [clojure.data.json :as json]
            [gowherene.reader.regexes :refer [re-postal-code]]))

(defonce google-api-token
  (if-let [token
           (or
            ;; Look for the secrets file first (deployment)
            (let [secrets-file "/run/secrets/google-api-token"]
              (and (.exists (io/file secrets-file)) (str/trim (slurp secrets-file))))
            ;; Otherwise use environment variable
            (env :google-api-token))]
    (do (println (str "Using token |" token "|")) token)
    (throw (RuntimeException. "Google api token missing"))))

(defn- raw-geocode-google
  [address]
  (let [{:keys [status body]} (client/get
                               "https://maps.googleapis.com/maps/api/geocode/json"
                               {:throw-exceptions false
                                :query-params {:address address
                                               :key google-api-token}})]
    (if (= 400 status)
      {:error (str "Google geocoding returned 400 for '" address "'") :data nil}
      {:error nil :data (json/read-json body)})))

(defn- maybe-append-singapore
  [address]
  (if (not (re-find #"(?i)singapore" address))
    (str address " Singapore")
    address))

(defn geocode-google
  ([address]
   ;; Default to 3 tries
   (geocode-google address 3))
  ([address tries]
   (let [{error :error
          {:keys [status results]} :data
          :as output} (raw-geocode-google (maybe-append-singapore address))]
     (cond
       error output

       (= "OK" status)
       (get-in results [0 :geometry :location])

       (and (= "OVER_QUERY_LIMIT" status) (> tries 0))
       (recur address (dec tries))

       (and (= "OVER_QUERY_LIMIT" status) (zero? tries))
       (println (str "Over query limit while geocoding '" address "'"))))))

(defn- raw-geocode-onemap
  [address]
  (let [{:keys [status body]} (client/get
                               "https://www.onemap.gov.sg/api/common/elastic/search"
                               {:throw-exceptions false
                                :query-params {:searchVal address
                                               :returnGeom "Y"
                                               :getAddrDetails "Y"}})]
    (if (not (= 200 status))
      {:error (str "Onemap geocoding returned " status " for '" address "'") :data nil}
      {:error nil :data (json/read-json body)})))

(defn geocode-onemap
  [address]
  (let [{error :error
         {:keys [found results]} :data
         :as output} (raw-geocode-onemap address)]
    (when (and found (pos? found))
      (let [result (get results 0)]
        {:lat (Float/parseFloat (:LATITUDE result))
         :lng (Float/parseFloat (:LONGITUDE result))}))))

(defn render-location
  [{:keys [postal-code road-name building-number]}]
  (str/join
   " "
   [(or building-number "")
    (or road-name "")
    (or postal-code "")]))

(defn geocode
  [search-key]
  (or (geocode-onemap search-key)
      (geocode-google search-key)))

(defn add-latlng
  [{:keys [type value location] :as input}]
  (if-let [search-key
           (case type
             :postal-code value
             :labelled (render-location location)
             nil)]
    (assoc input :latlng (geocode search-key))
    input))
