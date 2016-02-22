/// <reference path="../references.ts" />

module Controller {
    'use strict';

    import WebRoot = Utils.WebRoot;

    export class LoginController {
        // Current user information populated in login.html
        public loginRequest : LoginRequest = {
            email: "",
            password: "",
            rememberUser: false
        };

        public newUserRequest : NewUserRequest = {
            displayName: "",
            email: "",
            password: ""
        };

        private $http : angular.IHttpService;
        private $cookies: angular.cookies.ICookiesService;
        private $window: angular.IWindowService;

        private static EmailKey: string = "email";

        constructor(
            $http: angular.IHttpService,
            $cookies: angular.cookies.ICookiesService,
            $window: angular.IWindowService) {

            this.$http = $http;
            this.$cookies = $cookies;
            this.$window = $window;

            var email = $cookies.get(LoginController.EmailKey);

            if (email) {
                this.loginRequest.email = email;
            }
        }

        public loginAction() {
            this.$http.post(WebRoot.Url('/FrontEndSvc/login'), this.loginRequest)

                .error((error) => {
                    console.error('Login Error', error);
                })

                .success((sessionId: string) => {
                    console.error('Login sucess', sessionId);
                    this.$cookies.put(LoginController.EmailKey, this.loginRequest.email);
                    this.$cookies.put(WebRoot.SessionIdKey, sessionId);
                    this.$window.location.pathname = '/main'
                });
        }

        public newUserAction() {
            this.$http.put(WebRoot.Url('/FrontEndSvc/newUser'), this.newUserRequest)

                .error((error) => {
                    console.error('New User Error', error);
                })

                .success((sessionId: string) => {
                    this.$cookies.put(LoginController.EmailKey, this.newUserRequest.email);
                    this.$cookies.put(WebRoot.SessionIdKey, sessionId);
                    this.$window.location.pathname = '/main'
                });
        }
    }
}