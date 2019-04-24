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
 ::spinner-modal-class
 (fn [db _]
   (when (pos? (count (:loading db))) "is-active")))

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
   (let [plotted-urls (keys (:results db))]
     (> (count plotted-urls) 0))))

(re-frame/reg-sub
 ::recommendations
 (fn [db _]
   (apply concat (vals (:results db)))))

(re-frame/reg-sub
 ::url-list
 (fn [db _]
   (keys (:results db))))
