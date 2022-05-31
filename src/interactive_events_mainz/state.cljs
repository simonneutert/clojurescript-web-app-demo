(ns interactive-events-mainz.state)

(defonce db
  (atom {:events
         {:all {}
          :showing {}
          :categories #{}
          :selected-month 0
          :categories-selected #{}}}))

(defn get-events
  []
  (:events @db))

(defn get-events-all
  []
  (:all (get-events)))

(defn get-events-showing
  []
  (:showing (get-events)))

(defn get-events-categories
  []
  (:categories (get-events)))

(defn get-events-categories-selected
  []
  (:categories-selected (get-events)))

(defn update-events!
  [new-state]
  (->> {:all new-state}
       (merge (get-events))
       (swap! db assoc :events)))

(defn update-events-showing!
  [new-state]
  (->> {:showing new-state}
       (merge (get-events))
       (swap! db assoc :events)))

(defn update-events-categories!
  [new-state]
  (->> {:categories new-state}
       (merge (get-events))
       (swap! db assoc :events)))

(defn update-events-categories-selected!
  [new-state]
  (->> {:categories-selected new-state}
       (merge (get-events))
       (swap! db assoc :events)))

(defn update-events-selected-month!
  [new-state]
  (->> {:selected-month new-state}
       (merge (get-events))
       (swap! db assoc :events)))
