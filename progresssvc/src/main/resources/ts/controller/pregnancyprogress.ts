/// <reference path="../references.ts" />

module controller {
    'use strict';

    import ProgressModel = entities.ProgressModel;
    import User = entities.User;
    import LocalDate = entities.LocalDate;

    class EnhancedProgressModel implements ProgressModel {
        public daysPassed:number;
        public daysRemaining:number;
        public dueDate:LocalDate;

        public get WeeksPassed() {
            return Math.floor(this.daysPassed / 7);
        }

        public get WeeksRemaining() {
            return Math.floor(this.daysRemaining / 7);
        }

        constructor(model:ProgressModel) {
            this.daysPassed = model.daysPassed;
            this.daysRemaining = model.daysRemaining;
            this.dueDate = model.dueDate;
        }

        public get FormattedDueDate() {
            // Format the due date - handling the zero-index months
            var asMomentDate = moment()
                .year(this.dueDate.year).month(this.dueDate.month - 1).date(this.dueDate.day);
            return asMomentDate.format("LL");
        }
    }

    /** Extend the scope with the progress model */
    interface PregnancyProgressScope extends angular.IScope {
        progress: EnhancedProgressModel;
    }

    export class PregnancyProgressController {
        private $scope: PregnancyProgressScope;
        private frontend:service.FrontEndSvc;
        private usermgmt:service.UserManagementSvc;

        constructor($scope: PregnancyProgressScope, frontend:service.FrontEndSvc, usermgmt:service.UserManagementSvc) {
            this.$scope = $scope;
            this.frontend = frontend;
            this.usermgmt = usermgmt;

            this.usermgmt.userChangedEvent(user => {
                this.setDueDate(user.userId);
            });
        }

        private setDueDate(userId:number) {
            this.frontend.getDueDate(userId)
                .error(error => console.error('Progress Error', error))
                .success((response:ProgressModel) => {
                    this.$scope.progress = new EnhancedProgressModel(response);
                });
        }

        private dueDatePickerOpen:boolean = false;

        public get DueDatePickerOpen() {
            return this.dueDatePickerOpen;
        }

        public set DueDatePickerOpen(isOpen:boolean) {
            this.dueDatePickerOpen = isOpen;
        }

        private dueDate:number = Date.now();

        public get DueDate() {
            return this.dueDate;
        }

        public set DueDate(newDate:number) {
            this.dueDate = newDate;
        }

        public UpdateDueDate() {
            var parsedDueDate = moment(this.dueDate);
            var asLocalDate:LocalDate = {
                year: parsedDueDate.year(),
                month: parsedDueDate.month() + 1,   // The months are zero-indexed
                day: parsedDueDate.date()
            };

            this.frontend.putDueDate(asLocalDate)
                .error(error => console.error('Could not put due date', error))
                .success((response:ProgressModel) => {
                    console.log('Successfully set due date', response);
                    this.$scope.progress = new EnhancedProgressModel(response);
                });
        }

        public ChangeDueDate() {
            this.frontend.deleteDueDate()
                .error(error => console.error('Could not put due date', error))
                .success((response) => {
                    console.log('Successfully reset due date', response);
                    this.$scope.progress = null;
                });
        }
    }
}