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

            this.frontEndService.getNameStatsYears().success((years : number[]) => this.$scope.availableYears = years);
            this.frontEndService.getNameStatsCount().success((stats : NameSummaryStat[]) => this.nameSummary = stats);

            this.$scope.selectYear = (year) => this.yearSelected(year, isBoy);
        }

        private yearSelected(year: number, isBoy: boolean) {
            var forCountry = this.frontEndService.getNameStatsByCountryForYear(year, isBoy);
            var forMonth = this.frontEndService.getNameStatsByMonthForYear(year, isBoy);
            var forRegion = this.frontEndService.getNameStatsByRegionForYear(year, isBoy);
            var forAll = this.frontEndService.getNameStatsCompleteForYear(year, isBoy);

            forCountry.success((results: NameStatByCountry[]) => this.$scope.nameStatsByCountry = results);
            forMonth.success((results: NameStatByMonth[]) => this.$scope.nameStatsByMonth = results);
            forRegion.success((results: NameStatByRegion[]) => this.$scope.nameStatsByRegion = results);
            forAll.success((results: NameStat[]) => this.$scope.nameStats = results);
        }
    }
}
