(ns frontend.events
  (:require
   [ajax.core :as ajax]
   [re-frame.core :as re-frame]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   [frontend.db :as db]))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced
  [_ _]
  db/default-db))

(re-frame/reg-event-db
 ::set-active-panel
 (fn-traced
  [db [_ active-panel]]
  (assoc db :active-panel active-panel)))

(re-frame/reg-event-db
 ::set-about-active
 (fn-traced
  [db [_ value]]
  (assoc db :about-active value)))

(re-frame/reg-event-db
 ::set-error-message
 (fn-traced
  [db [_ value]]
  (assoc db :error-message value)))

(re-frame/reg-event-db
 ::set-url-input
 (fn-traced
  [db [_ value]]
  (assoc db :url-input value)))

(re-frame/reg-event-fx
 ::parse-url
 (fn-traced
  [{:keys [db]} [_ action]]
  {:db         (assoc db :loading-action action)
   :http-xhrio {:method          :get
                :uri             "/parse"
                ;; TODO handle when :url-input is blank
                :params          {:url (:url-input db)}
                :timeout         15000
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      [::parse-success]
                :on-failure      [::parse-failure]}}))

(defn- handle-results
  [existing action url data]
  (.log js/console #js {:existing existing
                        :action action
                        :data data})
  (case action
    :append (assoc existing url data)
    :plot {url data}
    existing))

(re-frame/reg-event-db
 ::parse-success
 (fn [db [_ {:keys [error data]}]]
   (dissoc
    (if error
      (assoc db :error-message error)
      (update db :results handle-results (:loading-action db) (:url-input db) data))
    :loading-action :url-input)))

(re-frame/reg-event-db
 ::parse-failure
 (fn [db [_ {:keys [error data]}]]
   (dissoc
    (assoc db :error-message "Couldn't read your URL :(")
    :loading-action)))

(re-frame/reg-event-db
 ::remove-url
 (fn [db [_ url]]
   (update db :results dissoc url)))
