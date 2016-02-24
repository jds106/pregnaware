/// <reference path="references.ts" />

module App {
    // Register the app with Angular
    var app = angular.module('graviditate', ['ngCookies', 'ngAnimate', 'ui.bootstrap']);

    // The Pregnancy Progress control
    app.directive('pregnancyProgress', () : angular.IDirective => {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'html/snippets/pregnancyprogress.html',
            controller: controller.PregnancyProgressController,
            controllerAs: 'ctrl'
        };
    });

    // The Baby Names control
    app.directive('babyNames', () : angular.IDirective => {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'html/snippets/babynames.html',
            controller: controller.BabyNamesController,
            controllerAs: 'ctrl'
        };
    });

    // Register the services
    app.service('frontend', service.FrontEndSvc);
    app.service('usermgmt', service.UserManagementSvc);

    app.controller('LoginCtrl', controller.LoginController);
    app.controller('MainCtrl', controller.MainController);
}