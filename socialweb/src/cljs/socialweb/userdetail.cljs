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

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text))
)


(defn handleChange [e]
  ;(.log js/console e  )  
  ;(.log js/console "The change ....")
  (swap! socialcore/app-state assoc-in [:selecteduser (keyword (.. e -nativeEvent -target -id))] (.. e -nativeEvent -target -value))
)

(defn check-user-validity []
  (let [
    cnt (count (filter (fn [x] (if (and (= (:email x) (:email (:selecteduser @socialcore/app-state))) (not= (:id x) (:id (:selecteduser @socialcore/app-state)))) true false)) (:users @socialcore/app-state)))
    ]
    (if (or  (> cnt 0) (< (count (:name (:selecteduser @socialcore/app-state))) 1) (< (count (:email (:selecteduser @socialcore/app-state))) 1) (< (count (:password (:selecteduser @socialcore/app-state))) 8) (< (count (:role (:selecteduser @socialcore/app-state))) 1)) false true)
  )
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
      newusers (remove (fn [user] (if (= (:id user) (:id (:selecteduser @socialcore/app-state)) ) true false  )) users)
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

(defn update-users []
  (let [
      users (:users @socialcore/app-state)
      deluser (remove (fn [user] (if (= (:id user) (:id (:selecteduser @socialcore/app-state)) ) true false  )) users)
      adduser (into [] (conj deluser {:id (:id (:selecteduser @socialcore/app-state)) :locked (:locked (:selecteduser @socialcore/app-state)) :email (:email (:selecteduser @socialcore/app-state)) :password (:password (:selecteduser @socialcore/app-state)) :role (:role (:selecteduser @socialcore/app-state)) :pic (.-src (.getElementById js/document "userpic")) :name (:name (:selecteduser @socialcore/app-state)) :confirmed (:confirmed (:selecteduser @socialcore/app-state)) :source (:source (:selecteduser @socialcore/app-state))})) 
    ]
    (swap! socialcore/app-state assoc-in [:users] adduser)
    (js/window.history.back)
  )
)

(defn showmessage []
  (let []

    ;;(.log js/console (str  "In setLoginError" (:error error) ))
    (jquery
      (fn []
        (-> (jquery "#infomodal")
          (.modal)
        )
      )
    )
  )
)


(defn OnUpdateUserSuccess [response]
  (let [
      result (get response "result")
    ]
    (if (= "success" result)
      (update-users)
      (let [error (get response "error")]
        (swap! socialcore/app-state assoc-in [:modalTitle] 
          (str "Error update user")
        ) 

        (swap! socialcore/app-state assoc-in [:modalText] error)
        (showmessage)
      )
    )
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
      :Authorization (str "Bearer "  (:token (:token @socialcore/app-state)))}
    :format :json
    :response-format :json
    :params {:name (if (nil? (:name (:selecteduser @socialcore/app-state))) "" (:name (:selecteduser @socialcore/app-state))) :email (:email (:selecteduser @socialcore/app-state)) :password (:password (:selecteduser @socialcore/app-state)) :role (:role (:selecteduser @socialcore/app-state)) :locked (:locked (:selecteduser @socialcore/app-state)) :pic (:pic (:selecteduser @socialcore/app-state)) :id (:id (:selecteduser @socialcore/app-state))}})
)


(defn OnCreateUserError [response]
  (let [     
      newdata {:id (get response (keyword "id") ) }
    ]

  )
  ;; TO-DO: Delete User from Core
  ;;(.log js/console (str  (get (first response)  "Title") ))
)


(defn create-user [id]
  (let [
      users (:users @socialcore/app-state)
      adduser (into [] (conj users {:id id :name (:name (:selecteduser @socialcore/app-state)) :email (:email (:selecteduser @socialcore/app-state)) :locked (:locked (:selecteduser @socialcore/app-state)) :confirmed (:confirmed (:selecteduser @socialcore/app-state)) :source "site" :pic (.-src (.getElementById js/document "userpic")) :password (:password (:selecteduser @socialcore/app-state)) :role (:role (:selecteduser @socialcore/app-state))} )) 
    ]
    (swap! socialcore/app-state assoc-in [:users] adduser)
    (swap! socialcore/app-state assoc-in [:selecteduser :id] id)
    (js/window.history.back)
  )
)

