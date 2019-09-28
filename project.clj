(defproject utils-common "0.1.2-SNAPSHOT"

  :description "various utilities"

  :url "https://bitbucket.org/pupcus/utils-common"

  :scm {:url "git@bitbucket.org:pupcus/utils-common"}

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/tools.logging "0.5.0"]
                 [com.lowagie/itext "2.1.7"]
                 [commons-codec "1.13"]
                 [hiccup "1.0.5"]]

  :profiles {:dev  {:resource-paths ["dev-resources"]
                    :dependencies [[org.clojure/clojure "1.10.1"]
                                   [org.slf4j/slf4j-log4j12 "1.7.25"]]}}

  :deploy-repositories {"releases" {:url "https://repo.clojars.org" :creds :gpg :sign-releases false}
                        "snapshots" {:url "https://repo.clojars.org" :creds :gpg :sign-releases false}}


  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
