(ns socialweb.userdetail  (:use [net.unit8.tower :only [t]])
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
            [om.dom :as omdom :include-macros true]
            [cljs.core.async :refer [put! dropping-buffer chan take! <!]]
            [om-bootstrap.input :as i]
            [cljs-time.core :as tm]
            [cljs-time.format :as tf]
            [socialweb.settings :as settings]
  )
  (:import goog.History)
)

(def jquery (js* "$"))

(enable-console-print!)

(def ch (chan (dropping-buffer 2)))
(defonce app-state (atom  {:email "" :name "" :locked false :password "" :roles [{:name "admin"} {:name "manager"} {:name "user"}] :isinsert false :role "admin" :view 1 :current "User Detail"} ))

(defn handleChange [e]
  ;(.log js/console e  )  
  ;(.log js/console "The change ....")
  (swap! app-state assoc-in [(keyword (.. e -nativeEvent -target -id))] (.. e -nativeEvent -target -value))
)


(defn OnDeleteUserError [response]
  (let [     
      newdata {:id (get response (keyword "id") ) }
    ]

  )
  ;; TO-DO: Delete User from Core
  ;;(.log js/console (str  (get (first response)  "Title") ))
)


(defn OnDeleteUserSuccess [response]
  (let [
      users (:users @socialcore/app-state    )  
      newusers (remove (fn [user] (if (= (:id user) (:id @app-state) ) true false  )) users)
    ]
    (swap! socialcore/app-state assoc-in [:users] newusers)
  )
  (js/window.history.back)
)

(defn OnUpdateUserError [response]
  (let [     
      newdata {:id (get response (keyword "id") ) }
    ]

  )
  ;; TO-DO: Delete User from Core
  ;;(.log js/console (str  (get (first response)  "Title") ))
)


(defn OnUpdateUserSuccess [response]
  (let [
      users (:users @socialcore/app-state)
      deluser (remove (fn [user] (if (= (:id user) (:id @app-state) ) true false  )) users)
      adduser (into [] (conj deluser {:id (:id @app-state) :locked (:locked @app-state) :email (:email @app-state) :password (:password @app-state) :role (:role @app-state) :pic (.-src (.getElementById js/document "userpic")) :name (:name @app-state) :confirmed (:confirmed @app-state) :source (:source @app-state)})) 
    ]
    (swap! socialcore/app-state assoc-in [:users] adduser)
    (js/window.history.back)
  )
)


(defn deleteUser [id]
  (DELETE (str settings/apipath  "api/user?id=" id) {
    :handler OnDeleteUserSuccess
    :error-handler OnDeleteUserError
    :headers {
      :content-type "application/json" 
      :Authorization (str "Bearer "  (:token (:token @socialcore/app-state)))}
    :format :json})
)



(defn updateUser []
  (PUT (str settings/apipath  "api/user") {
    :handler OnUpdateUserSuccess
    :error-handler OnUpdateUserError
    :headers {
      :content-type "application/json" 
      :Authorization (str "Bearer "  (:token (:token @socialcore/app-state)))}
    :format :json
    :params {:name (if (nil? (:name @app-state)) "" (:name @app-state)) :email (:email @app-state) :password (:password @app-state) :role (:role @app-state) :locked (:locked @app-state) :pic (:pic @app-state) :id (:id @app-state)}})
)


(defn OnCreateUserError [response]
  (let [     
      newdata {:id (get response (keyword "id") ) }
    ]

  )
  ;; TO-DO: Delete User from Core
  ;;(.log js/console (str  (get (first response)  "Title") ))
)


(defn OnCreateUserSuccess [response]
  (let [
      users (:users @socialcore/app-state    )  
      adduser (into [] (conj users {:login (:login @app-state) :password (:password @app-state) :role (:role @app-state)} )) 
    ]
    (swap! socialcore/app-state assoc-in [:users] adduser)
    (js/window.history.back)
  )
)

(defn createUser []
  (POST (str settings/apipath  "api/user") {
    :handler OnCreateUserSuccess
    :error-handler OnCreateUserError
    :headers {
      :content-type "application/json" 
      :Authorization (str "Bearer "  (:token (:token @socialcore/app-state)))}
    :format :json
    :params { :name (:name @app-state) :email (:email @app-state) :password (:password @app-state) :role (:role @app-state) :locked (:locked @app-state) :pic (:pic @app-state)}})
)


(defn onDropDownChange [id value]
  ;(.log js/console () e)
  (swap! app-state assoc-in [:role] value) 
)