(defn OnCreateUserSuccess [response]
  (let [
      result (get response "result")
    ]
    (if (= "success" result)
      (create-user (get (get response "info") "id"))
      (let [error (get response "error")]
        (swap! socialcore/app-state assoc-in [:modalTitle] 
          (str "Error create user")
        ) 
        (.log js/console error)
        (swap! socialcore/app-state assoc-in [:modalText] error)
        (showmessage)
      )
    )
  )
)

(defn createUser []
  (POST (str settings/apipath "api/user") {
    :handler OnCreateUserSuccess
    :error-handler OnCreateUserError
    :headers {
      :Authorization (str "Bearer "  (:token (:token @socialcore/app-state)))}
    :format :json
    :response-format :json
    :params { :name (:name (:selecteduser @socialcore/app-state)) :email (:email (:selecteduser @socialcore/app-state)) :password (:password (:selecteduser @socialcore/app-state)) :role (:role (:selecteduser @socialcore/app-state)) :source (:source (:selecteduser @socialcore/app-state)) :confirmed (:confirmed (:selecteduser @socialcore/app-state)) :locked (:locked (:selecteduser @socialcore/app-state)) :pic (:pic (:selecteduser @socialcore/app-state))}})
)


(defn onDropDownChange [id value]
  ;(.log js/console () e)
  (swap! socialcore/app-state assoc-in [:selecteduser :role] value) 
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
         (.selectpicker "val" (:role (:selecteduser @socialcore/app-state)))
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
           (.bootstrapToggle (clj->js {:on "email confirmed" :off "email not confirmed"}))
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
                (swap! socialcore/app-state assoc-in [:selecteduser :locked] islocked)
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


(defn OnGetUser [response]
  (let [
    
    ]
    (swap! socialcore/app-state assoc-in [:selecteduser :id] (get response "id") )
    (swap! socialcore/app-state assoc-in [:selecteduser :email] (get response "email"))
    (swap! socialcore/app-state assoc-in [:selecteduser :role] (get response "role") )
    (swap! socialcore/app-state assoc-in [:selecteduser :locked] (get response "locked"))
    (swap! socialcore/app-state assoc-in [:selecteduser :pic] (get response "pic"))
    (swap! socialcore/app-state assoc-in [:selecteduser :password] (get response "password"))
    (swap! socialcore/app-state assoc-in [:selecteduser :confirmed] (get response "confirmed"))
    (swap! socialcore/app-state assoc-in [:selecteduser :source] (get response "source"))
    (swap! socialcore/app-state assoc-in [:selecteduser :name] (get response "name"))
  )
)


(defn get-user [id]
  (GET (str settings/apipath "api/user?id=" id) {
    :handler OnGetUser
    :response-format :json
    :error-handler error-handler
    :headers {:content-type "application/json" :Authorization (str "Bearer "  (:token  (:token @socialcore/app-state))) }
  })
)

(defn setUser []
  (let [
        users (:users @socialcore/app-state)
        user (first (filter (fn [user] (if (= (:id (:selecteduser @socialcore/app-state)) (:id user))  true false)) (:users @socialcore/app-state )))
        ]
    (swap! socialcore/app-state assoc-in [:selecteduser :id]  (:id user) ) 
    (swap! socialcore/app-state assoc-in [:selecteduser :role]  (:role user) ) 
    (swap! socialcore/app-state assoc-in [:selecteduser :source]  (:source user) ) 
    (swap! socialcore/app-state assoc-in [:selecteduser :name]  (:name user) ) 
    (swap! socialcore/app-state assoc-in [:selecteduser :password] (:password user))
    (swap! socialcore/app-state assoc-in [:selecteduser :confirmed] (:confirmed user))
    (swap! socialcore/app-state assoc-in [:selecteduser :email] (:email user) )
    (swap! socialcore/app-state assoc-in [:selecteduser :pic] (:pic user) )
    (swap! socialcore/app-state assoc-in [:selecteduser :locked] (:locked user) )
    (if (= "" (:pic user))
      (get-user (:id user))
    )
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
      (not= (:id (:selecteduser @socialcore/app-state)) nil)
      (not= (:id (:selecteduser @socialcore/app-state)) 0)
      (not= (:id (:selecteduser @socialcore/app-state)) "")
    )
    (setUser)
  )
)

(defn handleFromChange [e]
  ;;(.log js/console e  )  
  (.log js/console "The change ....")

)


(defn onMount [data]
  (swap! socialcore/app-state assoc-in [:current] "User Detail")
  (getUserDetail)
  (put! ch 46)
  (put! ch 47)
)


