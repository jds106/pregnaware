/// <reference path="../references.ts" />

module login {
    'use strict';
    import WrappedUser = models.WrappedUser;

    export class LoginController {
        private $scope: LoginScope;
        private $location: ng.ILocationService;
        private frontEndService: services.FrontEndService;

        constructor($scope: LoginScope, $location: ng.ILocationService, frontEndService: services.FrontEndService) {
            this.$scope = $scope;
            this.$location = $location;
            this.frontEndService = frontEndService;

            $scope.showLogin = () => {
                $scope.isRegisterVisible = false;
                $scope.isLoginVisible = true;
            };

            $scope.showRegister = () => {
                $scope.isLoginVisible = false;
                $scope.isRegisterVisible = true;
            };

            $scope.login = () => {
                this.frontEndService.login($scope.email, $scope.password)
            };

            $scope.register = () => {
                this.frontEndService.newUser($scope.displayName, $scope.email, $scope.password)
            };
        }
    }
}