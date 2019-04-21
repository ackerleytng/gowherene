(ns gowherene.app.api
  (:require [clojure.core.async :refer [go]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.middleware.defaults
             :refer [wrap-defaults
                     site-defaults
                     api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :as resp]
            [gowherene.logging.requests :refer [log-request]]
            [gowherene.app.handler :refer [handle]]))

(defn- do-with-timing
  "Takes a thunk, executes it under timing and returns
   [timing-in-nanoseconds, result]"
  [f]
  (let [start (System/nanoTime)
        out (f)]
    [(- (System/nanoTime) start) out]))

(defn- wrap-dir-index
  [handler]
  (fn [req]
    (handler (update req :uri
                     #(if (= "/" %) "/index.html" %)))))

(defroutes static-routes
  (route/resources "/"))

(defroutes api-routes
  (GET "/ping" []
       (resp/response "OK"))
  (GET "/parse" [url]
       (let [[time results] (do-with-timing #(handle url))]
         (go (log-request url results time))
         (resp/response results)))
  (route/not-found "Not Found"))

(defroutes app-routes
  (wrap-dir-index (wrap-defaults static-routes site-defaults))
  (wrap-json-response (wrap-defaults api-routes api-defaults)))
