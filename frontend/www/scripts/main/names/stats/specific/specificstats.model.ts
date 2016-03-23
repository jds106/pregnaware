/// <reference path="../../../../references.ts" />

module main.names.stats.specific {
    'use strict';

    export interface SpecificStatsModel extends ng.IScope {
        name: string
        isBoy: boolean
        availableYears: number[]
        orderedMonths: string[]
        orderedRegions: string[]
        orderedCountries: string[]

        nameStats: NameStat[]
        nameStatsByCountry: NameStatByCountry[]
        nameStatsByMonth: NameStatByMonth[]
        nameStatsByRegion: NameStatByRegion[]

        getNameCountForYearMonth: (year: number, month: string) => number
        getNameCountForYearRegion: (year: number, region: string) => number
        getNameCountForYearCountry: (year: number, country: string) => number

        floor: (number) => number
        close: () => void
    }
}