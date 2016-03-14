/// <reference path="../../references.ts" />

module main.account {
    'use strict';

    import WrappedUser = models.WrappedUser;

    export class AccountController {
        private $scope:AccountModel;
        private $uibModalInstance: ng.ui.bootstrap.IModalServiceInstance;
        private routeService: services.RouteService;
        private frontEndService:services.FrontEndService;
        private userService:services.UserService;

        constructor($scope:AccountModel,
                    $uibModalInstance: ng.ui.bootstrap.IModalServiceInstance,
                    routeService: services.RouteService,
                    frontEndService:services.FrontEndService,
                    userService:services.UserService) {

            this.$scope = $scope;
            this.$uibModalInstance = $uibModalInstance;
            this.routeService = routeService;
            this.frontEndService = frontEndService;
            this.userService = userService;

            this.$scope.newPassword = "";
            this.$scope.confirmPassword = "";
            this.$scope.passwordMatch = false;
            this.$scope.passwordMismatch = false;

            // Handle cancelling the changes
            this.$scope.cancelChanges = () => this.$uibModalInstance.dismiss();

            // Handle the persistence of the changes
            this.$scope.saveChanges =
                (newDisplayName:string, originalDisplayName:string,
                 newEmail:string, originalEmail: string, newPassword:string) => {

                    AccountController.saveChanges(
                        this, newDisplayName, originalDisplayName, newEmail, originalEmail, newPassword);
                };

            // Warn when the user's passwords do not match
            this.$scope.$watch('password', () => AccountController.handlePasswordChange($scope));
            this.$scope.$watch('confirmPassword', () => AccountController.handlePasswordChange($scope));

            // Detect changes to the user
            this.userService.userSetEvent(user => {
                if (user) {
                    this.$scope.newDisplayName = user.displayName;
                    this.$scope.originalDisplayName = user.displayName;

                    this.$scope.newEmail = user.email;
                    this.$scope.originalEmail = user.email;

                } else {
                    this.$scope.newDisplayName = null;
                    this.$scope.originalDisplayName = null;

                    this.$scope.newEmail = null;
                    this.$scope.originalEmail = null;
                }
            });
        }

        private static handlePasswordChange(scope:AccountModel) {
            if (scope.newPassword != "") {
                scope.passwordMatch = scope.newPassword == scope.confirmPassword;
                scope.passwordMismatch = scope.newPassword != scope.confirmPassword;
            } else {
                scope.passwordMatch = false;
                scope.passwordMismatch = false;
            }
        }

        private static saveChanges(self:AccountController,
                                   newDisplayName:string, originalDisplayName:string,
                                   newEmail:string, originalEmail:string, newPassword:string) {

            var displayName = (newDisplayName != originalDisplayName) ? newDisplayName : null;
            var email = (newEmail != originalEmail) ? newEmail : null;
            var password = newPassword ? newPassword : null;

            self.frontEndService.editUser(displayName, email, password)
                .error(error => self.routeService.errorPage("Failed to edit user", error))
                .success((updatedUser:WrappedUser) => {
                    self.userService.User = updatedUser;
                    self.$uibModalInstance.close();
                });
        }
    }
}
