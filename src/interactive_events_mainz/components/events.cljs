(ns interactive-events-mainz.components.events
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require [hiccups.runtime :as hiccupsrt]
            [interactive-events-mainz.helper :as helper]
            [interactive-events-mainz.state :as state]))

(def now (helper/now))

(defn categories-checkboxes
  []
  (-> (helper/dom-element-by-id "events-categories")
      (.getElementsByTagName "input")))

(defn elements-id-set
  [elements]
  (->> elements
       (mapv #(.-id %))
       (set)))

(defn categories-checkboxes-ids
  []
  (->> (categories-checkboxes)
       (elements-id-set)))

(defn categories-checked
  []
  (->> (filterv #(= true (.-checked %)) (categories-checkboxes))
       (elements-id-set)))

(defn categories-checked-count
  []
  (count (categories-checked)))

(defn categories-checked-with-default
  []
  (let [cat-checked-count (categories-checked-count)]
    (if (> cat-checked-count 0)
      (categories-checked)
      (categories-checkboxes-ids))))

(defn event-component
  [event]
  (let [{:keys [title
                description
                event_time_start
                event_time_end
                event_date_start
                event_date_end
                venueZip
                venueTown
                venueTitle]} event]
    [[:tr.table-expand-row {:data-open-details true}
      [:td
       [:span.event-date event_date_start " bis " event_date_end]
       [:br]
       [:span.event-time event_time_start " bis " event_time_end " Uhr"]]
      [:td title]
      [:td
       [:span.venue-title venueTitle]
       [:span " "]
       [:span.venue-zip "(" venueZip " " venueTown ")"]]]
     [:tr.table-expand-row-content
      [:td.table-expand-row-nested {:colspan 3}
       [:p description]]]]))

(defn sort-events-asc
  [events]
  (->> events
       (filter #(> (js/parseInt (:endTime %)) now))
       (sort-by #(js/parseInt (:startTime %)))))

(defn sort-events-asc!
  [events]
  (let [events-sorted (sort-events-asc events)]
    (state/update-events! events-sorted)
    events-sorted))

(defn in-future?
  [event]
  (> (js/parseInt (:endTime event)) now))

(defn in-month?
  [event ^number month-int]
  (let [event-date (new js/Date. (* (:endTime event) 1000))
        event-date-month-endtime (+ (.getMonth event-date) 1)]
    (or
     (= event-date-month-endtime month-int)
     (and (< (js/parseInt (:startTime event)) now) (> event-date-month-endtime month-int)))))

(defn event-ends-in-month?
  [event ^number month-int]
  (when (and (in-future? event) (in-month? event month-int)) event))

(defn events-end-in-month
  [events ^number month-int]
  (->> (event-ends-in-month? event month-int)
       (for [event events])
       (remove nil?)
       (sort-events-asc)))

(defn listen-row-click
  []
  (let [elems (->>
               (.querySelectorAll js/document "[data-open-details]")
               (.from js/Array))]
    (doseq [elem elems]
      (.addEventListener elem
                         "click"
                         (fn [e]
                           (.preventDefault e)
                           (let [row (.closest (.-target e) "tr")]
                             (.toggle (.-classList row) "is-active")
                             (.toggle (.-classList (.-nextElementSibling row)) "is-active")))
                         false))))

(defn clear-table-events
  []
  (let [elems (->>
               (.querySelectorAll js/document "[data-open-details]")
               (.from js/Array))]
    (doseq [elem elems] (.removeEventListener elem "click" {} false))))

(defn table-layout
  [body]
  [:table.table-expand
   [:thead
    [:tr.table-expand-row
     [:th "Date"]
     [:th "Content"]
     [:th "Location"]]]
   [:tbody body]])

(defn list-of-events->hiccups
  [events]
  (table-layout (apply concat (for [event events] (event-component event)))))

(defn list-of-filtered-events->hiccups
  []
  (-> (state/get-events-showing)
      (list-of-events->hiccups)))

(defn list-of-events-all->hiccups
  []
  (-> (state/get-events-all)
      (list-of-events->hiccups)))

(defn categories-checkboxes-changed?
  [cat-checked last-checked]
  (or
   (= cat-checked 0)
   (not= cat-checked last-checked)))

(defn rerender
  []
  (helper/replace-inner-html
   (helper/dom-element-by-id "events-list")
   (html (list-of-filtered-events->hiccups)))
  (clear-table-events)
  (listen-row-click))

(defn rerender-events
  [events]
  (helper/replace-inner-html
   (helper/dom-element-by-id "events-list")
   (html (list-of-events->hiccups events)))
  (clear-table-events)
  (listen-row-click))

(defn apply-search!
  []
  (let [search-str (helper/read-value-of-dom-id "event-search-input")
        events-showing (state/get-events-showing)]
    (case (count search-str)
      0 (rerender)
      1 nil
      2 nil
      (->>
       (filter #(clojure.string/includes? (.toLowerCase (str (select-keys % [:title :description :venueTitle]))) search-str) events-showing)
       (rerender-events)))))

(defn apply-filters-month
  []
  (let [month-int (helper/read-value-of-dom-id->int "event-month-select")
        cat-checked (categories-checked-with-default)]
    (if (= month-int 0)
      (filter #(contains? cat-checked (:rawCategory %)) (state/get-events-all))
      (filter #(contains? cat-checked (:rawCategory %)) (events-end-in-month (state/get-events-all) month-int)))))

(defn apply-filters-month!
  []
  (state/update-events-showing! (apply-filters-month))
  (rerender)
  (apply-search!))

(defn apply-filters-category!
  []
  (let [cat-checked (categories-checked)
        last-checked-count (count (state/get-events-categories-selected))]
    (state/update-events-categories-selected! cat-checked)

    (if (not= last-checked-count (categories-checked-count))
      (->>
       (apply-filters-month)
       (filter #(contains? cat-checked (:rawCategory %)))
       (state/update-events-showing!))
      (state/update-events-showing! (apply-filters-month)))
    (rerender)
    (apply-search!)))

(defn listen-categories-selected!
  []
  (doseq [checkbox (categories-checkboxes)]
    (.addEventListener checkbox "change" apply-filters-category! false)))

(defn listen-search-input!
  []
  (.addEventListener (helper/dom-element-by-id "event-search-input")
                     "keyup"
                     apply-search!
                     false))

(defn listen-month-select!
  []
  (.addEventListener (helper/dom-element-by-id "event-month-select")
                     "change"
                     apply-filters-month!
                     false))

(defn render-categories
  []
  (->>
   (html
    (for [category (state/get-events-categories)]
      [:div [:input {:type :checkbox :id category :name category :value category}]
       [:label {:for category} category]]))
   (helper/replace-inner-html (helper/dom-element-by-id "events-categories")))
  (listen-categories-selected!))

(defn- initial-render
  [xhr]
  (let [events (helper/extract-json xhr)]
    (state/update-events-showing! (sort-events-asc! events))
    (state/update-events-categories! (into #{} (map #(:rawCategory %) (state/get-events-all))))
    (rerender)
    (render-categories)
    (listen-month-select!)
    (listen-search-input!)))

(defn dom-skeleton
  []
  (html
   [:div#events-search
    [:label {:for "event-search"} "Suche:"]
    [:input#event-search-input {:name "event-search" :placeholder "Suchen ..."}]]
   [:label {:for "event-month-select"} "Monat wählen:"]
   [:select#event-month-select
    [:option {:value 0} "Zeige alle"]
    (for [month (range 1 13)] [:option {:value month} (str month)])]
   [:div
    [:label "Kategorien:"]
    [:div#events-categories]]
   [:div#events-list]))

(goog-define EVENTS_URL "http://localhost:9000/events.json")

(defn init
  []
  (helper/replace-inner-html
   (helper/dom-element-by-id "events")
   (dom-skeleton))
  (helper/async-get-data EVENTS_URL initial-render))
