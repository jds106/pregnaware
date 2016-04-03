/// <reference path="references.ts" />

module App {
    // Register the app with Angular
    import ITemplateCacheService = angular.ITemplateCacheService;
    var app = angular.module('pregnaware', ['ngCookies', 'ngAnimate', 'ui.bootstrap']);

    app.service('errorService', services.ErrorService);
    app.service('frontEndService', services.FrontEndService);
    app.service('userService', services.UserService);
    app.service('stateService', services.StateService);

    // The root directive to launch the app
    app.directive('root', () => <ng.IDirective>{
        controller: main.MainController,
        restrict: 'E',
        replace: true,
        templateUrl: '/scripts/main/main.view.html'
    });

    // Add the directives required by the main view
    main.MainController.directives.forEach((d:ng.IDirective) => app.directive(d.name, () => d));
}