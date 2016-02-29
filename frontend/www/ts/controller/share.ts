/// <reference path="../references.ts" />

module controller {
    'use strict';

    import WrappedUser = entities.WrappedUser;

    interface ShareScope extends angular.IScope {
        friendEmail: string;
        mailToLink: string;

        friendDisplayName: string;

        showExistingUserSuccess: boolean;
        showNewUserSuccess: boolean;
    }

    export class ShareController {
        private $scope: ShareScope;
        private $window: angular.IWindowService;
        private frontend: service.FrontEndSvc;
        private usermgmt: service.UserManagementSvc;

        private user: WrappedUser;

        constructor(
            $scope: ShareScope,
            $window: angular.IWindowService,
            frontend: service.FrontEndSvc,
            usermgmt: service.UserManagementSvc) {

            this.$scope = $scope;
            this.$window = $window;
            this.frontend = frontend;
            this.usermgmt = usermgmt;

            this.$scope.showExistingUserSuccess = false;
            this.$scope.showNewUserSuccess = false;

            this.usermgmt.userSetEvent((user: WrappedUser) => {
                this.user = user;
            });
        }

        // TODO: Fix up sharing

        //public Share(email: string) {
        //    // Reset previous values
        //    this.$scope.friendDisplayName = null;
        //    this.$scope.mailToLink = null;
        //    this.$scope.showExistingUserSuccess = false;
        //    this.$scope.showNewUserSuccess = false;
        //
        //    this.frontend.findUser(email)
        //        .error(error => this.shareNew(email))
        //        .success((response: User) => this.shareExisting(response));
        //}
        //
        //private shareExisting(existingUser: WrappedUser) {
        //    this.$scope.friendDisplayName = existingUser.displayName;
        //    this.frontend.addFriend(existingUser)
        //        .error(error => console.error("Could not add new user as a friend", error))
        //        .success(response => {
        //            this.$scope.friendEmail = null;
        //            this.$scope.showExistingUserSuccess = true;
        //        });
        //}
        //
        //private shareNew(newUserEmail: string) {
        //    this.frontend.createFriend(newUserEmail)
        //        .error(error => console.error("Could not add new user as a friend", newUserEmail))
        //        .success((sessionId : string) => {
        //            this.$scope.friendEmail = null;
        //            this.$scope.showNewUserSuccess = true;
        //
        //            var urlRoot = this.$window.location.protocol +"//" + this.$window.location.host
        //            this.$scope.mailToLink =
        //                ShareController.makeMailTo(
        //                    this.user,
        //                    newUserEmail,
        //                    this.frontend.getCreateFriendLink(urlRoot, sessionId));
        //        });
        //}
        //
        //private static makeMailTo(user: User, newUserEmail: string, sessionLink: string) {
        //    var subject = "Your friend " + user.displayName + " would like you to share her pregnancy progress!";
        //    var body =
        //    `Hi,
        //
        //    You have been invited to share in your friend's pregnancy on Pregnaware!
        //
        //    Click this link to register and start sharing in her growing anticipation:
        //        ${sessionLink}
        //
        //    Thanks,
        //    ${user.displayName}
        //    `;
        //
        //    return "mailto:" + newUserEmail + "?subject=" + encodeURI(subject) + "&body=" + encodeURI(body);
        //}
    }
}
