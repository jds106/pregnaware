/// <reference path="../references.ts" />

module main {
    'use strict';

    export class MainController {

        // Sub-components of the "Main" view
        private static navDirective:ng.IDirective = {
            name: 'navBar',
            controller: main.nav.NavController,
            scope: {},
            restrict: 'E',
            replace: true,
            templateUrl: '/scripts/main/nav/nav.view.html'
        };

        private static progressDirective:ng.IDirective = {
            name: 'pregnancyProgress',
            controller: main.progress.ProgressController,
            scope: {},
            restrict: 'E',
            replace: true,
            templateUrl: '/scripts/main/progress/progress.view.html'
        };

        private static namesDirective:ng.IDirective = {
            name: 'names',
            controller: main.names.NamesController,
            scope: {},
            restrict: 'E',
            replace: true,
            templateUrl: '/scripts/main/names/names.view.html'
        };

        // Login
        private static loginDirective:ng.IDirective = {
            name: 'login',
            controller: login.LoginController,
            scope: {},
            restrict: 'E',
            replace: true,
            templateUrl: '/scripts/login/login.view.html'
        };

        // Error
        private static errorDirective:ng.IDirective = {
            name: 'error',
            controller: error.ErrorController,
            scope: {},
            restrict: 'E',
            replace: true,
            templateUrl: '/scripts/error/error.view.html'
        };

        public static directives:ng.IDirective[] = [
            MainController.navDirective,
            MainController.progressDirective,
            MainController.namesDirective,

            MainController.loginDirective,
            MainController.errorDirective
        ];

        constructor($scope: MainModel, userService:services.UserService, errorService: services.ErrorService) {
            $scope.isLoggedIn = false;
            $scope.isError = false;

            // Successfully fetching a user implies the logged-in state is true
            userService.userSetEvent((user) => $scope.isLoggedIn = (user != null));

            // Set and clear the error message
            errorService.errorEvent(<services.ErrorHandler>{
                onErrorSet: (url, description, error) => $scope.isError = true,
                onErrorClear: () => $scope.isError = false
            });
        }
    }
}