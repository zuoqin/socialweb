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

            [om.dom :as omdom :include-macros true]
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

(def zones
  [{:id 1 :name "International Date Line West" :city "No City" :diff -12}
   {:id 2 :name "Coordinated Universal Time" :city "No City" :diff -11}
   {:id 3 :name "Aleutian Island" :city "No City" :diff -10}
   {:id 4 :name "Alaska" :city "No City" :diff -9}
   {:id 5 :name "California" :city "Baja" :diff -8}
   {:id 6 :name "Arizona" :city "Arizona" :diff -7}
   {:id 7 :name "Central America" :city "Chicago" :diff -6}
   {:id 8 :name "Eastern Time" :city "New York" :diff -5}
   {:id 9 :name "Atlantic Time" :city "No City" :diff -4}
   {:id 10 :name "Newfoundland" :city "No City" :diff -3.5}
   {:id 11 :name "Brazil" :city "Buenos Aires" :diff -3}
   {:id 12 :name "Coordinated Universal Time -2" :city "No City" :diff -2}
   {:id 13 :name "Azores" :city "No City" :diff -1}
   {:id 14 :name "Spain" :city "Casablanka" :diff 0}
   {:id 15 :name "Western Europe" :city "Amsterdam" :diff 1}
   {:id 16 :name "Central Europe" :city "Athens" :diff 2}
   {:id 17 :name "Eastern Europe" :city "Minsk" :diff 3}
   {:id 18 :name "Azerbaijan" :city "Baku" :diff 4}
   {:id 19 :name "Russia Ural" :city "Ekaterinburg" :diff 5}
   {:id 20 :name "India" :city "New Delhi" :diff 5.5}
   {:id 21 :name "Kazachstan" :city "Astana" :diff 6}
   {:id 22 :name "Thailand" :city "Bangkok" :diff 7}
   {:id 23 :name "China" :city "Beijing" :diff 8}
   {:id 24 :name "South Korea" :city "Seoul" :diff 9}
   {:id 25 :name "Australia" :city "Sidney" :diff 10}
   {:id 26 :name "Russia Sibir" :city "Magadan" :diff 11}
   {:id 27 :name "Russia Far East" :city "Anadyr" :diff 12}
   {:id 28 :name "Chatham Island" :city "No City" :diff 12.75}
   {:id 29 :name "Samoa" :city "No City" :diff 13}
   {:id 30 :name "Kritimati Island" :city "No City" :diff 14}
  ]
)




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
     diff (js/parseFloat (:diff (:selectedzone @socialcore/app-state)))

     cnt (count (filter (fn [x] (if (and (= (:name x) (:name (:selectedzone @socialcore/app-state))) (= (:city x) (:city (:selectedzone @socialcore/app-state))) (not= (:id x) (:id (:selectedzone @socialcore/app-state)))) true false)) (:zones ((keyword (str (:id (:selecteduser @socialcore/app-state)))) @socialcore/app-state))))
  ]
    (if (and (< cnt 1) (> (count (:city (:selectedzone @socialcore/app-state))) 0) (> (count (:name (:selectedzone @socialcore/app-state))) 0) (<= diff 14) (>= diff -12)) true false)
  )
)

(defn OnDeleteZoneSuccess [response]
  (let [
      zones (:zones ((keyword (str (:id (:selecteduser @socialcore/app-state)))) @socialcore/app-state))
      newzones (remove (fn [zone] (if (= (:id zone) (:id (:selectedzone @socialcore/app-state)) ) true false)) zones)
    ]

    (swap! socialcore/app-state assoc-in [(keyword (str (:id (:selecteduser @socialcore/app-state))) ) :zones] newzones)

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
      zones (:zones ((keyword (str (:id (:selecteduser @socialcore/app-state))) ) @socialcore/app-state))
      newzones (remove (fn [zone] (if (= (:id zone) (:id (:selectedzone @socialcore/app-state)) ) true false  )) zones)
      addzones (into [] (conj newzones {:id (:id (:selectedzone @socialcore/app-state)) :name (:name (:selectedzone @socialcore/app-state)) :city (:city (:selectedzone @socialcore/app-state)) :diff (js/parseFloat (:diff (:selectedzone @socialcore/app-state)))})) 
    ]
    (swap! socialcore/app-state assoc-in [(keyword (str (:id (:selecteduser @socialcore/app-state)))) :zones] addzones)
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
    zones (:zones ((keyword (str (:id (:selecteduser @socialcore/app-state)))) @socialcore/app-state))
    addzones (into [] (conj zones {:id (get response "id") :name (:name (:selectedzone @socialcore/app-state)) :city (:city (:selectedzone @socialcore/app-state)) :diff (js/parseFloat (:diff (:selectedzone @socialcore/app-state))) })) 
    ]
    (swap! socialcore/app-state assoc-in [(keyword (str (:id (:selecteduser @socialcore/app-state))) ) :zones] addzones)
    (js/window.history.back)
  )
)



