module models {
    export interface WrappedBabyName {
        nameId: number
        userId: number
        suggestedBy: number
        suggestedByName: string
        suggestedDate: LocalDate
        name: string
        isBoy: boolean
    }
}