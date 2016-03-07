/// <reference path="../../references.ts" />

module main.nav {
    'use strict';

    import WrappedUser = models.WrappedUser;

    export class NavController {
        private $scope: NavModel;
        private $uibModal: ng.ui.bootstrap.IModalService;
        private frontEndService: services.FrontEndService;
        private userService: services.UserService;

        constructor(
            $scope: NavModel,
            $uibModal: ng.ui.bootstrap.IModalService,
            frontEndService: services.FrontEndService,
            userService: services.UserService) {

            this.$scope = $scope;
            this.$uibModal = $uibModal;
            this.frontEndService = frontEndService;
            this.userService = userService;

            this.userService.userSetEvent(user => this.$scope.user = user);

            this.$scope.updateAccountSettings = () => {
                this.$uibModal.open({
                    animation: true,
                    templateUrl: '/scripts/main/account/account.view.html',
                    controller: main.account.AccountController,
                    controllerAs: 'vm',
                    size: 'lg',
                });
            }
        }

        private static logout() {

        }

        //private static viewUser(self: NavBarController, userId: number = null) {
        //    self.frontEndService.getUser(userId)
        //        .error(error => console.error("Could not find user: " + userId))
        //        .success((user: WrappedUser) => self.userService.ViewedUser = user);
        //}
    }
}
