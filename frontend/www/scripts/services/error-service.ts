/// <reference path="../references.ts" />

module services {
    'use strict';

    // Error handler definition
    export interface ErrorHandler {
        onErrorSet: (url: string, description: string, error: string) => void;
        onErrorClear: () => void;
    }

    export class ErrorService {
        private $location: ng.ILocationService;

        // The list of error handlers
        private errorListeners : ErrorHandler[] = [];

        constructor($location: ng.ILocationService) {
            this.$location = $location;
        }

        /** Allow clients to register for the error notification */
        public errorEvent(handler: ErrorHandler) {
            this.errorListeners.push(handler);
        }

        /** Broadcast the error to all listeners */
        public raiseError(description: string, error: string) {
            this.errorListeners.forEach(h => h.onErrorSet(this.$location.absUrl(), description, error));
        }

        /** Clear the error event */
        public clearError() {
            this.errorListeners.forEach(h => h.onErrorClear());
        }
    }
}
