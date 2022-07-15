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
    [[:tr.table-expand-row {:data-open-details true
                            :style "cursor: pointer"}
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
     (and
      (< (js/parseInt (:startTime event)) now)
      (> event-date-month-endtime month-int)))))

(defn event-ends-in-month?
  [event ^number month-int]
  (when (and
         (in-future? event)
         (in-month? event month-int)) event))

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
     [:th {:width "180px"} "Date"]
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

(defn apply-search
  [events-list]
  (let [search-str (helper/read-value-of-dom-id "event-search-input")
        search-str-lower (clojure.string/lower-case search-str)]
    (case (count search-str)
      0 events-list
      1 events-list
      2 events-list
      (filter #(clojure.string/includes? (clojure.string/lower-case (str (select-keys % [:title :description :venueTitle]))) search-str-lower) events-list))))

(defn apply-filters-month
  []
  (let [month-int (helper/read-value-of-dom-id->int "event-month-select")
        cat-checked (categories-checked-with-default)
        all-events (state/get-events-all)]
    (if (= month-int 0)
      (filter #(contains? cat-checked (:rawCategory %)) all-events)
      (filter #(contains? cat-checked (:rawCategory %)) (events-end-in-month all-events month-int)))))

(defn apply-filters-month!
  []
  (let [events (apply-filters-month)]
    (state/update-events-showing! events)
    events))

(defn apply-filters-category
  []
  (let [cat-checked (categories-checked-with-default)
        last-checked-count (count (state/get-events-categories-selected))]
    (state/update-events-categories-selected! cat-checked)
    (let [events (apply-filters-month)]
      (if (not= last-checked-count (categories-checked-count))
        (filter #(contains? cat-checked (:rawCategory %)) events)
        events))))

(defn apply-filters-category!
  []
  (let [events (apply-filters-category)]
    (state/update-events-showing! events)
    events))

(defn event-search-input-length
  []
  (let [counter (count (helper/read-value-of-dom-id "event-search-input"))
        counter-int (js/parseInt counter)]
    counter-int))

(defn apply-search?
  [from-length]
  (> (event-search-input-length) from-length))

(defn search-empty?
  []
  (= (event-search-input-length) 0))

(defn apply-filter-chain!
  [event-type]
  (case event-type
    "month" (->> (apply-filters-month!)
                 apply-search
                 rerender-events)
    "category" (->> (apply-filters-category!)
                    apply-search
                    rerender-events)
    "search" (do (when (apply-search? 2)
                   (->> (state/get-events-showing)
                        apply-search
                        rerender-events))
                 (when (search-empty?) (rerender-events (state/get-events-showing))))))

(defn listen-month-select!
  []
  (.addEventListener (helper/dom-element-by-id "event-month-select")
                     "change"
                     (fn [_event] (apply-filter-chain! "month"))
                     false))

(defn listen-categories-selected!
  []
  (doseq [checkbox (categories-checkboxes)]
    (.addEventListener checkbox
                       "change"
                       (fn [_event] (apply-filter-chain! "category"))
                       false)))

(defn listen-search-input!
  []
  (.addEventListener (helper/dom-element-by-id "event-search-input")
                     "keyup"
                     (fn [_event] (apply-filter-chain! "search"))
                     false))

(defn render-categories
  []
  (->>
   (html
    [:div.grid-container
     [:fieldset.fieldset
      [:legend "Kategorien"]
      [:div.grid-x.grid-padding-x.small-up-1.medium-up-3.large-up-5
       (for [category (state/get-events-categories)]
         [:div.cell
          [:input {:type :checkbox :id category :name category :value category}]
          [:label {:for category} category]])]]])
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
   [:div.grid-x.grid-margin-x
    [:div.cell.small-12.medium-6
     [:div#events-search
      [:label {:for "event-search-input"}
       "Suche:"
       [:input#event-search-input {:type "text"
                                   :name "event-search"
                                   :placeholder "Suchen ..."}]]]]
    [:div.cell.small-12.medium-6
     [:label {:for "event-month-select"} "Monat wählen:"]
     [:select#event-month-select
      [:option {:value 0} "Zeige alle"]
      (for [month (range 1 13)] [:option {:value month} (str month)])]]]
   [:div#events-categories]
   [:div#events-list]))

(goog-define EVENTS_URL "http://localhost:9000/events.json")

(defn init
  []
  (helper/replace-inner-html
   (helper/dom-element-by-id "events")
   (dom-skeleton))
  (helper/async-get-data EVENTS_URL initial-render))
