module models {
    export interface NameStat {
        name: string
        isBoy: boolean
        year: number
        count: number
        percent: number
    }

    export interface NameStatByCountry extends NameStat {
        country: string
    }

    export interface NameStatByMonth extends NameStat {
        month: string
    }

    export interface NameStatByRegion extends NameStat {
        region: string
    }

    export interface NameSummaryStat {
        year: number
        isBoy: boolean
        count: number
    }
}


