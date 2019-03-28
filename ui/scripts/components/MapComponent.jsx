import React, {Component} from 'react';
import scriptLoader from 'react-async-script-loader';

const HIGHLIGHT_OPACITY = 0.5;
const UNHIGHLIGHT_OPACITY = 1.0;
const BORDER_COLOR = "#FFFFFF";
const BORDER_WIDTH = 1.0;
const allTerrsText = {};

class MapComponent extends Component {
    constructor(props) {
        super(props);
        this.state = {
            DOMLoaded: false,
            mapInitialized: false,
        }
    }

    initializeMap() {
        this.setupTerritoriesMouseAction();
        this.setupTerritoriesText();
    }

    /* This must be here */
    componentDidUpdate() {
        if (!this.state.DOMLoaded) {
            this.setState({
                DOMLoaded: true
            });
        }

        if (!this.state.mapInitialized) {
            this.initializeMap();
            this.setState({
                mapInitialized: true
            });
        }
    }

    setMouseOver(region, isLinked) {
        if (isLinked) {
            for (let i = 0; i < region.length; i++)
                region[i].node.style.opacity = HIGHLIGHT_OPACITY;
        } else {
            region.node.style.opacity = HIGHLIGHT_OPACITY;
        }
    }

    setMouseOut(region, isLinked) {
        if (isLinked) {
            for (let i = 0; i < region.length; i++)
                region[i].node.style.opacity = UNHIGHLIGHT_OPACITY;
        } else {
            region.node.style.opacity = UNHIGHLIGHT_OPACITY;
        }
    }

    setMouseDown(region, isLinked) {
        let id = undefined;
        if (isLinked) {
            id = region[0].data('id');
            this.setTerritoryText(id, id + 1);
        } else {
            id = region.data('id');
            this.setTerritoryText(id, id + 1);
        }
    }

    setupTerritoriesMouseAction() {
        for (let i in rsrGroups) {
            let region = rsrGroups[i];

            if (linkedRegions.indexOf(region) !== -1) {
                for (let j = 0; j < region.length; j++) {
                    region[j].node.style.strokeWidth = BORDER_WIDTH;
                    region[j].node.style.stroke = BORDER_COLOR;
                }
                region.mouseover(e => this.setMouseOver(region, true)
                ).mouseout(e => this.setMouseOut(region, true)
                ).mousedown(e => this.setMouseDown(region, true));
            } else {
                for (let j = 0; j < region.length; j++) {
                    let terr = region[j];
                    terr.node.style.strokeWidth = BORDER_WIDTH;
                    terr.node.style.stroke = BORDER_COLOR;

                    terr.mouseover(e => this.setMouseOver(terr, false)
                    ).mouseout(e => this.setMouseOut(terr, false)
                    ).mousedown(e => this.setMouseDown(terr, false));
                }
            }
        }
    }

    setupTerritoriesText() {
        for (let i in allTerrs) {
            let region = allTerrs[i], bbox = region.getBBox();
            let text = undefined;
            let x = bbox.x + bbox.width / 2;
            let y = bbox.y + bbox.height / 2;

            let terrID = undefined;
            if (linkedRegions.indexOf(region) !== -1) terrID = region[0].data('id');
            else terrID = region.data('id');

            text = rsr.text(x, y, terrID);
            allTerrsText[terrID] = text;
        }
    }

    setTerritoryText = (regionID, stringContent) => {
        allTerrsText[regionID].attr({text: stringContent});
    };

    /*
    Local method to fetch the current game ID from the
    URL path.
     */
    getGameId() {
        if (this.state.DOMLoaded) return window.location.pathname.substring(1);

        return null;
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
    'assets/javascripts/worldMap.js'
)(MapComponent)