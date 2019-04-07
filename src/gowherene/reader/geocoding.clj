(ns gowherene.reader.geocoding
  (:require [clj-http.client :as client]
            [environ.core :refer [env]]
            [clojure.data.json :as json]
            [gowherene.reader.regexes :refer [re-postal-code]]))

(defn- raw-geocode-google
  [address]
  (let [{:keys [status body]} (client/get
                               "https://maps.googleapis.com/maps/api/geocode/json"
                               {:throw-exceptions false
                                :query-params {:address address
                                               :key (env :google-api-token)}})]
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
  [postal-code]
  (let [{:keys [status body]} (client/get
                               "https://developers.onemap.sg/commonapi/search"
                               {:throw-exceptions false
                                :query-params {:searchVal postal-code
                                               :returnGeom "Y"
                                               :getAddrDetails "Y"}})]
    (if (not (= 200 status))
      {:error (str "Onemap geocoding returned " status " for '" postal-code "'") :data nil}
      {:error nil :data (json/read-json body)})))

(defn geocode-onemap
  [postal-code]
  (let [{error :error
         {:keys [found results]} :data
         :as output} (raw-geocode-onemap postal-code)]
    (when (pos? found)
      (let [result (get results 0)]
        {:lat (Float/parseFloat (:LATITUDE result))
         :lng (Float/parseFloat (:LONGITUDE result))}))))

(defn geocode
  [address]
  (when address
    (if-let [postal-code (re-find re-postal-code address)]
      (or (geocode-onemap postal-code)
          ;; Fallback to google
          (geocode-google address))
      (geocode-google address))))
