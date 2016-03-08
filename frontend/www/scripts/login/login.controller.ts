/// <reference path="../references.ts" />

module login {
    'use strict';
    import WrappedUser = models.WrappedUser;

    export class LoginController {
        private $scope: LoginScope;
        private $location: ng.ILocationService;
        private frontEndService: services.FrontEndService;
        private userService: services.UserService;
        private routeService: services.RouteService;

        constructor(
            $scope: LoginScope, $location: ng.ILocationService,
            frontEndService: services.FrontEndService, userService: services.UserService,
            routeService: services.RouteService) {

            this.$scope = $scope;
            this.$location = $location;
            this.frontEndService = frontEndService;
            this.userService = userService;
            this.routeService = routeService;

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
                    .error((e) => console.error('Failed to log in', e))
                    .success(() => this.getUserAndOpenMain());
            };

            $scope.register = () => {
                this.frontEndService.newUser($scope.displayName, $scope.email, $scope.password)
                    .error((e) => console.error('Failed to register in', e))
                    .success(() => this.getUserAndOpenMain());
            };
        }

        private getUserAndOpenMain() {
            this.frontEndService.getUser()
                .error((e) => console.error('Failed to get user'))
                .success((user: WrappedUser) => {
                    this.userService.User = user;
                    this.routeService.mainPage();
                });
        }
    }
}