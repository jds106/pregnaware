/// <reference path="../../references.ts" />

module main.nav {
    'use strict';

    import WrappedUser = models.WrappedUser;
    import WrappedFriend = models.WrappedFriend;

    export class NavController {
        private $scope: NavModel;
        private $uibModal: ng.ui.bootstrap.IModalService;
        private $locale: ng.ILocaleService;
        private frontEndService: services.FrontEndService;
        private userService: services.UserService;

        constructor(
            $scope: NavModel,
            $uibModal: ng.ui.bootstrap.IModalService,
            $locale: ng.ILocaleService,
            frontEndService: services.FrontEndService,
            userService: services.UserService) {

            this.$scope = $scope;
            this.$uibModal = $uibModal;
            this.$locale = $locale;
            this.frontEndService = frontEndService;
            this.userService = userService;

            this.userService.userSetEvent(user => this.$scope.user = user);

            this.$scope.locale = this.$locale.id;
            this.$scope.confirmFriendRequest = (friend) => this.confirmFriendRequest(friend);
            this.$scope.ignoreFriendRequest = (friend) => this.ignoreFriendRequest(friend);

            this.$scope.logout = () => {
                this.userService.User = null;
                this.userService.Friend = null;
                this.frontEndService.logout();
            };

            this.$scope.viewFriend = (friend: WrappedFriend) => {
                this.userService.Friend = friend;
            };

            this.$scope.viewUser = () => {
                this.userService.Friend = null;
            };

            this.$scope.addFriend = () => {
                this.$uibModal.open({
                    animation: true,
                    templateUrl: '/scripts/main/share/share.view.html',
                    controller: main.share.ShareController,
                    controllerAs: 'vm',
                    size: 'lg',
                });
            };

            // Pop-up the account settings screen
            this.$scope.updateAccountSettings = () => {
                this.$uibModal.open({
                    animation: true,
                    templateUrl: '/scripts/main/account/account.view.html',
                    controller: main.account.AccountController,
                    controllerAs: 'vm',
                    size: 'lg',
                });
            };
        }

        private confirmFriendRequest(friend: WrappedFriend) {
            this.frontEndService.addFriend(friend.email)
                .error((e) => console.error('Failed to confirm friend', e))
                .success(() => {
                   this.frontEndService.getUser()
                       .error((e) => console.error('Failed to fetch user after friend confirmation', e))
                       .success((user: WrappedUser) => this.userService.User = user)
                });
        }

        private ignoreFriendRequest(friend: WrappedFriend) {
            this.frontEndService.deleteFriend(friend.userId)
                .error((e) => console.error('Failed to ignore friend', e))
                .success(() => {
                    this.frontEndService.getUser()
                        .error((e) => console.error('Failed to fetch user after ignoring friend', e))
                        .success((user: WrappedUser) => this.userService.User = user)
                });
        }
    }
}
