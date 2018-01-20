(ns app.core
  (:require [reagent.core :as r]
            [reagent.dom.server :as rs]
            [ajax.core :refer [GET]]
            [clojure.string :as str]))

(enable-console-print!)

(def url-placeholder "Enter your url here (from sethlui, smartlocal...)")

(def app-state (r/atom {:add-to-plot false
                        :loading false
                        :error-message nil
                        :url url-placeholder
                        :data []}))

;; ------------------------
;; Helper functions

(defn by-id [id]
  (.getElementById js/document id))

;; ------------------------
;; Scrolling stuff
;;   From https://gist.github.com/jasich/21ab25db923e85e1252bed13cf65f0d8

(defn cur-doc-top []
  (+ (.. js/document -body -scrollTop) (.. js/document -documentElement -scrollTop)))

(defn element-top [elem top]
  (if (.-offsetParent elem)
    (let [client-top (or (.-clientTop elem) 0)
          offset-top (.-offsetTop elem)]
      (+ top client-top offset-top (element-top (.-offsetParent elem) top)))
    top))

(defn scroll-to-id
  [elem-id]
  (let [speed 400
        moving-frequency 10
        elem (by-id elem-id)
        hop-count (/ speed moving-frequency)
        doc-top (cur-doc-top)
        gap (/ (- (element-top elem 0) doc-top) hop-count)]
    (doseq [i (range 1 (inc hop-count))]
      (let [hop-top-pos (* gap i)
            move-to (+ hop-top-pos doc-top)
            timeout (* moving-frequency i)]
        (.setTimeout js/window (fn []
                                 (.scrollTo js/window 0 move-to))
                     timeout)))))

;; ------------------------
;; Google Maps stuff

(defn gmap-latlng
  [{:keys [lat lng]}]
  (js/google.maps.LatLng. lat lng))

(defn gmap-latlng-bounds [latlngs]
  (when (seq latlngs)
    (reduce #(.extend %1 %2)
            (js/google.maps.LatLngBounds.)
            latlngs)))

(def singapore-bounds
  (gmap-latlng-bounds (map gmap-latlng [{:lat 1.365035 :lng 103.644231}
                                        {:lat 1.355701 :lng 104.036779}
                                        {:lat 1.471183 :lng 103.810720}
                                        {:lat 1.236344 :lng 103.832829}])))

(defn compute-and-fit-bounds
  [map-object data]
  (let [new-bounds (->> data
                        (map :latlng)
                        (map gmap-latlng)
                        gmap-latlng-bounds)]
    (.fitBounds map-object (or new-bounds singapore-bounds) 0)))

(defn infowindow-content
  [header content]
  [:div
   [:b header] [:br]
   content])

(defn gmap-info-window
  [header content]
  (js/google.maps.InfoWindow.
   (clj->js {:content (rs/render-to-static-markup [infowindow-content header content])})))

(defn gmap-marker
  [map-object latlng label title content]
  (let [marker (js/google.maps.Marker.
                (clj->js {:position latlng
                          :map map-object
                          :title title
                          :label label}))]
    (.addListener marker
                  "click"
                  (fn [] (.open (gmap-info-window title content)
                                map-object
                                marker)))
    marker))

(defn gmap-marker-remove
  [marker]
  (.setMap marker nil))

