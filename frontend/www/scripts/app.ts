/// <reference path="references.ts" />

module App {
    // Register the app with Angular
    import ITemplateCacheService = angular.ITemplateCacheService;
    var app = angular.module('pregnaware', ['ngRoute', 'ngCookies', 'ngAnimate', 'ui.bootstrap']);

    app.service('frontEndService', services.FrontEndService);
    app.service('routeService', services.RouteService);
    app.service('userService', services.UserService);

    // Configure the application routes
    app.config(services.RouteConfig);

    // Add the directives required by the main view
    main.MainController.directives.forEach((d: ng.IDirective) => app.directive(d.name, () => d));

    // Initialise the controller for the index.html page
    app.controller('IndexController', IndexController);

    //app.run(function($templateCache: ITemplateCacheService) => { $templateCache.removeAll();})
}