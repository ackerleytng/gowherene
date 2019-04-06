(ns gowherene.reader.client
  (:require [clj-http.client :as client]
            [clj-http-ssrf.core :as ssrf]
            [clj-http-ssrf.reserved :as reserved]))

(defn- safe-scheme?
  [scheme]
  (#{:http :https} scheme))

(defn- safe-host?
  [ip-address]
  (not (some #(inet.data.ip/network-contains? % ip-address)
             (reserved/reserved-ip-ranges))))

(defn- safe-port?
  [port]
  (#{80 443} port))

(def ^:private safe-middleware
  (conj client/default-middleware
        (ssrf/wrap-predicates
         :scheme-pred safe-scheme?
         :host-pred   safe-host?
         :port-pred   safe-port?)))

(defn retrieve [url]
  "Accesses the url and returns the contents of the page at the url,
   or nil on error."
  (client/with-middleware safe-middleware
    (client/get
     url
     {;; Handle HTTP exceptions as data instead (in status)
      :throw-exceptions    false
      ;; get will return nil if the host is unknown (cannot be resolved)
      :ignore-unknown-host true
      ;; Allow a maximum of 5 redirects, and don't throw an exception after
      ;;   Not sure how to test being redirected too much
      :max-redirects 5 :redirect-strategy :graceful})))

(defn maybe-prefix-url
  [url]
  ;; As long as a protocol is specified we let clj-http-ssrf handle it
  (if (re-find #"^[a-z]+://" url)
    url
    ;; Default to https
    (str "https://" url)))
