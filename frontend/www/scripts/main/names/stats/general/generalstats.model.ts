/// <reference path="../../../../references.ts" />

module main.names.stats.general {
    import NameStatByMonth = models.NameStatByMonth;
    'use strict';

    import NameStat = models.NameStat;
    import NameStatByCountry = models.NameStatByCountry;
    import NameStatByRegion = models.NameStatByRegion;

    export interface GeneralStatsModel extends ng.IScope {
        isBoy: boolean
        availableYears: number[]

        nameStats : NameStat[]
        nameStatsByCountry : NameStatByCountry[]
        nameStatsByMonth : NameStatByMonth[]
        nameStatsByRegion : NameStatByRegion[]

        selectYear: (year: number) => void
    }
}