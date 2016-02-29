/// <reference path="../references.ts" />

module controller {
    'use strict';
    import WrappedUser = entities.WrappedUser;
    import EditUserRequest = entities.EditUserRequest;

    interface NewFriendScope extends angular.IScope {
        displayName: string;
        email: string;

        password: string;
        confirmPassword: string;

        passwordMatch: boolean
        passwordMismatch: boolean
    }

    export class NewFriendController {
        private $scope: NewFriendScope;
        private $window: angular.IWindowService;
        private frontend: service.FrontEndSvc;
        private usermgmt: service.UserManagementSvc;

        private user: WrappedUser;

        constructor(
            $scope: NewFriendScope,
            $window: angular.IWindowService,
            frontend: service.FrontEndSvc,
            usermgmt: service.UserManagementSvc) {

            this.$scope = $scope;
            this.$window = $window;
            this.frontend = frontend;
            this.usermgmt = usermgmt;

            // TODO: Extract session from the URL parameter
            this.$scope.password = "";
            this.$scope.confirmPassword = "";
            this.$scope.passwordMatch = false;
            this.$scope.passwordMismatch = false;

            this.$scope.$watch('password', () => NewFriendController.handlePasswordChange($scope));
            this.$scope.$watch('confirmPassword', () => NewFriendController.handlePasswordChange($scope));

            this.usermgmt.userSetEvent(user => {
                this.user = user;

                this.$scope.displayName = this.user.displayName;
                this.$scope.email = this.user.email;
            });
        }

        private static handlePasswordChange(scope: NewFriendScope) {
            if (scope.password != "") {
                scope.passwordMatch = scope.password == scope.confirmPassword;
                scope.passwordMismatch = scope.password != scope.confirmPassword;
            } else {
                scope.passwordMatch = false;
                scope.passwordMismatch = false;
            }
        }

        public saveChanges() {
            var editUserRequest : EditUserRequest = {
                displayName: (this.$scope.displayName != this.user.displayName) ? this.$scope.displayName : null,
                email: (this.$scope.email != this.user.email) ? this.$scope.email : null,
                password: (this.$scope.password != "") ? this.$scope.password : null
            };

            this.frontend.editUser(editUserRequest)
                .error(error => console.error("Failed to edit user", error))
                .success((updatedUser: WrappedUser) => {
                    this.usermgmt.User = updatedUser;
                    this.$window.location.pathname = "/main";
                });
        }
    }
}