(defn setRolesDropDown []
  (jquery
     (fn []
       (-> (jquery "#roles" )
         (.selectpicker {})
       )
     )
   )
   (jquery
     (fn []
       (-> (jquery "#roles" )
         (.selectpicker "val" (:role @app-state))
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


(defn setNewUserValue [key val]
  (swap! app-state assoc-in [(keyword key)] val)
)

(defn setcheckboxtoggle []
  (let []

    (jquery
      (fn []
         (-> (jquery (str "#chckblock"))
           (.bootstrapToggle (clj->js {:on "Locked" :off "Unlocked"}))
         )
      )
    )

    (jquery
      (fn []
         (-> (jquery (str "#chckconfr"))
           (.bootstrapToggle (clj->js {:on "Confirmed" :off "Not confirmed"}))
         )
      )
    )

    (jquery
      (fn []
        (-> (jquery (str "#chckblock"))
          (.on "change"
            (fn [e]
              (let [
                  islocked (.. e -currentTarget -checked)

                  ;tr1 (.log js/console "gg")
                ]
                (.stopPropagation e)
                ;(.stopImmediatePropagation (.. e -nativeEvent) )
                (swap! app-state assoc-in [:locked] islocked)
              )
            )
          )
        )
      )
    )
  )
)


(defn setcontrols [value]
  (case value
    46 (setRolesDropDown)
    47 (setcheckboxtoggle)
  )
)


(defn initqueue []
  (doseq [n (range 1000)]
    (go ;(while true)
      (take! ch(
        fn [v] (
           ;.log js/console v
           ;(setcalculatedfields) 
           setcontrols v
           
           ;(.log js/console v)  
          )
        )
      )
    )
  )
)


(initqueue)

(defn array-to-string [element]
  (let [
      newdata {:empname (get element "empname") } 
    ]
    (:empname newdata)
  )
)

(defn setUser []
  (let [
        users (:users @socialcore/app-state)
        user (first (filter (fn [user] (if (= (:id @app-state) (:id user))  true false)) (:users @socialcore/app-state )))
        ]
    (swap! app-state assoc-in [:id]  (:id user) ) 
    (swap! app-state assoc-in [:role]  (:role user) ) 
    (swap! app-state assoc-in [:name]  (:name user) ) 
    (swap! app-state assoc-in [:password] (:password user))
    (swap! app-state assoc-in [:confirmed] (:confirmed user))
    (swap! app-state assoc-in [:email] (:email user) )
    (swap! app-state assoc-in [:pic] (:pic user) )
    (swap! app-state assoc-in [:locked] (:locked user) )
  )
)




(defn OnError [response]
  (let [     
      newdata { :error (get (:response response)  "error") }
    ]
    ;(.log js/console (str  response )) 
    
  )
  
  
)


(defn getUserDetail []
  ;(.log js/console (str "token: " " " (:token  (first (:token @t5pcore/app-state)))       ))
  (if
    (and 
      (not= (:id @app-state) nil)
      (not= (:id @app-state) "")
    )
    (setUser)
  )
)

(defn handleFromChange [e]
  ;;(.log js/console e  )  
  (.log js/console "The change ....")

)


(defn onMount [data]
  (swap! app-state assoc-in [:current] 
    "User Detail"
  )
  (getUserDetail)
  (put! ch 46)
  (put! ch 47)
)


(defn handle-change [e owner]
  ;(.log js/console () e)
  (swap! app-state assoc-in [:form (keyword (.. e -target -id))] 
    (.. e -target -value)
  ) 
)


(defn buildRolesList [data owner]
  (map
    (fn [text]
      (dom/option {:key (:name text) :value (:name text)
                    :onChange #(handle-change % owner)} (:name text))
    )
    (:roles @app-state )
  )
)

(def reader (js/FileReader.))

(defn readurl [input]
  (if (not (nil? (.-files input)))
    (let [
        
        
      ]
      (set! (.-onload reader) (fn [e] 
        ;(.log js/console (.-result (.-target e)))
        (swap! app-state assoc-in [:pic] (.-result (.-target e)))
      ))
      (.readAsDataURL reader (aget (.-files input) 0))
    )
  )
)


(defcomponent userdetail-page-view [data owner]
  (did-mount [_]
    (onMount data)
  )
  (did-update [this prev-props prev-state]
    (.log js/console "Update happened") 

    ;(put! ch 46)
  )
  (render
    [_]
    (let [style {:style {:margin "10px;" :padding-bottom "0px;"}}
      styleprimary {:style {:margin-top "70px"}}
      ]
      (dom/div
        (om/build socialcore/website-view data {})
        (dom/div {:id "user-detail-container"}
          (dom/span
            (dom/div  (assoc styleprimary  :className "panel panel-default"  :id "divUserInfo")
              
              (dom/div {:className "panel-heading"}
                (dom/h5 "name: "
                  (dom/input {:id "name" :type "text" :onChange (fn [e] (handleChange e)) :value (:name @app-state)})
                )

                (dom/h5 "email: "
                  (dom/input {:id "email" :type "email" :onChange (fn [e] (handleChange e)) :value (:email @app-state)})
                )

                (dom/h5 "Password: "
                  (dom/input {:id "password" :type "password" :onChange (fn [e] (handleChange e)) :value (:password @app-state)})
                )


                (dom/div {:className "form-group"}
                  (dom/p
                    (dom/label {:className "control-label" :for "roles" }
                      "Role: "
                    )
                  
                  )
                 
                  (omdom/select #js {:id "roles"
                                     :className "selectpicker"
                                     :data-show-subtext "true"
                                     :data-live-search "true"
                                     :onChange #(handle-change % owner)
                                     }                
                    (buildRolesList data owner)
                  )
                  
                )

                (dom/div {:className "row"}
                  (dom/label {:className "checkbox-inline"}
                    (dom/input {:id (str "chckblock") :type "checkbox" :checked (:locked @data) :data-toggle "toggle" :data-size "large" :data-width "100" :data-height "26"})
                  )
                )

                (dom/div {:className "row" :style {:margin-top "5px"}}
                  (dom/label {:className "checkbox-inline"}
                    (dom/input {:id (str "chckconfr") :disabled true :type "checkbox" :checked (:confirmed @data) :data-toggle "toggle" :data-size "large" :data-width "200" :data-height "26"})
                  )
                )

                (dom/h5 "Picture: "
                  ;(dom/input {:type "file" :onChange (fn [e] (js/readURL (.. e -target))) :name "file"})
                  (dom/input {:type "file" :onChange (fn [e] (readurl (.. e -target))) :name "file"})
                  (dom/img {:id "userpic" :style {:display (if (or (nil? (:pic @app-state)) (= "" (:pic @app-state))) "none" "block") :max-width "200px" :max-height "200px"} :src (:pic @app-state) :alt "User image"})
                )
              )              
            )
          )
        )
        (dom/nav {:className "navbar navbar-default" :role "navigation"}
          (dom/div {:className "navbar-header"}
            (b/button {:className "btn btn-default" :onClick (fn [e] (if (= (:isinsert @app-state) true) (createUser) (updateUser)) )} (if (= (:isinsert @app-state) true) "Insert" "Update"))
            (b/button {:className "btn btn-danger" :style {:visibility (if (= (:isinsert @app-state) true) "hidden" "visible")} :onClick (fn [e] (deleteUser (:id @app-state)))} "Delete")

            (b/button {:className "btn btn-info"  :onClick (fn [e] (-> js/document
      .-location
      (set! "#/users")))  } "Cancel")
          )
        )
      )
    )

  )
)


(sec/defroute userdetail-page "/userdetail/:id" {id :id}
  (let [
    user (first (filter (fn [x] (if (= (js/parseInt id) (:id x)) true false)) (:users @socialcore/app-state)))
    ;tr1 (.log js/console (str "id=" id "; user=" user))
    ]
    (swap! app-state assoc-in [:id]  (:id user))
    (swap! app-state assoc-in [:isinsert]  false )
    (om/root userdetail-page-view
             app-state
             {:target (. js/document (getElementById "app"))})

  )
)


(sec/defroute newuserdetail-new-page "/userdetail" {}
  (
    (swap! app-state assoc-in [:login]  "" ) 
    (swap! app-state assoc-in [:isinsert]  true )
 
    (swap! app-state assoc-in [:role ]  "user" ) 
    (swap! app-state assoc-in [:password] "" )
    (swap! app-state assoc-in [:name] "" )
    (swap! app-state assoc-in [:email] "" )
    (swap! app-state assoc-in [:id] nil )
    (swap! app-state assoc-in [:pic] "" )
    (swap! app-state assoc-in [:locked] false )


    (om/root userdetail-page-view
             app-state
             {:target (. js/document (getElementById "app"))})

  )
)
