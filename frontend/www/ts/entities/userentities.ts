/// <reference path="../references.ts" />
module entities {
    'use strict';

    // ---- Requests ----
    export interface LoginRequest {
        email: string
        password: string
        rememberUser: boolean
    }

    export interface AddUserRequest {
        displayName: string
        email: string
        password: string
    }

    export interface EditUserRequest {
        displayName: string
        email: string
        password: string
    }

    // ---- Responses ----

    export interface WrappedUser {
        userId: number
        displayName: string
        email: string
        dueDate: LocalDate
        joinedDate: LocalDate
        babyNames: WrappedBabyName[]
        friends: WrappedFriend[]
        friendRequestsSent: WrappedFriendToBe[]
        friendRequestsReceived: WrappedFriendToBe[]
    }

    export interface WrappedBabyName {
        nameId: number
        userId: number
        suggestedBy: number
        suggestedByName: string
        name: string
        isBoy: boolean
    }

    export interface WrappedFriend {
        userId: number
        displayName: string
        email: string,
        dueDate: LocalDate
        babyNames: WrappedBabyName[]
    }

    export interface WrappedFriendToBe {
        userId: number
        displayName: string
        email: string
    }

    export interface LocalDate {
        year: number
        month: number
        day: number
    }
}