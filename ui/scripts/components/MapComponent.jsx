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
            armiesLeftToAssign: undefined,
            currGameState: undefined,
            currentPlayer: undefined
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
            this.updateArmyCounts(() => {
                this.setupTerritoriesText();
            });
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
        if (isLinked) id = region[0].data('id');
        else id = region.data('id');

        const newTerrDatas = this.state.terrDatas.slice();
        newTerrDatas[id].armies += 1;

        this.setState({
            terrDatas: newTerrDatas
        }, () => {
            this.setTerritoryText(id, this.state.terrDatas[id].armies);

            // Send POST request
            
        });
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
            this.updateArmyCounts(() => {
                allTerrsText[terrID] = window.rsr.text(x, y, this.state.terrDatas[terrID].armies);
            });
        }
    };

    setTerritoryText = (regionID, stringContent) => {
        allTerrsText[regionID].attr({text: stringContent});
    };

    /*
    AJAX call to retrieve current army count associated to territory from input ID
     */
    updateArmyCountById = (terrID, callback) => {
        axios.get('/territoryInfo/' + terrID + '/' + this.getGameId()).then(res => {
            const newTerrDatas = this.state.terrDatas.slice();
            newTerrDatas[terrID].armies += 1;

            this.setState({
                terrDatas: newTerrDatas
            }, callback)
        });
    };

    /*
    AJAX call to retrieve all territory army counts
     */
    updateArmyCounts = callback => {
        axios.get('/territoriesInfo/' + this.getGameId()).then(res => {
            this.setState({
                terrDatas: res.data.sort((a, b) => a.id - b.id)
            }, callback);
        });
    };

    updateGameState = callback => {
        axios.get('/gameInfo/' + this.getGameId()).then(res => {
            let gameInfo = res.data;
            this.setState({
                currGameState: gameInfo,
                currPlayer: gameInfo.players[gameInfo.turn % gameInfo.players.length].name
            }, callback);
        });
    };

    /*
    Local method to fetch the current game ID from the
    URL path.
     */
    getGameId() {
        if (this.state.DOMLoaded) return window.location.pathname.substring(1);
        return null;
    }

    /*
    REQUESTS TO CHANGE BACKEND DATA
    */
    handleEndTurn = callback => {
        axios.get('/endTurn/' + this.getGameId()).then(() => callback(() => {
            console.log(this.state.currPlayer);
        }));
        console.log("Turn ended!");
    };

    render() {
        return (
            <React.Fragment>
                <button onClick={() => this.handleEndTurn(this.updateGameState)}>End Turn</button>
                <div id="rsr"/>
            </React.Fragment>
        );
    }
}

export default scriptLoader(
    'assets/javascripts/raphael-min.js',
    'assets/javascripts/worldMap.js'
)(MapComponent)
