import React, {Component} from 'react';
import scriptLoader from 'react-async-script-loader';

class MapComponent extends Component {
    constructor(props) {
        super(props);
        this.state = {
            DOMLoaded: false
        }
    }

    /* This must be here */
    componentDidUpdate() {
        if (!this.state.DOMLoaded) {
            this.setState({
                DOMLoaded: true
            });
        }
    }

    test2 = () => {
        console.log(this.getGameId());
        for (var i in allTerrsText) {
            allTerrsText[i].attr({text: "HIII"});
        }
    };

    /*
    Local method to fetch the current game ID from the
    URL path.
     */
    getGameId() {
        if (this.state.DOMLoaded) {
            return window.location.pathname.substring(1);
        }

        return null;
    }


    render() {
        return (
            <React.Fragment>
                <button onClick={this.test2}>Press Me</button>
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