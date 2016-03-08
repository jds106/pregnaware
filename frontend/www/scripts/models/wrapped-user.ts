module models {
    export interface WrappedUser {
        userId: number
        displayName: string
        email: string
        dueDate: LocalDate
        joinedDate: LocalDate
        lastAccessedTime: number,
        babyNames: WrappedBabyName[]
        friends: WrappedFriend[]
        friendRequestsSent: WrappedFriend[]
        friendRequestsReceived: WrappedFriend[]
    }
}