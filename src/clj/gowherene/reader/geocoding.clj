(ns gowherene.reader.geocoding
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-http.client :as client]
            [environ.core :refer [env]]
            [clojure.data.json :as json]))

(defn- read-token
  [secrets-file env-key]
  (or
   ;; Look for the secrets file first (deployment)
   (and (.exists (io/file secrets-file)) (str/trim (slurp secrets-file)))
   ;; Otherwise use environment variable / .lein-env
   (env env-key)))

(defonce onemap-api-token
  (when-let [token (read-token "/run/secrets/onemap-api-token" :onemap-api-token)]
    (println "Loaded OneMap API token")
    token))

(defn- raw-geocode-onemap
  [address]
  (let [opts (cond-> {:throw-exceptions false
                      :query-params {:searchVal address
                                     :returnGeom "Y"
                                     :getAddrDetails "Y"}}
               onemap-api-token (assoc :headers {"Authorization" onemap-api-token}))
        {:keys [status body]} (client/get
                               "https://www.onemap.gov.sg/api/common/elastic/search"
                               opts)]
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
  (geocode-onemap search-key))

(defn add-latlng
  [{:keys [type value location] :as input}]
  (if-let [search-key
           (case type
             :postal-code value
             :labelled (render-location location)
             nil)]
    (assoc input :latlng (geocode search-key))
    input))
