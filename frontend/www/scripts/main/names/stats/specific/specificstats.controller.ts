/// <reference path="../../../../references.ts" />

module main.names.stats.specific {
    'use strict';

    export class SpecificStatsController {
        private $scope:SpecificStatsModel;
        private frontEndService:services.FrontEndService;

        constructor($scope:SpecificStatsModel,
                    name: string,
                    frontEndService:services.FrontEndService) {

            this.$scope = $scope;
            this.frontEndService = frontEndService;

            this.$scope.name = name;
        }
    }
}
