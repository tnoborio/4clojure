(ns foreclojure.register
  (:import [org.jasypt.util.password StrongPasswordEncryptor])
  (:use [foreclojure.utils]
        [somnium.congomongo]
        (hiccup [form-helpers]))
  (:require [sandbar.stateful-session :as session]
            (ring.util [response :as response])))

(defn form-row [[type name info]]
  [:tr
   [:td (label name info)]
   [:td (type name)]])

(def-page register-page []
  [:div {:class "error"} (session/flash-get :error)]
  (form-to [:post "/register"]
           [:table
            (map form-row
                 [[text-field :user "Username (4-12 chars.)"]
                  [password-field :pwd "Password (7-12 chars.)"]
                  [password-field :repeat-pwd "Repeat Password"]
                  [text-field :email "Email"]])
            [:tr
             [:td (submit-button {:type "image" :src "/register.png"} "Register")]]]))

(defn do-register [user pwd repeat-pwd email]
  (assuming [(nil? (fetch-one :users :where {:user user}))
             "User already exists",
             (< 3 (.length user) 13)
             "Username must be 4-12 characters long",
             (< 6 (.length pwd) 13)
             "Password must be 7-12 characters long",
             (= pwd repeat-pwd)
             "Passwords don't match",
             (not (empty? email))
             "Please enter a valid email address"]
    (do
      (insert! :users
               {:user user
                :pwd (.encryptPassword (StrongPasswordEncryptor.) pwd)
                :email email})
      (session/session-put! :user user)
      (response/redirect "/"))
    (flash-error why "/register")))
