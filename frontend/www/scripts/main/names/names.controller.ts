/// <reference path="../../references.ts" />

module main.names {
    'use strict';

    import WrappedUser = models.WrappedUser;
    import WrappedFriend = models.WrappedFriend;
    import WrappedBabyName = models.WrappedBabyName;

    export class NamesController {

        private $scope:NamesModel;
        private $uibModal: ng.ui.bootstrap.IModalService;
        private routeService: services.RouteService;
        private frontEndService:services.FrontEndService;
        private userService:services.UserService;

        private user:WrappedUser;
        private selectedFriend: WrappedFriend;

        constructor($scope:NamesModel,
                    $uibModal: ng.ui.bootstrap.IModalService,
                    routeService: services.RouteService,
                    frontEndService:services.FrontEndService,
                    userService:services.UserService) {

            this.$scope = $scope;
            this.$uibModal = $uibModal;
            this.routeService = routeService;
            this.frontEndService = frontEndService;
            this.userService = userService;

            this.$scope.addCurrentNameGirl = (name: string) => this.addCurrentNameGirl(name);
            this.$scope.addCurrentNameBoy = (name: string) => this.addCurrentNameBoy(name);
            this.$scope.deleteName = (entry: WrappedBabyName) => this.deleteName(entry);

            this.$scope.isNameInvalid = (name) => this.isNameInvalid(name);

            this.userService.userSetEvent(user => {
                this.user = user;

                if (user) {
                    this.$scope.viewedUser = this.user.displayName;
                    this.$scope.canEdit = true;
                    this.$scope.boysNames = this.user.babyNames.filter(n => n.isBoy);
                    this.$scope.girlsNames = this.user.babyNames.filter(n => !n.isBoy);

                } else {
                    this.$scope.viewedUser = null;
                    this.$scope.canEdit = false;
                    this.$scope.boysNames = [];
                    this.$scope.girlsNames = [];
                }
            });

            this.userService.friendSelectedEvent((friend:WrappedFriend) => {
                var babyNames:WrappedBabyName[];

                if (friend) {
                    this.$scope.viewedUser = friend.displayName;
                    this.$scope.canEdit = false;
                    babyNames = friend.babyNames;

                } else if (this.user) {
                    this.$scope.viewedUser = this.user.displayName;
                    this.$scope.canEdit = true;
                    babyNames = this.user.babyNames

                } else {
                    this.$scope.viewedUser = null;
                    this.$scope.canEdit = false;
                    babyNames = [];
                }

                this.selectedFriend = friend;
                this.$scope.boysNames = babyNames.filter(n => n.isBoy);
                this.$scope.girlsNames = babyNames.filter(n => !n.isBoy);
            });

            // Pop-up the general name stats page
            this.$scope.showGeneralNameStats = (isBoy: boolean) => {
                this.$uibModal.open({
                    animation: true,
                    templateUrl: '/scripts/main/names/stats/general/generalstats.view.html',
                    controller: main.names.stats.general.GeneralStatsController,
                    controllerAs: 'vm',
                    size: 'lg',
                    resolve: {
                        isBoy: () => isBoy
                    }
                });
            };

            // Pop-up the specific name stats page
            this.$scope.showSpecificNameStats = (name, isBoy) => {
                this.$uibModal.open({
                    animation: true,
                    templateUrl: `/scripts/main/names/stats/specific/specificstats.view.html`,
                    controller: main.names.stats.specific.SpecificStatsController,
                    controllerAs: 'vm',
                    size: 'lg',
                    resolve: {
                        name: () => name,
                        isBoy: () => isBoy
                    }
                });
            };
        }

        /** Basic name validation logic */
        private isNameInvalid(name: string) : boolean {
            if (!this.user || !name)
                return true;

            name = name.trim();

            if (name.length == 0) {
                return true;
            } else {
                return this.user.babyNames.filter(existingName => existingName.name == name).length > 0;
            }
        }

        private addCurrentNameGirl(name: string) {
            name = name.trim();

            var suggestedForUserId:number = this.selectedFriend ? this.selectedFriend.userId : this.user.userId;
            this.frontEndService.putName(name, false, suggestedForUserId)
                .error(error => this.routeService.errorPage("Failed to add girl's name", error))
                .success((response:WrappedBabyName) => {
                    this.$scope.girlsNames.push(response);
                    this.user.babyNames.push(response);
                    this.$scope.currentNameGirl = "";
                });
        }

        private addCurrentNameBoy(name: string) {
            name = name.trim();

            var suggestedForUserId:number = this.selectedFriend ? this.selectedFriend.userId : this.user.userId;
            this.frontEndService.putName(name, true, suggestedForUserId)
                .error(error => this.routeService.errorPage("Failed to add boy's name", error))
                .success((response:WrappedBabyName) => {
                    this.$scope.boysNames.push(response);
                    this.user.babyNames.push(response);
                    this.$scope.currentNameBoy = "";
                });
        }

        private deleteName(entry:WrappedBabyName) {
            this.frontEndService.deleteName(entry.nameId)
                .error(error => this.routeService.errorPage("Failed to remove name", error))
                .success(response => {
                    this.user.babyNames = this.user.babyNames.filter(e => e != entry);

                    if (entry.isBoy) {
                        this.$scope.boysNames = this.$scope.boysNames.filter(e => e != entry);
                    } else {
                        this.$scope.girlsNames = this.$scope.girlsNames.filter(e => e != entry);
                    }
                });
        }
    }
}