;; (defn handle-change [e owner]
;;   ;(.log js/console () e)
;;   (swap! app-state assoc-in [:form (keyword (.. e -target -id))] 
;;     (.. e -target -value)
;;   ) 
;; )


(defn buildRolesList [data owner]
  (map
    (fn [text]
      (dom/option {:key (:name text) :value (:name text)
                    ;:onChange #(handle-change % owner)
} (:name text))
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
        (swap! socialcore/app-state assoc-in [:selecteduser :pic] (.-result (.-target e)))
      ))
      (.readAsDataURL reader (aget (.-files input) 0))
    )
  )
)

(defn addModal []
  (dom/div
    (dom/div {:id "infomodal" :className "modal fade" :role "dialog"}
      (dom/div {:className "modal-dialog"} 
        ;;Modal content
        (dom/div {:className "modal-content"} 
          (dom/div {:className "modal-header"} 
                   (b/button {:type "button" :className "close" :data-dismiss "modal"})
                   (dom/h4 {:className "modal-title"} (:modalTitle @socialcore/app-state) )
                   )
          (dom/div {:className "modal-body"}
                   (dom/p (:modalText @socialcore/app-state))
                   )
          (dom/div {:className "modal-footer"}
                   (b/button {:type "button" :className "btn btn-default" :data-dismiss "modal"} "Close")
          )
        )
      )
    )
  )
)



(defcomponent userdetail-page-view [data owner]
  (did-mount [_]
    (onMount data)
  )
  (did-update [this prev-props prev-state]
    ;(.log js/console "Update happened") 

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
                (dom/div {:className "row"}
                  (dom/div {:className "col-md-1"} "Name:")
                  (dom/div {:className "col-md-2"}
                    (dom/input {:id "name" :type "text" :onChange (fn [e] (handleChange e)) :value (:name (:selecteduser @socialcore/app-state))})
                  )
                  (if (< (count (:name (:selecteduser @socialcore/app-state))) 1)
                    (dom/div {:style {:color "red" :margin-top "5px"}}
                    "name should not be empty"
                    )
                  )
                )
                                    
                (dom/div {:className "row"}
                  (dom/div {:className "col-md-1"} (if (or (= (:source (:selecteduser @socialcore/app-state)) "site") (= (:source (:selecteduser @socialcore/app-state)) ""))  "email: " "ID: "))
                  (dom/div {:className "col-md-2"}
                    (dom/input {:id "email" :type (if (or (= (:source (:selecteduser @socialcore/app-state)) "site") (= (:source (:selecteduser @socialcore/app-state)) "")) "email" "text") :readOnly (if (or (= (:source (:selecteduser @socialcore/app-state)) "site") (= (:source (:selecteduser @socialcore/app-state)) ""))  false true)  :onChange (fn [e] (handleChange e)) :value (:email (:selecteduser @socialcore/app-state))})
                  )
                  (if (and (or (= (:source (:selecteduser @socialcore/app-state)) "site") (= (:source (:selecteduser @socialcore/app-state)) "")) (not (socialcore/valid-email (:email (:selecteduser @socialcore/app-state)))))
                    (dom/div {:style {:color "red" :margin-top "5px"}}
                    "email should be valid address"
                    )
                  )
                )
                
                (dom/div {:className "row"}
                  (dom/div {:className "col-md-1"} "Password:")
                  (dom/div {:className "col-md-2"}
                    (dom/input {:id "password" :type "password" :readOnly (if (or (= (:source (:selecteduser @socialcore/app-state)) "site") (= (:source (:selecteduser @socialcore/app-state)) "")) false true) :onChange (fn [e] (handleChange e)) :value (:password (:selecteduser @socialcore/app-state))})
                  )
                  (if (and (or (= (:source (:selecteduser @socialcore/app-state)) "site") (= (:source (:selecteduser @socialcore/app-state)) "")) (< (count (:password (:selecteduser @socialcore/app-state))) 8))
                    (dom/div { :style {:color "red" :margin-top "5px"}}
                    "password should be at least 8 characters long"
                    )
                  )
                )

                (if (or (= (:role (:user @socialcore/app-state)) "admin") (= (:role (:user @socialcore/app-state)) "manager"))
                  (dom/div
                    (dom/div {:className "row"}
                      (dom/div {:className "col-md-1" } "Role: ")
                      (dom/div {:className "col-md-2"}
                        (omdom/select #js {:id "roles"
                                         :className "selectpicker"
                                         :data-show-subtext "true"
                                         :data-live-search "true"
                                         ;:onChange #(handle-change % owner)
                                         }                
                          (buildRolesList data owner)
                        )
                      )
                    )


                    (dom/div {:className "row"}
                      (dom/div {:className "col-md-1"})
                      (dom/div {:className "col-md-2"}
                        (dom/label {:className "checkbox-inline"}
                          (dom/input {:id (str "chckblock") :type "checkbox" :checked (:locked (:selecteduser @data)) :data-toggle "toggle" :data-size "large" :data-width "100" :data-height "26"})
                        )
                      )
                    )

                    (dom/div {:className "row" :style {:margin-top "5px"}}
                      (dom/div {:className "col-md-1"})
                      (dom/div {:className "col-md-2"}
                        (dom/label {:className "checkbox-inline"}
                          (dom/input {:id (str "chckconfr") :disabled true :type "checkbox" :checked (:confirmed (:selecteduser @data)) :data-toggle "toggle" :data-size "large" :data-width "200" :data-height "26"})
                        )
                      )
                    )
                  )
                )

                (dom/div {:className "row" :style {:margin-top "5px"}}
                  (dom/div {:className "col-md-1"}
                     "Picture: "
                  )
                  (dom/div {:className "col-md-2"}
                    (dom/input {:type "file" :style {:width "120px"} :onChange (fn [e] (readurl (.. e -target))) :name "file"})
                  )
                  ;(dom/input {:type "file" :onChange (fn [e] (js/readURL (.. e -target))) :name "file"})
                  
                )
                (dom/div {:className "row"}
                  (dom/img {:id "userpic" :style {:display (if (or (nil? (:pic (:selecteduser @socialcore/app-state))) (= "" (:pic (:selecteduser @socialcore/app-state)))) "none" "block") :max-width "200px" :max-height "200px"} :src (:pic (:selecteduser @socialcore/app-state)) :alt "User image"})
                )
              )              
            )
          )
        )
        (dom/nav {:className "navbar navbar-default" :role "navigation"}
          (dom/div {:className "navbar-header"}
            (dom/button {:className "btn btn-default" :disabled (or (not (socialcore/valid-email (:email (:selecteduser @socialcore/app-state)))) (not (check-user-validity))) :onClick (fn [e] (if (= (:id (:selecteduser @socialcore/app-state)) 0) (createUser) (updateUser)) )} (if (= (:id (:selecteduser @socialcore/app-state)) 0) "Insert" "Update"))
            (dom/button {:className "btn btn-danger" :style {:visibility (if (= (:id (:selecteduser @socialcore/app-state)) 0) "hidden" "visible")} :disabled (if (= (:id (:user @socialcore/app-state)) (:id (:selecteduser @socialcore/app-state))) true false) :onClick (fn [e] (deleteUser (:id (:selecteduser @socialcore/app-state))))} "Delete")

            (b/button {:className "btn btn-info"  :onClick (fn [e]     (js/window.history.back))} "Cancel")
          )
        )
        (addModal)
      )
    )
  )
)


