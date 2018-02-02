(ns socialweb.core
  (:use [net.unit8.tower :only [t]])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [ajax.core :refer [GET PUT POST]]
            [om.dom :as omdom :include-macros true]
            [cljs-time.core :as tc]
            [cljs-time.format :as tf]
            [cljs-time.coerce :as te]
            [cljs-time.local :as tl]
            [cljs.core.async :refer [put! dropping-buffer chan take! <! >! timeout close!]]
            [om-bootstrap.button :as b]
            [socialweb.settings :as settings]
            [clojure.string :as str]
            [goog.string :as gstring]
            [goog.string.format]
  )
  (:import goog.History)
)

(enable-console-print!)

(def ch (chan (dropping-buffer 2)))


(defonce app-state (atom {:selecteduser "" :search "" :users [] :view 1}))


;{:id 1 :name "Masha" :email "masha@gmail.com"} {:id 2 :name "Petya" :email "petya@gmail.com"} {:id 3 :name "Sanya" :email "sanya@gmail.com"}
;:beeper {:zones [{:id 1 :name "Russia" :city "Moscow" :diff 3} {:id 2 :name "Russia West" :city "Kaliningrad" :diff 1} {:id 3 :name "Russia East" :city "Vladivostok" :diff 12}]}

(def jquery (js* "$"))

(defn setVersionInfo [info]
  (swap! app-state assoc-in [:verinfo] 
    (:info info)
  )

  (swap! app-state assoc-in [:versionTitle]
    (str "Информация о текущей версии")
  ) 

  (swap! app-state assoc-in [:versionText]
    (str (:info info))
  ) 


  ;;(.log js/console (str  "In setLoginError" (:error error) ))
  (jquery
    (fn []
      (-> (jquery "#versioninfoModal")
        (.modal)
      )
    )
  )
)

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text))
)


(defn handleChange [e]
  (swap! app-state assoc-in [(keyword (.. e -nativeEvent -target -id))] (.. e -nativeEvent -target -value))
)


(defn map-zone [zone]
  {:id (get zone 0) :name (get zone 1) :city (get zone 2) :diff (get zone 3)}
)

(defn OnGetZones [response]
  (swap! app-state assoc-in [(keyword (str (:id (:user @app-state)))) :zones] (map map-zone response))
  (set! (.-display (.-style (.getElementById js/document "socialbuttons"))) "none")
  (aset js/window "location" "#/users")
)


(defn reqzones []
  (GET (str settings/apipath "api/zone?id=" (:id (:user @app-state ))) {:handler OnGetZones
      :error-handler error-handler
      :headers {:content-type "application/json" :Authorization (str "Bearer "  (:token  (:token @app-state))) }
    }
  )
)


(defn setUser [theUser]
  (let [cnt (count (:users @app-state))]
    (swap! app-state assoc-in [:users cnt] {:role (nth theUser 1)  :email (nth theUser 0) :locked (nth theUser 2) :id (nth theUser 3) :pic (nth theUser 4) :password (nth theUser 5) :source (nth theUser 6) :confirmed (nth theUser 7)  :name (nth theUser 8)})
  )
  

  ;;(.log js/console (nth theUser 0))
  ;;(.log js/console (:login (:user @tripcore/app-state) ))
  (if (= (nth theUser 0) (:email (:user @app-state) ))
    (let []
      (swap! app-state assoc-in [:user :role] (nth theUser 1) )
      (swap! app-state assoc-in [:user :id] (nth theUser 3) )
      (swap! app-state assoc-in [:user :email] (nth theUser 0))
      (swap! app-state assoc-in [:user :locked] (nth theUser 2))
      (swap! app-state assoc-in [:selecteduser] (nth theUser 3))
    )
  )
)


(defn OnGetUser [response]
  (let [
    
    ]
    (doall (map setUser response))
    (reqzones)
  )
)


(defn load-users [page]
  (GET (str settings/apipath "api/users?page=" page) {
    :handler OnGetUser
    :error-handler error-handler
    :headers {:content-type "application/json" :Authorization (str "Bearer "  (:token  (:token @app-state))) }
  })
)


(defn onVersionInfo []
  (let [     
      newdata { :info "Global Asset Management System пользовательский интерфейс обновлен 01.09.2017 09:28" }
    ]
   
    (setVersionInfo newdata)
  )
  ;(.log js/console (str  response ))
)

