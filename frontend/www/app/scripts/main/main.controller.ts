/// <reference path="../references.ts" />

module main {
    'use strict';

    export class MainController {

        private static navDirective : ng.IDirective = {
            name: 'navBar',
            controller: main.nav.NavController,
            controllerAs: 'vm',
            scope: {},
            restrict: 'E',
            replace: true,
            templateUrl: '/scripts/main/nav/nav.view.html'
        };

        private static progressDirective : ng.IDirective = {
            name: 'pregnancyProgress',
            controller: main.progress.ProgressController,
            controllerAs: 'vm',
            scope: {},
            restrict: 'E',
            replace: true,
            templateUrl: '/scripts/main/progress/progress.view.html'
        };

        private static namesDirective : ng.IDirective = {
            name: 'names',
            controller: main.names.NamesController,
            controllerAs: 'vm',
            scope: {},
            restrict: 'E',
            replace: true,
            templateUrl: '/scripts/main/names/names.view.html'
        };

        public static directives: ng.IDirective[] = [
            MainController.navDirective,
            MainController.progressDirective,
            MainController.namesDirective,
        ];
    }
}