/// <reference path="../references.ts" />

module controller {
    'use strict';

    import User = entities.User;
    import CookieKeys = utils.CookieKeys;

    export class NavBarController {
        private $cookies: angular.cookies.ICookiesService;
        private $window:angular.IWindowService;
        private frontend: service.FrontEndSvc;
        private usermgmt: service.UserManagementSvc;

        private user: User;

        constructor(
            $cookies: angular.cookies.ICookiesService,
            $window:angular.IWindowService,
            frontend: service.FrontEndSvc,
            usermgmt: service.UserManagementSvc) {

            this.$cookies = $cookies;
            this.$window = $window;
            this.frontend = frontend;
            this.usermgmt = usermgmt;

            this.usermgmt.userSetEvent(user => this.user = user);
        }

        public get User() {
            return this.user;
        }

        public Logout() {
            this.$cookies.remove(CookieKeys.SessionIdKey);
            this.$window.location.pathname = '/login';
        }

        //public ViewUser(userId: number = null) {
        //    this.frontend.getUser(userId)
        //        .error(error => console.error("Could not find user: " + userId))
        //        .success((user: User) => this.usermgmt.ViewedUser = user);
        //}
    }
}
