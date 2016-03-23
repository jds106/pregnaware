/// <reference path="../../../../references.ts" />

module main.names.stats.specific {
    'use strict';

    import NameStat = models.NameStat;
    import NameStatByCountry = models.NameStatByCountry;
    import NameStatByMonth = models.NameStatByMonth;
    import NameStatByRegion = models.NameStatByRegion;
    import NameSummaryStat = models.NameSummaryStat;

    export class SpecificStatsController {
        private $scope:SpecificStatsModel;
        private $uibModalInstance: ng.ui.bootstrap.IModalServiceInstance;
        private frontEndService:services.FrontEndService;

        private nameSummary: NameSummaryStat[];

        constructor($scope:SpecificStatsModel,
                    isBoy: boolean,
                    name: string,
                    $uibModalInstance: ng.ui.bootstrap.IModalServiceInstance,
                    frontEndService:services.FrontEndService) {

            this.$scope = $scope;
            this.$uibModalInstance = $uibModalInstance;
            this.frontEndService = frontEndService;

            this.$scope.isBoy = isBoy;
            this.$scope.name = name;

            this.$scope.orderedMonths = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

            this.$scope.orderedRegions = [
                'North West', 'North East', 'South East', 'South West', 'East Midlands', 'West Midlands',
                'East', 'Wales', 'London', 'Yorkshire and The Humber'
            ];

            this.$scope.orderedCountries = ['England', 'Wales'];

            this.frontEndService.getNameStatsCount()
                .success((stats : NameSummaryStat[]) => {
                    this.nameSummary = stats;
                });

            this.frontEndService.getNameStatsCompleteForName(name, isBoy)
                .success((stats: NameStat[]) => {
                    this.addPercent(stats);
                    this.$scope.nameStats = stats;

                    var years = [];
                    stats.forEach(stat => {
                        if (years.indexOf(stat.year) == -1)
                            years.push(stat.year);
                    });

                    this.$scope.availableYears = years.sort((l,r) => r - l);
                });

            this.frontEndService.getNameStatsByCountryForName(name, isBoy)
                .success((stats: NameStatByCountry[]) => this.$scope.nameStatsByCountry = stats);

            this.frontEndService.getNameStatsByMonthForName(name, isBoy)
                .success((stats: NameStatByMonth[]) => this.$scope.nameStatsByMonth = stats);

            this.frontEndService.getNameStatsByRegionForName(name, isBoy)
                .success((stats: NameStatByRegion[]) => this.$scope.nameStatsByRegion = stats);

            this.$scope.getNameCountForYearMonth = (year, month) => this.getNameCountForYearMonth(year, month);
            this.$scope.getNameCountForYearRegion = (year, region) => this.getNameCountForYearRegion(year, region);
            this.$scope.getNameCountForYearCountry = (year, country) => this.getNameCountForYearCountry(year, country);

            this.$scope.floor = (x) => Math.floor(x);
            this.$scope.close = () => this.$uibModalInstance.dismiss();
        }

        private addPercent(stats: NameStat[]) {
            if (!this.nameSummary)
                return;

            stats.forEach((stat) => {
                var summary = this.nameSummary.filter((s) => s.year == stat.year && s.isBoy == stat.isBoy);
                if (summary.length == 0)
                    stat.percent = 100;
                else
                    stat.percent = 100 * stat.count / summary[0].count;

            });
        }

        private getNameCountForYearMonth(year: number, month: string) : number {
            if (!this.$scope.nameStatsByMonth)
                return NaN;

            var stat = this.$scope.nameStatsByMonth.filter(s => s.year == year && s.month == month);
            return stat.length > 0 ? stat[0].count : NaN;
        }

        private getNameCountForYearRegion(year: number, region: string) : number {
            if (!this.$scope.nameStatsByRegion)
                return NaN;

            var stat = this.$scope.nameStatsByRegion.filter(s => s.year == year && s.region == region);
            return stat.length > 0 ? stat[0].count : null;
        }

        private getNameCountForYearCountry(year: number, country: string) : number {
            if (!this.$scope.nameStatsByCountry)
                return NaN;

            var stat = this.$scope.nameStatsByCountry.filter(s => s.year == year && s.country == country);
            return stat.length > 0 ? stat[0].count : NaN;
        }
    }
}
