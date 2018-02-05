(ns socialsrv.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [socialsrv.layout :refer [error-page]]
            [socialsrv.routes.home :refer [home-routes]]
            [socialsrv.routes.services :refer [service-routes]]
            [compojure.route :as route]
            [socialsrv.env :refer [defaults]]
            [mount.core :as mount]
            [socialsrv.middleware :as middleware]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :refer [response]]
  )
)

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    (-> #'home-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    (wrap-json-response #'service-routes)
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))


(defn app [] (middleware/wrap-base #'app-routes))
