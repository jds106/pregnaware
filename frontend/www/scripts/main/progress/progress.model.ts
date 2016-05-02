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
        private daysPassed: number;
        private daysRemaining: number;

        private gestationPeriod : moment.Duration = moment.duration({days: 280});

        private daysInTrimester1 = 14 * 7;
        private daysInTrimester2 = 14 * 7;
        private daysInTrimester3 = 12 * 7;

        public get progress() {
            var weeks = Math.floor(this.daysPassed / 7);
            var days = this.daysPassed % 7;
            return `${weeks}w ${days}d`
        }

        public get remaining() {
            var weeks = Math.floor(this.daysRemaining / 7);
            var days = (this.daysRemaining) % 7;
            return `${weeks}w ${days}d`
        }

        public get daysPassedTrimester1() {
            return Math.max(0, Math.min(this.daysInTrimester1, this.daysPassed));
        }

        public get daysPassedTrimester2() {
            return Math.max(0, Math.min(this.daysInTrimester2, this.daysPassed - this.daysInTrimester1));
        }

        public get daysPassedTrimester3() {
            return Math.max(0,
                Math.min(this.daysInTrimester3, this.daysPassed - this.daysInTrimester1 - this.daysInTrimester2));
        }

        public get babySize() {
            var sizes = [
                // First trimester
                "Microscopic",                  // 0 - 1
                "Microscopic",                  // 1 - 2
                "Microscopic",                  // 2 - 3
                "Microscopic",                  // 3 - 4
                "Poppy seed (2mm)",             // 4 - 5
                "Sesame seed (3mm)",            // 5 - 6
                "Lentil (5mm)",                 // 6 - 7
                "Blueberry (1.2cm)",            // 7 - 8
                "Kidney bean (1.6cm / 1g)",     // 8 - 9
                "Grape (2.3cm / 2g)",           // 9 - 10
                "Green olive (3.1cm / 4g)",     // 10 - 11
                "Fig (4.1cm / 7g)",             // 11 - 12
                "Lime (5.4cm / 14g)",           // 12 - 13
                "Pea pod (7.4cm / 23g)",        // 13 - 14

                // Second trimester
                "As big as a lemon (8.7cm / 43g)",                                                  // 14 - 15
                "As big as an apple (10.1cm / 70g)",                                                // 15 - 16
                "As big as an avocado (11.6cm / 100g)",                                             // 16 - 17
                "As heavy as a turnip (13cm / 140g)",                                               // 17 - 18
                "As big as a bell pepper (14.2cm / 190g)",                                          // 18 - 19
                "As big as an heirloom tomato (15.3cm / 240g)",                                     // 19 - 20
                "As long as a small banana (16.4cm head-to-bottom, 25.6cm head-to-heel / 300g)",    // 20 - 21
                "As long as a carrot (26.7cm head-to-heel / 360g)",                                 // 21 - 22
                "As big as a spaghetti squash (27.8cm head-to-heel / 430g)",                        // 22 - 23
                "As heavy as a large mango (28.9cm head-to-heel / 500g)",                           // 23 - 24

                // Third trimester
                "As long as an ear of corn (30cm head-to-heel / 600g)",             // 24 - 25
                "As heavy as a swede (34.6cm head-to-heel / 660g)",                 // 25 - 26
                "As heavy as a red cabbage (35.6cm head-to-heel / 760g)",           // 26 - 27
                "As heavy as a head of cauliflower (36.6cm head-to-heel / 875g)",   // 27 - 28
                "As heavy as an aubergine (37.6cm head-to-heel / 1kg)",             // 28 - 29
                "As big Butternut squash (38.6cm head-to-heel / 1.2kg)",            // 29 - 30
                "As big as a good-sized cabbage (39.9cm head-to-heel / 1.3kg)",     // 30 - 31
                "As heavy as a coconut (41.1cm head-to-heel / 1.5kg)",              // 31 - 32
                "As long as a kale (42.4cm head-to-heel / 1.7kg)",                  // 32 - 33
                "As heavy as a pineapple (43.7cm head-to-heel / 1.9kg)",            // 33 - 34
                "As big as a cantaloupe melon (45cm head-to-heel / 2.1kg)",         // 34 - 35
                "As heavy as a honeydew melon (46.2cm head-to-heel / 2.4kg)",       // 35 - 36
                "As big as a romaine lettuce (47.4cm head-to-heel / 2.6kg)",        // 36 - 37
                "As long as a stalk of Swiss chard (48.6cm head-to-heel / 2.9kg)",  // 37 - 38
                "As long as a leek (49.8cm head-to-heel / 3kg)",                    // 38 - 39
                "As heavy as a mini-watermelon (50.7cm head-to-heel / 3.3kg)",      // 39 - 40
                "As big as a small pumpkin (51.2cm head-to-heel / 3.5kg)",          // 40 - 41
            ];

            var weeks = Math.min(40, Math.floor(this.daysPassed / 7));
            return sizes[weeks];
        }

        constructor(dueDate: LocalDate) {
            // Note handling of zero-index months
            this.dueDate = moment().startOf('day').year(dueDate.year).month(dueDate.month - 1).date(dueDate.day);

            var conceptionDate = this.dueDate.clone().subtract(this.gestationPeriod);
            var today = moment().startOf('day');

            this.daysPassed = today.diff(conceptionDate, 'days');
            this.daysRemaining = this.dueDate.diff(today, 'days');

            console.log("Due date:",this.dueDate.toISOString());
            console.log("Today:",today.toISOString());
            console.log("Conception:",conceptionDate.toISOString());
            console.log("daysPassed:",this.daysPassed);
            console.log("daysRemaining:",this.daysRemaining);
        }

        public get dueDateVal() : number {
            return this.dueDate.valueOf();
        }
    }
}