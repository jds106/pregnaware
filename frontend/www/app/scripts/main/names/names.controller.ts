/// <reference path="../../references.ts" />

module main.names {
    'use strict';

    import WrappedUser = models.WrappedUser;
    import WrappedFriend = models.WrappedFriend;
    import WrappedBabyName = models.WrappedBabyName;

    export class NamesController {

        private $scope:NamesModel;
        private frontEndService:services.FrontEndService;
        private userService:services.UserService;

        private user:WrappedUser;
        private selectedFriend: WrappedFriend;

        constructor($scope:NamesModel,
                    frontEndService:services.FrontEndService,
                    userService:services.UserService) {

            this.$scope = $scope;
            this.frontEndService = frontEndService;
            this.userService = userService;

            this.$scope.addCurrentNameGirl = (name: string) => NamesController.addCurrentNameGirl(this, name);
            this.$scope.addCurrentNameBoy = (name: string) => NamesController.addCurrentNameBoy(this, name);
            this.$scope.deleteName = (entry: WrappedBabyName) => NamesController.deleteName(this, entry);

            this.userService.userSetEvent(user => {
                this.user = user;
                this.$scope.viewedUser = this.user.displayName;
                this.$scope.canEdit = true;
                this.$scope.boysNames = this.user.babyNames.filter(n => n.isBoy);
                this.$scope.girlsNames = this.user.babyNames.filter(n => !n.isBoy);
            });

            this.userService.friendSelectedEvent((friend:WrappedFriend) => {
                var babyNames:WrappedBabyName[];

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

        private static addCurrentNameGirl(self: NamesController, name: string) {
            var suggestedForUserId:number = self.selectedFriend ? self.selectedFriend.userId : self.user.userId;
            self.frontEndService.putName(name, false, suggestedForUserId)
                .error(error => console.error("Failed to add girl's name", error))
                .success((response:WrappedBabyName) => {
                    self.$scope.girlsNames.push(response);
                    self.$scope.currentNameGirl = "";
                });
        }

        private static addCurrentNameBoy(self: NamesController, name: string) {
            var suggestedForUserId:number = self.selectedFriend ? self.selectedFriend.userId : self.user.userId;
            self.frontEndService.putName(name, true, suggestedForUserId)
                .error(error => console.error("Failed to add boy's name", error))
                .success((response:WrappedBabyName) => {
                    self.$scope.boysNames.push(response);
                    self.$scope.currentNameBoy = "";
                });
        }

        private static deleteName(self: NamesController, entry:WrappedBabyName) {
            self.frontEndService.deleteName(entry.nameId)
                .error(error => console.error("Failed to remove name", error))
                .success(response => {
                    if (entry.isBoy) {
                        self.$scope.boysNames = self.$scope.boysNames.filter(e => e != entry);
                    } else {
                        self.$scope.girlsNames = self.$scope.girlsNames.filter(e => e != entry);
                    }
                });
        }
    }
}
