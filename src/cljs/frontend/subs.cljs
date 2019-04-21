(ns frontend.subs
  (:require
   [clojure.string :as str]
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 ::about-modal-class
 (fn [db _]
   (when (:about-active db) "is-active")))

(re-frame/reg-sub
 ::error-modal-class
 (fn [db _]
   (when (:error-message db) "is-active")))

(re-frame/reg-sub
 ::error-message
 (fn [db _]
   (:error-message db)))

(re-frame/reg-sub
 ::url-input
 (fn [db _]
   (:url-input db)))

(re-frame/reg-sub
 ::show-clear-button
 (fn [db _]
   (not (str/blank? (:url-input db)))))

(re-frame/reg-sub
 ::show-append-button
 (fn [db _]
   (let [plotted-urls (->> (:recommendations db)
                           (map :url)
                           (into #{}))]
     (> (count plotted-urls) 0))))

(re-frame/reg-sub
 ::recommendations
 (fn [db _]
   (:recommendations db)))

(re-frame/reg-sub
 ::append-loading
 (fn [db _]
   (when (= :append (:loading-action db)) "is-loading")))

(re-frame/reg-sub
 ::plot-loading
 (fn [db _]
   (when (= :plot (:loading-action db)) "is-loading")))
