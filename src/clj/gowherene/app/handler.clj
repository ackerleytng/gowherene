(ns gowherene.app.handler
  (:require [clojure.string :as string]
            [gowherene.reader.core :as reader]
            [gowherene.reader.client :as client]
            [clojure.data.json :as json]
            [ring.util.response :refer [response bad-request content-type]]))

(defn validate [url]
  (if (nil? url)
    {:error "Null url!" :data nil}
    (let [trimmed (string/trim url)]
      (if (= "" trimmed)
        {:error "Empty url!" :data nil}
        {:error nil :data trimmed}))))

(defn normalize
  [{:keys [data error] :as input}]
  (if error input
      (update input :data client/maybe-prefix-url)))

(defn retrieve
  [{:keys [data error] :as input}]
  (if error input
      (let [{:keys [status body] :as everything} (client/retrieve data)]
        (cond
          (nil? everything)
          {:error (str "Couldn't connect to " data) :data nil}

          (= status 200)
          {:error nil :data body}

          :else
          {:error (str "Couldn't retrieve url! (" status ")") :data nil}))))

(defn process
  [{:keys [data error] :as input}]
  (if error input
      (try
        {:error nil :data (reader/process data)}
        (catch Exception e
          {:error (str "Error while reading requested page: (" (.getMessage e) ")")
           :data nil}))))

(defn cleanup
  [{:keys [data error] :as input}]
  (cond
    error input
    (zero? (count data)) {:error "Couldn't find any addresses! :(" :data nil}
    :else input))

(defn parse-url [query-string]
  (let [parts (string/split query-string #"=")]
    (when (= (count parts) 2)
      (second parts))))

(defn handle
  [request]
  (let [url (-> request :params :url)]
    (-> url
        validate
        normalize
        retrieve
        process
        cleanup
        response)))
