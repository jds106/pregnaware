/// <reference path="references.ts" />
module App {
    // Register the app with Angular
    var app = angular.module('graviditate', ['ngCookies', 'ngAnimate', 'ui.bootstrap']);

    // Render if due date available
    app.directive('dueDateSet', function() {
        return { restrict: 'E', replace: true, templateUrl: 'html/snippets/DueDateSet.html' }
    });

    // Render if due date missing
    app.directive('dueDateMissing', function() {
        return { restrict: 'E', replace: true, templateUrl: 'html/snippets/DueDateMissing.html' }
    });

    app.controller('LoginCtrl', Controller.LoginController);
    app.controller('MainCtrl', Controller.MainController);
}