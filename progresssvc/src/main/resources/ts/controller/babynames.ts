/// <reference path="../references.ts" />

module controller {
    'use strict';
    import NamingEntry = entities.NamingEntry;

    import User = entities.User;
    import NamingEntries = entities.NamingEntries;

    class EnhancedNamingEntry implements NamingEntry {
        public nameId: number;
        public gender: string;
        public name: string;
        public suggestedByUserId: number;
        public suggestedBy: string;

        constructor(entry: NamingEntry, user: User) {
            this.nameId = entry.nameId;
            this.name = entry.name;
            this.gender = entry.gender;
            this.suggestedByUserId = entry.suggestedByUserId;

            if (entry.suggestedByUserId == user.userId) {
                this.suggestedBy = user.displayName
            } else {
                this.suggestedBy = "UNKNOWN";
                for (let friendIndex = 0; friendIndex < user.friends.length; friendIndex++) {
                    let friend = user.friends[friendIndex];
                    if (friend.userId == entry.suggestedByUserId) {
                        this.suggestedBy = friend.displayName;
                        break;
                    }
                }
            }
        }
    }

    /** Extend the scope with the names */
    interface BabyNamesScope extends angular.IScope {
        viewedUser: User;
        canEdit: boolean;

        // The name lists
        boysNames: EnhancedNamingEntry[];
        girlsNames: EnhancedNamingEntry[];

        // New names to be added
        currentNameGirl: string;
        currentNameBoy: string;
    }

    export class BabyNamesController {
        private $scope: BabyNamesScope;
        private frontend: service.FrontEndSvc;
        private usermgmt: service.UserManagementSvc;

        private user: User;

        constructor($scope: BabyNamesScope, frontend: service.FrontEndSvc, usermgmt: service.UserManagementSvc) {
            this.$scope = $scope;
            this.frontend = frontend;
            this.usermgmt = usermgmt;

            this.usermgmt.userSetEvent(user => {
                this.user = user;
            });

            this.usermgmt.viewedUserChangedEvent(user => {
                this.$scope.viewedUser = user;

                // Can only edit when the logged-in user is the same as the viewed user
                this.$scope.canEdit = this.user.userId == user.userId;

                this.frontend.getNames(user.userId)
                    .error(error => console.error("Failed to get baby names", error))
                    .success((response : NamingEntries) => {
                        let enhancedEntries = response.entries.map(e => new EnhancedNamingEntry(e, user));
                        this.$scope.boysNames = enhancedEntries.filter(n => n.gender.toLowerCase() == "boy");
                        this.$scope.girlsNames = enhancedEntries.filter(n => n.gender.toLowerCase() == "girl");
                    });
            });
        }

        public AddCurrentNameGirl() {
            this.frontend.putName(this.$scope.currentNameGirl, "girl", this.user.userId)
                .error(error => console.error("Failed to add girl's name", error))
                .success((response: NamingEntry) => {
                    this.$scope.girlsNames.push(new EnhancedNamingEntry(response, this.user));
                    this.$scope.currentNameGirl = "";
                });
        }

        public AddCurrentNameBoy() {
            this.frontend.putName(this.$scope.currentNameBoy, "boy", this.user.userId)
                .error(error => console.error("Failed to add boy's name", error))
                .success((response: NamingEntry) => {
                    this.$scope.boysNames.push(new EnhancedNamingEntry(response, this.user));
                    this.$scope.currentNameBoy = "";
                });
        }

        public DeleteName(entry: NamingEntry) {
            this.frontend.deleteName(entry.nameId, this.user.userId)
                .error(error => console.error("Failed to remove name", error))
                .success(response => {
                    if (entry.gender == "boy") {
                        this.$scope.boysNames = this.$scope.boysNames.filter(e => e != entry);
                    } else if (entry.gender == "girl") {
                        this.$scope.girlsNames = this.$scope.girlsNames.filter(e => e != entry);
                    }
                });
        }
    }
}