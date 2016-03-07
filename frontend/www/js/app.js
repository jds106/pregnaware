/// <reference path="typings/tsd.d.ts" />
/// <reference path="app.ts" /> 
/// <reference path="references.ts" />
var App;
(function (App) {
    // Register the app with Angular
    var app = angular.module('pregnaware', ['ngRoute', 'ngCookies', 'ngAnimate', 'ui.bootstrap']);
    app.config(RouteProvider.setRoutes);
    var RouteProvider = (function () {
        function RouteProvider() {
        }
        RouteProvider.setRoutes = function ($routeProvider, $locationProvider) {
            $routeProvider.when('/Login', {
                template: '<div>Found login!</div>'
            });
            $locationProvider.html5Mode(true);
        };
        return RouteProvider;
    })();
})(App || (App = {}));
//# sourceMappingURL=app.js.map