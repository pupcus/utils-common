(defproject utils-common "0.0.1-SNAPSHOT"
  
  :description        "common java to clojure utils"
  
  :dependencies       [[org.clojure/clojure "1.2.1"]
                       [org.clojure/clojure-contrib "1.2.0"]
                       [com.lowagie/itext "2.1.7"]
                       [commons-codec "1.4"]
                       [log4j "1.2.15" :exclusions [javax.mail/mail javax.jms/jms com.sun.jdmk/jmxtools com.sun.jmx/jmxri]]
                       [org.slf4j/slf4j-log4j12 "1.6.1"]]

  :dev-dependencies   [[swank-clojure/swank-clojure "1.2.1"]]

  :dev-resources      "dev-resources/")
