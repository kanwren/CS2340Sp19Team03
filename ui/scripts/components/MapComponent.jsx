import React, {Component} from 'react';
import scriptLoader from 'react-async-script-loader';
import axios from 'axios';

const HIGHLIGHT_OPACITY = 0.5;
const UNHIGHLIGHT_OPACITY = 1.0;
const BORDER_COLOR = "#FFFFFF";
const BORDER_WIDTH = 1.0;
const allTerrsText = {};

const ORIG_HEIGHT = 628;
const ORIG_WIDTH = 1227;
const MAP_TO_WIDTH_SCALE = 0.8;

class MapComponent extends Component {
    constructor(props) {
        super(props);
        this.state = {
            DOMLoaded: false,
            mapInitialized: false,
            mapScaleFactor: (window.innerWidth * MAP_TO_WIDTH_SCALE) / ORIG_WIDTH,
            terrDatas: undefined,
            curr: undefined
        }
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

    resize = () => this.resizeMap();

    componentDidMount() {
        window.addEventListener('resize', this.resize);
    }

    componentWillUnmount() {
        window.removeEventListener('resize', this.resize);
    }

    initializeMap() {
        this.setState({mapScaleFactor: (window.innerWidth * MAP_TO_WIDTH_SCALE) / ORIG_WIDTH}, () => {
            this.setupTerritoriesMouseAction();
            this.setupTerritoriesText();
            this.updateArmyCounts();
            console.log(this.state.terrDatas);
        });
    }

    resizeMap() {
        this.setState({mapScaleFactor: (window.innerWidth * MAP_TO_WIDTH_SCALE) / ORIG_WIDTH}, () => {
            let scale = this.state.mapScaleFactor;
            window.rsr.setViewBox(0, 0, ORIG_WIDTH, ORIG_HEIGHT, true);
            window.rsr.setSize(ORIG_WIDTH * scale, ORIG_HEIGHT * scale);
        });
    }

    setMouseOver = (region, isLinked) => {
        if (isLinked) {
            for (let i = 0; i < region.length; i++)
                region[i].node.style.opacity = HIGHLIGHT_OPACITY;
        } else {
            region.node.style.opacity = HIGHLIGHT_OPACITY;
        }
    };

    setMouseOut = (region, isLinked) => {
        if (isLinked) {
            for (let i = 0; i < region.length; i++)
                region[i].node.style.opacity = UNHIGHLIGHT_OPACITY;
        } else {
            region.node.style.opacity = UNHIGHLIGHT_OPACITY;
        }
    };

    setMouseDown = (region, isLinked) => {
        let id = undefined;

        if (isLinked) {
            id = region[0].data('id');
            this.updateArmyCountById(id);
            this.setTerritoryText(id, this.state.curr);
        } else {
            id = region.data('id');
            this.updateArmyCountById(id);
            this.setTerritoryText(id, this.state.curr);
        }
    };



    setupTerritoriesMouseAction() {
        for (let i in window.rsrGroups) {
            let region = window.rsrGroups[i];

            if (window.linkedRegions.indexOf(region) !== -1) {
                for (let j = 0; j < region.length; j++) {
                    region[j].node.style.strokeWidth = BORDER_WIDTH;
                    region[j].node.style.stroke = BORDER_COLOR;
                }
                region.mouseover(() => this.setMouseOver(region, true)
                ).mouseout(() => this.setMouseOut(region, true)
                ).mousedown(() => this.setMouseDown(region, true));
            } else {
                for (let j = 0; j < region.length; j++) {
                    let terr = region[j];
                    terr.node.style.strokeWidth = BORDER_WIDTH;
                    terr.node.style.stroke = BORDER_COLOR;

                    terr.mouseover(() => this.setMouseOver(terr, false)
                    ).mouseout(() => this.setMouseOut(terr, false)
                    ).mousedown(() => this.setMouseDown(terr, false));
                }
            }
        }
    }

    getRegionId = region => {
        if (window.linkedRegions.indexOf(region) !== -1) return region[0].data('id');
        else return region.data('id');
    };

    setupTerritoriesText = () => {
        for (let i in window.allTerrs) {
            let region = window.allTerrs[i], bbox = region.getBBox();
            let x = (bbox.x + bbox.width / 2), y = (bbox.y + bbox.height / 2);

            let terrID = this.getRegionId(region);
            let textContent = this.updateArmyCountById(terrID);

            allTerrsText[terrID] = window.rsr.text(x, y, textContent);
        }
    };

    setTerritoryText = (regionID, stringContent) => {
        allTerrsText[regionID].attr({text: stringContent});
    };

    /*
    Retrieve current army count associated to territory from input ID
     */
    updateArmyCountById = terrID => {
        axios.get('/' + terrID + '/' + this.getGameId()).then(res => {
            const terrData = res.data;
            this.setState({
                curr: terrData.armies
            })
        });
    };

    updateArmyCounts = async () => {
        let res = await axios.get('/territoriesInfo/' + this.getGameId());
        let {data} = await res.data;
        this.setState({
            terrDatas: data
        })
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
