/// <reference path="../references.ts" />

module services {
    'use strict';

    import WrappedUser = models.WrappedUser;
    import WrappedFriend = models.WrappedFriend;

    // User changed handler definition
    type UserSetHandler = (user: WrappedUser) => void;
    type FriendSelectedHandler = (friend: WrappedFriend) => void;

    export class UserService {
        private user: WrappedUser;
        private selectedFriend: WrappedFriend;

        // The list of user-set handlers
        private userListeners : UserSetHandler[] = [];

        // The list of handlers called when the viewed user is changed
        private selectedFriendListeners : FriendSelectedHandler[] = [];

        /** Allow clients to register for the user-set notification (happens once) */
        public userSetEvent(handler: UserSetHandler) {
            this.userListeners.push(handler);

            if (this.user)
                handler(this.user);
        }

        /** Update the current user (and broadcast to all listeners) */
        public set User(user: WrappedUser) {
            this.user = user;
            this.userListeners.forEach(h => h(user));
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
