(ns app.core
  (:require [reagent.core :as r]
            [reagent.dom.server :as rs]
            [ajax.core :refer [GET]]
            [clojure.string :as str]))

(enable-console-print!)

(def url-placeholder "Enter your url here (from sethlui, smartlocal...)")

(def app-state (r/atom {:add-to-plot false
                        :loading false
                        :url url-placeholder
                        :data []}))

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
          (compute-and-fit-bounds @map-atom data)))
      :reagent-render
      (fn []
        [:div {:style {:height "100vh"}}])})))

(defn handle-data
  [response]
  (.log js/console (clj->js [:response response]))
  (swap! app-state assoc :loading false)
  (if (@app-state :add-to-plot)
    (swap! app-state update :data into response)
    (swap! app-state assoc :data response)))

(defn parse-url-error [response]
  (.log js/console [:error-response response])
  (swap! app-state assoc :loading false))

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
;; Main app

(defn app []
  [:div
   [:section.section
    [:div.container.is-fluid
     [:h1.title "Mappout your recommendations!"]
     [controls]]]
   [:div.container.is-widescreen
    ;; props passed to a reagent component must be a map
    [recommendation-map {:data (@app-state :data)}]]])

(defn by-id [id]
  (.getElementById js/document id))

(defn ^:export run []
  (r/render [app]
            (by-id "app")))

(run)
