/// <reference path="../references.ts" />

module controller {
    'use strict';

    import WrappedUser = entities.WrappedUser;
    import WrappedFriend = entities.WrappedFriend;
    import WrappedBabyName = entities.WrappedBabyName;

    /** Extend the scope with the names */
    interface BabyNamesScope extends angular.IScope {
        viewedUser: string;
        canEdit: boolean;

        // The name lists
        boysNames: WrappedBabyName[];
        girlsNames: WrappedBabyName[];

        // New names to be added
        currentNameGirl: string;
        currentNameBoy: string;
    }

    export class BabyNamesController {
        private $scope: BabyNamesScope;
        private frontend: service.FrontEndSvc;
        private usermgmt: service.UserManagementSvc;

        private user: WrappedUser;
        private selectedFriend: WrappedFriend;

        constructor($scope: BabyNamesScope, frontend: service.FrontEndSvc, usermgmt: service.UserManagementSvc) {
            this.$scope = $scope;
            this.frontend = frontend;
            this.usermgmt = usermgmt;

            this.usermgmt.userSetEvent(user => {
                this.user = user;
            });

            this.usermgmt.friendSelectedEvent((friend : WrappedFriend) => {
                var babyNames : WrappedBabyName[];

                if (friend == null) {
                    this.$scope.viewedUser = this.user.displayName;
                    this.$scope.canEdit = true;
                    babyNames = this.user.babyNames


                } else {
                    this.$scope.viewedUser = friend.displayName;
                    this.$scope.canEdit = false;
                    babyNames = friend.babyNames;
                }

                this.selectedFriend = friend;
                this.$scope.boysNames = babyNames.filter(n => n.isBoy);
                this.$scope.girlsNames = babyNames.filter(n => !n.isBoy);
            });
        }

        public AddCurrentNameGirl() {
            var suggestedForUserId: number = this.selectedFriend ? this.selectedFriend.userId : this.user.userId;
            this.frontend.putName(this.$scope.currentNameGirl, false, suggestedForUserId)
                .error(error => console.error("Failed to add girl's name", error))
                .success((response: WrappedBabyName) => {
                    this.$scope.girlsNames.push(response);
                    this.$scope.currentNameGirl = "";
                });
        }

        public AddCurrentNameBoy() {
            var suggestedForUserId: number = this.selectedFriend ? this.selectedFriend.userId : this.user.userId;
            this.frontend.putName(this.$scope.currentNameBoy, true, suggestedForUserId)
                .error(error => console.error("Failed to add boy's name", error))
                .success((response: WrappedBabyName) => {
                    this.$scope.boysNames.push(response);
                    this.$scope.currentNameBoy = "";
                });
        }

        public DeleteName(entry: WrappedBabyName) {
            this.frontend.deleteName(entry.nameId)
                .error(error => console.error("Failed to remove name", error))
                .success(response => {
                    if (entry.isBoy) {
                        this.$scope.boysNames = this.$scope.boysNames.filter(e => e != entry);
                    } else {
                        this.$scope.girlsNames = this.$scope.girlsNames.filter(e => e != entry);
                    }
                });
        }
    }
}