(defn addVersionInfo []
  (dom/div
    (dom/div {:id "versioninfoModal" :className "modal fade" :role "dialog"}
      (dom/div {:className "modal-dialog"} 
        ;;Modal content
        (dom/div {:className "modal-content"} 
          (dom/div {:className "modal-header"} 
                   (b/button {:type "button" :className "close" :data-dismiss "modal"})
                   (dom/h4 {:className "modal-title"} (:versionTitle @app-state) )
                   )
          (dom/div {:className "modal-body"}
                   (dom/p (:versionText @app-state))
                   )
          (dom/div {:className "modal-footer"}
                   (b/button {:type "button" :className "btn btn-default" :data-dismiss "modal"} "Close")
          )
        )
      )
    )
  )
)

(defn doswaps []
  (let [a (rand-int 26)
        b (rand-int 26)
        c (rand-int 26)
    ]
    (swap! app-state assoc-in [:fake] (str a b c))
  )
)

(defn split-thousands [n-str]
  (let [index (str/index-of n-str ".")
        lstr (subs n-str 0 (if (nil? index) (count n-str) index))
        rstr (if (nil? index) "" (subs n-str index)) 
        splitstr (->> lstr
          reverse
          (partition 3 3 [])
          (map reverse)
          reverse
          (map #(apply str %))
          (str/join " "))
    ]
    (str splitstr rstr)
  )
)


 
(defcomponent logout-view [_ _]
  (render
    [_]
    (let [style {:style {:margin "10px"}}]
      (dom/div style (dom/a (assoc style :href "#/login") "Login"))
    )
  )
)


(defn handle-chkb-change [e]
  ;(.log js/console (.. e -target -id) )  
  ;(.log js/console "The change ....")
  (.stopPropagation e)
  (.stopImmediatePropagation (.. e -nativeEvent) )
  (swap! app-state assoc-in [(keyword  (.. e -currentTarget -id) )] 
    (if (= true (.. e -currentTarget -checked)  ) 1 0)
  )
  ;(CheckCalcLeave)
  ;(set! (.-checked (.. e -currentTarget)) false)
  ;(dominalib/remove-attr!  (.. e -currentTarget) :checked)
  ;;(dominalib/set-attr!  (.. e -currentTarget) :checked true)
)

(defn handle-change [e owner]
  
  (swap! app-state assoc-in [:form (keyword (.. e -target -id))] 
    (.. e -target -value)
  ) 
)

(defn handle-change-currency [e owner]
  
  (swap! app-state assoc-in [:form (keyword (.. e -target -id))] 
    (.. e -target -value)
  ) 
)



(defn onDidUpdate [data]
  ;(setClientsDropDown)
    ;; (jquery
    ;;   (fn []
    ;;     (-> (jquery "#side-menu")
    ;;       (.metisMenu)
    ;;     )
    ;;   )
    ;; )

)

(defn onMount [data]
  ;(.log js/console "Mount core happened")

  (if (or (:isalert @app-state) (:isnotification @app-state))
    (go
         (<! (timeout 500))
         (put! ch 42)
    )
  )
)



(defn handleCheck [e]
  (.stopPropagation e)
  (.stopImmediatePropagation (.. e -nativeEvent) )
  (swap! app-state assoc-in [(keyword (.. e -nativeEvent -target -id))] (.. e -nativeEvent -target -checked))
)

(defn printMonth []
  (.print js/window)
)

;; (defn map-zone [zone]
;;   {:id (get zone 0) :name (get zone 1) :city (get zone 2) :diff (get zone 3)}
;; )

;; (defn OnGetZones [response]
;;    (swap! app-state assoc-in [(keyword (:selecteduser @app-state)) :zones] (map map-zone response))
;; )

;; (defn error-handler [{:keys [status status-text]}]
;;   (.log js/console (str "something bad happened: " status " " status-text))
;; )

(defn getZones [] 
  (GET (str settings/apipath "api/zone?id=" (:selecteduser @app-state) ) {
    :handler OnGetZones
    :error-handler error-handler
    :headers {
      :content-type "application/json"
      :Authorization (str "Bearer "  (:token (:token @app-state))) }
  })
)

(defn onDropDownChange [id value]
  (swap! app-state assoc-in [:selecteduser] value)

  
  (if (nil? (:zones ((keyword value) @app-state)))
    (getZones)
  )
)

(defn comp-users
  [user1 user2]
  (if (> (compare (:email user1) (:email user2)) 0) 
      false
      true
  )
)


(defn buildUsersList [data owner]
  (map
    (fn [text]
      (dom/option {:key (:id text) :value (:id text)
                    :onChange #(handle-change % owner)} (:email text))
    )
    (sort (comp comp-users) (:users @app-state )) 
  )
)

(defcomponent main-navigation-view [data owner]
  (render [_]
    (let [style {:style {:margin "10px" :padding-bottom "0px"}}
      stylehome {:style {:margin-top "10px"} }
      ]
      (dom/nav {:className "navbar navbar-default navbar-fixed-top" :role "navigation"}
        (dom/div {:className "navbar-header"}
          (dom/button {:type "button" :className "navbar-toggle"
            :data-toggle "collapse" :data-target ".navbar-ex1-collapse"}
            (dom/span {:className "sr-only"} "Toggle navigation")
            (dom/span {:className "icon-bar"})
            (dom/span {:className "icon-bar"})
            (dom/span {:className "icon-bar"})
          )
          (dom/a  (assoc stylehome :className "navbar-brand")
            (dom/span {:id "pageTitle"} (:current @data))
          )
        )
        (dom/div {:className "collapse navbar-collapse navbar-ex1-collapse" :id "menu"}
          (dom/ul {:className "nav navbar-nav" :style {:padding-top "17px"}}
            (dom/li {:style {:visibility (if (= (:current @app-state) "Zones") "visible" "hidden")}}
              ;; (dom/a (assoc style :href "#/eportal")
              ;;   (dom/span {:className "glyphicon glyphicon-home"})
              ;;     "Trips block"
              ;;   )
              (dom/div {:style {:margin-right "10px" :visibility (if (and (= (:current @app-state) "Zones") (or (= (:role (:user @app-state)) "admin") (= (:role (:user @app-state)) "admin")) ) "visible" "hidden")}}
                (omdom/select #js {:id "users"
                                   :className "selectpicker"
                                   :data-show-subtext "true"
                                   :data-live-search "true"
                                   :onChange #(handle-change % owner)
                                   }                
                  (buildUsersList data owner)
                )
              )
            )
            (dom/li {:style {:display (if (or (= (:current @app-state) "Zones") (= (:current @app-state) "Users")) "block" "none")}}
              (dom/h5 {:style {:margin-left "5px" :margin-right "5px" :height "32px" :margin-top "1px"}} " "
      (dom/input {:id "search" :type "text" :placeholder "Search" :style {:height "32px" :margin-top "1px"} :value  (:search @app-state) :onChange (fn [e] (handleChange e )) })  )
            )
            (dom/li {:style {:margin-left "5px" :display (if (or (= (:current @app-state) "Zones") (= (:current @app-state) "Zones")) "block" "none")}}
              (b/button {:className "btn btn-info"  :onClick (fn [e] (printMonth))  } "Print zones")
            )
          )
         
          (dom/ul {:className "nav navbar-nav navbar-right"}
            (dom/li
              (dom/a (assoc style :href "#/zones")
                 (dom/span {:className "glyphicon glyphicon-cog"})
                 "Zones"
              )
            )         
            (dom/li {:style {:visibility 
                                           (if 
                                             (or (= (:role (:user @app-state)) "admin")
                                                 (= (:role (:user @app-state)) "manager")) "visible" "hidden")}}

              (dom/a (assoc style :href "#/users") 
                (dom/span {:className "glyphicon glyphicon-log-out"})
                "Users"
              )
            )
            (dom/li
              (dom/a (assoc style :href "#/login" :onClick (fn [e]   (set! (.-display (.-style (.getElementById js/document "socialbuttons"))) "block") ;(set! (.-display (.-style (aget (.getElementsByClassName js/document "g-signin2") 0))) "block")
                ))
                (dom/i {:className "fa fa-sign-out fa-fw"})
                "Exit"
              )
            )
          )
        )
      )
    )
  )
)

(defn setUsersDropDown []
  (jquery
     (fn []
       (-> (jquery "#users" )
         (.selectpicker {})
       )
     )
   )
   (jquery
     (fn []
       (-> (jquery "#users" )
         (.selectpicker "val" (:selecteduser @app-state)
                          )
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

(defmulti website-view
  (
    fn [data _]   
      (:view (if (= data nil) @app-state @data ))
      ;;(:view @app-state )
  )
)

(defmethod website-view 0
  [data owner] 
  ;(.log js/console "zero found in view")
  (logout-view data owner)
)



(defmethod website-view 1
  [data owner] 
  ;(.log js/console "Two is found in view")
  (main-navigation-view data owner)
)

(defmethod website-view 2
  [data owner] 
  ;(.log js/console "Two is found in view")
  (main-navigation-view data owner)
)


(defn setcontrols [value]
  
)

(defn initqueue []
  (doseq [n (range 1000)]
    (go ;(while true)
      (take! ch(
        fn [v] (
           setcontrols v
          )
        )
      )
    )
  )
)

(initqueue)
