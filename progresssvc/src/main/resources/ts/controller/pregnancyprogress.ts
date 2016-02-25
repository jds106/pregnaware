/// <reference path="../references.ts" />

module controller {
    import FrontEndSvc = service.FrontEndSvc;
    'use strict';

    import ProgressModel = entities.ProgressModel;
    import User = entities.User;
    import LocalDate = entities.LocalDate;

    class EnhancedProgressModel implements ProgressModel {
        public daysPassed:number;
        public daysRemaining:number;
        public dueDate:LocalDate;

        public get weeksPassed() {
            return Math.floor(this.daysPassed / 7);
        }

        public get weeksRemaining() {
            return Math.floor(this.daysRemaining / 7);
        }

        constructor(model:ProgressModel) {
            this.daysPassed = model.daysPassed;
            this.daysRemaining = model.daysRemaining;
            this.dueDate = model.dueDate;
        }

        public get formattedDueDate() {
            // Format the due date - handling the zero-index months
            var asMomentDate = moment()
                .year(this.dueDate.year).month(this.dueDate.month - 1).date(this.dueDate.day);
            return asMomentDate.format("LL");
        }
    }

    /** Extend the scope with the progress model */
    interface PregnancyProgressScope extends angular.IScope {
        progress: EnhancedProgressModel;
        viewedUser: User;
        canEdit: boolean;

        dueDatePickerOpen: boolean;
        dueDate: number;
    }

    export class PregnancyProgressController {
        private $scope: PregnancyProgressScope;
        private frontend:service.FrontEndSvc;
        private usermgmt:service.UserManagementSvc;

        private user: User;

        constructor($scope: PregnancyProgressScope, frontend:service.FrontEndSvc, usermgmt:service.UserManagementSvc) {
            this.$scope = $scope;
            this.frontend = frontend;
            this.usermgmt = usermgmt;

            this.$scope.dueDatePickerOpen = false;
            this.$scope.dueDate = Date.now();

            this.usermgmt.userSetEvent(user => {
                this.user = user;
            });

            this.usermgmt.viewedUserChangedEvent(user => {
                this.$scope.viewedUser = user;

                // Can only edit when the logged-in user is the same as the viewed user
                this.$scope.canEdit = this.user.userId == user.userId;

                this.frontend.getDueDate(user.userId)
                    .error(error => this.$scope.progress = null)
                    .success((response:ProgressModel) => {
                        this.$scope.progress = new EnhancedProgressModel(response);
                    });
            });
        }

        public UpdateDueDate(dueDate: number) {
            var parsedDueDate = moment(dueDate);
            var asLocalDate:LocalDate = {
                year: parsedDueDate.year(),
                month: parsedDueDate.month() + 1,   // The months are zero-indexed
                day: parsedDueDate.date()
            };

            this.frontend.putDueDate(asLocalDate, this.user.userId)
                .error(error => console.error('Could not put due date', error))
                .success((response:ProgressModel) => {
                    this.$scope.progress = new EnhancedProgressModel(response);
                });
        }

        public ChangeDueDate() {
            this.frontend.deleteDueDate(this.user.userId)
                .error(error => console.error('Could not put due date', error))
                .success((response) => {
                    this.$scope.progress = null;
                });
        }
    }
}