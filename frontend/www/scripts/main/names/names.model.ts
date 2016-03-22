/// <reference path="../../references.ts" />

module main.names {
    'use strict';

    import WrappedBabyName = models.WrappedBabyName;

    export interface NamesModel extends ng.IScope {
        viewedUser: string;
        canEdit: boolean;

        // The name lists
        boysNames: WrappedBabyName[];
        girlsNames: WrappedBabyName[];

        // New names to be added
        currentNameGirl: string;
        currentNameBoy: string;

        // Functions to add / remove names
        addCurrentNameGirl: (name:string) => void;
        addCurrentNameBoy: (name:string) => void;
        deleteName: (entry:WrappedBabyName) => void;

        // Functions to show naming stats
        showGeneralNameStats: (isBoy: boolean) => void
        showSpecificNameStats: (name: string, isBoy: boolean) => void

        isNameInvalid: (string) => boolean
    }
}