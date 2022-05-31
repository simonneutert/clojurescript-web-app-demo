(ns interactive-events-mainz.helper)

(defn dom-ready
  [callback]
  (if (not= (.-readyState js/document) "loading")
    (callback)
    (.addEventListener js/document "DOMContentLoaded" callback)))

(defn async-get-data
  [url callback]
  (let [xhr (js/XMLHttpRequest.)]
    (.overrideMimeType xhr "application/json")
    (.open xhr "GET" url true)
    (set! (.-onreadystatechange xhr)
          #(when
            (= (.-readyState xhr) 4)
             (callback xhr)))
    (.send xhr)
    xhr))

(defn js-now
  []
  (.now js/Date))

(defn now
  []
  (js/parseInt (/ (js-now) 1000)))

(defn extract-json
  [xhr]
  (js->clj (.parse js/JSON (.-response xhr)) :keywordize-keys true))

(defn dom-element-by-id
  [id]
  (.getElementById js/document id))

(defn read-value-of-dom-id
  [dom-id]
  (->> dom-id
       (dom-element-by-id)
       (.-value)))

(defn read-value-of-dom-id->int
  [dom-id]
  (js/parseInt (read-value-of-dom-id dom-id)))

(defn replace-inner-html
  [dom-element content]
  (set! (.-innerHTML dom-element) content))
