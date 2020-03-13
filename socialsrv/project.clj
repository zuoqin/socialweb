(defproject socialsrv "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo" :username [:env/my_datomic_username]
                        :password [:env/my_datomic_password]}}
  :dependencies [[bouncer "1.0.0"]
                 [ch.qos.logback/logback-classic "1.1.7"]
                 ;;[com.datomic/datomic-free "0.9.5394" :exclusions [org.slf4j/log4j-over-slf4j org.slf4j/slf4j-nop]]
                 [com.datomic/datomic-pro "0.9.5350" :exclusions [org.slf4j/log4j-over-slf4j org.slf4j/slf4j-nop]]
                 [org.flatland/ordered "1.5.7"]
                 [jakarta.xml.bind/jakarta.xml.bind-api "2.3.3"]
                 [compojure "1.5.1"]
                 [cprop "0.1.9"]
                 [luminus-http-kit "0.1.4"]
                 [luminus-nrepl "0.1.4"]
                 [luminus/ring-ttl-session "0.3.1"]
                 [markdown-clj "0.9.91"]
                 [metosin/compojure-api "1.1.9"]
                 [metosin/ring-http-response "0.8.0"]
                 [mount "0.1.11"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.webjars.bower/tether "1.3.7"]
                 [org.webjars/bootstrap "4.0.0-alpha.5"]
                 [org.webjars/font-awesome "4.7.0"]
                 [org.webjars/jquery "3.1.1"]
                 [ring/ring-json "0.4.0"]
                 [ring-middleware-format "0.7.0"]
                 [ring-webjars "0.1.1"]
                 [ring/ring-core "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [selmer "1.10.5"]
                 [com.draines/postal "2.0.2"]
                 [clj-jwt "0.1.1"]
                 [cheshire "5.8.0"]
                 [jumblerg/ring.middleware.cors "1.0.1"]        
                 ]

  :min-lein-version "2.0.0"

  :jvm-opts ["-server" "-Dconf=.lein-env"]
  :source-paths ["src/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main socialsrv.core

  :plugins [[lein-cprop "1.0.1"]]

  :profiles
  {:uberjar {:omit-source true
             :aot :all
             :uberjar-name "socialsrv.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:dependencies [[prone "1.1.4"]
                                 [ring/ring-mock "0.3.2"]
                                 [ring/ring-devel "1.5.0"]
                                 [pjstadig/humane-test-output "0.8.1"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.18.1"]]
                  
                  :source-paths ["env/dev/clj" "test/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:resource-paths ["env/test/resources"]}
   :profiles/dev {}
   :profiles/test {}})
