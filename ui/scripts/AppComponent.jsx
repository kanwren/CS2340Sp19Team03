import React, {Component} from 'react';

import MapComponent from './components/MapComponent.jsx';

class AppComponent extends Component {
    constructor(props) {
        super(props);
        this.state = {};
    }

    render() {
        return (
            <React.Fragment>
                <MapComponent />
            </React.Fragment>
        );
    }
}

export default AppComponent;