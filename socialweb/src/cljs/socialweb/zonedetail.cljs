(ns socialweb.zonedetail  (:use [net.unit8.tower :only [t]])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [socialweb.core :as socialcore]
            [ajax.core :refer [GET POST PUT DELETE]]
            [clojure.string :as str]
            [om-bootstrap.button :as b]
            [om-bootstrap.panel :as p]
            [cljs.core.async :refer [put! dropping-buffer chan take! <!]]
            [om-bootstrap.input :as i]
            [cljs-time.core :as tm]
            [cljs-time.format :as tf]
            [cljs-time.coerce :as tc]
            [socialweb.settings :as settings]
  )
  (:import goog.History)
)

(def jquery (js* "$"))

(enable-console-print!)

(def custom-formatter (tf/formatter "dd/MM/yyyy"))

(def custom-formatter1 (tf/formatter "MMM dd yyyy hh:mm:ss"))

(def ch (chan (dropping-buffer 2)))
(defonce app-state (atom  {:city "" :name "" :diff 0 :view 1 :id 0 :current "Zone Detail"} ))

(defn OnDeleteZoneError [response]
  (let [     
      newdata {:id (get response (keyword "id") ) }
    ]

  )
  ;; TO-DO: Delete Zone from Core
  ;;(.log js/console (str  (get (first response)  "Title") ))
)


(defn check-zone-valid []
  (let [
     diff (js/parseFloat (:diff @app-state))
  ]
    (if (and (> (count (:city @app-state)) 0) (> (count (:name @app-state)) 0) (<= diff 14) (>= diff -12)) true false)
  )
)

(defn OnDeleteZoneSuccess [response]
  (let [
      zones (:zones ((keyword (str (:selecteduser @socialcore/app-state))) @socialcore/app-state))
      newzones (remove (fn [zone] (if (= (:id zone) (:id @app-state) ) true false)) zones)
    ]

    (swap! socialcore/app-state assoc-in [(keyword (str (:selecteduser @socialcore/app-state)) ) :zones] newzones)

    (js/window.history.back)
  )
)



(defn OnUpdateZoneError [response]
  (let [     
      newdata {:id (get response (keyword "id") ) }
    ]

  )
  ;; TO-DO: Delete Trip from Core
  ;;(.log js/console (str  (get (first response)  "Title") ))
)


(defn OnUpdateZoneSuccess [response]
  (let [
      zones (:zones ((keyword (str (:selecteduser @socialcore/app-state)) ) @socialcore/app-state))
      newzones (remove (fn [zone] (if (= (:id zone) (:id @app-state) ) true false  )) zones)
      addzones (into [] (conj newzones {:id (:id @app-state) :name (:name @app-state) :city (:city @app-state) :diff (js/parseFloat (:diff @app-state))})) 
    ]
    (swap! socialcore/app-state assoc-in [(keyword (str (:selecteduser @socialcore/app-state))) :zones] addzones)
    (js/window.history.back)
  )
)



(defn OnCreateZoneError [response]
  (let [     
      newdata {:id (get response (keyword "id") ) }
    ]

  )
  ;; TO-DO: Delete Trip from Core
  ;;(.log js/console (str  (get (first response)  "Title") ))
)


(defn OnCreateZoneSuccess [response]
  (let [
      zones (:zones ((keyword (str (:selecteduser @socialcore/app-state))) @socialcore/app-state))  
      addzones (into [] (conj zones {:id (get response (keyword "id")) :name (:name @app-state) :city (:city @app-state) :diff (js/parseFloat (:diff @app-state)) })) 
    ]
    (swap! socialcore/app-state assoc-in [(keyword (str (:selecteduser @socialcore/app-state)) ) :zones] addzones)
    (js/window.history.back)
  )
)



(defn deleteZone [zoneid]
  (DELETE (str settings/apipath  "api/zone?id=" zoneid) {
    :handler OnDeleteZoneSuccess
    :error-handler OnDeleteZoneError
    :headers {
      :content-type "application/json" 
      :Authorization (str "Bearer "  (:token (:token @socialcore/app-state)))}
    :format :json})
)

(defn updateZone []
  (PUT (str settings/apipath  "api/zone") {
    :handler OnUpdateZoneSuccess
    :error-handler OnUpdateZoneError
    :headers {
      :Authorization (str "Bearer "  (:token (:token @socialcore/app-state)))}
    :format :json
    :params {:id (:id @app-state)  :city (:city @app-state) :name (:name @app-state) :diff (js/parseFloat (:diff @app-state))}})
)

(defn createZone []
  (POST (str settings/apipath  "api/zone") {
    :handler OnCreateZoneSuccess
    :error-handler OnCreateZoneError
    :headers {
      :Authorization (str "Bearer "  (:token (:token @socialcore/app-state)))}
    :format :json
    :params { :user (js/parseInt (:selecteduser @socialcore/app-state)) :name (:name @app-state) :city (:city @app-state) :diff (js/parseFloat (:diff @app-state))}})
)

(defn setNewZoneValue [key val]
  (swap! app-state assoc-in [(keyword key)] val)
)


(defn setcontrols []

  ;;(.log js/console "fieldcode"       )
)

