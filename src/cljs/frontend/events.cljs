(ns frontend.events
  (:require
   [clojure.set :as set]
   [ajax.core :as ajax]
   [re-frame.core :as re-frame]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   [frontend.db :as db]
   [frontend.query :refer [addr-bar-urls set-addr-bar-urls!]]))

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
 ::parse-success
 (fn-traced
  [{:keys [db]} [_ url {:keys [error data]}]]
  (let [db-changes {:db (cond-> db
                          error (assoc :error-message error)
                          (not error) (update :results assoc url data)
                          true (update :loading #(remove #{url} %)))}]
    (if error
      db-changes
      (assoc db-changes ::addr-bar-add-url url)))))

(re-frame/reg-event-db
 ::parse-failure
 (fn-traced
  [db [_ url {:keys [error data]}]]
  (-> db
      (assoc :error-message "Couldn't read your URL :(")
      (update :loading #(remove #{url} %)))))

;; Effects for url handling in address bar

(re-frame/reg-fx
 ;; Only add one at a time, because we only add if it was successfully parsed
 ;;   and parsing is only done one at a time
 ::addr-bar-add-url
 (fn-traced
  [url]
  (let [existing (addr-bar-urls)
        new (if (some #{url} existing)
              existing
              (conj existing url))]
    (set-addr-bar-urls! new))))

(re-frame/reg-fx
 ;; Removing urls is always successful, so just remove in bulk
 ::addr-bar-remove-urls
 (fn-traced
  [urls]
  (let [existing (addr-bar-urls)
        new (remove (set urls) existing)]
    (set-addr-bar-urls! new))))

;; Events for url handling

(re-frame/reg-event-fx
 ;; This event should not be dispatched to from any components.
 ;; Instead dispatch to one of
 ;;   add-url
 ;;   remove-url
 ;;   replace-urls
 ::-parse-url
 (fn-traced
  [{:keys [db]} [_ url]]
  {:db         (update db :loading conj url)
   :http-xhrio {:method          :get
                :uri             "/parse"
                :params          {:url url}
                :timeout         15000
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      [::parse-success url]
                :on-failure      [::parse-failure url]}}))

(re-frame/reg-event-fx
 ::add-url
 (fn-traced
  [{:keys [db]} [_]]
  (let [url (:url-input db)
        db-changes {:db (dissoc db :url-input)}]
    (if (get-in db [:results url])
      ;; Already present in results, no changes required
      db-changes
      ;; Wasn't in results, do parsing
      (assoc
       db-changes
       :dispatch [::-parse-url url])))))

(re-frame/reg-event-fx
 ::remove-url
 (fn-traced
  [{:keys [db]} [_ url]]
  {:db (update db :results dissoc url)
   ::addr-bar-remove-urls [url]}))

(re-frame/reg-event-fx
 ::replace-urls
 (fn-traced
  [{:keys [db]} [_ urls]]
  (let [existing (set (keys (:results db)))
        new (if (= :from-url-input urls) #{(:url-input db)} (set urls))
        to-add (set/difference new existing)
        to-remove (set/difference existing new)]
    {:db (cond-> db
           true (update :results #(apply dissoc % to-remove))
           (= :from-url-input urls) (dissoc :url-input))
     :dispatch-n (map (fn [url] [::-parse-url url]) to-add)
     ::addr-bar-remove-urls to-remove})))
