/// <reference path="../../references.ts" />

module main.progress {
    'use strict';

    import LocalDate = models.LocalDate;
    import Moment = moment.Moment;

    /** Extend the scope with the progress model */
    export interface ProgressModel extends angular.IScope {
        progress: EnhancedProgressModel;
        viewedUser: string;
        canEdit: boolean;

        dueDatePickerOpen: boolean;
        dueDate: number;

        updateDueDate: (dueDate:number) => void;
        changeDueDate: () => void;
    }

    export class EnhancedProgressModel {
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
}