(sec/defroute userdetail-page "/userdetail/:id" {id :id}
  (let [
    user (first (filter (fn [x] (if (= (js/parseInt id) (:id x)) true false)) (:users @socialcore/app-state)))
    ;tr1 (.log js/console (str "id=" id "; user=" user))
    ]
    (swap! socialcore/app-state assoc-in [:selecteduser :id]  (:id user))

    (om/root userdetail-page-view
             socialcore/app-state
             {:target (. js/document (getElementById "app"))})

  )
)


(sec/defroute newuserdetail-new-page "/userdetail" {}
  (let []

    (swap! socialcore/app-state assoc-in [:selecteduser :login]  "" ) 
 
    (swap! socialcore/app-state assoc-in [:selecteduser :role ]  "user" ) 
    (swap! socialcore/app-state assoc-in [:selecteduser :password] "" )
    (swap! socialcore/app-state assoc-in [:selecteduser :name] "" )
    (swap! socialcore/app-state assoc-in [:selecteduser :email] "" )
    (swap! socialcore/app-state assoc-in [:selecteduser :id] 0)
    (swap! socialcore/app-state assoc-in [:selecteduser :pic] "" )

    (swap! socialcore/app-state assoc-in [:selecteduser :source] "site" )
    (swap! socialcore/app-state assoc-in [:selecteduser :locked] false )
    (swap! socialcore/app-state assoc-in [:selecteduser :confirmed] false )


    (om/root userdetail-page-view
             socialcore/app-state
             {:target (. js/document (getElementById "app"))})
  )
)
