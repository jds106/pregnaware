/// <reference path="../references.ts" />

module error {
    'use strict';
    import WrappedUser = models.WrappedUser;

    export class ErrorController {
        private $scope: ErrorScope;
        private $location: ng.ILocationService;
        private routeService: services.RouteService;

        constructor($scope: ErrorScope, $location: ng.ILocationService, routeService: services.RouteService) {
            this.$scope = $scope;
            this.$location = $location;
            this.routeService = routeService;

            this.$scope.loginPage = () => this.routeService.loginPage();

            this.$scope.errorDescription = decodeURI($location.search().description);
            this.$scope.errorUri = decodeURI($location.search().uri);
            this.$scope.errorMsg = decodeURI($location.search().msg);

            var subject = `[ERROR] Pregnaware UI Failure`;
            var body =
                `
Description:
${this.$scope.errorDescription}

URI:
${this.$scope.errorUri}

Detail:
${this.$scope.errorMsg}
            `;

            this.$scope.mailStr =
                "mailto:support@pregnaware.co.uk?subject=" + encodeURI(subject) + "&body=" + encodeURI(body);

        }
    }
}