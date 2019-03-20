import React, {Component} from 'react';
import scriptLoader from 'react-async-script-loader';

class MapComponent extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <React.Fragment>
                <div id="rsr"/>
            </React.Fragment>
        );
    }
}

export default scriptLoader(
    'assets/javascripts/raphael-min.js',
    'assets/javascripts/worldMap.js',
    'assets/javascripts/worldMapSetup.js'
)(MapComponent)