(defn deleteZone [zoneid]
  (DELETE (str settings/apipath  "api/zone?id=" zoneid) {
    :handler OnDeleteZoneSuccess
    :error-handler OnDeleteZoneError
    :headers {
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
    :params {:id (:id (:selectedzone @socialcore/app-state)) :city (:city (:selectedzone @socialcore/app-state)) :name (:name (:selectedzone @socialcore/app-state)) :diff (js/parseFloat (:diff (:selectedzone @socialcore/app-state)))}})
)

(defn createZone []
  (POST (str settings/apipath  "api/zone") {
    :handler OnCreateZoneSuccess
    :error-handler OnCreateZoneError
    :headers {
      :Authorization (str "Bearer "  (:token (:token @socialcore/app-state)))}
    :format :json
    :response-format :json
    :params { :user (js/parseInt (:id (:selecteduser @socialcore/app-state))) :name (:name (:selectedzone @socialcore/app-state)) :city (:city (:selectedzone @socialcore/app-state)) :diff (js/parseFloat (:diff (:selectedzone @socialcore/app-state)))}})
)

;; (defn setNewZoneValue [key val]
;;   (swap! app-state assoc-in [(keyword key)] val)
;; )


(defn pickupzone [id]
  (let [
    zone (first (filter (fn [x] (if (= (str (:id x)) id) true false)) zones))
  ]
  (swap! socialcore/app-state assoc-in [:selectedzone :name] (:name zone))
  (swap! socialcore/app-state assoc-in [:selectedzone :city] (:city zone))
  (swap! socialcore/app-state assoc-in [:selectedzone :diff] (:diff zone))
  )
)

(defn onDropDownChange [id value]
  ;(.log js/console (str "id=" id "; value=" value))
  (pickupzone value)
)

(defn setZonesDropDown []
  (jquery
     (fn []
       (-> (jquery "#zoneslist" )
         (.selectpicker {})
       )
     )
   )
   (jquery
     (fn []
       (-> (jquery "#zoneslist" )
         (.selectpicker "val" -1)
         (.on "change"
           (fn [e]
             (
               onDropDownChange (.. e -target -id) (.. e -target -value)
             )
           )
         )
       )
     )
   )
)


(defn buildZonesList [data owner]
  (map
    (fn [item]
      (dom/option {:key (:id item) :value (:id item) :data-subtext (str (:city item) (if (> (:diff item) 0) "  +" "  ") (:diff item))
} (:name item))
    )
    zones
  )
)


(defn setcontrols [value]
  
  (case value
    46 (setZonesDropDown)
    (.log js/console (str "value = " value))
  )
)


(defn initqueue []
  (doseq [n (range 1000)]
    (go ;(while true)
      (take! ch(
        fn [v] (
           ;(setcalculatedfields) 
           setcontrols v
           ;.log js/console "Core.ASYNVC working!!!" 
          )
        )
      )
    )
  )
)

(initqueue)




(defn setZoneNullValues []
  (swap! socialcore/app-state assoc-in [:selectedzone :diff] 0)
  (swap! socialcore/app-state assoc-in [:selectedzone :city] "" )
  (swap! socialcore/app-state assoc-in [:selectedzone :name ] "")
)

(defn setZone []
  (let [
        ;login (:selecteduser @socialcore/app-state)
        zone (first (filter (fn [zone] (if (= (:id (:selectedzone @socialcore/app-state)) (:id zone)) true false)) (:zones ( (keyword (str (:id (:selecteduser @socialcore/app-state)))) @socialcore/app-state) )))  ]

       ;(.log js/console "the zone")
       ;(.log js/console zone)

       (if (= zone nil)
         (setZoneNullValues)
         (let []
           (swap! socialcore/app-state assoc-in [:selectedzone :diff] (:diff zone))
           (swap! socialcore/app-state assoc-in [:selectedzone :name] (:name zone))
           (swap! socialcore/app-state assoc-in [:selectedzone :city] (:city zone))
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
  (swap! socialcore/app-state assoc-in [:selectedzone (keyword (.. e -nativeEvent -target -id))] (.. e -nativeEvent -target -value))
)



(defn onMount [data]
  ;(setTripNullValues)
  (swap! socialcore/app-state assoc-in [:current] "Zone Detail")
  (getZoneDetail)
  ;(put! ch 42)
  (put! ch 46)
)



(defcomponent zones-menu-view [data owner]
  (render [_]
    (let [
       elem (.getElementById js/document "btnzonesmenu")
      ;rect (.getBoundingClientRect (.getElementById js/document "btnzonesmenu"))
      l 337
      ;tr1 (.log js/console elem)
      ]
      (dom/div {:className "dropdown"}
        (dom/button {:className "btn btn-default dropdown-toggle" :type "button" :id "btnzonesmenu" :data-toggle "dropdown"} "Select"
          (dom/span {:className "caret"})
        )

        (dom/ul {:className "dropdown-menu" :role "menu" :aria-labelledby "btnzonesmenu" :style {:left (str l "px")}}
          (map (fn [x]
            (dom/li {:role "presentation"}
              (dom/a {:role "menuitem" :tabIndex "-1" :onClick (fn [e] (pickupzone (:id x)))} (:name x))
            )
          ) zones)
        )
      )
    )
  )
)

(defcomponent zonedetail-page-view [data owner]
  (did-mount [_]
    (onMount data)
  )
  (did-update [this prev-props prev-state]
    ;(.log js/console "Update happened") 
    (swap! socialcore/app-state assoc-in [:current] "Zone Detail")
    ;(put! ch 42)
  )
  (render
    [_]
    (let [style {:style {:margin "10px;" :padding-bottom "0px;"}}
      styleprimary {:style {:margin-top "70px"}}
      ]
      (dom/div
        (om/build socialcore/website-view data {})
        (dom/div {:className "panel panel-default" :style {:margin-top "65px"}}
          (dom/div {:className "panel-heading"}
            (dom/div {:className "panel-title"} 
              (dom/div {:className "row"}
                (dom/div {:className "col-md-1"}
                  "Name:"
                )
                (dom/div {:className "col-md-2"}
                  (dom/input {:id "name" :type "text" :style {:width "100%"} :value  (if (nil? (:name (:selectedzone @socialcore/app-state))) "" (:name (:selectedzone @socialcore/app-state))) :onChange (fn [e] (handleChange e)) }))
                (dom/div {:className "col-md-2"}
                  (omdom/select #js {:id "zoneslist"
                                   :className "selectpicker"
                                   :data-show-subtext "true"
                                   :data-live-search "true"
                                   :title "Pick up from the list"
                                   }                
                    (buildZonesList data owner)
                  )
                  ;(om/build zones-menu-view data {})
                )
              (if (< (count (:name (:selectedzone @socialcore/app-state))) 1)
                (dom/div {:className "col-md-2" :style {:color "red" :margin-top "5px"}}
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
                (dom/input {:id "city" :type "text" :style {:width "100%"} :value (if (nil? (:city (:selectedzone @socialcore/app-state))) "" (:city (:selectedzone @socialcore/app-state))) :onChange (fn [e] (handleChange e))})
              )
              (if (< (count (:city (:selectedzone @socialcore/app-state))) 1)
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
                (dom/input {:id "diff" :type "number" :step "0.5" :style {:width "100%"} :value (if (nil? (:diff (:selectedzone @socialcore/app-state))) 0 (:diff (:selectedzone @socialcore/app-state))) :onChange (fn [e] (handleChange e))})
              )
              (if (or (> (js/parseFloat (:diff (:selectedzone @socialcore/app-state))) 14) (< (js/parseFloat (:diff (:selectedzone @socialcore/app-state))) -12))
                (dom/div {:style {:color "red" :margin-top "5px"}}
                  "Difference to GMT should be in [-12 +14] range"
                )
              )
            )
          )
        )
        (dom/nav {:className "navbar navbar-default" :role "navigation"}
          (dom/div {:className "navbar-header"}
            (dom/button {:className "btn btn-default" :disabled (not (check-zone-valid) ) :onClick (fn [e] (if (= (:id (:selectedzone @socialcore/app-state)) 0) (createZone) (updateZone)) )} (if (= (:id (:selectedzone @socialcore/app-state)) 0) "Insert" "Update") )

            (dom/button {:className "btn btn-danger" :style {:visibility (if (= (:id (:selectedzone @socialcore/app-state)) 0) "hidden" "visible")} :onClick (fn [e] (deleteZone (:id (:selectedzone @socialcore/app-state))))} "Delete")

            (dom/button {:className "btn btn-info"  :onClick (fn [e]     (js/window.history.back))  } "Cancel")
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
    ;(.log js/console id)
    (swap! socialcore/app-state assoc-in [:selectedzone :id]  (js/parseInt id) ) 
    (om/root zonedetail-page-view
             socialcore/app-state
             {:target (. js/document (getElementById "app"))})
  )
)
