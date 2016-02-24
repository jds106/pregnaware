/// <reference path="../references.ts" />
module entities {
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

    export interface User {
        userId: number
        displayName: string
        email: string
        friends: Friend[]
    }

    export interface Friend {
        userId: number
        displayName: string
        email: string
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

    export interface NamingEntry {
        gender: string
        name: string
        suggestedBy: string
    }

    export interface NamingEntries {
        entries: NamingEntry[]
    }
}