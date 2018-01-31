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

(defonce app-state (atom  {:users []}))


(defn OnGetUsers [response]
   (swap! app-state assoc :users  (get response "Users")  )
   (.log js/console (:users @app-state)) 

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
        (dom/div
          (dom/button {:className "btn btn-primary" :style {:margin-top "70px"} :onClick (fn [e] (-> js/document
                                                                                   .-location
                                                                                   (set! "#/userdetail")))} "Add New")
        )
        (dom/div {:className "panel panel-primary"}
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


