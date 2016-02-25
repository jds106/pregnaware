/// <reference path="../references.ts" />

module service {
    'use strict';

    import User = entities.User;

    // User changed handler definition
    type UserChangedHandler = (user: User) => void;

    export class UserManagementSvc {
        private frontend: service.FrontEndSvc;

        private user: User;
        private viewedUser: User;

        // The list of user-set handlers
        private userListeners : UserChangedHandler[] = [];

        // The list of handlers called when the viewed user is changed
        private viewedUserListeners : UserChangedHandler[] = [];

        constructor(frontend: service.FrontEndSvc) {
            this.frontend = frontend;

            this.frontend.getUser()
                .error((error) => console.error('Could not find user name', error))
                .success((response: User) => this.User = response);
        }

        /** Allow clients to register for the user-set notification (happens once) */
        public userSetEvent(handler: UserChangedHandler) {
            this.userListeners.push(handler);

            if (this.user)
                handler(this.user);
        }

        /** Update the current user (and broadcast to all listeners) */
        public set User(user: User) {
            if (this.user) {
                throw new Error("Cannot change user once logged in");
            } else {
                this.user = user;
                this.userListeners.forEach(h => h(user));

                // Also update the viewed user to be the same as the current user
                this.ViewedUser = user;
            }
        }

        /** Register a client to subscribe to the viewed user being changed */
        public viewedUserChangedEvent(handler: UserChangedHandler) {
            this.viewedUserListeners.push(handler);

            if (this.viewedUser)
                handler(this.viewedUser);
        }

        /** Update the current user (and broadcast to all listeners) */
        public set ViewedUser(viewedUser: User) {
            this.viewedUser = viewedUser;
            this.viewedUserListeners.forEach(h => h(viewedUser));
        }
    }
}
