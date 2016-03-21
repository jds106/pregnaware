/// <reference path="../../../../references.ts" />

module main.names.stats.general {
    'use strict';

    export class GeneralStatsController {
        private $scope:GeneralStatsModel;
        private $uibModalInstance: ng.ui.bootstrap.IModalServiceInstance;
        private frontEndService:services.FrontEndService;

        constructor($scope:GeneralStatsModel,
                    $uibModalInstance: ng.ui.bootstrap.IModalServiceInstance,
                    frontEndService:services.FrontEndService) {

            this.$scope = $scope;
            this.$uibModalInstance = $uibModalInstance;
            this.frontEndService = frontEndService;
        }
    }
}
