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


(defonce app-state (atom {:sort-list 0 :email "" :userspage 0 :nomoreusers false :selecteduser {} :search "" :users [] :view 1}))


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

(defn check-password-complexity [password]
  (let [
    
  ]
    (if (> (count password) 7) true false)
  )
)

(defn valid-email [email]
  (let [
    pattern #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"
   res (and (string? email) (re-matches pattern email))
  ]
    (if (nil? res) false true)
  )
)

(defn handleChange [e]
  (swap! app-state assoc-in [(keyword (.. e -nativeEvent -target -id))] (.. e -nativeEvent -target -value))
)


(defn map-zone [zone]
  {:id (get zone "id") :name (get zone "name") :city (get zone "city") :diff (get zone "diff")}
)

(defn OnGetZones [response]
  (swap! app-state assoc-in [(keyword (str (:id (:selecteduser @app-state)))) :zones] (map map-zone response))
  ;(aset js/window "location" "#/zones")
)


(defn reqzones []
  (GET (str settings/apipath "api/zone?id=" (:id (:selecteduser @app-state ))) {:handler OnGetZones
    :response-format :json
    :error-handler error-handler
    :headers {:Authorization (str "Bearer "  (:token  (:token @app-state))) }
    }
  )
)




(defn OnGetUsers [response]
  (let [
    ;tr1 (.log js/console (str "received " (count users) " users"))

    ;users (filter (fn [x] (if (> (count (filter (fn [y] (if (= (:id y) (:id x)) true false)) (:users @app-state))) 0) false true)) users)
    ]
    (doall (map (fn [user] (let [
      cnt (count (filter (fn [y] (if (= (:id y) (get user "id")) true false)) (:users @app-state)))
      ]
      (if (= cnt 0)
        (let []
          (swap! app-state assoc-in [:users] (conj (:users @app-state) {:role (get user "role")  :email (get user "email") :locked (get user "locked") :id (get user "id") :pic "" :password (get user "password") :source (get user "source") :confirmed (get user "confirmed")  :name (get user "name")}))
          ;(.log js/console (str "adding user " (nth user 3)))
        )
      ))) response)
    )
    (if (= (count (filter (fn [x] (if (= (:id x) (:id (:user @app-state))) true false)) (:users @app-state))) 0)
      (swap! app-state assoc-in [:users] (conj (:users @app-state) (:user @app-state)))
    )
    (if (< (count response) 5) 
      (swap! app-state assoc-in [:nomoreusers] true)
    )
  )
)


(defn load-users [page]
  (if (>= page 0)
    (swap! app-state update-in [:userspage] inc)
    (swap! app-state assoc-in [:nomoreusers] true)
  )
  
  (swap! app-state assoc :state 1 )
  (GET (str settings/apipath "api/users?page=" page) {
    :handler OnGetUsers
    :response-format :json
    :error-handler error-handler
    :headers {:content-type "application/json" :Authorization (str "Bearer "  (:token  (:token @app-state))) }
  })
)


(defn OnGetUser [response]
  (let [
    
    ]
    ;; (swap! app-state assoc-in [:user :id] (nth response 0) )
    ;; (swap! app-state assoc-in [:user :email] (nth response 1))
    ;; (swap! app-state assoc-in [:user :role] (nth response 2) )
    ;; (swap! app-state assoc-in [:user :locked] (nth response 3))
    ;; (swap! app-state assoc-in [:user :pic] (nth response 4))
    ;; (swap! app-state assoc-in [:user :password] (nth response 5))
    ;; (swap! app-state assoc-in [:user :confirmed] (nth response 6))
    ;; (swap! app-state assoc-in [:user :source] (nth response 7))
    ;; (swap! app-state assoc-in [:user :name] (nth response 8))
    (swap! app-state assoc-in [:selecteduser :id] (:id (:token @app-state)))
  ;;(.log js/console (nth theUser 0))
  ;;(.log js/console (:login (:user @tripcore/app-state) ))
    ;(load-users (:userspage @app-state))
  )
)

(defn get-user [id]
  (GET (str settings/apipath "api/user?id=" id) {
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

;; (defn getZones [] 
;;   (GET (str settings/apipath "api/zone?id=" (:id (:selecteduser @app-state)) ) {
;;     :handler OnGetZones
;;     :error-handler error-handler
;;     :headers {
;;       :content-type "application/json"
;;       :Authorization (str "Bearer "  (:token (:token @app-state))) }
;;   })
;; )

(defn onDropDownChange [id value]
  (swap! app-state assoc-in [:selecteduser :id] value)

  
  (if (nil? (:zones ((keyword value) @app-state)))
    (reqzones)
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
      (dom/option {:key (:id text) :data-subtext (:email text) :value (:id text)
                    :onChange #(handle-change % owner)} (str (:name text) (if (or (= (:source text) "site") (= (:source text) "")) "" (str " (" (:source text) ")"))) )
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
            (dom/li {:style {:display (if (or (= (:current @data) "Zones") (= (:current @data) "Users")) "block" "none")}}
              (dom/input {:id "search" :type "text" :placeholder "search" :style {:height "32px" :margin-top "1px"} :value  (:search @app-state) :onChange (fn [e] (handleChange e )) })
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
            (if (or (= (:role (:user @app-state)) "admin") (= (:role (:user @app-state)) "manager"))
              (dom/li
                (dom/a (assoc style :href "#/users")
                  (dom/span {:className "glyphicon glyphicon-log-out"})
                  "Users"
                )
              )
            )
            (dom/li
              (dom/a (assoc style :href (str "#/userdetail/" (:id (:user @app-state))) )
                (dom/span {:className "glyphicon glyphicon-user"})
                "Profile"
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
         (.selectpicker "val" (:id (:selecteduser @app-state))
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
