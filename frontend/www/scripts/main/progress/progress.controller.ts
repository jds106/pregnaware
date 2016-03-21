/// <reference path="../../references.ts" />

module main.progress {
    'use strict';

    import WrappedUser = models.WrappedUser;
    import WrappedFriend = models.WrappedFriend;
    import LocalDate = models.LocalDate;
    import Moment = moment.Moment;
    import FrontEndSvc = services.FrontEndService;

    export class ProgressController {
        private $scope:ProgressModel;
        private routeService: services.RouteService;
        private frontEndService:services.FrontEndService;
        private userService:services.UserService;

        private user:WrappedUser;

        constructor($scope:ProgressModel,
                    routeService: services.RouteService,
                    frontEndService:services.FrontEndService,
                    userService:services.UserService) {

            this.$scope = $scope;
            this.routeService = routeService;
            this.frontEndService = frontEndService;
            this.userService = userService;

            this.$scope.dueDatePickerOpen = false;
            this.$scope.dueDate = Date.now();

            this.$scope.updateDueDate = (dueDate: number) => this.updateDueDate(dueDate);
            this.$scope.changeDueDate = () => this.changeDueDate();

            this.userService.userSetEvent((user:WrappedUser) => {
                this.user = user;
                if (user) {
                    this.$scope.viewedUser = this.user.displayName;
                    this.$scope.canEdit = true;

                    this.$scope.progress =
                        this.user.dueDate
                            ? new EnhancedProgressModel(this.user.dueDate)
                            : null;

                } else {
                    this.$scope.viewedUser = null;
                    this.$scope.canEdit = false;
                    this.$scope.progress = null;
                }
            });

            this.userService.friendSelectedEvent((friend:WrappedFriend) => {
                if (friend) {
                    this.$scope.viewedUser = friend.displayName;
                    this.$scope.canEdit = false;
                    this.$scope.progress = friend.dueDate
                        ? new EnhancedProgressModel(friend.dueDate)
                        : null;

                } else if (this.user) {
                    this.$scope.viewedUser = this.user.displayName;
                    this.$scope.canEdit = true;
                    this.$scope.progress =
                        this.user.dueDate
                            ? new EnhancedProgressModel(this.user.dueDate)
                            : null;
                } else {
                    this.$scope.viewedUser = null;
                    this.$scope.canEdit = false;
                    this.$scope.progress = null;
                }
            });
        }

        private updateDueDate(dueDate:number) {
            var parsedDueDate = moment(dueDate);
            var asLocalDate:LocalDate = {
                year: parsedDueDate.year(),
                month: parsedDueDate.month() + 1,   // The months are zero-indexed
                day: parsedDueDate.date()
            };

            this.frontEndService.putDueDate(asLocalDate)
                .error(error => this.routeService.errorPage('Could not put due date', error))
                .success((response:LocalDate) => this.$scope.progress = new EnhancedProgressModel(response));
        }

        private changeDueDate() {
            this.frontEndService.deleteDueDate()
                .error(error => this.routeService.errorPage('Could not put due date', error))
                .success(() => this.$scope.progress = null);
        }
    }
}