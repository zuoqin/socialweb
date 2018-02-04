(ns socialsrv.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]


            [clojure.string :as str]
            [clj-jwt.core  :refer :all]
            [clj-jwt.key   :refer [private-key]]
            [clj-time.core :refer [now plus days]]

            [socialsrv.routes.dbservices :as dbservices]
            [socialsrv.routes.user :as userapi]
            [socialsrv.routes.zone :as zoneapi]
            ))

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Social WEB API"
                           :description "Core Services"}}}}
  

  (context "/" []
    :tags ["authorization"]
    (POST "/token" []
      ;:return String
      :form-params [grant_type :- String, username :- String, password :- String]
      :summary  "login/password with form-parameters"
      (ok (let [result(dbservices/checkUser username password) ]

        (case (:result result)
          4 {:error "Email is not confirmed"}
          3 {:access_token (-> (dbservices/claim username) jwt to-str) :expires_in 99999 :token_type "bearer" :id (:info result)}
          2 {:error (str "Incorrect password, user will be locked after another " (:info result) " incorrect input" (if (> (:info result) 1) "s" ""))}
          1 {:error "User is locked, ask administrator to unlock"}
          0 {:error "User does not exist"}
          ""
          )
        ) 
      )
    )


   (POST "/socialtoken" []
      ;:return String
      :body-params [id :- String, from :- String, picture :- String, name :- String]
      :summary  "data from social network"
      (ok (let [result (dbservices/checkSocialUser id from picture name)] 
            {:access_token (-> (dbservices/claim id) jwt to-str) :expires_in 99999 :token_type "bearer" :id result}
        )
      )
    )

    (OPTIONS "/socialtoken" []
      :summary  "Allows OPTIONS requests"
      (ok "")
    )
  )

  (context "/api" []
    :tags ["register"]

   (POST "/register" []
      ;:return String
      :body-params [email :- String, password :- String]
      :summary  "Register into time zones"
      (ok (let [res (userapi/registerUser email password)]
            {:result res}
        )
      )
    )

   (PUT "/register" []
      ;:return String
      :body-params [email :- String]
      :summary  "Send user password"
      (ok (let [res (userapi/sendUserInfo email)]
            {:result res}
        )
      )
    )

   (GET "/register" []
      ;:return String
      :query-params [id :- Long]
      :summary  "Registration confirm into time zones"
      (ok (let [] (userapi/confirmUser id)
            {:result "success"}
        )
      )
    )

    (OPTIONS "/register" []
      :summary  "Allows OPTIONS requests"
      (ok "")
    )
  )

  (context "/api" []
    :tags ["invite"]

   (POST "/invite" []
      ;:return String
      :header-params [authorization :- String]
      :body-params [email :- String]
      :summary  "Invite into time zones"
      (ok (let [res (userapi/inviteUser email)]
            {:result res}
        )
      )
   )

   (OPTIONS "/invite" []
     :summary  "Allows OPTIONS requests"
     (ok "")
   )
  )

  (context "/api" []
    :tags ["user"]

    (GET "/user" []
      :header-params [authorization :- String]
      :query-params [id :- Long]
      :summary      "retrieve user"

      (ok  (userapi/get-user-by-id (nth (str/split authorization #" ") 1) id))
    )


    (GET "/users" []
      :header-params [authorization :- String]
      :query-params [{page :- Long -1}]
      :summary      "retrieve all users for current login"

      (ok  (userapi/getUsers (nth (str/split authorization #" ") 1) page)) 
    )

    (POST "/user" []
      ;;:return      Long
      :header-params [authorization :- String]
      :body-params [name :- String, email :- String, password :- String, role :- String, pic :- String, locked :- Boolean, confirmed :- Boolean]
      :summary     "Create new user"
      (ok (userapi/createUser (nth (str/split authorization #" ") 1) name email password role locked pic confirmed)))

    (DELETE "/user" []
      ;;:return      Long
      :header-params [authorization :- String]
      :query-params [id :- Long]
      :summary     "Delete user"
      (ok (userapi/deleteUser (nth (str/split authorization #" ") 1) id )))

    (PUT "/user" []
      ;;:return      Long
      :header-params [authorization :- String]
      :body-params [id :- Long, name :- String, email :- String, password :- String, role :- String, pic :- String, locked :- Boolean]
      :summary     "Update user"
      (ok (userapi/updateUser (nth (str/split authorization #" ") 1) id name email password role locked pic)))

    (OPTIONS "/user" []
      :summary  "Allows OPTIONS requests"
      (ok "")
    )

    (OPTIONS "/users" []
      :summary  "Allows OPTIONS requests"
      (ok "")
    )
  )


  (context "/api" []
    :tags ["zone"]

    (GET "/zone" []
      :header-params [authorization :- String]
      :query-params [id :- Long ]
      :summary      "retrieve all zones for given user"

      (ok  (zoneapi/getZones (nth (str/split authorization #" ") 1) id)))

    (POST "/zone" []
      ;;:return      Long
      :header-params [authorization :- String]
      :body-params [user :- Long, name :- String, city :- String, diff :- Double]
      :summary     "Create new zone"
      (ok (zoneapi/createZone (nth (str/split authorization #" ") 1) user name city diff)))

    (DELETE "/zone" []
      ;;:return      Long
      :header-params [authorization :- String]
      :query-params [id :- Long]
      :summary     "Delete zone"
      (ok (zoneapi/deleteZone (nth (str/split authorization #" ") 1) id )))

    (PUT "/zone" []
      ;;:return      Long
      :header-params [authorization :- String]
      :body-params [id :- Long, name :- String, city :- String, diff :- Double]
      :summary     "Update zone"
      (ok (zoneapi/updateZone (nth (str/split authorization #" ") 1) id  name city diff)))

    (OPTIONS "/zone" []
      :summary  "Allows OPTIONS requests"
      (ok "")
    )    
  )
)



    ;; (GET "/plus" []
    ;;   :return       Long
    ;;   :query-params [x :- Long, {y :- Long 1}]
    ;;   :summary      "x+y with query-parameters. y defaults to 1."
    ;;   (ok (+ x y)))

    ;; (POST "/minus" []
    ;;   :return      Long
    ;;   :body-params [x :- Long, y :- Long]
    ;;   :summary     "x-y with body-parameters."
    ;;   (ok (- x y)))

    ;; (GET "/times/:x/:y" []
    ;;   :return      Long
    ;;   :path-params [x :- Long, y :- Long]
    ;;   :summary     "x*y with path-parameters"
    ;;   (ok (* x y)))

    ;; (POST "/divide" []
    ;;   :return      Double
    ;;   :form-params [x :- Long, y :- Long]
    ;;   :summary     "x/y with form-parameters"
    ;;   (ok (/ x y)))

    ;; (GET "/power" []
    ;;   :return      Long
    ;;   :header-params [x :- Long, y :- Long]
    ;;   :summary     "x^y with header-parameters"
    ;;   (ok (long (Math/pow x y))))
