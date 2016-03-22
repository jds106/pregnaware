/// <reference path="../../../../references.ts" />

module main.names.stats.specific {
    'use strict';

    export class SpecificStatsController {
        private $scope:SpecificStatsModel;
        private frontEndService:services.FrontEndService;

        constructor($scope:SpecificStatsModel,
                    isBoy: boolean,
                    name: string,
                    frontEndService:services.FrontEndService) {

            this.$scope = $scope;
            this.frontEndService = frontEndService;

            this.$scope.isBoy = isBoy;
            this.$scope.name = name;
        }
    }
}
