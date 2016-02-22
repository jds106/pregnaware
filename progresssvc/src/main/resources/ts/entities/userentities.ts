/// <reference path="../references.ts" />
module Controller {
    'use strict';

    export interface NewUserRequest {
        displayName: string,
        email: string,
        password: string
    }

    // Populated and sent to the server to request a session
    export interface LoginRequest {
        email: string
        password: string
        rememberUser: boolean
    }

    export interface LocalDate {
        year: number
        month: number
        day: number
    }

    export interface ProgressModel {
        dueDate: LocalDate
        daysPassed: number
        daysRemaining: number
    }
}