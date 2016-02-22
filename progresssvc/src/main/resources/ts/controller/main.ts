/// <reference path="../references.ts" />

module Controller {
    'use strict';

    import WebRoot = Utils.WebRoot;
    import ProgressModel = Controller.ProgressModel;
    import LocalDate = Controller.LocalDate;

    class EnhancedProgressModel implements ProgressModel {
        public daysPassed: number;
        public daysRemaining: number;
        public dueDate: LocalDate;

        public get WeeksPassed() { return Math.floor(this.daysPassed / 7); }
        public get WeeksRemaining() { return Math.floor(this.daysRemaining / 7); }

        constructor(model: ProgressModel) {
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

    export class MainController {
        private $http:angular.IHttpService;
        private $cookies:angular.cookies.ICookiesService;
        private $window:angular.IWindowService;
        private $scope:angular.IScope;

        private names;

        private progressModel: EnhancedProgressModel;

        constructor($http:angular.IHttpService,
                    $cookies:angular.cookies.ICookiesService,
                    $window:angular.IWindowService,
                    $scope:angular.IScope) {

            this.$http = $http;
            this.$cookies = $cookies;
            this.$window = $window;
            this.$scope = $scope;

            this.NamesAction();
            this.DueDateAction();
        }

        private get SessionId() {
            return this.$cookies.get(WebRoot.SessionIdKey);
        }

        private NamesAction() {
            this.$http.get(WebRoot.Url('/FrontEndSvc/NamingSvc/names', this.SessionId))

                .error((error) => {
                    console.error('Names Error', error);
                })

                .success((result) => {
                    this.names = result
                });
        }

        private DueDateAction() {
            this.$http.get(WebRoot.Url('/FrontEndSvc/ProgressSvc/progress', this.SessionId))

                .error((error) => {
                    console.error('Progress Error', error);
                })

                .success((response) => {
                    this.progressModel = new EnhancedProgressModel(<ProgressModel>JSON.parse(response));
                });
        }

        public get Names() {
            return this.names;
        }

        public get Progress() {
            return this.progressModel;
        }

        // Due date selection logic
        private dueDatePickerOpen:boolean = false;
        public get DueDatePickerOpen() { return this.dueDatePickerOpen; }
        public set DueDatePickerOpen(isOpen:boolean) { this.dueDatePickerOpen = isOpen; }

        private dueDate:number = Date.now();
        public get DueDate() { return this.dueDate; }
        public set DueDate(newDate:number) { this.dueDate = newDate; }

        public UpdateDueDate() {
            var parsedDueDate = moment(this.dueDate);
            var asLocalDate : LocalDate = {
                year: parsedDueDate.year(),
                month: parsedDueDate.month() + 1,   // The months are zero-indexed
                day: parsedDueDate.date()
            };

            console.log("Setting due date to: " + asLocalDate);
            this.$http.put(WebRoot.Url('/FrontEndSvc/ProgressSvc/progress', this.SessionId), asLocalDate)
                .error((error) => {
                    console.error('Could not put due date', error);
                })
                .success((response) => {
                    console.log('Successfully set due date', response);
                    this.progressModel = new EnhancedProgressModel(<ProgressModel>JSON.parse(response));
                });
        }

        public ChangeDueDate() {
            this.$http.delete(WebRoot.Url('/FrontEndSvc/ProgressSvc/progress', this.SessionId))
                .error((error) => {
                    console.error('Could not put due date', error);
                })
                .success((response) => {
                    console.log('Successfully reset due date', response);
                    this.progressModel = null;
                });
        }
    }
}