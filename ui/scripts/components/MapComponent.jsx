import React, {Component} from 'react';
import scriptLoader from 'react-async-script-loader';
import axios from 'axios';
import Topbar from "./Topbar.jsx"
import Sidebar from "./Sidebar.jsx";

import Tracer from "./Tracer.jsx";

import "./MapComponent.css";

// Territory highlight and border settings
const HIGHLIGHT_OPACITY = 0.5;
const UNHIGHLIGHT_OPACITY = 1.0;
const BORDER_COLOR = "#FFFFFF";
const BORDER_WIDTH = 1.0;
const ARMY_FONT_SIZE = 20;
const allTerrsText = {};

// Initial game dimensions and scale
const ORIG_HEIGHT = 628, ORIG_WIDTH = 1227;
const MAP_TO_WIDTH_SCALE = 0.75;

// Other logistical constants
const INITIAL_ARMIES_TO_ASSIGN = 3.0;
const playerMap = {};
const TERR_COLORS = ['#51d0ff', '#ff5151', '#51ffa2', '#ffff51', '#af66ff', '#ffa726', '#ff66cc'];
const PHASES = ["ASSIGN", "ATTACK", "FORTIFY"];

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
            currPlayer: undefined,
            selectedTerritory: undefined,
            isAttackPhase: false,
            attackingRegion: undefined,
            attackedRegion: undefined,
            adjTerrs: undefined,
            phaseIndex: 0,
            fixedPlayer: undefined,
            tracer: new Tracer(),
            eventFuncs: {}
        }
    }

    /* This must be here */
    componentDidUpdate() {
        if (!this.state.DOMLoaded) {
            this.setState({
                DOMLoaded: true,
                fixedPlayer: this.getPlayerNameFromPath()
            });
        }

        if (!this.state.mapInitialized) {
            this.initializeMap();
        }
    }

    resize = () => this.resizeMap();

    componentDidMount() {
        window.addEventListener('resize', this.resize);

        this.intervalId = setInterval(() => {
            console.log("INTERVAL HERE");
        }, 1000);
    }

    //defenderConquered/ => boolean

    componentWillUnmount() {
        window.removeEventListener('resize', this.resize);
    }

    initializeMap() {
        this.setState({
            mapScaleFactor: (window.innerWidth * MAP_TO_WIDTH_SCALE) / ORIG_WIDTH,
            mapInitialized: true,
        }, () => {
            this.updateArmyCounts(() => {
                this.setupTerritoriesText();
                this.updateGameState(() => {
                    let players = this.state.currGameState.players;
                    players.forEach((e, i) => {
                        playerMap[e.name] = i;
                    });
                    this.setupTerritoriesMouseAction();
                    this.updateTerritoryColors();
                });
            });

            let scale = this.state.mapScaleFactor;
            window.rsr.setViewBox(0, 0, ORIG_WIDTH, ORIG_HEIGHT, true);
            window.rsr.setSize(ORIG_WIDTH * scale, ORIG_HEIGHT * scale);
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
        let id = this.getRegionId(region);

        this.setState({
            selectedTerritory: this.state.terrDatas[id]
        });

        if (isLinked) {
            for (let i = 0; i < region.length; i++) {
                region[i].node.style.opacity = HIGHLIGHT_OPACITY;
            }
        } else {
            region.node.style.opacity = HIGHLIGHT_OPACITY;
        }
    };

    setMouseOut = (region, isLinked) => {
        let id = this.getRegionId(region);

        this.setState({
            selectedTerritory: undefined
        });

        if (isLinked) {
            for (let i = 0; i < region.length; i++) {
                region[i].node.style.opacity = UNHIGHLIGHT_OPACITY;
            }
        } else {
            region.node.style.opacity = UNHIGHLIGHT_OPACITY;
        }
    };

    getAdjacentTerritoryIds = (terrID, callback) => {
        axios.get('/territoryAdjacencies/' + terrID + '/' + this.getGameIdFromPath()).then(res => {
            this.setState({
                adjTerrs: res.data
            }, callback);
        });
    };

    setMouseDown = (region, isLinked) => {
        let id = this.getRegionId(region);

        if (!this.state.isAttackPhase && this.state.armiesLeftToAssign > 0 && (this.state.currPlayer === this.state.terrDatas[id].owner.name)) {
            const newTerrDatas = this.state.terrDatas.slice();
            newTerrDatas[id].armies += 1;

            this.setState({
                terrDatas: newTerrDatas,
                armiesLeftToAssign: this.state.armiesLeftToAssign - 1
            }, () => {
                this.setTerritoryText(id, this.state.terrDatas[id].armies);

                // Send POST request
                axios.get("").then(() => {
                    this.incrementTerritoryArmyCount(id, 1, () => {
                    });
                });
            });
        }
    };

    setupTerritoriesMouseAction() {
        for (let i in window.rsrGroups) {
            let region = window.rsrGroups[i];

            if (window.linkedRegions.indexOf(region) !== -1) { //special
                let regionId = region[0].data('id');

                if (this.state.terrDatas === undefined) return;
                let owner = this.state.terrDatas[regionId].owner.name;
                for (let j = 0; j < region.length; j++) {
                    region[j].node.style.strokeWidth = BORDER_WIDTH;
                    region[j].node.style.stroke = BORDER_COLOR;
                    region[j].node.setAttribute("regionId", regionId);
                    this.setRegionColor(region[j], TERR_COLORS[playerMap[owner]]);
                }
                region.mouseover(() => this.setMouseOver(region, true)
                ).mouseout(() => this.setMouseOut(region, true)
                ).mousedown(() => this.setMouseDown(region, true));
            } else {
                for (let j = 0; j < region.length; j++) {
                    let regionId = region[j].data('id');

                    if (this.state.terrDatas === undefined) return;
                    let owner = this.state.terrDatas[regionId].owner.name;
                    let terr = region[j];
                    terr.node.style.strokeWidth = BORDER_WIDTH;
                    terr.node.style.stroke = BORDER_COLOR;
                    terr.node.setAttribute("regionId", regionId);
                    this.setRegionColor(terr, TERR_COLORS[playerMap[owner]]);

                    terr.mouseover(() => this.setMouseOver(terr, false)
                    ).mouseout(() => this.setMouseOut(terr, false)
                    ).mousedown(() => this.setMouseDown(terr, false));
                }
            }
        }
    }

    addMouseOverEventToTerritories = (f) => {
        
        for (let i in window.rsrGroups) {
            let region = window.rsrGroups[i];

            if (window.linkedRegions.indexOf(region) !== -1) { //special
                region.mouseover(f);
            } else {
                for (let j = 0; j < region.length; j++) {
                    let terr = region[j];
                    terr.mouseover(f);
                }
            }
        }
    }
    addMouseDownEventToTerritories = (f) => {
        for (let i in window.rsrGroups) {
            let region = window.rsrGroups[i];

            if (window.linkedRegions.indexOf(region) !== -1) { //special
                region.mousedown(f);
            } else {
                for (let j = 0; j < region.length; j++) {
                    let terr = region[j];
                    terr.mousedown(f);
                }
            }
        }
    }
    addMouseUpEventToTerritories = (f) => {
        for (let i in window.rsrGroups) {
            let region = window.rsrGroups[i];

            if (window.linkedRegions.indexOf(region) !== -1) { //special
                region.mouseup(f);
            } else {
                for (let j = 0; j < region.length; j++) {
                    let terr = region[j];
                    terr.mouseup(f);
                }
            }
        }
    }

    removeMouseOverEventToTerritories = (name) => {
        name = this.state.eventFuncs[name];
        for (let i in window.rsrGroups) {
            let region = window.rsrGroups[i];

            if (window.linkedRegions.indexOf(region) !== -1) { //special
                region.unmouseover(name);
            } else {
                for (let j = 0; j < region.length; j++) {
                    let terr = region[j];
                    terr.unmouseover(name);
                }
            }
        }
    }
    removeMouseDownEventToTerritories = (name) => {
        for (let i in window.rsrGroups) {
            let region = window.rsrGroups[i];

            if (window.linkedRegions.indexOf(region) !== -1) { //special
                region.unmousedown(name);
            } else {
                for (let j = 0; j < region.length; j++) {
                    let terr = region[j];
                    terr.unmousedown(name);
                }
            }
        }
    }
    removeMouseUpEventToTerritories = (name) => {
        name = this.state.eventFuncs[name];
        for (let i in window.rsrGroups) {
            let region = window.rsrGroups[i];

            if (window.linkedRegions.indexOf(region) !== -1) { //special
                let regionId = region[0].data('id');
                region.unmouseup(name);
            } else {
                for (let j = 0; j < region.length; j++) {
                    let terr = region[j];
                    let regionId = terr.data('id');
                    terr.unmouseup(name);
                }
            }
        }
    }

    setRegionColor = (region, color) => {
        region.attr('fill', color);
    };

    updateTerritoryColors() {
        for (let i in window.rsrGroups) {
            let region = window.rsrGroups[i];

            if (window.linkedRegions.indexOf(region) !== -1) { //special
                let regionId = region[0].data('id');

                if (this.state.terrDatas === undefined) return;
                let owner = this.state.terrDatas[regionId].owner.name;
                for (let j = 0; j < region.length; j++) {
                    this.setRegionColor(region[j], TERR_COLORS[playerMap[owner]]);
                }
            } else {
                for (let j = 0; j < region.length; j++) {
                    let regionId = region[j].data('id');
                    if (this.state.terrDatas === undefined) return;
                    let owner = this.state.terrDatas[regionId].owner.name;
                    this.setRegionColor(region[j], TERR_COLORS[playerMap[owner]]);
                }
            }
        }
    }

    getRegionId = region => {
        if (window.linkedRegions.indexOf(region) !== -1) {
            return region[0].data('id');
        } else {
            return region.data('id');
        }
    };

    setupTerritoriesText = () => {
        for (let i in window.allTerrs) {
            let region = window.allTerrs[i], bbox = region.getBBox();
            let x = (bbox.x + bbox.width / 2), y = (bbox.y + bbox.height / 2);

            let terrID = this.getRegionId(region);
            this.updateArmyCounts(() => {
                allTerrsText[terrID] = window.rsr.text(x, y, this.state.terrDatas[terrID].armies).attr({"font-size": ARMY_FONT_SIZE});
            });
        }
    };

    setTerritoryText = (regionID, stringContent) => {
        allTerrsText[regionID].attr({text: stringContent});
    };

    updateTerritoryById = (terrID, callback) => {
        axios.get('/territoryInfo/' + terrID + '/' + this.getGameIdFromPath()).then(res => {
            const newTerrDatas = this.state.terrDatas.slice();
            newTerrDatas[terrID] = res.data;

            this.setState({
                terrDatas: newTerrDatas
            }, callback);
        });
    };

    updateArmyCounts = callback => {
        axios.get('/territoriesInfo/' + this.getGameIdFromPath()).then(res => {
            this.setState({
                terrDatas: res.data.sort((a, b) => a.id - b.id)
            }, callback);
        });
    };

    updateGameState = callback => {
        axios.get('/gameInfo/' + this.getGameIdFromPath()).then(res => {
            let gameInfo = res.data;
            this.setState({
                currGameState: gameInfo,
                currPlayer: gameInfo.players[gameInfo.turn % gameInfo.players.length].name,
                armiesLeftToAssign: INITIAL_ARMIES_TO_ASSIGN
            }, callback);
        });
    };

    getGameIdFromPath = () => {
        return window.location.pathname.substring(1);
    };

    getPlayerNameFromPath = () => {
        var location = window.location + ""; // String conversion

        return location.split("playerName=")[1];
    };

    addAttackMDEvent = (f) => {
        for (let i in window.rsrGroups) {
            let region = window.rsrGroups[i];

            if (window.linkedRegions.indexOf(region) !== -1) { //special
                let regionId = region[0].data('id');
                let g = (e) => {
                    f(e, regionId, g)
                }
                region.mousedown(g);
            } else {
                for (let j = 0; j < region.length; j++) {
                    let terr = region[j];
                    let regionId = terr.data('id');
                    let g = (e) => {
                        f(e, regionId, g)
                    }
                    terr.mousedown(g);
                }
            }
        }
    }
    beginAttackPhase = () => {
        if (this.state.armiesLeftToAssign === 0) {
            this.setState({
                isAttackPhase: !this.state.isAttackPhase,
                phaseIndex: 1
            });

            this.state.tracer.turnOnTracerLine();
            let f = (e) => {
                console.log("Click!");
                let regionId = e.target.getAttribute('regionId');
                let territory = this.state.terrDatas[regionId];
                if (this.state.currPlayer === territory.owner.name) {
                    this.setState({ attackingRegion: territory });
                    this.getAdjacentTerritoryIds(regionId);
                }
            };

            let d = (e) => {
                let regionId = e.target.getAttribute('regionId');
                let territory = this.state.terrDatas[regionId];
                let attacker = this.state.attackingRegion;
                if ((attacker !== undefined) && (this.state.currPlayer !== territory.owner.name) && (this.state.adjTerrs.indexOf(parseInt(regionId)) !== -1)) {
                    this.setState({ attackedRegion: territory });
                    this.removeMouseDownEventToTerritories(f);
                    this.removeMouseUpEventToTerritories(d);
                } else {
                    this.setState({ attackedRegion: undefined });
                    this.setState({ attackingRegion: undefined })
                }

                console.log("Attacker: " + this.state.attackingRegion.name);
                console.log("Defender: " + this.state.attackedRegion.name);

            }

            this.addMouseDownEventToTerritories(f);
            this.addMouseUpEventToTerritories(d);
        }
    };

    /*
    REQUESTS TO CHANGE BACKEND DATA
    */
    handleEndTurn = () => {
        if (this.state.armiesLeftToAssign === 0) {
            this.setState({
                attackedRegion: undefined,
                attackingRegion: undefined,
                isAttackPhase: false,
                phaseIndex: 0
            });

            axios.get('/endTurn/' + this.getGameIdFromPath()).then(() => {
                this.updateGameState(() => {
                    console.log("Current Player: " + this.state.currPlayer);
                });
            });

            console.log("Turn ended!");
        } else {
            console.log("Cannot end turn!");
        }
    };

    incrementTerritoryArmyCount = (terrID, count, callback) => {
        axios.get('/addArmiesToTerritory/' + count + '/' + terrID + '/' + this.getGameIdFromPath())
            .then(() => callback());
    };

    updateAttackerDefenderTextAndColors = () => {
        if (this.state.attackingRegion === undefined ||
            this.state.attackedRegion === undefined) return;
        let aId = this.state.attackingRegion.id, dId = this.state.attackedRegion.id;

        this.setTerritoryText(aId, this.state.terrDatas[aId].armies);
        this.setTerritoryText(dId, this.state.terrDatas[dId].armies);
        this.updateTerritoryColors();
    };

    render() {
        return (
            <React.Fragment>
                <Topbar
                    gameState={this.state.currGameState}
                    terrColors={TERR_COLORS}
                    fixedPlayer={this.state.fixedPlayer}
                />
                <div className="flex-box">
                    <div id="rsr"/>
                    <Sidebar
                        armiesLeftToAssign={this.state.armiesLeftToAssign}
                        currPlayer={this.state.currPlayer}
                        selectedTerritory={this.state.selectedTerritory}
                        handleEndTurn={this.handleEndTurn}
                        handleBeginAttackPhase={this.beginAttackPhase}
                        isAttackPhase={this.state.isAttackPhase}
                        attackingRegion={this.state.attackingRegion}
                        attackedRegion={this.state.attackedRegion}
                        currPhase={PHASES[this.state.phaseIndex]}
                        gameId={this.getGameIdFromPath()}
                        handleUpdateArmies={() => this.updateArmyCounts(this.updateAttackerDefenderTextAndColors)}
                        colors={TERR_COLORS}
                        gameState={this.state.currGameState}
                    />
                </div>
            </React.Fragment>
        );
    }
}

export default scriptLoader(
    'assets/javascripts/raphael-min.js',
    'assets/javascripts/worldMap.js'
)(MapComponent)
