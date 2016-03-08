/// <reference path="../../references.ts" />

module main.nav {
    'use strict';

    import WrappedFriend = models.WrappedFriend;
    import WrappedUser = models.WrappedUser;

    export interface NavModel extends ng.IScope {

        user: WrappedUser;

        /* Report the user's local */
        locale: string;

        /* Modify the current user's account settings */
        updateAccountSettings: () => void;

        /* Send a friend request */
        addFriend: () => void;

        /* Log the current user out and return to the login screen */
        logout: () => void;

        /* Switch the view to the current user */
        viewUser: () => void;

        /* Switch the view to the selected friend */
        viewFriend: (friend: WrappedFriend) => void;

        /* Confirm an existing friend request */
        confirmFriendRequest: (friend: WrappedFriend) => void;

        /* Ignore a friend request */
        ignoreFriendRequest: (friend: WrappedFriend) => void;
    }
}
