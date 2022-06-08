(ns interactive-events-mainz.core
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require [hiccups.runtime :as hiccupsrt]
            [interactive-events-mainz.helper :as helper]
            [interactive-events-mainz.components.events :as events]))

(def js-now (.now js/Date))
(def now (js/parseInt (/ js-now 1000)))

(defn- app-component-layout []
  (->>
   [:div.grid-container
    [:h1#title "Mainz Events"]
    [:div#events]]
   html))

(defn- init-components
  []
  (events/init))

(defn init-app []
  (helper/replace-inner-html (helper/dom-element-by-id "app") (app-component-layout))
  (init-components))

(helper/dom-ready init-app)
