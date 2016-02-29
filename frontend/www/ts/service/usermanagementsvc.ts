/// <reference path="../references.ts" />

module service {
    'use strict';

    import WrappedUser = entities.WrappedUser;
    import WrappedFriend = entities.WrappedFriend;

    // User changed handler definition
    type UserSetHandler = (user: WrappedUser) => void;
    type FriendSelectedHandler = (friend: WrappedFriend) => void;

    export class UserManagementSvc {
        private frontend: service.FrontEndSvc;

        private user: WrappedUser;
        private selectedFriend: WrappedFriend;

        // The list of user-set handlers
        private userListeners : UserSetHandler[] = [];

        // The list of handlers called when the viewed user is changed
        private selectedFriendListeners : FriendSelectedHandler[] = [];

        constructor(frontend: service.FrontEndSvc) {
            this.frontend = frontend;

            this.frontend.getUser()
                .error((error) => console.error('Could not find user name', error))
                .success((response : WrappedUser) => this.User = response);
        }

        /** Allow clients to register for the user-set notification (happens once) */
        public userSetEvent(handler: UserSetHandler) {
            this.userListeners.push(handler);

            if (this.user)
                handler(this.user);
        }

        /** Update the current user (and broadcast to all listeners) */
        public set User(user: WrappedUser) {
            if (this.user) {
                throw new Error("Cannot change user once logged in");
            } else {
                this.user = user;
                this.userListeners.forEach(h => h(user));
            }
        }

        /** Register a client to subscribe to the viewed user being changed */
        public friendSelectedEvent(handler: FriendSelectedHandler) {
            this.selectedFriendListeners.push(handler);

            if (this.selectedFriend)
                handler(this.selectedFriend);
        }

        /** Update the current user (and broadcast to all listeners) */
        public set Friend(friend: WrappedFriend) {
            this.selectedFriend = friend;
            this.selectedFriendListeners.forEach(h => h(friend));
        }
    }
}
