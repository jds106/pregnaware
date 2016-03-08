/// <reference path="../../references.ts" />

module main.share {
    'use strict';

    import WrappedUser = models.WrappedUser;
    import WrappedFriend = models.WrappedFriend;

    export class ShareController {
        private $scope: ShareModel;
        private $uibModalInstance: ng.ui.bootstrap.IModalServiceInstance;

        // This is required to get the URL (i.e. read-only)
        private $window: ng.IWindowService;

        private frontEndService: services.FrontEndService;
        private userService: services.UserService;

        private user: WrappedUser;

        constructor(
            $scope: ShareModel,
            $uibModalInstance: ng.ui.bootstrap.IModalServiceInstance,
            $window: ng.IWindowService,
            frontEndService: services.FrontEndService,
            userService: services.UserService) {

            this.$scope = $scope;
            this.$uibModalInstance = $uibModalInstance;
            this.$window = $window;
            this.frontEndService = frontEndService;
            this.userService = userService;

            this.$scope.showExistingUserSuccess = false;
            this.$scope.showNewUserSuccess = false;

            this.userService.userSetEvent((user: WrappedUser) => {
                this.user = user;
            });

            this.$scope.close = () => this.$uibModalInstance.dismiss();

            this.$scope.share = (email: string) => ShareController.share(this, email);
        }

        private static share(self: ShareController, email: string) {
            // Reset previous values
            self.$scope.friendDisplayName = null;
            self.$scope.mailToLink = null;
            self.$scope.showExistingUserSuccess = false;
            self.$scope.showNewUserSuccess = false;

            self.frontEndService.addFriend(email)
                .error(error => {
                    self.$scope.friendEmail = null;
                    self.$scope.showNewUserSuccess = true;

                    var url = self.$window.location.protocol +"//" + self.$window.location.host;
                    self.$scope.mailToLink = ShareController.makeMailTo(self.user, email, url);
                })

                .success((friend: WrappedFriend) => {
                    self.$scope.friendDisplayName = friend.displayName;
                    self.$scope.friendEmail = null;
                    self.$scope.showExistingUserSuccess = true;
                });
        }

        private static makeMailTo(user: WrappedUser, newUserEmail: string, url: string) {
            var subject = `Pregnaware request from your friend ${user.displayName}`;
            var body =
            `Hi,

            You have been invited to share in your friend's pregnancy on Pregnaware! Click the link
            below to set up an account, and add your friend's email address '${user.email}':

            ${url}

            Looking forward to sharing with you!

            ${user.displayName}.
            `;

            return "mailto:" + newUserEmail + "?subject=" + encodeURI(subject) + "&body=" + encodeURI(body);
        }
    }
}
