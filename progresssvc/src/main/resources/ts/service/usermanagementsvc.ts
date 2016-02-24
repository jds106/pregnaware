/// <reference path="../references.ts" />

module service {
    'use strict';

    import User = entities.User;

    // User changed handler definition
    type UserChangedHandler = (user: User) => void;

    export class UserManagementSvc {
        private frontend: service.FrontEndSvc;

        private user: User;

        // The list of user-update handlers
        private userListeners : UserChangedHandler[] = [];

        constructor(frontend: service.FrontEndSvc) {
            this.frontend = frontend;

            this.frontend.getUser()
                .error((error) => {
                    console.error('Could not find user name', error);
                })
                .success((response: User) => {
                    this.user = response;
                    this.userListeners.forEach(h => h(this.user));
                });
        }

        /** Allow clients to register for user-changed events */
        public userChangedEvent(handler: UserChangedHandler) {
            this.userListeners.push(handler);

            if (this.user)
                handler(this.user);
        }

        /** Get the current user */
        public get User() {
            return this.user;
        }

        /** Update the current user (and broadcast to all listeners) */
        public set User(user: User) {
            this.user = user;
            this.userListeners.forEach(h => h(user));
        }
    }
}
