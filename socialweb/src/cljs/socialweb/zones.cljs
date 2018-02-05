(ns socialweb.zones (:use [net.unit8.tower :only [t]])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [socialweb.core :as socialcore]
            [ajax.core :refer [GET POST]]
            [socialweb.settings :as settings]
            [cljs-time.format :as tf]
            [cljs-time.coerce :as te]
            [cljs-time.core :as tc]
            [clojure.string :as str]
            [cljs.core.async :refer [put! dropping-buffer chan take! <! timeout]]
  )
  (:import goog.History)
)

(enable-console-print!)

(def ch (chan (dropping-buffer 2)))
(defonce app-state (atom  {:users [] :zones [] }))
(def custom-formatter1 (tf/formatter "dd/MM/yyyy HH:mm:ss"))


(defn OnGetUsers [response]
   (swap! app-state assoc :users  (get response "Users")  )
   (.log js/console (:users @app-state)) 

)

(defn updatepage []
  (go
    (<! (timeout 1000))
    (let [a (rand-int 26)
          b (rand-int 26)
          c (rand-int 26)
      ]
      ;(.log js/console "update fakse")
      (swap! socialcore/app-state assoc-in [:fake] (str a b c))
      (updatepage)
    )
  )
)

(defn comp-zones
  [zone1 zone2]
  (if (> (compare (:name zone1) (:name zone2)) 0)
      false
      true
  )
)


(defcomponent showzones-view [data owner]
  (render
    [_]
    (dom/div {:className "panel-body" :style {:display "block"}}
      (map (fn [item]
        (dom/div {:className "row tablerow"}
          (dom/div {:className "col-md-3"}
            (dom/a {:href (str  "#/zonedetail/" (:id item) )}
              (:name item)
            )
          )

          (dom/div {:className "col-md-3"}
            (dom/a {:href (str  "#/zonedetail/" (:id item) )}
              (:city item)
            )
          )

          (dom/div {:className "col-md-3"}
            (dom/a {:href (str  "#/zonedetail/" (:id item) )}
               (:diff item)
            )
          )

          (dom/div {:className "col-md-3"}
            (dom/a {:href (str  "#/zonedetail/" (:id item) )}
              (tf/unparse custom-formatter1 (te/from-long (+ (te/to-long (tc/now)) (* 1000 60 60 (:diff item)))))
            )
          )
        )                  
        ) (sort (comp comp-zones) (filter (fn [x] (if (or (> (.indexOf (str/lower-case (:name x)) (str/lower-case (:search @socialcore/app-state))) -1) (> (.indexOf (str/lower-case (:city x)) (str/lower-case (:search @socialcore/app-state))) -1)) true false)) (:zones ((keyword (str (:id (:selecteduser @data)))) @data))))
      )
    )
  )
)

(defn onMount [data]
  (swap! socialcore/app-state assoc-in [:current] "Zones")
  (if (= (count (:zones ((keyword (str (:id (:selecteduser @data)))) @data))) 0)
    (socialcore/reqzones)
  )
  (put! ch 42)
)

(defn setcontrols []
  (socialcore/setUsersDropDown)
  (updatepage)
)


(defn initqueue []
  (doseq [n (range 1000)]
    (go ;(while true)
      (take! ch(
        fn [v] (
           setcontrols
          )
        )
      )
    )
  )
)

(initqueue)


(defcomponent zones-view [data owner]
  (will-mount [_]
    (onMount data)
  )
  (render [_]
    (let [style {:style {:margin "10px" :padding-bottom "0px"}}
      styleprimary {:style {:margin-top "70px"}}
      ]
      (dom/div
        (om/build socialcore/website-view socialcore/app-state {})
        (dom/div { :style {:margin-top "70px" :margin-left "5px"}}
          (dom/button {:className "btn btn-primary no-print" :onClick (fn [e] (-> js/document
        .-location
        (set! "#/zonedetail/0")))} "Add New")
        )
        (dom/div {:className "panel panel-primary" :style {:margin-left "5px"}}
          (dom/div {:className "panel-heading"}
            (dom/div {:className "row"}
              (dom/div {:className "col-md-3"}
                "Name"
              )
              (dom/div {:className "col-md-3"}
                "City"
              )
              (dom/div {:className "col-md-3"}
                "Difference"
              )

              (dom/div {:className "col-md-3"}
                "Local Time"
              )
            )
          )
          (om/build showzones-view data {})
        )
      )
    )
  )
)




(sec/defroute zones-page "/zones" []
  (swap! socialcore/app-state assoc-in [:view] 2)
  (om/root zones-view
           socialcore/app-state
           {:target (. js/document (getElementById "app"))}))
