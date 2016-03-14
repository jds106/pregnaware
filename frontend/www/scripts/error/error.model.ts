module error {
    'use strict';

    export interface ErrorScope extends ng.IScope {
        errorDescription: string
        errorUri: string
        errorMsg: string

        mailStr: string

        loginPage: () => void
    }
}