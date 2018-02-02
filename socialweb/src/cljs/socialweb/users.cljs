(ns socialweb.users (:use [net.unit8.tower :only [t]])
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            [socialweb.core :as socialcore]
            [ajax.core :refer [GET POST]]
            [socialweb.settings :as settings]
  )
  (:import goog.History)
)

(enable-console-print!)
(def jquery (js* "$"))
(defonce app-state (atom  {:users []}))


(defn OnGetUsers [response]
   (swap! app-state assoc :users  (get response "Users")  )
   (.log js/console (:users @app-state)) 

)

(defn handlechange [e]
  (swap! socialcore/app-state assoc-in [(keyword (.. e -nativeEvent -target -id))] (.. e -nativeEvent -target -value))
)


(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text))
)


(defn getUsers [data] 
  (GET (str settings/apipath "api/user") {
    :handler OnGetUsers
    :error-handler error-handler
    :headers {
      :content-type "application/json"
      :Authorization (str "Bearer "  (:token  (first (:token @socialcore/app-state)))) }
  })
)

(defn show-modal-invite []
  (let []
    (swap! socialcore/app-state assoc-in [:state] 0)

    (swap! socialcore/app-state assoc-in [:modalTitle] (str "Invitation to Time Zones Manager"))
    (swap! socialcore/app-state assoc-in [:modalText] (str "Enter email address below to send invitation"))
    (jquery
      (fn []
        (-> (jquery "#inviteModal")
          (.modal)
        )
      )
    )
  )
)

(defn addModal []
  (dom/div
    (dom/div {:id "infoModal" :className "modal fade" :role "dialog"}
      (dom/div {:className "modal-dialog"} 
        ;;Modal content
        (dom/div {:className "modal-content"} 
          (dom/div {:className "modal-header"} 
                   (dom/button {:type "button" :className "close" :data-dismiss "modal"})
                   (dom/h4 {:className "modal-title"} (:modalTitle @socialcore/app-state) )
                   )
          (dom/div {:className "modal-body"}
                   (dom/p (:modalText @socialcore/app-state))
                   )
          (dom/div {:className "modal-footer"}
                   (dom/button {:type "button" :className "btn btn-default" :data-dismiss "modal"} "Close")
          )
        )
      )
    )
  )
)


(defn OnInviteError [response]
  (let [     
      ;;newdata {:id (get response (keyword "id") ) }
    ]
    (swap! socialcore/app-state assoc-in [:state] 0)
    (swap! socialcore/app-state assoc-in [:modalTitle] "Send invitation error")
    (swap! socialcore/app-state assoc-in [:modalText] (str "There was an error sending invitation to " (:email @socialcore/app-state)))
    (-> (jquery "#inviteModal .close")
          (.click)
    )
    (jquery
      (fn []
        (-> (jquery "#infoModal")
          (.modal)
        )
      )
    )
  )
  ;; TO-DO: Delete User from Core
  ;;(.log js/console (str  (get (first response)  "Title") ))
)


(defn OnInviteSuccess [response]
  (let [ 
    ]
    (swap! socialcore/app-state assoc-in [:state] 0)
    (swap! socialcore/app-state assoc-in [:modalTitle] "Send invitation success")
    (swap! socialcore/app-state assoc-in [:modalText] (str "Invitation sent to " (:email @socialcore/app-state)))
    (-> (jquery "#inviteModal .close")
          (.click)
    )
    (jquery
      (fn []
        (-> (jquery "#infoModal")
          (.modal)
        )
      )
    )
  )
)

(defn sendinvitation []
  (let [
    ;units (:childs (:selectedgroup @shelters/app-state))
    ]
    (swap! socialcore/app-state assoc-in [:state] 1)
    (POST (str settings/apipath  "api/invite") {
      :handler OnInviteSuccess
      :error-handler OnInviteError
      :headers {
        :Authorization (str "Bearer "  (:token (:token @socialcore/app-state)))}
      :format :json
      :params { :email (:email @socialcore/app-state) }})
  )
)