(defn do-build-marker
  [map-object {:keys [place address latlng]}]
  (when place
    (let [label (get (re-find #"^\s*(\d+)" place) 1)]
      (gmap-marker map-object (gmap-latlng latlng) label place address))))

(defn do-plot!
  [map-object markers-atom data]
  ;; Need to use mapv because map is lazy
  (let [markers (mapv (partial do-build-marker map-object) data)]
    ;; Need to use doseq because map is lazy
    (doseq [m @markers-atom]
      (gmap-marker-remove m))
    ;; Not sure why the use of markers here does not force execution above
    (reset! markers-atom markers)))

(defn recommendation-map []
  (let [map-atom (r/atom nil)
        markers (r/atom [])]
    (r/create-class
     {:display-name "recommendation-map"
      :component-did-mount
      (fn [this]
        (let [map-canvas (r/dom-node this)
              map-options (clj->js {:disableDefaultUI true
                                    :zoomControl true})
              map-object (js/google.maps.Map. map-canvas map-options)
              data (-> this r/props :data)]
          (reset! map-atom map-object)
          (compute-and-fit-bounds map-object data)))
      :component-did-update
      (fn [this]
        (let [data (-> this r/props :data)]
          (do-plot! @map-atom markers data)
          (compute-and-fit-bounds @map-atom data)
          (scroll-to-id "recommendation-map")
          ))
      :reagent-render
      (fn []
        [:div#recommendation-map {:style {:height "100vh"}}])})))

(defn handle-data
  [response]
  (.log js/console (clj->js [:response response]))
  (swap! app-state assoc :loading false)
  (if (zero? (count response))
    (swap! app-state assoc :error-message "Couldn't find any addresses! :(")
    (if (@app-state :add-to-plot)
      (swap! app-state update :data into response)
      (swap! app-state assoc :data response))))

(defn parse-url-error [response]
  (.log js/console (clj->js [:error-response response]))
  (swap! app-state assoc :loading false)
  (swap! app-state assoc :error-message "Couldn't read your URL :("))

(defn parse-url
  [{:keys [url add-to-plot]}]
  (.log js/console (clj->js [:url url
                             :add-to-plot add-to-plot]))
  (swap! app-state assoc :loading true)
  (GET "/parse" {:params {:url url}
                 :handler handle-data
                 :error-handler parse-url-error
                 :response-format :json
                 :keywords? true}))

;; ------------------------
;; Controls components

(defn url-input-handle-key-press [e]
  (when (= "Enter" (.-key e))
    (parse-url @app-state)))

(defn url-input-handle-focus [e]
  (when (= (@app-state :url) url-placeholder)
    (swap! app-state assoc :url "")))

(defn url-input-handle-blur [e]
  (when (str/blank? (@app-state :url))
    (swap! app-state assoc :url url-placeholder)))

(defn url-input-handle-change [e]
  (swap! app-state assoc :url (.. e -target -value)))

(defn url-input []
  [:input#url.input
   {:type "text"
    :value (@app-state :url)
    :on-key-press url-input-handle-key-press
    :on-focus url-input-handle-focus
    :on-blur url-input-handle-blur
    :on-change url-input-handle-change}])

(defn plot-button []
  [:a#plot
   {:class ["button" "is-info" (when (@app-state :loading) "is-loading")]
    :on-click #(parse-url @app-state)} "Plot!"])

(defn controls []
  [:div#controls
   [:div.field.has-addons
    [:div.control.is-expanded
     [url-input]]
    [:div.control
     [plot-button]]]
   [:div.field.is-grouped.is-grouped-centered
    [:div.control
     [:label.checkbox
      [:input {:type "checkbox"
               :checked (@app-state :add-to-plot)
               :on-change #(swap! app-state update :add-to-plot not)}]
      " Add to current plot"]]]])

;; ------------------------
;; Error modal

(defn clear-error []
  (swap! app-state assoc :error-message nil))

(defn error-modal []
  [:div#error
   {:class ["modal" (when (@app-state :error-message) "is-active")]}
   [:div.modal-background
    {:on-click clear-error}]
   [:div.modal-content
    [:div.card
     [:div.card-content (@app-state :error-message)]]]
   [:button.modal-close.is-large {:aria-label "close"
                                  :on-click clear-error}]])

;; ------------------------
;; Main app

(defn app []
  [:div
   [error-modal]
   [:section.section
    [:div.container.is-fluid
     [:h1.title "Mappout your recommendations!"]
     [controls]]]
   [:div.container.is-widescreen
    ;; props passed to a reagent component must be a map
    [recommendation-map {:data (@app-state :data)}]]])

(defn ^:export run []
  (r/render [app]
            (by-id "app")))

(run)
