module models {
    export interface WrappedFriend {
        userId: number
        displayName: string
        email: string,
        dueDate: LocalDate
        babyNames: WrappedBabyName[]
        friendDate: LocalDate
    }
}