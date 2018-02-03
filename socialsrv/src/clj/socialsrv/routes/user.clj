(ns socialsrv.routes.user
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            

            [clj-jwt.core  :refer :all]
            [clj-jwt.key   :refer [private-key]]
            [clj-time.core :refer [now plus days]]

            [socialsrv.db.user :as db]
            [postal.core :as postal]
            [clojure.string :as str]
))


(defn get-user-by-id [token id]
  (let [
    usercode (:iss (-> token str->jwt :claims)  ) 
    result (db/get-user-by-id id)         
    ]
    ;(println (str "usercode=" usercode) )
    result
  )
)


(defn getUsers [token page]
  (let [
    usercode (:iss (-> token str->jwt :claims)  ) 
    result (into [] (db/get-users usercode page))         
    ]
    ;(println (str "usercode=" usercode) )
    result
  )
)

(defn registerUser [email password]
  (let [
    id (db/create-user "" email password "user" false "" false)
    tr1 (println (str "email=" email "; id=" id))
    msg (str  "
      <html>
        <head>
        </head>
        <body>
          <h1>Welcome to Time Zones manager!</h1>
          <p>
            To activate your account, please, follow this
            <a href=\"http://devstat.aytm.com:3000/api/register?id=" id "\">
  link
            </a>
          </p>
          <p>
            This is an automated email, don't reply to it.
          </p>
        </body>
      </html>"
    )
    result (if (> id 0)
      (postal/send-message {:host "mail.smtp2go.com"
                                :port 2525
                                :tls true
                                :user "zuoqin"
                                :pass "@Qwerty123"}
                               {:from "noreply@timezones.com"
                                :to email
                                :subject "Account activation"
                                :body [{:type "text/html; charset=utf-8"
                 :content msg}]
                               }
      )
      {:message "User exists"}
      )
    ]
    (:message result)
  )
)

(defn sendUserInfo [email]
  (let [
    user (db/find-user email) 
    ;tr1 (println (str "email=" email "; id=" id))
    msg (str  "
      <html>
        <head>
        </head>
        <body>
          <h1>Dear " (nth user 3) "</h1>
          <p>
            Welcome to Time Zones manager!
          </p>
          <p>
            Your password for Time Zones Manager: " (nth user 2) "
          </p>
          <p>
            This is an automated email, don't reply to it.
          </p>
        </body>
      </html>"
    )
    result (if (= "" (nth user 0))
      {:message "User does not exist"}
      (postal/send-message {:host "mail.smtp2go.com"
                                :port 2525
                                :tls true
                                :user "zuoqin"
                                :pass "@Qwerty123"}
                               {:from "noreply@timezones.com"
                                :to email
                                :subject "Password reminder"
                                :body [{:type "text/html; charset=utf-8"
                 :content msg}]
                               }
      )
      
      )
    ]
    (if (= "messages sent" (:message result)) (str "Password has been sent to " email) (:message result))
  )
)


(defn inviteUser [email]
  (let [
    user (db/find-user email) 
    msg (str  "
      <html>
        <head>
        </head>
        <body>
          <h1>Welcome to Time Zones manager!</h1>
          <p>
  Dear friend, you have been invited to join Time Zones Manager! Please, follow this
            <a href=\"http://devstat.aytm.com:3449/#/register" "\">
  link
            </a> to register.
          </p>
          <p>
            This is an automated email, don't reply to it.
          </p>
        </body>
      </html>"
      )
    result (if (= "" (nth user 0))
       (try (postal/send-message {:host "mail.smtp2go.com"
                                    :port 2525
                                    :tls true
                                    :user "zuoqin"
                                    :pass "@Qwerty123"}
                                   {:from "noreply@timezones.com"
                                    :to email
                                    :subject "Your invitation to Time Zones Manager"
                                    :body [{:type "text/html; charset=utf-8"
                                            :content msg}]
                                    }
                                   )
        (catch Exception e {:message (str "Error sending email to: " email)}))
        {:message (str email " already exists")}
      ) 
    ]
    (:message result)
  )
)


(defn createUser [token name email password role locked pic]
  (let [
    usercode (:iss (-> token str->jwt :claims)  ) 
    ;;; TO-DO: add check authorization to add 

    result {:res "Success"}
    ]
    
    {:id (db/create-user name email password role locked pic true)}
    ;; TO-DO Add check successfull
;    result
  )
)

(defn confirmUser [id]
  (let [
    result {:res "Success"}
    ]
    
    (db/confirm-user id)
    ;; TO-DO Add check successfull
    result
  )
)


(defn updateUser [token id name email password role locked picture]
  (let [
    usercode (:iss (-> token str->jwt :claims)  ) 
    ;;; TO-DO: add check authorization to add 

    result {:res "Success"}
    ]
    
    (db/update-user id name email password role locked picture)
    ;; TO-DO Add check successfull
    result
  )
)


(defn deleteUser [token id]
  (let [
    usercode (:iss (-> token str->jwt :claims)  ) 
    ;;; TO-DO: add check authorization to add 

    result {:res "Success"}
    ]
    
    (db/delete-user id)
    ;; TO-DO Add check successfull
    result
  )
)
