/// <reference path="../references.ts" />

module controller {
    import CookieKeys = utils.CookieKeys;
    import LoginRequest = entities.LoginRequest;
    import AddUserRequest = entities.AddUserRequest;
    'use strict';

    export class LoginController {
        // Current user information populated in login.html
        public loginRequest : LoginRequest = {
            email: "",
            password: "",
            rememberUser: false
        };

        public newUserRequest : AddUserRequest = {
            displayName: "",
            email: "",
            password: ""
        };

        private $cookies: angular.cookies.ICookiesService;
        private $location: angular.ILocationService;
        private frontend: service.FrontEndSvc;

        constructor(
            $cookies: angular.cookies.ICookiesService,
            $location: angular.ILocationService,
            frontend: service.FrontEndSvc) {

            this.$cookies = $cookies;
            this.$location = $location;
            this.frontend = frontend;

            var email = $cookies.get(CookieKeys.EmailKey);

            if (email) {
                this.loginRequest.email = email;
            }
        }

        public loginAction() {
            this.frontend.login(this.loginRequest)
                .error((error) => {
                    console.error('Login Error', error);
                })
                .success((sessionId: string) => {
                    this.$cookies.put(CookieKeys.EmailKey, this.loginRequest.email);
                    this.$cookies.put(CookieKeys.SessionIdKey, sessionId);
                    this.$location.path = '/main'
                });
        }

        public newUserAction() {
            this.frontend.newUser(this.newUserRequest)
                .error((error) => {
                    console.error('New User Error', error);
                })
                .success((sessionId: string) => {
                    this.$cookies.put(CookieKeys.EmailKey, this.newUserRequest.email);
                    this.$cookies.put(CookieKeys.SessionIdKey, sessionId);
                    this.$location.path = '/main'
                });
        }
    }
}