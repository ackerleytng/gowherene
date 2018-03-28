(ns gowherene.reader.client
  (:require [clj-http.client :as client]
            [clj-http-ssrf.core :as ssrf]
            [clj-http-ssrf.reserved :as reserved]))

(defn safe-scheme?
  [scheme]
  (#{:http :https} scheme))

(defn safe-host?
  [ip-address]
  (not (some #(inet.data.ip/network-contains? % ip-address)
             (reserved/reserved-ip-ranges))))

(defn safe-port?
  [port]
  (#{80 443} port))

(defn safe-get
  [url]
  (if url
    (client/with-middleware
      (conj client/default-middleware
            (ssrf/wrap-predicates
             :scheme-pred safe-scheme?
             :host-pred   safe-host?
             :port-pred   safe-port?))
      (client/get url {:throw-exceptions    false
                       :ignore-unknown-host true}))))

(defn maybe-prefix-url
  [url]
  ;; As long as a protocol is specified we let clj-http-ssrf handle it
  (if (re-find #"^[a-z]+://" url)
    url
    ;; Default to http
    (str "http://" url)))

(defn retrieve
  "Accesses the url and returns the contents of the page at the url,
   or nil on error."
  [url]
  (-> url
      maybe-prefix-url
      safe-get))