(defn initqueue []
  (doseq [n (range 1000)]
    (go ;(while true)
      (take! ch(
        fn [v] (
           ;(setcalculatedfields) 
           setcontrols 
           ;.log js/console "Core.ASYNVC working!!!" 
          )
        )
      )
    )
  )
)

(initqueue)




(defn setZoneNullValues []
  (swap! app-state assoc-in [:diff] 0)
  (swap! app-state assoc-in [:city] "" )
  (swap! app-state assoc-in [:name ] "")
)

(defn setZone []
  (let [
        login (:selecteduser @socialcore/app-state)
        zone (first (filter (fn [zone] (if (= (:id @app-state) (:id zone)) true false)) (:zones ( (keyword (str (:selecteduser @socialcore/app-state))) @socialcore/app-state) )))  ]

       ;(.log js/console "the zone")
       ;(.log js/console zone)

       (if (= zone nil)
         (setZoneNullValues)
         (let []
           (swap! app-state assoc-in [:diff] (:diff zone))
           (swap! app-state assoc-in [:name] (:name zone))
           (swap! app-state assoc-in [:city] (:city zone))
         )
       )
    )
)




(defn OnError [response]
  (let [     
      newdata { :error (get (:response response)  "error") }
    ]
     
    
  )
  
  
)


(defn getZoneDetail []

  (setZone)
  ;; (if
  ;;   (and 
  ;;     (not= (:tripid @app-state) nil)
  ;;     (not= (:tripid @app-state) 0)
  ;;   )
    
  
  ;; )
)

(defn handleChange [e]
  ;(.log js/console e  )  
  ;(.log js/console "The change ....")
  (swap! app-state assoc-in [(keyword (.. e -nativeEvent -target -id))] (.. e -nativeEvent -target -value))
)


(defn onMount [data]
  ;(setTripNullValues)
  (swap! socialcore/app-state assoc-in [:current] "Zone Detail")
  (getZoneDetail)
  (put! ch 42)
)

(defcomponent zonedetail-page-view [data owner]
  (did-mount [_]
    (onMount data)
  )
  (did-update [this prev-props prev-state]
    (.log js/console "Update happened") 
    (swap! socialcore/app-state assoc-in [:current] "Zone Detail")
    (put! ch 42)
  )
  (render
    [_]
    (let [style {:style {:margin "10px;" :padding-bottom "0px;"}}
      styleprimary {:style {:margin-top "70px"}}
      ]
      (dom/div
        ;(om/build tripcore/website-view data {})
        (dom/div {:className "panel panel-default"}
          (dom/div {:className "panel-heading"}
            (dom/div {:className "panel-title"} 
              (dom/div {:className "row"}
                (dom/div {:className "col-md-1"}
                  "Name:"
                )
                (dom/div {:className "col-md-2"}
                  (dom/input {:id "name" :type "text" :style {:width "100%"} :value  (:name @app-state) :onChange (fn [e] (handleChange e)) }))
              (if (< (count (:name @app-state)) 1)
                (dom/div {:style {:color "red" :margin-top "5px"}}
                  "Name should not be empty"
                )
              )
              )
            )
          )
          (dom/div {:className "panel-body"}
            (dom/div {:className "row"}
              (dom/div {:className "col-md-1"}
                "City:"
              )            
              (dom/div {:className "col-md-2"}
                (dom/input {:id "city" :type "text" :style {:width "100%"} :value (:city @app-state) :onChange (fn [e] (handleChange e))})
              )
              (if (< (count (:city @app-state)) 1)
                (dom/div {:style {:color "red" :margin-top "5px"}}
                  "City should not be empty"
                )
              )
            )

            (dom/div {:className "row" :style {:margin-top "5px"}}
              (dom/div {:className "col-md-1"}
                "Difference:"
              )            
              (dom/div {:className "col-md-2"}
                (dom/input {:id "diff" :type "text" :value (:diff @app-state) :onChange (fn [e] (handleChange e))})
              )
              (if (or (> (js/parseFloat (:diff @app-state)) 14) (< (js/parseFloat (:diff @app-state)) -12))
                (dom/div {:style {:color "red" :margin-top "5px"}}
                  "Difference to GMT should be in [-12 +14] range"
                )
              )
            )
          )
        )
        (dom/nav {:className "navbar navbar-default" :role "navigation"}
          (dom/div {:className "navbar-header"}
            (dom/button {:className "btn btn-default" :disabled (not (check-zone-valid) ) :onClick (fn [e] (if (= (:id @app-state) 0) (createZone) (updateZone)) )} (if (= (:id @app-state) 0) "Insert" "Update") )

            (b/button {:className "btn btn-danger" :style {:visibility (if (= (:id @app-state) 0) "hidden" "visible")} :onClick (fn [e] (deleteZone (:id @app-state)))} "Delete")

            (b/button {:className "btn btn-info"  :onClick (fn [e]     (js/window.history.back))  } "Cancel")
          )
        )
      )
    )

  )
)


(sec/defroute zonedetail-page "/zonedetail/:id" {id :id}
  (let [
    zoneid id
    ]
    (.log js/console id)
    (swap! app-state assoc-in [:id]  (js/parseInt id) ) 
    (om/root zonedetail-page-view
             app-state
             {:target (. js/document (getElementById "app"))})
  )
)
