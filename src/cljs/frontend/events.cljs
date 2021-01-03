(ns frontend.events
  (:require
   [clojure.set :as set]
   [ajax.core :as ajax]
   [re-frame.core :as re-frame]
   [frontend.db :as db]
   [frontend.query :refer [addr-bar-urls set-addr-bar-urls!]]
   [frontend.config :as config]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-event-db
 ::set-about-active
 (fn [db [_ value]]
   (assoc db :about-active value)))

(re-frame/reg-event-db
 ::set-error-message
 (fn [db [_ value]]
   (assoc db :error-message value)))

(re-frame/reg-event-db
 ::set-url-input
 (fn [db [_ value]]
   (assoc db :url-input value)))

;; Result handlers

(re-frame/reg-event-fx
 ::assoc-results
 (fn [{:keys [db]} [_ url data]]
   {:db (update db :results assoc url data)
    ::addr-bar-add-url url}))

(re-frame/reg-event-fx
 ::dissoc-results
 (fn [{:keys [db]} [_ urls]]
   {:db (update db :results #(apply dissoc % urls))
    ::addr-bar-remove-urls urls}))

;; Parsing result handler

(re-frame/reg-event-fx
 ::parse-success
 (fn [{:keys [db]} [_ url {:keys [error data]}]]
   (let [effects {:db db}]
     (cond-> (update-in effects [:db :loading] #(remove #{url} %))
       error (assoc-in [:db :error-message] error)
       (not error) (assoc :dispatch [::assoc-results url data])))))

(re-frame/reg-event-db
 ::parse-failure
 (fn [db [_ url {:keys [error data]}]]
   (-> db
       (update :loading #(remove #{url} %))
       (assoc :error-message "Couldn't read your URL :("))))

;; Url handling in address bar

(re-frame/reg-cofx
 :addr-bar-urls
 (fn [coeffects _]
   (assoc coeffects :addr-bar-urls (addr-bar-urls))))

(re-frame/reg-fx
 ;; Only add one at a time, because we only add if it was successfully parsed
 ;;   and parsing is only done one at a time
 ::addr-bar-add-url
 (fn [url]
   (let [existing (addr-bar-urls)
         new (if (some #{url} existing)
               existing
               (conj existing url))]
     (set-addr-bar-urls! new))))

(re-frame/reg-fx
 ;; Removing urls is always successful, so just remove in bulk
 ::addr-bar-remove-urls
 (fn [urls]
   (->> (addr-bar-urls)
        (remove urls)
        set-addr-bar-urls!)))

(defn handle-proposed
  "Helper function to compute parsing or removing of urls from proposed sets"
  [{:keys [db] :as effects} proposed]
  (let [existing (set (keys (:results db)))
        to-add (set/difference proposed existing)
        to-remove (set/difference existing proposed)]
    (cond-> effects
      (not (empty? to-add))
      (assoc :dispatch-n (map (fn [url] [::parse-url url]) to-add))
      (not (empty? to-remove))
      (assoc :dispatch [::dissoc-results to-remove]))))

;; Events for url handling

(re-frame/reg-event-fx
 ::parse-url
 (fn [{:keys [db]} [_ url]]
   {:db         (update db :loading conj url)
    :http-xhrio {:method          :get
                 :uri             config/parse-path
                 :params          {:url url}
                 :timeout         15000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::parse-success url]
                 :on-failure      [::parse-failure url]}}))

;; Events that components should dispatch to

(re-frame/reg-event-fx
 ::add-url-from-input
 (fn [{:keys [db]} _]
   (let [url (:url-input db)
         existing (set (keys (:results db)))
         proposed (conj existing url)]
     (handle-proposed {:db (assoc db :url-input "")} proposed))))

(re-frame/reg-event-fx
 ::replace-urls-from-input
 (fn [{:keys [db]} _]
   (let [proposed #{(:url-input db)}]
     (handle-proposed {:db (assoc db :url-input "")} proposed))))

(re-frame/reg-event-fx
 ::replace-from-addr-bar
 [(re-frame/inject-cofx :addr-bar-urls)]
 (fn [{:keys [db addr-bar-urls]} _]
   (let [proposed (set addr-bar-urls)]
     (handle-proposed {:db db} proposed))))

(re-frame/reg-event-fx
 ::remove-url
 (fn [effects [_ url]]
   (assoc effects :dispatch [::dissoc-results #{url}])))
