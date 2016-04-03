/// <reference path="../references.ts" />

module services {
    'use strict';

    // Error handler definition
    export interface State {
        // Persist the current friend being viewed
        selectedUserId: number
    }

    type StateHandler = (state: State) => void;

    export class StateService {
        private stateListeners : StateHandler[] = [];
        private frontEndService: FrontEndService;
        private currentState: State = <State>{ };

        constructor(
            frontEndService: FrontEndService,
            userService: UserService,
            errorService: ErrorService) {

            this.frontEndService = frontEndService;

            userService.userSetEvent((user) => {
                this.currentState = <State>{ };

                if (user) {
                    this.frontEndService.getUserState()
                        .error((error) => errorService.raiseError('Could not fetch user state', error))
                        .success((response: string) => {
                            this.currentState = <State>JSON.parse(response);
                            this.stateListeners.forEach(h => h(this.currentState));
                        });

                }
            });
        }

        /** Allow clients to register for the state-change notification */
        public stateChangedEvent(handler: StateHandler) {
            this.stateListeners.push(handler);

            if (this.currentState)
                handler(this.currentState);
        }

        /** Broadcast the state change to all listeners, and persist */
        public changeState(stateChanger: (s: State) => State) {

            // Get the state changer to change the state
            this.currentState = stateChanger(this.currentState);

            // Publish the change
            this.stateListeners.forEach(h => h(this.currentState));

            // Store the state on the server
            this.frontEndService.putUserState(this.currentState);
        }
    }
}
