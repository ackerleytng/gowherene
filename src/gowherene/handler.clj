(ns gowherene.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults
                                              site-defaults
                                              api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :as resp]
            [gowherene.reader.core :as reader]))

(defn wrap-dir-index
  [handler]
  (fn [req]
    (handler (update req :uri
                     #(if (= "/" %) "/index.html" %)))))

(defroutes site-routes
  (route/resources "/"))

(defroutes api-routes
  (GET "/parse" [url]
       (resp/response (reader/handle url)))
  (route/not-found "Not Found"))

(defroutes app
  (wrap-dir-index (wrap-defaults site-routes site-defaults))
  (wrap-json-response (wrap-defaults api-routes api-defaults)))