(defcomponent addmodalinvite [data owner]
  (render [_]
    (let [
      ;tr1 (.log js/console (str "name=" (:name (:selectedgroup @data))))
      ]
      (dom/div
        (dom/div {:id "inviteModal" :className "modal fade" :role "dialog"}
          (dom/div {:className "modal-dialog"} 
            ;;Modal content
            (dom/div {:className "modal-content"} 
              (dom/div {:className "modal-header"} 
                (dom/button {:type "button" :className "close" :data-dismiss "modal"})
                (dom/h4 {:className "modal-title"} (:modalTitle @socialcore/app-state) )
              )
              (dom/div {:className "modal-body"}

                (dom/div {:className "panel panel-primary"}
                  (dom/div {:className "panel-heading" :style {:padding "1px"}}
                    (dom/h5 {:style {:text-align "center"}} (:modalText @socialcore/app-state))
                  )
                  (dom/div {:className "panel-body"}
                    (dom/div {:className "row" :style {:margin-left "0px" :margin-right "0px"}}
                      (dom/div {:className "col-md-2" :style {:text-align "left"}}
                        "email"
                      )
                      (dom/div {:className "col-md-2"}
                        (dom/input {:id "email" :type "email" :multiple true :style {:padding-left "10px"} :placeholder "email" :onChange (fn [e] (handlechange e)) :required true :value (:email @socialcore/app-state)})
                      )
                    )
                  )
                )
              )
              (dom/div {:className "modal-footer"}
                (dom/div {:className "row"}
                  (dom/div {:className "col-xs-6" :style {:text-align "center"}}
                    (dom/button {:id "btnsavegroup" :disabled (if (= (:state @socialcore/app-state) 1) true false) :type "button" :className (if (= (:state @socialcore/app-state) 0) "btn btn-default" "btn btn-default m-progress" ) :onClick (fn [e] (sendinvitation))} "Send")
                  )

                  (dom/div {:className "col-xs-6" :style {:text-align "center"}}
                    (dom/button {:type "button" :disabled (if (= (:state @socialcore/app-state) 1) true false) :className "btn btn-default" :data-dismiss "modal"} "Cancel")
                  )
                )
              )
            )
          )
        )
      )
    )
  )
)


(defcomponent showusers-view [data owner]
  (render
    [_]
    (dom/div {:className "panel-body" :style {:display "block"}}
      (map (fn [item]
        (dom/div {:className "row"}
          (dom/div {:className "col-md-4" :style {:text-align "center"}}
            (dom/a {:href (str  "#/userdetail/" (:id item))} (:email item))
          )
          (dom/div {:className "col-md-4" :style {:text-align "center"}}
            (dom/a {:href (str  "#/userdetail/" (:id item))} (:role item))
          )

          (dom/div {:className "col-md-4" :style {:text-align "center"}}
            (dom/a {:href (str  "#/userdetail/" (:id item))} (if (= true (:locked item)) "Yes" "No"))
          )
        )                
        )(:users data)
      )
    )
  )
)

(defn onMount [data]
  (swap! socialcore/app-state assoc-in [:current] 
    "Users"
  )
)



(defcomponent users-view [data owner]
  (will-mount [_]
    (onMount data)
  )
  (render [_]
    (let [
      ]
      (dom/div
        (om/build socialcore/website-view socialcore/app-state {})
        (dom/div {:className "row" :style {:margin-top "70px" :margin-left "5px"}}
          (dom/button {:className "btn btn-primary" :style {} :onClick (fn [e] (-> js/document .-location (set! "#/userdetail")))} "Add New")

          (if (or (= (:role (:user @socialcore/app-state)) "admin") (= (:role (:user @socialcore/app-state)) "admin"))
            (dom/button {:className "btn btn-primary" :onClick (fn [e] (show-modal-invite))} "Invite")
          )
        )
        (dom/div {:className "panel panel-primary" :style {:margin-left "5px"}}
          (dom/div {:className "panel-heading"}
            (dom/div {:className "row"}
              (dom/div {:className "col-md-4" :style {:text-align "center"}}
                "user"
              )
              (dom/div {:className "col-md-4" :style {:text-align "center"}}
                "role"
              )
              (dom/div {:className "col-md-4" :style {:text-align "center"}}
                "locked"
              )
            )
          )
          (om/build showusers-view  data {})
          (addModal)
          (om/build addmodalinvite  data {})
        )
      ) 
    )
  )
)




(sec/defroute users-page "/users" []
  (swap! socialcore/app-state assoc-in [:view] 2)
  (om/root users-view
           socialcore/app-state
           {:target (. js/document (getElementById "app"))}))


