/// <reference path="../references.ts" />

module services {
    'use strict';

    export class RouteConfig {
        constructor($routeProvider: ng.route.IRouteProvider, $locationProvider: ng.ILocationProvider) {

            $routeProvider.when('/login', <ng.route.IRoute>{
                templateUrl: '/scripts/login/login.view.html',
                controller: login.LoginController,
            });

            $routeProvider.when('/main', <ng.route.IRoute>{
                templateUrl: '/scripts/main/main.view.html',
                controller: main.MainController
            });

            $routeProvider.otherwise(<ng.route.IRoute>{
                redirectTo: '/main'
            });

            // The server rewrites all URLs to point at the index.html, so enable HTML5 for pretty URLs
            $locationProvider.html5Mode({enabled: true});
        }
    }

    export class RouteService {
        private $location: ng.ILocationService;
        constructor($location: ng.ILocationService) {
            this.$location = $location;
        }

        public mainPage() {
            this.$location.path('/main');
        }

        public loginPage() {
            this.$location.path('/login');
        }
    }
}