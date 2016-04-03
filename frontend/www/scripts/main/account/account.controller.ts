/// <reference path="../../references.ts" />

module main.account {
    'use strict';

    import WrappedUser = models.WrappedUser;

    export class AccountController {
        private $scope:AccountModel;
        private $uibModalInstance: ng.ui.bootstrap.IModalServiceInstance;
        private frontEndService:services.FrontEndService;
        private userService:services.UserService;
        private errorService: services.ErrorService;

        constructor($scope:AccountModel,
                    $uibModalInstance: ng.ui.bootstrap.IModalServiceInstance,
                    frontEndService:services.FrontEndService,
                    userService:services.UserService,
                    errorService: services.ErrorService) {

            this.$scope = $scope;
            this.$uibModalInstance = $uibModalInstance;
            this.frontEndService = frontEndService;
            this.userService = userService;
            this.errorService = errorService;

            this.$scope.newPassword = "";
            this.$scope.confirmPassword = "";
            this.$scope.passwordMatch = false;
            this.$scope.passwordMismatch = false;

            // Handle cancelling the changes
            this.$scope.cancelChanges = () => this.$uibModalInstance.dismiss();

            // Handle the persistence of the changes
            this.$scope.saveChanges =
                (newDisplayName:string, originalDisplayName:string,
                 newEmail:string, originalEmail: string, newPassword:string) =>
                    this.saveChanges(newDisplayName, originalDisplayName, newEmail, originalEmail, newPassword);

            // Warn when the user's passwords do not match
            this.$scope.$watch('password', () => this.handlePasswordChange());
            this.$scope.$watch('confirmPassword', () => this.handlePasswordChange());

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

        private handlePasswordChange() {
            if (this.$scope.newPassword != "") {
                this.$scope.passwordMatch = this.$scope.newPassword == this.$scope.confirmPassword;
                this.$scope.passwordMismatch = this.$scope.newPassword != this.$scope.confirmPassword;
            } else {
                this.$scope.passwordMatch = false;
                this.$scope.passwordMismatch = false;
            }
        }

        private saveChanges(
            newDisplayName:string, originalDisplayName:string,
            newEmail:string, originalEmail:string, newPassword:string) {

            var displayName = (newDisplayName != originalDisplayName) ? newDisplayName : null;
            var email = (newEmail != originalEmail) ? newEmail : null;
            var password = newPassword ? newPassword : null;

            this.frontEndService.editUser(displayName, email, password)
                .error(error => this.errorService.raiseError("Failed to edit user", error))
                .success((updatedUser:WrappedUser) => {
                    this.userService.User = updatedUser;
                    this.$uibModalInstance.close();
                });
        }
    }
}
