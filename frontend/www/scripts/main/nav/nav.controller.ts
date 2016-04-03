/// <reference path="../../references.ts" />

module main.nav {
    import State = services.State;
    'use strict';

    import WrappedUser = models.WrappedUser;
    import WrappedFriend = models.WrappedFriend;

    export class NavController {
        private $scope: NavModel;
        private frontEndService: services.FrontEndService;
        private userService: services.UserService;
        private errorService: services.ErrorService;

        constructor(
            $scope: NavModel,
            $uibModal: ng.ui.bootstrap.IModalService,
            $locale: ng.ILocaleService,
            frontEndService: services.FrontEndService,
            userService: services.UserService,
            stateService: services.StateService,
            errorService: services.ErrorService) {

            this.$scope = $scope;
            this.frontEndService = frontEndService;
            this.userService = userService;
            this.errorService = errorService;

            this.userService.userSetEvent(user => this.$scope.user = user);

            this.$scope.locale = $locale.id;
            this.$scope.confirmFriendRequest = (friend) => this.confirmFriendRequest(friend);
            this.$scope.ignoreFriendRequest = (friend) => this.ignoreFriendRequest(friend);

            this.$scope.logout = () => {
                this.frontEndService.logout();
            };

            this.$scope.viewFriend = (friend: WrappedFriend) => {
                console.log("Changing to friend", friend);
                stateService.changeState((s) => { s.selectedUserId = friend.userId; return s; });
            };

            this.$scope.viewUser = () => {
                console.log("Changing to user");
                stateService.changeState((s) => { s.selectedUserId = this.userService.User.userId; return s; });
            };

            stateService.stateChangedEvent((state: State) => {
                console.log("State changed", state);
                if (this.userService.User) {
                    if (state.selectedUserId == this.userService.User.userId) {
                        this.userService.Friend = null;
                    } else {
                        for(let i = 0; i < this.userService.User.friends.length; i++) {
                            if (this.userService.User.friends[i].userId == state.selectedUserId) {
                                this.userService.Friend = this.userService.User.friends[i];
                            }
                        }
                    }
                }
            });

            this.$scope.isUserSelected = () => this.userService.Friend == null;
            this.$scope.isFriendSelected = (friend: WrappedFriend) => this.userService.Friend == friend;

            this.$scope.addFriend = () => {
                $uibModal.open({
                    animation: true,
                    templateUrl: '/scripts/main/share/share.view.html',
                    controller: main.share.ShareController,
                    controllerAs: 'vm',
                    size: 'lg',
                });
            };

            // Pop-up the account settings screen
            this.$scope.updateAccountSettings = () => {
                $uibModal.open({
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
                .error((error) => this.errorService.raiseError('Failed to confirm friend', error))
                .success(() => {
                   this.frontEndService.getUser()
                       .error((error) => this.errorService.raiseError('Failed to fetch user after friend confirmation', error))
                       .success((user: WrappedUser) => this.userService.User = user)
                });
        }

        private ignoreFriendRequest(friend: WrappedFriend) {
            this.frontEndService.deleteFriend(friend.userId)
                .error((error) => this.errorService.raiseError('Failed to ignore friend', error))
                .success(() => {
                    this.frontEndService.getUser()
                        .error((error) => this.errorService.raiseError('Failed to fetch user after ignoring friend', error))
                        .success((user: WrappedUser) => this.userService.User = user)
                });
        }
    }
}
