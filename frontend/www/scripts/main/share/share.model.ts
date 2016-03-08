/// <reference path="../../references.ts" />

module main.share {
    'use strict';

    export interface ShareModel extends angular.IScope {
        friendEmail: string;
        mailToLink: string;

        friendDisplayName: string;

        showExistingUserSuccess: boolean;
        showNewUserSuccess: boolean;

        /* Attempts to add a friend */
        share: (email: string) => void;

        /* Close the dialog */
        close: () => void;
    }
}
