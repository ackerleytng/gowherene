(ns frontend.utils.recommendations)

(defn handle-recommendations
  [existing action data]
  (.log js/console (clj->js {:existing existing
                             :action action
                             :data data}))
  (case action
    :append (concat existing data)
    :plot data
    existing))
