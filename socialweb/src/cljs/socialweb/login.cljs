(ns socialweb.login
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :refer-macros [defroute]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [socialweb.core :as socialcore]
            [socialweb.settings :as settings]
            [socialweb.users :as users]
            [socialweb.zones :as zones]
            [socialweb.zonedetail :as zonedetail]
            [socialweb.userdetail :as userdetail]
            [socialweb.registration :as registration]

            [cljs-time.format :as tf]
            [cljs-time.core :as tc]
            [cljs-time.coerce :as te]
            [cljs-time.local :as tl]
            [socialweb.localstorage :as ls]
            [ajax.core :refer [GET POST]]
            [om-bootstrap.input :as i]
            [om-bootstrap.button :as b]
            [om-bootstrap.panel :as p]
	    [chord.client :refer [ws-ch]]
            [cljs.core.async :refer [put! dropping-buffer chan take! <! >! timeout close!]]
  )
  (:import goog.History)
)

(enable-console-print!)

(def iconBase "/images/")
(def application
  (js/document.getElementById "app"))

(defn set-html! [el content]
  (aset el "innerHTML" content))


(sec/set-config! :prefix "#")

(let [history (History.)
      navigation EventType/NAVIGATE]
  (goog.events/listen history
                     navigation
                     #(-> % .-token sec/dispatch!))
  (doto history (.setEnabled true)))


(def ch (chan (dropping-buffer 2)))
(def jquery (js* "$"))
(defonce app-state (atom  {:error "" :modalText "Modal Text" :modalTitle "Modal Title" :state 0} ))


(defn setLoginError [error]
  (swap! app-state assoc-in [:error] 
    (:error error)
  )

  (swap! app-state assoc-in [:modalTitle] 
    (str "Login Error")
  ) 

  (swap! app-state assoc-in [:modalText] 
    (str (:error error))
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


(defn onLoginError [ response]
  (let [     
      newdata { :error (get response (keyword "response")) }
    ]
   
    (setLoginError newdata)
  )
  
  ;(.log js/console (str  response ))
)



(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text))
)




(defn onLoginSuccess [response]
  (
    let [     
      error (get response (keyword "error"))
      newdata {:token (get response (keyword "access_token"))  :expires (get response (keyword "expires_in") ) }
    ]
    (swap! app-state assoc-in [:state] 0)
    (.log js/console (str "error: " error))
    ;;(.log js/console (str (select-keys (js->clj response) [:Title :Reference :Introduction])  ))    

    (if (nil? error)
      (let []
        (swap! socialcore/app-state assoc-in [:token] newdata )
        (swap! socialcore/app-state assoc-in [:view] 1 )
        (swap! socialcore/app-state assoc-in [:users] [] )        
        (socialcore/load-users 0)
      )
      (setLoginError {:error error})
    )
  )
)


(defn OnLogin [response]
  (if (= (count response) 0)
    (onLoginError {:response "Incorrect username or password"} )
    (onLoginSuccess response)
  )
  
  ;;(.log js/console (str  (response) ))
  ;;(.log js/console (str  (get (first response)  "Title") ))
)

(defn dologin [username password]
  (let [

    ]
    (swap! app-state assoc-in [:state] 1)
    ;; currently logged in user
    (swap! socialcore/app-state assoc-in [:user :email] username)

    ;; currently selected user


    (POST (str settings/apipath "token")
      {:handler OnLogin
       :error-handler onLoginError
       :headers {:content-type "application/x-www-form-urlencoded"}
       :body (str "grant_type=password&username=" username "&password=" password) 
      })

    ;(swap! app-state assoc-in [:state] 0)
    ;(aset js/window "location" "#/zones")
    
  )
)




(defn checklogin [owner e]
  (let [
    theusername (-> (om/get-node owner "txtUserName") .-value)
    thepassword (-> (om/get-node owner "txtPassword") .-value)
    ]
    (.preventDefault (.. e -nativeEvent))
    (.stopPropagation (.. e -nativeEvent))
    (.stopImmediatePropagation (.. e -nativeEvent))
    ;(aset js/window "location" "http://localhost:3449/#/something")
    ;(.log js/console (str "user=" theusername " password=" thepassword))
    (dologin (str theusername) (str thepassword)) 
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
                   (b/button {:type "button" :className "btn btn-default" :data-dismiss "modal"} "Close")
          )
        )
      )
    )
  )
)


(defn set-google-button []
  (set! (.-display (.-style (.getElementById js/document "socialbuttons"))) "block")
  ;(set! (.-display (.-style (aget (.getElementsByClassName js/document "g-signin2") 0))) "block")
)

(defn onMount [data owner]
  (.focus (om/get-node owner "txtUserName" ))
  (set! (.-title js/document) "Beeper Login")
  (set-google-button)
)



(defcomponent login-page-view [data owner]
  (did-update [this prev-props prev-state]
    (.log js/console "starting login screen" )
  )
  (did-mount [_]
    (.focus (om/get-node owner "txtUserName" ))
  )
  (render
    [_]
    (dom/div {:className "container" :style {:width "100%" :padding-top "83px" :backgroundImage "url(/images/loginbackground.png)" :backgroundSize "cover"}  }
      ;(om/build t5pcore/website-view data {})
      ;(dom/h1 "Login Page")
      ;(dom/img {:src "images/LogonBack.jpg" :className "img-responsive company-logo-logon"})
      (dom/form {:className "form-signin"}
        (dom/input #js {:type "text" :ref "txtUserName"
           :defaultValue  settings/demouser  :className "form-control" :placeholder "User Name" } )
        (dom/input {:className "form-control" :ref "txtPassword" :id "txtPassword" :defaultValue settings/demopassword :type "password"  :placeholder "Password"} )
        (dom/button #js {
          :className (if (= (:state @app-state) 0) "btn btn-lg btn-primary btn-block" "btn btn-lg btn-primary btn-block m-progress" )  :type "button" :onClick (fn [e](checklogin owner e))} "Login")


        (dom/button #js {
          :className (if (= (:state @app-state) 0) "btn btn-lg btn-primary btn-block" "btn btn-lg btn-primary btn-block m-progress" )  :type "button"  :onClick (fn [e] (-> js/document
      .-location
      (set! "#/registration")))} "Register")
        
        ;(dom/div {:className "g-signin2" :data-onsuccess "onSignIn"})
      )
      (addModal)
      (dom/div {:style {:margin-bottom "100px"}})
    )
  )
)


(defn gotomap [counter]
  (let []
    (aset js/window "location" "#/map")
    (swap! app-state assoc-in [:state] 0)
  )
)


(defn setcontrols [value]
  (case value
    42 (gotomap 0)
  )
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

(sec/defroute login-page "/login" []
  (om/root login-page-view 
           app-state
           {:target (. js/document (getElementById "app"))}
  )
)

(defn get-social-user [id from]
  (swap! socialcore/app-state assoc-in [:user :email] id)
  (POST (str settings/apipath "socialtoken")
    {:handler OnLogin
     :error-handler onLoginError
     :headers {}
     :format :json
     :params {:id id :from from :picture (.-picture js/window) :name (.-username js/window)} 
    }
  )
)

;;(.getId js/google_profile)
(sec/defroute google-login-page "/login/:id" [id query-params]
  (let [
    
    ]
    (.log js/console (str "from= " (:from query-params)))
    (get-social-user id (:from query-params))    
  )
)


(defn main []
  (-> js/document
      .-location
      (set! "#/login"))

  ;;(aset js/window "location" "#/login")
)

(main)

