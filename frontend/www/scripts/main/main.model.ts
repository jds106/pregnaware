module main {
    'use strict';

    export interface MainModel extends ng.IScope {
        isLoggedIn: boolean;
        isError: boolean;
    }
}