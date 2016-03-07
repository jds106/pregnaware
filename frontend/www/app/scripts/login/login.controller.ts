/// <reference path="../references.ts" />

module login {
    'use strict';

    export class LoginController {
        private $scope: LoginScope;
        private $location: ng.ILocationService;
        private frontEndService: services.FrontEndService;
        private routeService: services.RouteService;

        constructor(
            $scope: LoginScope, $location: ng.ILocationService,
            frontEndService: services.FrontEndService, routeService: services.RouteService) {

            this.$scope = $scope;
            this.$location = $location;
            this.frontEndService = frontEndService;
            this.routeService = routeService;

            $scope.showLogin = () => {
                $scope.isRegisterVisible = false;
                $scope.isLoginVisible = true;
            };

            $scope.showRegister = function() {
                $scope.isLoginVisible = false;
                $scope.isRegisterVisible = true;
            };

            $scope.login = () => {
                this.frontEndService.login($scope.email, $scope.password)
                    .success(() => this.routeService.mainPage())
                    .error((e) => console.error('Failed to log in', e));
            };

            $scope.register = function() {
                this.frontEndService.newUser($scope.displayName, $scope.email, $scope.password)
                    .success(() => this.routeService.mainPage())
                    .error((e) => console.error('Failed to log in', e));
            };
        }
    }
}