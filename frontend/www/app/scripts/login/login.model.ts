module login {
    'use strict';

    export interface LoginScope extends ng.IScope {
        displayName: string
        email: string
        password: string

        isLoginVisible : boolean
        isRegisterVisible: boolean

        showLogin: () => void
        showRegister: () => void

        login: () => void
        register: () => void
    }
}