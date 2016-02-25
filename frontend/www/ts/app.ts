/// <reference path="references.ts" />

module App {
    // Register the app with Angular
    var app = angular.module('graviditate', ['ngCookies', 'ngAnimate', 'ui.bootstrap']);

    // The common nav bar
    app.directive('navBar', () : angular.IDirective => {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'html/snippets/navbar.html',
            controller: controller.NavBarController,
            controllerAs: 'ctrl',
            scope: {
                navBarScope: '=navBar'
            }
        };
    });

    // The common footer
    app.directive('commonFooter', () : angular.IDirective => {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'html/snippets/footer.html'
        };
    });

    // The Pregnancy Progress control
    app.directive('pregnancyProgress', () : angular.IDirective => {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'html/snippets/pregnancyprogress.html',
            controller: controller.PregnancyProgressController,
            controllerAs: 'ctrl',
            scope: {
                pregnancyProgressScope: '=pregnancyProgress'
            }
        };
    });

    // The Baby Names control
    app.directive('babyNames', () : angular.IDirective => {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'html/snippets/babynames.html',
            controller: controller.BabyNamesController,
            controllerAs: 'ctrl',
            scope: {
                babyNamesScope: '=babyNames'
            }
        };
    });

    // Register the services
    app.service('frontend', service.FrontEndSvc);
    app.service('usermgmt', service.UserManagementSvc);

    app.controller('LoginCtrl', controller.LoginController);
    app.controller('MainCtrl', controller.MainController);
    app.controller('AccountCtrl', controller.AccountController);
    app.controller('NewFriendCtrl', controller.NewFriendController);
    app.controller('ShareCtrl', controller.ShareController);
}