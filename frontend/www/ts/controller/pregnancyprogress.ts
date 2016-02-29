/// <reference path="../references.ts" />

module controller {
    'use strict';

    import WrappedUser = entities.WrappedUser;
    import WrappedFriend = entities.WrappedFriend;
    import LocalDate = entities.LocalDate;
    import Moment = moment.Moment;
    import FrontEndSvc = service.FrontEndSvc;

    class EnhancedProgressModel {
        public dueDate:Moment;
        public daysPassed: number;
        public daysRemaining: number;

        private gestationPeriod : moment.Duration = moment.duration({days: 280});

        public get weeksPassed() {
            return Math.floor(this.daysPassed / 7);
        }

        public get weeksRemaining() {
            return Math.floor(this.daysRemaining / 7);
        }

        constructor(dueDate: LocalDate) {
            // Note handling of zero-index months
            this.dueDate = moment().year(dueDate.year).month(dueDate.month - 1).date(dueDate.day);

            var conceptionDate = this.dueDate.clone().subtract(this.gestationPeriod);
            var today = moment();

            this.daysPassed = today.diff(conceptionDate, 'days');
            this.daysRemaining = this.dueDate.diff(today, 'days');
        }

        public get formattedDueDate() {
            return this.dueDate.format("LL");
        }
    }

    /** Extend the scope with the progress model */
    interface PregnancyProgressScope extends angular.IScope {
        progress: EnhancedProgressModel;
        viewedUser: string;
        canEdit: boolean;

        dueDatePickerOpen: boolean;
        dueDate: number;
    }

    export class PregnancyProgressController {
        private $scope: PregnancyProgressScope;
        private frontend:service.FrontEndSvc;
        private usermgmt:service.UserManagementSvc;

        private user: WrappedUser;
        private selectedFriend: WrappedFriend;

        constructor($scope: PregnancyProgressScope, frontend:service.FrontEndSvc, usermgmt:service.UserManagementSvc) {
            this.$scope = $scope;
            this.frontend = frontend;
            this.usermgmt = usermgmt;

            this.$scope.dueDatePickerOpen = false;
            this.$scope.dueDate = Date.now();

            this.usermgmt.userSetEvent((user: WrappedUser) => {
                this.user = user;
                this.$scope.viewedUser = this.user.displayName;
                this.$scope.canEdit = true;
                this.$scope.progress = new EnhancedProgressModel(this.user.dueDate);
            });

            this.usermgmt.friendSelectedEvent((friend: WrappedFriend) => {
                if (friend == null) {
                    this.$scope.viewedUser = this.user.displayName;
                    this.$scope.canEdit = true;
                    this.$scope.progress = new EnhancedProgressModel(this.user.dueDate);
                } else {
                    this.$scope.viewedUser = friend.displayName;
                    this.$scope.canEdit = false;
                    this.$scope.progress = new EnhancedProgressModel(friend.dueDate);
                }

                this.selectedFriend = friend;
            });
        }

        public UpdateDueDate(dueDate: number) {
            var parsedDueDate = moment(dueDate);
            var asLocalDate:LocalDate = {
                year: parsedDueDate.year(),
                month: parsedDueDate.month() + 1,   // The months are zero-indexed
                day: parsedDueDate.date()
            };

            this.frontend.putDueDate(asLocalDate)
                .error(error => console.error('Could not put due date', error))
                .success((response:LocalDate) => {
                    this.$scope.progress = new EnhancedProgressModel(response);
                });
        }

        public ChangeDueDate() {
            this.frontend.deleteDueDate()
                .error(error => console.error('Could not put due date', error))
                .success((response) => {
                    this.$scope.progress = null;
                });
        }
    }
}