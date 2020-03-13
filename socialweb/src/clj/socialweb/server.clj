(ns socialweb.server
  (:require [clojure.java.io :as io]
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [environ.core :refer [env]]
            ;;[clj-http.client :as client]
            [clojure.data.json :as json]
            ;;[org.httpkit.client :as http]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [clj-time.core :as t]

            [clojure.java.io :as io]

            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(def custom-formatter (f/formatter "dd/MM/yyyy"))
;;(def apipath "http://10.20.35.21:3000/")
(def apipath "http://localhost:3000/")
(def xlsdir "c:/DEV/Java/")

(def bloombergportdir "c:/DEV/output/")
;(def imagepath "C:/DEV/clojure/sberpb/sberstatic/resources/public/img/tradeidea/")

;; (defn copy-file [source-path dest-path]
;;   (io/copy (io/file source-path) (io/file dest-path)))


;; (defn on-upload-image [request]
;;   (let [tr1 (println request)

;;       filepath (.getAbsolutePath (:tempfile (:file (:params request))))
;;       newfilename (:filename (:file (:params request)))
;;       ;tr1 (println (str "Path to file: " filepath " File name: " newfilename))

;;       tr1 (copy-file (.getAbsolutePath (:tempfile (:file (:params request)))) (str imagepath newfilename))

;;       tr1 (io/delete-file filepath)
;;     ]
;;     {:body { :location (str "/" newfilename)  }}
;;   )
;; )


;; (defn on-save-html [request]
;;   (let [;tr1 (println request)
;;         ;url (str apipath "api/syssetting")
;;         tr1 1 ;(tradeidea/update-tradeidea request)
;;     ]
;;      (response/found "/tradeidea/1")
;;   )
;; )


(defn sort-portfs-by-name [portf1 portf2] 
  (let [
        name1 (first portf1)
        name2 (first portf2)
        ]
    
    (if (<  (compare name1  name2) 0)
    true
    false)
  )
)

(defn comp-positions [position1 position2]
  (let [
    ;tr1 (println (str "pos1= " position1 " pos2= " position2))
    ]
    (if (or
         (> (compare (:assettype position1) (:assettype position2))  0)
         (and (= (compare (:assettype position1) (:assettype position2)) 0) (> (compare (:currency position1) (:currency position2)) 0))
         (and (= (compare (:assettype position1) (:assettype position2)) 0 ) (= (:currency position1) (:currency position2)) (> (:usdvalue position1) (:usdvalue position2)))
      )        
        true
        false
    )
  )
)

(defn comp-deals
  [deal1 deal2]
  (let [;tr1 (println (str "deal1= " deal1 " deal2= " deal2))
            ]
    (if (or
         (> (:date deal1) (:date deal2))
         (and (= (:date deal1) (:date deal2)) (> (compare (:security deal1) (:security deal2)) 0 ))
          ;(> (c/to-long (f/parse custom-formatter (:date deal1))) (c/to-long (f/parse custom-formatter (:date deal2))))
          ;(and (= (c/to-long (f/parse custom-formatter (:date deal1))) (c/to-long (f/parse custom-formatter (:date deal2)))) (> (compare (:security deal1) (:security deal2)) 0 ))
          )
          true
          false
        )
  )
)

(defn map-deal [deal]
  (let [
        ;tr1 (println (str deal))
        trans (loop [result [] trans (get deal "transactions") ]
                (if (seq trans) 
                  (let [
                        tran (first trans)
                        ;tr1 (.log js/console (str "tran: " tran ))
                        ]
                    (recur (conj result {:security (get deal "security") :date (get tran "date" ) :direction (get tran "direction") :nominal (get tran "nominal") :wap (get tran "wap") :wapusd (get tran "wapusd") :waprub (get tran "waprub")})
                         (rest trans))
                  )
                  result)
        )        
        result trans
        
    ]
    ;

    result
  )
)



(defroutes routes
  (GET "/" _
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (io/input-stream (io/resource "public/index.html"))})



  ;;(POST "/tradeidea/imageupload" request (on-upload-image request) )
  ;;(POST "/tradeidea/" request (on-save-html request) )


  (resources "/")
)


(def http-handler
  (-> routes
      (wrap-defaults api-defaults)
      wrap-with-logger
      wrap-gzip))

(defn -main [& [port]]
  (let [
    port (Integer. (or port (env :port) 10555))
    ;tr1 (println (str "token=" (env :token)))
    ]
    (run-jetty http-handler {:port port :join? false})
  )
)
