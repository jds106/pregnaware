/// <reference path="../references.ts" />

module controller {
    'use strict';

    import User = entities.User;
    import NamingEntries = entities.NamingEntries;

    export class BabyNamesController {
        private frontend: service.FrontEndSvc;
        private usermgmt: service.UserManagementSvc;

        private user: User;
        private names : NamingEntries;

        constructor(frontend: service.FrontEndSvc, usermgmt: service.UserManagementSvc) {
            this.frontend = frontend;
            this.usermgmt = usermgmt;

            this.usermgmt.userChangedEvent(user => {
                this.user = user;

                this.frontend.getNames(user.userId)
                    .error(error => console.error("Failed to get baby names", error))
                    .success((response : NamingEntries) => this.names = response);
            });
        }

        public get NamesForBoys() {
            return (this.names)
                ? this.names.entries.filter((n) => n.gender.toLowerCase() == "boy")
                : null;
        }

        public get NamesForGirls() {
            return (this.names)
                ? this.names.entries.filter((n) => n.gender.toLowerCase() == "girl")
                : null;
        }

        // Baby name logic
        private currentNameGirl: string;
        private currentNameBoy: string;

        public get CurrentNameGirl() { return this.currentNameGirl; }
        public set CurrentNameGirl(newName: string) { this.currentNameGirl = newName; }

        public get CurrentNameBoy() { return this.currentNameBoy; }
        public set CurrentNameBoy(newName: string) { this.currentNameBoy = newName; }

        public AddCurrentNameGirl() { this.addCurrentName("girl", this.currentNameGirl); }
        public AddCurrentNameBoy() { this.addCurrentName("boy", this.currentNameBoy); }

        private addCurrentName(gender: string, name: string) {

        }
    }
}