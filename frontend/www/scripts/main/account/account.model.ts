/// <reference path="../../references.ts" />

module main.account {
    'use strict';

    export interface AccountModel extends angular.IScope {
        newDisplayName: string;
        originalDisplayName: string;

        newEmail: string;
        originalEmail: string;

        newPassword: string;
        confirmPassword: string;

        passwordMatch: boolean;
        passwordMismatch: boolean;

        cancelChanges: () => void;

        saveChanges: (newDisplayName:string, originalDisplayName:string,
                      newEmail:string, originalEmail: string,
                      newPassword:string) => void;
    }
}
