/// <reference path="../references.ts" />

module controller {
    'use strict';

    import User = entities.User;

    export class MainController {
        private $window:angular.IWindowService;
        private frontend: service.FrontEndSvc;
        private usermgmt: service.UserManagementSvc;

        private user: User;

        constructor(
            $window:angular.IWindowService,
            frontend: service.FrontEndSvc,
            usermgmt: service.UserManagementSvc) {

            this.$window = $window;
            this.frontend = frontend;
            this.usermgmt = usermgmt;

            this.usermgmt.userChangedEvent(user => {
                this.user = user;
            });
        }

        public get User() {
            return this.user;
        }

        public LogoutAction() {
            this.frontend.logout()
                .error((error) => {
                    console.error("Unable to log out: " + error)
                })
                .success((response) => {
                    this.$window.location.pathname = '/login';
                });
        }
    }
}