/// <reference path="../../references.ts" />

module main.nav {
    'use strict';

    import WrappedFriend = models.WrappedFriend;
    import WrappedUser = models.WrappedUser;

    export interface NavModel extends ng.IScope {

        user: WrappedUser;

        logout: () => void;
        viewUser: (userId: string) => void;

        updateAccountSettings: () => void;
    }
}
