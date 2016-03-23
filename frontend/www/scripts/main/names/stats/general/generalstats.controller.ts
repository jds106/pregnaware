/// <reference path="../../../../references.ts" />

module main.names.stats.general {
    'use strict';

    import NameStat = models.NameStat;
    import NameStatByCountry = models.NameStatByCountry;
    import NameStatByMonth = models.NameStatByMonth;
    import NameStatByRegion = models.NameStatByRegion;
    import NameSummaryStat = models.NameSummaryStat;

    export class GeneralStatsController {
        private $scope:GeneralStatsModel;
        private $uibModalInstance: ng.ui.bootstrap.IModalServiceInstance;
        private frontEndService:services.FrontEndService;

        private nameSummary: NameSummaryStat[];

        constructor($scope:GeneralStatsModel,
                    isBoy: boolean,
                    $uibModalInstance: ng.ui.bootstrap.IModalServiceInstance,
                    frontEndService:services.FrontEndService) {

            this.$scope = $scope;
            this.$uibModalInstance = $uibModalInstance;
            this.frontEndService = frontEndService;

            this.$scope.isBoy = isBoy;

            this.frontEndService.getNameStatsCount()
                .success((stats : NameSummaryStat[]) => {
                    this.nameSummary = stats;

                    var years = [];
                    stats.forEach(stat => {
                        if (years.indexOf(stat.year) == -1)
                            years.push(stat.year);
                    });

                    this.$scope.availableYears = years.sort((l,r) => r - l);
                    this.$scope.selectedYear = this.$scope.availableYears[0];
                    this.yearSelected(this.$scope.selectedYear, isBoy);
                });

            this.$scope.selectYear = (year) => this.yearSelected(year, isBoy);
            this.$scope.babiesBornInYear = (year) => this.babiesBornInYear(year, isBoy);
            this.$scope.close = () => this.$uibModalInstance.dismiss();
        }

        private yearSelected(year: number, isBoy: boolean) {
            this.$scope.nameStatsByCountry = [];
            this.$scope.nameStatsByMonth = [];
            this.$scope.nameStatsByRegion = [];
            this.$scope.nameStats = [];

            var forCountry = this.frontEndService.getNameStatsByCountryForYear(year, isBoy);
            var forMonth = this.frontEndService.getNameStatsByMonthForYear(year, isBoy);
            var forRegion = this.frontEndService.getNameStatsByRegionForYear(year, isBoy);
            var forAll = this.frontEndService.getNameStatsCompleteForYear(year, isBoy);

            forCountry.success((results: NameStatByCountry[]) => {
                this.addPercent(results, year, isBoy);
                this.$scope.nameStatsByCountry = results;
            });

            forMonth.success((results: NameStatByMonth[]) =>
            {
                this.addPercent(results, year, isBoy);
                this.$scope.nameStatsByMonth = results;
            });

            forRegion.success((results: NameStatByRegion[]) => {
                this.addPercent(results, year, isBoy);
                this.$scope.nameStatsByRegion = results;
            });

            forAll.success((results: NameStat[]) => {
                this.addPercent(results, year, isBoy);
                this.$scope.nameStats = results;
            });
        }

        private addPercent(stats: NameStat[], year: number, isBoy: boolean) {
            if (!this.nameSummary)
                return;

            var summary = this.nameSummary.filter((s) => s.year == year && s.isBoy == isBoy);
            if (summary.length == 0)
                return;

            stats.forEach(s => s.percent = 100 * s.count / summary[0].count);
        }

        private babiesBornInYear(year: number, isBoy: boolean) : number {
            if (!this.nameSummary)
                return 0;

            var summary = this.nameSummary.filter((s) => s.year == year && s.isBoy == isBoy);
            return summary.length == 0 ? 0 : summary[0].count;
        }
    }
}
