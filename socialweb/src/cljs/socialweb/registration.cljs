(ns socialweb.registration  (:use [net.unit8.tower :only [t]])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :refer-macros [defroute]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [socialweb.core :as socialcore]
            [socialweb.settings :as settings]
            [ajax.core :refer [GET POST]]
            [om-bootstrap.input :as i]
            [om-bootstrap.button :as b]
            [om-bootstrap.panel :as p]


            [cljs.core.async :refer [put! dropping-buffer chan take! <!]]
  )
  (:import goog.History)
)

(enable-console-print!)


(def application
  (js/document.getElementById "app"))

(defn set-html! [el content]
  (aset el "innerHTML" content))

(defonce app-state (atom  {:username "" :password "" :error "" :modalText "Modal Text" :modalTitle "Modal Title" :state 0} ))

(defn handleChange [e]
  (swap! app-state assoc-in [(keyword (.. e -nativeEvent -target -id))] (.. e -nativeEvent -target -value))
)


(sec/set-config! :prefix "#")

;; (let [history (History.)
;;       navigation EventType/NAVIGATE]
;;   (goog.events/listen history
;;                      navigation
;;                      #(-> % .-token sec/dispatch!))
;;   (doto history (.setEnabled true)))


(def ch (chan (dropping-buffer 2)))
(def jquery (js* "$"))


(defn setLoginError [title error]
  (swap! app-state assoc-in [:error] 
    (:error error)
  )

  (swap! app-state assoc-in [:modalTitle] 
    title
  ) 

  (swap! app-state assoc-in [:modalText] 
    (str error)
  ) 

  (swap! app-state assoc-in [:state] 0) 
 
  ;;(.log js/console (str  "In setLoginError" (:error error) ))
  (jquery
    (fn []
      (-> (jquery "#loginModal")
        (.modal)
      )
    )
  )
)


(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))


(defn OnCreateUserError [response]
  (let [     
      newdata {:error (:error (get response (keyword "response") ))  }
    ]
    (setLoginError "Create user failed" newdata)
  )

  ;;(.log js/console (str  (get (first response)  "Title") ))
)


(defn OnCreateUserSuccess [response]
  (let [msg (get response (keyword "result"))]
    (if (= msg "messages sent") (swap! app-state assoc-in [:success] true))


    (setLoginError (if (= msg "messages sent") "Registration succeeded" "Registration failed") (if (= msg "messages sent") "Check your email to confirm address" msg))
    ;(.log js/console msg)
  )
)

(defn doregister []
  (swap! app-state assoc-in [:state] 1)
  (swap! app-state assoc-in [:success] false)
  (POST (str settings/apipath  "api/register") {
    :handler OnCreateUserSuccess
    :error-handler OnCreateUserError
    :headers {
      :content-type "application/json" 
      :Authorization (str "Bearer "  "the empty token")}
    :format :json
    :params { :email (:username @app-state) :password (:password @app-state)}})
)




(defn checklogin []
  (let [
    ]
    ;(aset js/window "location" "http://localhost:3449/#/something")
    ;(.log js/console owner ) 

    (doregister) 
  )
)


(defn addModal []
  (dom/div
    (dom/div {:id "loginModal" :className "modal fade" :role "dialog"}
      (dom/div {:className "modal-dialog"} 
        ;;Modal content
        (dom/div {:className "modal-content"} 
          (dom/div {:className "modal-header"} 
                   (b/button {:type "button" :className "close" :data-dismiss "modal"})
                   (dom/h4 {:className "modal-title"} (:modalTitle @app-state) )
                   )
          (dom/div {:className "modal-body"}
                   (dom/p (:modalText @app-state))
                   )
          (dom/div {:className "modal-footer"}
                   (b/button {:type "button" :className "btn btn-default" :data-dismiss "modal" :onClick (fn [e] (if (= true (:success @app-state)) (-> js/document .-location (set! "#/login"))))} "Close")
          )
        )
      )
    )
  )
)



(defcomponent login-page-view [data owner]
  (did-update [this prev-props prev-state]
    (.log js/console "starting login screen" ) 
    
  )
  (did-mount [_]

  )
  (render
    [_]
    (dom/div {:className "container" :style {:width "100%" :padding-top "283px" :backgroundImage "url(/images/loginbackground.png)" :backgroundSize "cover"}  }
      ;(om/build t5pcore/website-view data {})
      ;(dom/h1 "Login Page")
      ;(dom/img {:src "images/LogonBack.jpg" :className "img-responsive company-logo-logon"})
      (dom/form {:className "form-signin"}
        (dom/input #js {:type "text" :id "username" :value (:username @app-state) :className "form-control" :placeholder "Email" :onChange (fn [e] (handleChange e ))})
        (if (not (socialcore/valid-email (:username @app-state)))
          (dom/div {:style {:color "red"}} "enter correct email address")
        )
        (dom/input {:className "form-control" :id "password" :value (:password @app-state) :type "password"  :placeholder "Password" :onChange (fn [e] (handleChange e ))} )
        (if (not (socialcore/check-password-complexity (:password @app-state)))
          (dom/div {:style {:color "red"}} "password should be at least 8 characters long")
        )
        
        (dom/div {:className (if (= (:state @app-state) 0) "btn btn-lg btn-primary btn-block" "btn btn-lg btn-primary btn-block m-progress") :disabled (or (not (socialcore/check-password-complexity (:password @app-state))) (not (socialcore/valid-email (:username @app-state))))  :type "button" :onClick (fn [e](checklogin))} "Registration")

        (dom/div {:className "btn btn-lg btn-primary btn-block" :type "button" :onClick (fn [e] (-> js/document .-location (set! "#/login")))} "Back")
        
      )
      (addModal)
      (dom/div {:style {:margin-bottom "200px"}})
    )
  )
)



(defmulti website-view
  (
    fn [data _]
      (:view (if (= data nil) @socialcore/app-state @data ))
  )
)

(defmethod website-view 0
  [data owner] 
  (login-page-view data owner)
)

(defmethod website-view 1
  [data owner] 
  (login-page-view data owner)
)


(sec/defroute login-page "/registration" []
  (om/root login-page-view 
           app-state
           {:target (. js/document (getElementById "app"))}
  )
)
