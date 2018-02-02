(ns socialsrv.routes.home
  (:require [socialsrv.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [socialsrv.db.user :as db]
  )
)

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page []
  (layout/render "about.html"))

(defn registration-page [id]
  (let [
    email (db/confirm-user id)
    res (db/find-user-by-id id)
    cnt (count res)
    email (first res)
    ]
    (if (> cnt 0)
      (layout/render "registration.html" {:email email})
      (layout/render "error.html")
    )
  )
)

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page))
  (GET "/registration/:id" [id] (registration-page (Long/parseLong id)))
)

